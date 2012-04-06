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

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.script.ScriptUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class PageImpl extends SpecificationImpl implements Page {

    private static final long serialVersionUID = -7151911061582725013L;
    static final Log LOG = LogFactory.getLog(PageImpl.class);
    private static final String CURRENT_PAGE_KEY = "__currentPage__";
    private static final String CURRENT_COMPONENT_KEY = "__currentComponent__";

    private String _pageName;
    private transient Page _superPage;
    private transient String _superSuffix;
    private transient String _superExtension;
    private transient Map _beginRenderListeners;
    private String _suffixScriptText;
    private CompiledScript _suffixScript;

    public void initialize(String pageName) {
        if (StringUtil.isEmpty(pageName)) {
            throw new IllegalArgumentException();
        }
        if (pageName.charAt(0) != '/') {
            pageName = "/" + pageName;
        }
        _pageName = pageName;
    }

    protected void prepareSuper() {
        if (_superPage != null && _superPage.isDeprecated() == false) {
            return;
        }
        synchronized (this) {
            if (_superPage != null && _superPage.isDeprecated() == false) {
                return;
            }
            // TODO mayaa以外からも取得できるようにする
            String extendsPath =
                SpecificationUtil.getMayaaAttributeValue(this, QM_EXTENDS);
            if (StringUtil.isEmpty(extendsPath)) {
                return;
            }
            Engine engine = ProviderUtil.getEngine();
            String suffixSeparator = engine.getParameter(SUFFIX_SEPARATOR);
            String[] pagePath = StringUtil.parsePath(extendsPath, suffixSeparator);

            _superPage = engine.getPage(
                    StringUtil.adjustRelativePath(_pageName, pagePath[0]));
            _superSuffix = pagePath[1];
            _superExtension = pagePath[2];
        }
    }

    public Page getSuperPage() {
        prepareSuper();
        return _superPage;
    }

    public String getSuperSuffix() {
        prepareSuper();
        return _superSuffix;
    }

    public String getSuperExtension() {
        prepareSuper();
        return _superExtension;
    }

    public String getPageName() {
        return _pageName;
    }

    public CompiledScript getSuffixScript() {
        String value = SpecificationUtil.getMayaaAttributeValue(
                this, QM_TEMPLATE_SUFFIX);
        if (StringUtil.isEmpty(value)) {
            value = SpecificationUtil.getMayaaAttributeValue(
                    ProviderUtil.getEngine(), QM_TEMPLATE_SUFFIX);
        }
        if (StringUtil.isEmpty(value)) {
            value = "";
        }
        if (_suffixScript != null && value.equals(_suffixScriptText)) {
            return _suffixScript;
        }
        _suffixScript = ScriptUtil.compile(value, String.class);
        _suffixScriptText = value;
        return _suffixScript;
    }

    protected Template findTemplateFromCache(String systemID) {
        Engine engine = ProviderUtil.getEngine();
        return (Template) engine.findSpecificationFromCache(systemID);
    }

    protected Template getTemplateFromFixedSuffix(
            String suffix, String extension) {
        if (suffix == null || extension == null) {
            throw new IllegalArgumentException();
        }
        Engine engine = ProviderUtil.getEngine();
        synchronized (engine) {
            String systemID = engine.getTemplateID(this, suffix, extension);
            Template template = findTemplateFromCache(systemID);
            if (template != null) {
                if (template.getSource().exists()) {
                    return template;
                }
                return null;
            }
            return engine.createTemplateInstance(this, suffix, extension);
        }
    }

    public Template getTemplate(String suffix, String extension) {
        if (suffix == null) {
            suffix = "";
        }
        if (extension == null) {
            extension = "";
        }
        if (EngineUtil.getMayaaExtensionName().equals(extension)) {
            return null;
        }
        Template template = getTemplateFromFixedSuffix(suffix, extension);
        if (template == null && StringUtil.hasValue(suffix)) {
            template = getTemplateFromFixedSuffix("", extension);
        }
        if (template == null) {
            throw new PageNotFoundException(_pageName, extension);
        }
        return template;
    }

    public ProcessStatus doPageRender(
            String requestedSuffix, String extension) {
        Page prevPage = getCurrentPage();
        setCurrentPage(this);
        Engine engine = ProviderUtil.getEngine();
        SpecificationUtil.execEvent(engine, QM_BEFORE_RENDER_PAGE);
        try {
            Page component = this;
            Page superPage = component.getSuperPage();
            if (superPage != null) {
                component = superPage;
            }
            Page prevComponent = getCurrentComponent();
            setCurrentComponent(component);
            SpecificationUtil.execEvent(engine, QM_BEFORE_RENDER_COMPONENT);
            try {
                notifyBeginRender();
                return RenderUtil.renderPage(true, this, null,
                        this, requestedSuffix, extension);
            } finally {
                setCurrentComponent(component);
                try {
                    SpecificationUtil.execEvent(engine, QM_AFTER_RENDER_COMPONENT);
                } finally {
                    setCurrentComponent(prevComponent);
                }
            }
        } finally {
            setCurrentPage(this);
            try {
                SpecificationUtil.execEvent(engine, QM_AFTER_RENDER_PAGE);
            } finally {
                setCurrentPage(prevPage);
            }
        }
    }

    protected static Page getCurrentPage() {
        return (Page)CycleUtil.getRequestScope().getAttribute(CURRENT_PAGE_KEY);
    }

    protected static void setCurrentPage(Page page) {
        CycleUtil.getRequestScope().setAttribute(CURRENT_PAGE_KEY, page);
    }


    public static Page getCurrentComponent() {
        return (Page)CycleUtil.getRequestScope().getAttribute(CURRENT_COMPONENT_KEY);
    }

    public static void setCurrentComponent(Page component) {
        CycleUtil.getRequestScope().setAttribute(CURRENT_COMPONENT_KEY, component);
    }

    // for serialize

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    // TemplateRenderer implements ----------------------------------

    public ProcessStatus renderTemplate(
            Page topLevelPage, Template[] templates) {
        if (topLevelPage == null || templates == null
                || templates.length == 0) {
            throw new IllegalArgumentException();
        }
        return templates[0].doTemplateRender(topLevelPage);
    }

    protected Map getBeginRenderListeners() {
        synchronized(this) {
            if (_beginRenderListeners == null) {
                _beginRenderListeners = new ReferenceMap(
                        AbstractReferenceMap.SOFT, AbstractReferenceMap.WEAK, true);
            }
        }
        return _beginRenderListeners;
    }

    public boolean registBeginRenderNotifier(TemplateProcessor processor) {
        boolean result =
            getBeginRenderListeners().containsKey(processor) == false;
        if (result == false) {
            getBeginRenderListeners().put(processor, Boolean.TRUE);
        }
        return result;
    }

    protected void notifyBeginRender() {
        for (Iterator it = getBeginRenderListeners().keySet().iterator();
                it.hasNext(); ) {
            TemplateProcessor listener = (TemplateProcessor) it.next();
            listener.notifyBeginRender(this);
        }
    }

}
