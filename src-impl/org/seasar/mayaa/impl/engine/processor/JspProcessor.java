/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.engine.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.servlet.jsp.tagext.VariableInfo;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.CycleWriter;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ChildEvaluationProcessor;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.processor.TryCatchFinallyProcessor;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.builder.library.TLDProcessorDefinition;
import org.seasar.mayaa.impl.builder.library.TLDScriptingVariableInfo;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.DefaultCycleLocalInstantiator;
import org.seasar.mayaa.impl.cycle.jsp.BodyContentImpl;
import org.seasar.mayaa.impl.cycle.jsp.PageContextImpl;
import org.seasar.mayaa.impl.cycle.script.ScriptUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.AbstractSoftReferencePool;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Koji Suga (Gluegent, Inc.)
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author Hisayoshi Sasaki (Gluegent, Inc.)
 */
public class JspProcessor extends TemplateProcessorSupport
        implements
            ChildEvaluationProcessor,
            TryCatchFinallyProcessor,
            CONST_IMPL {

    private static final long serialVersionUID = -5567131378936566428L;

    private static final PageContext _pageContext = new PageContextImpl();

    // TODO TagPoolの保持期間などパフォーマンスの調整
    private static final Map _tagPools =
        new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT, true);

    private static final String LOADED_TAG_KEY =
        JspProcessor.class.getName() + "#loadedTag";
    private static final String STOCK_VARIABLES_KEY =
        JspProcessor.class.getName() + "#stockVariables";
    static {
        CycleUtil.registVariableFactory(
                LOADED_TAG_KEY, new DefaultCycleLocalInstantiator() {
            public Object create(Object owner, Object[] params) {
                return null;
            }
        });
        CycleUtil.registVariableFactory(
                STOCK_VARIABLES_KEY, new DefaultCycleLocalInstantiator() {
            public Object create(Object owner, Object[] params) {
                return new HashMap();
            }
        });
    }

    private Class _tagClass;
    private List _properties;
    private String _attributesKey;
    private Boolean _nestedVariableExists;
    private transient TLDScriptingVariableInfo _variableInfo =
            new TLDScriptingVariableInfo();

    public static void clear() {
        _tagPools.clear();
    }

    public static boolean isSupportClass(Class test) {
        return test != null &&
                (Tag.class.isAssignableFrom(test)
                || SimpleTag.class.isAssignableFrom(test));
    }

    public void setTLDScriptingVariableInfo(
            TLDScriptingVariableInfo variableInfo) {
        if (variableInfo == null) {
            throw new IllegalArgumentException();
        }
        _variableInfo = variableInfo;
        _nestedVariableExists = null;
    }

    public TLDScriptingVariableInfo getTLDScriptingVariableInfo() {
        return _variableInfo;
    }

    // MLD method
    public void setTagClass(Class tagClass) {
        if (isSupportClass(tagClass) == false) {
            throw new IllegalArgumentException();
        }
        _tagClass = tagClass;
    }

    // MLD method
    public void addProcessorProperty(ProcessorProperty property) {
        if (property == null) {
            throw new IllegalArgumentException();
        }
        if (_attributesKey != null) {
            throw new IllegalStateException();
        }
        if (_properties == null) {
            _properties = new ArrayList();
        }
        _properties.add(property);
    }

    protected Iterator iterateProperties() {
        if (_properties == null) {
            return NullIterator.getInstance();
        }
        return _properties.iterator();
    }

    protected String getAttributesKey() {
        if (_attributesKey == null) {
            StringBuffer buffer = new StringBuffer();
            for (Iterator it = iterateProperties(); it.hasNext();) {
                ProcessorProperty property = (ProcessorProperty) it.next();
                String localName =
                    property.getName().getQName().getLocalName();
                buffer.append("%").append(localName);
            }
            _attributesKey = buffer.toString();
        }
        return _attributesKey;
    }

    protected TagPool getTagPool() {
        synchronized (_tagPools) {
            String key = _tagClass.getName() + getAttributesKey();
            TagPool pool = (TagPool) _tagPools.get(key);
            if (pool == null) {
                pool = new TagPool(_tagClass);
                _tagPools.put(key, pool);
            }
            return pool;
        }
    }

    protected void clearLoadedTag() {
        CycleUtil.clearLocalVariable(LOADED_TAG_KEY, this);
    }

    protected Tag getLoadedTag() {
        Tag tag = (Tag) CycleUtil.getLocalVariable(LOADED_TAG_KEY, this, null);
        if (tag == null) {
            tag = getTagPool().borrowTag();
            tag.setPageContext(_pageContext);
            CycleUtil.setLocalVariable(LOADED_TAG_KEY, this, tag);
        }
        return tag;
    }

    protected void releaseLoadedTag() {
        Tag tag = (Tag) CycleUtil.getLocalVariable(LOADED_TAG_KEY, this, null);
        CycleUtil.setLocalVariable(LOADED_TAG_KEY, this, null);
        tag.release();
        getTagPool().returnTag(tag);
    }

    protected ProcessStatus getProcessStatus(int status, boolean doStart) {
        if (status == Tag.EVAL_BODY_INCLUDE) {
            return ProcessStatus.EVAL_BODY_INCLUDE;
        } else if (status == Tag.SKIP_BODY) {
            return ProcessStatus.SKIP_BODY;
        } else if (status == Tag.EVAL_PAGE) {
            return ProcessStatus.EVAL_PAGE;
        } else if (status == Tag.SKIP_PAGE) {
            return ProcessStatus.SKIP_PAGE;
        } else if (!doStart && status == IterationTag.EVAL_BODY_AGAIN) {
            return ProcessStatus.EVAL_BODY_AGAIN;
        } else if (doStart && status == BodyTag.EVAL_BODY_BUFFERED) {
            return ProcessStatus.EVAL_BODY_BUFFERED;
        }
        throw new IllegalArgumentException();
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if (_tagClass == null) {
            throw new IllegalStateException();
        }
        topLevelPage.registBeginRenderNotifier(this);
        clearLoadedTag();
        Tag customTag = getLoadedTag();

        // javax.servlet.jsp.tagext.SimpleTag対応
        Object targetTag = customTag;
        if (customTag instanceof SimpleTagWrapper) {
            targetTag = ((SimpleTagWrapper) customTag).getSimpleTag();
        }
        for (Iterator it = iterateProperties(); it.hasNext();) {
            ProcessorProperty property = (ProcessorProperty) it.next();
            ObjectUtil.setProperty(targetTag,
                    property.getName().getQName().getLocalName(),
                    property.getValue().execute(null));
        }

        // javax.servlet.jsp.tagext.DynamicAttributes対応
        if (TLDProcessorDefinition.class.isAssignableFrom(
                getProcessorDefinition().getClass())) {
            TLDProcessorDefinition tldDef =
                (TLDProcessorDefinition) getProcessorDefinition();
            if (tldDef.isDynamicAttribute()) {
                setupDynamicAttributes(customTag);
            }
        }

        ProcessorTreeWalker processor = this;
        while ((processor = processor.getParentProcessor()) != null) {
            if (processor instanceof JspProcessor) {
                JspProcessor jspProcessor = (JspProcessor) processor;
                Tag parentTag = jspProcessor.getLoadedTag();
                if (parentTag == null) {
                    throw new IllegalStateException(
                            "the parent processor has no custom tag.");
                }
                customTag.setParent(parentTag);
                break;
            }
        }
        try {
            pushNestedVariables();
            final int result = customTag.doStartTag();
            return getProcessStatus(result, true);
        } catch (JspException e) {
            throw createJspRuntimeException(
                    getOriginalNode(), getInjectedNode(), e);
        }
    }

    /**
     * @param customTag
     */
    private void setupDynamicAttributes(Tag customTag) {
        Class custamTagClass = getTagClass(customTag);
        if ((DynamicAttributes.class.isAssignableFrom(custamTagClass)) == false) {
            String message =
                StringUtil.getMessage(
                        JspProcessor.class, 0,
                        custamTagClass.getName(),
                        DynamicAttributes.class.getName());
            throw new IllegalArgumentException(message);
        }

        // 明示的に指定されている属性を列挙
        Set definedQNames = new HashSet();
        for (Iterator it = iterateProperties(); it.hasNext();) {
            ProcessorProperty property = (ProcessorProperty) it.next();
            definedQNames.add(property.getName().getQName());
        }

        for (Iterator it = getInjectedNode().iterateAttribute(); it.hasNext();) {
            NodeAttribute attr = (NodeAttribute) it.next();
            QName qName = attr.getQName();

            // 明示されている属性、ネームスペースがMayaaの属性は処理が決まっているため動的属性として扱わない
            if (definedQNames.contains(qName)
                    || CONST_IMPL.URI_MAYAA.equals(qName.getNamespaceURI())) {
                continue;
            }

            try {
                // 式を実行してからsetDynamicAttribute
                CompiledScript script =
                    ScriptUtil.compile(attr.getValue(), Object.class);
                Object execValue = script.execute(null);

                toDynamicAttributes(customTag).setDynamicAttribute(
                             qName.getNamespaceURI().getValue(),
                             qName.getLocalName(),
                             execValue);
            } catch (JspException e) {
                throw createJspRuntimeException(
                        getOriginalNode(), getInjectedNode(), e);
            }
        }
    }

    /**
     * customTagのクラスオブジェクトを取得します。
     * customTagがSimpleTagWrapperの場合、実体となるSimpleTagオブジェクトの
     * クラスを取得します。
     *
     * @param customTag クラスオブジェクトを取得するカスタムタグインスタンス
     * @return customTagのクラス
     */
    private Class getTagClass(Tag customTag) {
        if (customTag instanceof SimpleTagWrapper) {
            return ((SimpleTagWrapper) customTag).getSimpleTag().getClass();
        }
        return customTag.getClass();
    }

    /**
     * customTagのDynamicAttributesを取得します。
     * customTagがSimpleTagWrapperの場合、実体となるSimpleTagオブジェクトの
     * DynamicAttributesを取得します。
     *
     * @param customTag DynamicAttributesを取得するカスタムタグインスタンス
     * @return customTagのDynamicAttributes
     */
    private DynamicAttributes toDynamicAttributes(Tag customTag) {
        if (customTag instanceof SimpleTagWrapper) {
            return (DynamicAttributes) ((SimpleTagWrapper) customTag).getSimpleTag();
        }
        return (DynamicAttributes) customTag;
    }

    private RuntimeException createJspRuntimeException(
            SpecificationNode originalNode, SpecificationNode injectedNode,
            Throwable cause) {
        return new RuntimeException(
                new JspRuntimeException(
                        originalNode.getSystemID(), originalNode.getLineNumber(),
                        injectedNode.getSystemID(), injectedNode.getLineNumber(),
                        cause));
    }

    public ProcessStatus doEndProcess() {
        Tag customTag = getLoadedTag();
        try {
            int ret = customTag.doEndTag();
            return getProcessStatus(ret, true);
        } catch (JspException e) {
            throw createJspRuntimeException(
                    getOriginalNode(), getInjectedNode(), e);
        } finally {
            if (!canCatch()) {
                releaseLoadedTag();
                popNestedVariables();
            }
        }
    }

    public void notifyBeginRender(Page topLevelPage) {
        CycleUtil.clearLocalVariable(STOCK_VARIABLES_KEY, this);
    }

    protected Map getNestedVariablesMap() {
        return (Map) CycleUtil.getLocalVariable(STOCK_VARIABLES_KEY, this, null);
    }

    protected void pushNestedVariables() {
        operateNestedVariables(new NestedVariableOperator() {
            public void operate(AttributeScope pageScope,
                    VariableInfo info, boolean firstHit) {
                String name = info.getVarName();
                if (pageScope.hasAttribute(name)) {
                    if (firstHit) {
                        getNestedVariablesMap().clear();
                    }
                    getNestedVariablesMap().put(name,
                            pageScope.getAttribute(name));
                }
            }
        });
    }

    protected void popNestedVariables() {
        operateNestedVariables(new NestedVariableOperator() {
            public void operate(AttributeScope pageScope,
                    VariableInfo info, boolean firstHit) {
                String name = info.getVarName();
                Map map = getNestedVariablesMap();
                if (map.containsKey(name)) {
                    pageScope.setAttribute(name,
                            map.get(name));
                } else {
                    pageScope.removeAttribute(name);
                }
            }
        });
    }

    private interface NestedVariableOperator {
        void operate(AttributeScope pageScope, VariableInfo info, boolean firstHit);
    }

    protected void operateNestedVariables(NestedVariableOperator operator) {
        if (Boolean.FALSE.equals(_nestedVariableExists) == false) {
            TLDScriptingVariableInfo variableInfo =
                getTLDScriptingVariableInfo();
            if (variableInfo != null) {
                AttributeScope pageScope =
                    CycleUtil.getServiceCycle().getPageScope();
                boolean firstHit = true;
                for (Iterator it = variableInfo.variableInfos();
                        it.hasNext(); ) {
                    VariableInfo info = (VariableInfo)it.next();
                    if (info.getScope() == VariableInfo.NESTED) {
                        _nestedVariableExists = Boolean.TRUE;
                        operator.operate(pageScope, info, firstHit);
                        firstHit = false;
                    }
                }
            }
            if (_nestedVariableExists == null) {
                _nestedVariableExists = Boolean.FALSE;
            }
        }
    }

    public boolean isChildEvaluation() {
        return getLoadedTag() instanceof BodyTag;
    }

    public void setBodyContent(CycleWriter body) {
        if (body == null) {
            throw new IllegalArgumentException();
        }
        Tag tag = getLoadedTag();
        if (tag instanceof BodyTag) {
            BodyTag bodyTag = (BodyTag) tag;
            bodyTag.setBodyContent(new BodyContentImpl(body));
        } else {
            throw new IllegalStateException();
        }
    }

    public void doInitChildProcess() {
        Tag tag = getLoadedTag();
        if (tag instanceof BodyTag) {
            BodyTag bodyTag = (BodyTag) tag;
            try {
                bodyTag.doInitBody();
            } catch (JspException e) {
                throw createJspRuntimeException(
                        getOriginalNode(), getInjectedNode(), e);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean isIteration() {
        return getLoadedTag() instanceof IterationTag;
    }

    public ProcessStatus doAfterChildProcess() {
        Tag tag = getLoadedTag();
        if (tag instanceof IterationTag) {
            IterationTag iterationTag = (IterationTag) tag;
            try {
                int ret = iterationTag.doAfterBody();
                return getProcessStatus(ret, false);
            } catch (JspException e) {
                throw createJspRuntimeException(
                        getOriginalNode(), getInjectedNode(), e);
            }
        }
        throw new IllegalStateException();
    }

    public boolean canCatch() {
        try {
            return getLoadedTag() instanceof TryCatchFinally;
        } catch (Exception e) {
            return false;
        }
    }

    public void doCatchProcess(Throwable t) {
        if (t == null) {
            throw new IllegalArgumentException();
        }
        Tag tag = getLoadedTag();
        if (tag instanceof TryCatchFinally) {
            TryCatchFinally tryCatch = (TryCatchFinally) tag;
            try {
                tryCatch.doCatch(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public void doFinallyProcess() {
        Tag tag = getLoadedTag();
        if (tag instanceof TryCatchFinally) {
            TryCatchFinally tryCatch = (TryCatchFinally) tag;
            try {
                tryCatch.doFinally();
            } finally {
                releaseLoadedTag();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    // for serialize

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        _variableInfo = new TLDScriptingVariableInfo();
    }

    // support class

    protected static class TagPool extends AbstractSoftReferencePool {

        private static final long serialVersionUID = -4519484537723904500L;

        private Class _clazz;

        public TagPool(Class clazz) {
            if (isSupportClass(clazz) == false) {
                throw new IllegalArgumentException();
            }
            _clazz = clazz;
        }

        protected Object createObject() {
            if (Tag.class.isAssignableFrom(_clazz)) {
                return ObjectUtil.newInstance(_clazz);
            }
            return new SimpleTagWrapper((SimpleTag) ObjectUtil.newInstance(_clazz));
        }

        protected boolean validateObject(Object object) {
            return object instanceof Tag;
        }

        public Tag borrowTag() {
            return (Tag) borrowObject();
        }

        public void returnTag(Tag tag) {
            if (tag != null) {
                returnObject(tag);
            }
        }

    }

    public static class SimpleTagWrapper implements Tag {

        private SimpleTag _simpleTag;

        private Tag _parent;

        private boolean _parentDetermined;

        public SimpleTagWrapper(SimpleTag simpleTag) {
            if (simpleTag == null) {
                throw new IllegalArgumentException("simpleTag is null");
            }
            this._simpleTag = simpleTag;
        }

        public JspTag getSimpleTag() {
            return _simpleTag;
        }

        public Tag getParent() {
            if (!_parentDetermined) {
                JspTag simpleTagParent = _simpleTag.getParent();
                if (simpleTagParent != null) {
                    if (simpleTagParent instanceof Tag) {
                        _parent = (Tag) simpleTagParent;
                    } else {
                        _parent = new SimpleTagWrapper((SimpleTag) simpleTagParent);
                    }
                }
                _parentDetermined = true;
            }

            return _parent;
        }

        public void setPageContext(PageContext context) {
            _simpleTag.setJspContext(context);
        }

        public void setParent(Tag parentTag) {
            /* no-op */
        }

        public int doStartTag() throws JspException {
            try {
                _simpleTag.doTag();
            } catch (IOException e) {
                Log log = LogFactory.getLog(SimpleTagWrapper.class);
                log.warn(e.getMessage(), e);
            }
            return Tag.SKIP_BODY;
        }

        public int doEndTag() {
            /* no-op */
            return Tag.EVAL_PAGE;
        }

        public void release() {
            /* no-op */
        }

    }

}
