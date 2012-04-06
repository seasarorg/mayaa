/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.seasar.mayaa.builder.SpecificationBuilder;
import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.serialize.ProcessorReferenceResolver;
import org.seasar.mayaa.engine.specification.serialize.ProcessorResolveListener;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.DefaultCycleLocalInstantiator;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.engine.specification.serialize.ProcessorSerializeController;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateImpl
        extends SpecificationImpl
        implements Template, ProcessorReferenceResolver {

    private static final long serialVersionUID = 2126209350220642842L;

    private String _pageName;
    private String _suffix;
    private String _extension;
    private List _childProcessors = new ArrayList();

    public void initialize(Page page, String suffix, String extension) {
        if (page == null || suffix == null || extension == null) {
            throw new IllegalArgumentException();
        }
        if (_pageName != null) {
            // only once
            throw new IllegalStateException();
        }
        _pageName = page.getPageName();
        _suffix = suffix;
        _extension = extension;
    }

    public Page getPage() {
        return ProviderUtil.getEngine().getPage(_pageName);
    }

    public String getSuffix() {
        return _suffix;
    }

    public String getExtension() {
        return _extension;
    }

    protected String getMayaaAttribute(Specification spec, QName qname) {
        SpecificationNode mayaa = SpecificationUtil.getMayaaNode(spec);
        if (mayaa != null) {
            return SpecificationUtil.getAttributeValue(mayaa, qname);
        }
        return null;
    }

    protected String findMayaaAttribute(Page topLevelPage, QName qname) {
        String topLevelValue = getMayaaAttribute(topLevelPage, qname);
        if (StringUtil.hasValue(topLevelValue)) {
            return topLevelValue;
        }

        Specification spec = this;
        while (spec != null) {
            String value = getMayaaAttribute(spec, qname);
            if (StringUtil.hasValue(value)) {
                return value;
            }
            spec = EngineUtil.getParentSpecification(spec);
        }
        return null;
    }

    /**
     * m:mayaaタグのcontentType属性の値を取得します。
     * 静的な文字列として処理します。
     * topLevelPageに属性が無い場合は親mayaaを辿って探します。
     * MayaaファイルにcontentType属性がなく、テンプレートにmetaタグでcontent-type
     * が定義されている場合、ビルド時にMayaaのcontentType属性にコピーされます。
     *
     * @param topLevelPage
     * @return contentType属性の値、またはnull
     */
    protected String getContentType(Page topLevelPage) {
        String contentType = findMayaaAttribute(topLevelPage, QM_CONTENT_TYPE);
        if (contentType != null) {
            return contentType;
        }

        // default
        RequestScope request = CycleUtil.getRequestScope();
        String ret = request.getMimeType();
        if (ret == null) {
            ret = "text/html;charset=" + TEMPLATE_DEFAULT_CHARSET;
        } else if (ret.indexOf("charset") == -1) {
            ret = ret + ";charset=" + TEMPLATE_DEFAULT_CHARSET;
        }
        return ret;
    }

    /**
     * m:mayaaタグのnoCache属性の値を取得します。
     * 静的な文字列として処理します。
     * topLevelPageに属性が無い場合は親mayaaを辿って探します。
     *
     * @param topLevelPage
     * @return noCache属性の値、またはnull
     */
    protected boolean isNoCache(Page topLevelPage) {
        String noCache = findMayaaAttribute(topLevelPage, QM_NO_CACHE);
        if (noCache != null) {
            return ObjectUtil.booleanValue(noCache, false);
        }

        return false;
    }

    /**
     * m:mayaaタグのcacheControl属性の値を取得します。
     * 静的な文字列として処理します。
     * topLevelPageに属性が無い場合は親mayaaを辿って探します。
     *
     * @param topLevelPage
     * @return cacheControl属性の値、またはnull
     */
    protected String getCacheControl(Page topLevelPage) {
        return findMayaaAttribute(topLevelPage, QM_CACHE_CONTROL);
    }

    /**
     * content-typeの設定、cache-controlの設定などレスポンスの前処理をします。
     * noCache属性がセットされている場合、下記３つのヘッダが設定されます。
     * <ul><li>Pragma: no-cache</li>
     * <li>Cache-Control: no-cache</li>
     * <li>Expires: Thu, 01 Dec 1994 16:00:00 GMT</li>
     * </ul>
     * cacheControl属性がセットされている場合、その値がCache-Controlヘッダの
     * 値として設定されます。
     * noCache属性とcacheControl属性の両方がセットされている場合、Cache-Control
     * ヘッダの値はcacheControl属性の値が設定されます。
     *
     * @param topLevelPage
     */
    protected void prepareCycle(Page topLevelPage) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Response response = cycle.getResponse();
        String contentType = getContentType(topLevelPage);
        String cacheControl = getCacheControl(topLevelPage);
        response.setContentType(contentType);
        if (isNoCache(topLevelPage)) {
            response.addHeader("Pragma", "no-cache");
            if (cacheControl == null) {
                response.addHeader("Cache-Control",
                        EngineUtil.getEngineSetting(EngineImpl.NO_CACHE_VALUE, "no-cache"));
            }
            response.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
        }
        if (cacheControl != null) {
            response.addHeader("Cache-Control", cacheControl);
        }
    }

    public ProcessStatus doTemplateRender(Page topLevelPage) {
        RenderUtil.saveToCycle(this);
        prepareCycle(topLevelPage);
        ProcessStatus ret =
            RenderUtil.renderProcessorTree(topLevelPage, this);
        return ret;
    }

    protected void replaceProcessors(List processors) {
        synchronized (this) {
            clearChildProcessors();
            for (int i = 0; i < processors.size(); i++) {
                Object item = processors.get(i);
                if (item instanceof ProcessorTreeWalker) {
                    addChildProcessor((ProcessorTreeWalker)processors.get(i));
                }
            }
        }
    }

    public void build() {
        build(true);
    }

    public void build(boolean rebuild) {
        CycleUtil.beginDraftWriting();
        try {
            super.build(rebuild);
        } finally {
            CycleUtil.endDraftWriting();
        }
    }

    protected SpecificationBuilder getBuilder() {
        return ProviderUtil.getTemplateBuilder();
    }

    public boolean isDeprecated() {
        Date templateTime = getTimestamp();
        if (templateTime != null) {
            Date pageTime = getPage().getTimestamp();
            if (pageTime != null && pageTime.after(templateTime)) {
                return true;
            }
            Date engineTime = ProviderUtil.getEngine().getTimestamp();
            if (engineTime != null && engineTime.after(templateTime)) {
                return true;
            }
        }
        return super.isDeprecated();
    }

    // ProcessorTreeWalker implements --------------------------------

    public Map getVariables() {
        return null;
    }

    public void setParentProcessor(ProcessorTreeWalker parent) {
        throw new IllegalStateException();
    }

    public ProcessorTreeWalker getParentProcessor() {
        return null;
    }

    public ProcessorTreeWalker getStaticParentProcessor() {
        return null;
    }

    public void addChildProcessor(ProcessorTreeWalker child) {
        insertProcessor(_childProcessors.size(), child);
    }

    public void insertProcessor(int index, ProcessorTreeWalker child) {
        synchronized (_childProcessors) {
            if (child == null) {
                throw new IllegalArgumentException();
            }
            if (index < 0 || index > _childProcessors.size()) {
                throw new IndexOutOfBoundsException();
            }
            _childProcessors.add(index, child);
            child.setParentProcessor(this);
            for (index += 1; index < _childProcessors.size(); index++) {
                child = (ProcessorTreeWalker)_childProcessors.get(index);
                child.setParentProcessor(this);
            }
        }
    }

    public boolean removeProcessor(ProcessorTreeWalker child) {
        synchronized (this) {
            return _childProcessors.remove(child);
        }
    }

    public void clearChildProcessors() {
        synchronized (this) {
            _childProcessors.clear();
        }
    }

    public int getChildProcessorSize() {
        return _childProcessors.size();
    }

    public ProcessorTreeWalker getChildProcessor(int index) {
        return (ProcessorTreeWalker) _childProcessors.get(index);
    }

    // for serialize
    private static final String SERIALIZE_CONTROLLER_KEY =
        TemplateImpl.class.getName() + "#serializeController";
    static {
        CycleUtil.registVariableFactory(SERIALIZE_CONTROLLER_KEY,
                new DefaultCycleLocalInstantiator() {
            public Object create(Object[] params) {
                ProcessorSerializeController result =
                    new ProcessorSerializeController();
                result.init();
                return result;
            }
        });
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();

        if (_childProcessors != null) {
            for (int i = _childProcessors.size() - 1; i >= 0 ; i--) {
                ProcessorTreeWalker child =
                    (ProcessorTreeWalker) _childProcessors.get(i);
                child.setParentProcessor(this);
            }
        }
        nodeSerializer().specLoaded(this);
    }

    protected void afterDeserialize() {
        processorSerializer().release();
    }

    public static ProcessorSerializeController processorSerializer() {
        return (ProcessorSerializeController) CycleUtil.getGlobalVariable(
                SERIALIZE_CONTROLLER_KEY, null);
    }

    // ProcessorReferenceResolver implements ------------------------

    public void registResolveProcessorListener(
            String uniqueID, ProcessorResolveListener listener) {
        processorSerializer().registResolveProcessorListener(uniqueID, listener);
    }

    public void processorLoaded(String uniqueID, ProcessorTreeWalker item) {
        processorSerializer().processorLoaded(uniqueID, item);
    }

    public ProcessorReferenceResolver findProcessorResolver() {
        return processorSerializer();
    }

    // PositionAware implements ------------------------------------

    public boolean isOnTemplate() {
        return true;
    }

}
