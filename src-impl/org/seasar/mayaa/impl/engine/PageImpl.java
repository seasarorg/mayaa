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

import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.script.ScriptUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.management.SpecificationProfileRegistry;
import org.seasar.mayaa.impl.management.SpecificationStats;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class PageImpl extends SpecificationImpl implements Page {

    private static final long serialVersionUID = -4473136348846078029L;
    static final Log LOG = LogFactory.getLog(PageImpl.class);
    private static final String CURRENT_PAGE_KEY = "__currentPage__";
    private static final String CURRENT_COMPONENT_KEY = "__currentComponent__";

    static final class SuperPageInfo {
        final Page page;
        final String suffix;
        final String extension;

        SuperPageInfo(Page page, String suffix, String extension) {
            this.page = page;
            this.suffix = suffix;
            this.extension = extension;
        }
    }

    private String _pageName;

    private transient Map<TemplateProcessor, Boolean> _beginRenderListeners;
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

    SuperPageInfo resolveSuperPageInfo() {
        // 常に最新のextends定義を参照し、継承先はEngineキャッシュ経由で解決する。
        String extendsPath =
            SpecificationUtil.getMayaaAttributeValue(this, CONST_IMPL.QM_EXTENDS);
        if (StringUtil.isEmpty(extendsPath)) {
            return new SuperPageInfo(null, null, null);
        }
        Engine engine = ProviderUtil.getEngine();
        String suffixSeparator = engine.getParameter(CONST_IMPL.SUFFIX_SEPARATOR);
        String[] pagePath = StringUtil.parsePath(extendsPath, suffixSeparator);

        Page superPage = engine.getPage(
                StringUtil.adjustRelativePath(_pageName, pagePath[0]));
        return new SuperPageInfo(superPage, pagePath[1], pagePath[2]);
    }

    public Page getSuperPage() {
        return resolveSuperPageInfo().page;
    }

    public String getSuperSuffix() {
        return resolveSuperPageInfo().suffix;
    }

    public String getSuperExtension() {
        return resolveSuperPageInfo().extension;
    }

    public String getPageName() {
        return _pageName;
    }

    public CompiledScript getSuffixScript() {
        String value = SpecificationUtil.getMayaaAttributeValue(
                this, CONST_IMPL.QM_TEMPLATE_SUFFIX);
        if (StringUtil.isEmpty(value)) {
            value = SpecificationUtil.getMayaaAttributeValue(
                SpecificationUtil.getDefaultSpecification(), CONST_IMPL.QM_TEMPLATE_SUFFIX);
        }
        if (StringUtil.isEmpty(value)) {
            value = "";
        }
        if (_suffixScript != null && value.equals(_suffixScriptText)) {
            return _suffixScript;
        }
        _suffixScript = ScriptUtil.compile(value);
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
        final SpecificationStats profileStats = SpecificationProfileRegistry.getInstance()
                .getOrCreate(getSystemID(), SpecificationStats.TYPE_PAGE);
        final long startMs = System.currentTimeMillis();
        boolean renderError = false;
        try {
        Page prevPage = getCurrentPage();
        setCurrentPage(this);
        Specification defaultSpec = SpecificationUtil.getDefaultSpecification(); 
        SpecificationUtil.execEvent(defaultSpec, CONST_IMPL.QM_BEFORE_RENDER_PAGE);
        try {
            Page component = this;
            Page superPage = component.getSuperPage();
            if (superPage != null) {
                component = superPage;
            }
            Page prevComponent = getCurrentComponent();
            setCurrentComponent(component);
            SpecificationUtil.execEvent(defaultSpec, CONST_IMPL.QM_BEFORE_RENDER_COMPONENT);
            try {
                return RenderUtil.renderPage(true, this, null,
                        this, requestedSuffix, extension);
            } finally {
                setCurrentComponent(component);
                try {
                    SpecificationUtil.execEvent(defaultSpec, CONST_IMPL.QM_AFTER_RENDER_COMPONENT);
                } finally {
                    setCurrentComponent(prevComponent);
                }
            }
        } finally {
            setCurrentPage(this);
            try {
                SpecificationUtil.execEvent(defaultSpec, CONST_IMPL.QM_AFTER_RENDER_PAGE);
            } finally {
                setCurrentPage(prevPage);
            }
        }
        } catch (RuntimeException e) {
            renderError = true;
            throw e;
        } finally {
            profileStats.recordRender(System.currentTimeMillis() - startMs);
            if (renderError) {
                profileStats.recordRenderError();
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

    @Override
    @Deprecated
    public boolean registBeginRenderNotifier(TemplateProcessor processor) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(_beginRenderListeners, _pageName, _suffixScript, _suffixScriptText);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof PageImpl))
            return false;
        PageImpl other = (PageImpl) obj;
        return Objects.equals(_beginRenderListeners, other._beginRenderListeners)
                && Objects.equals(_pageName, other._pageName)
                && Objects.equals(_suffixScript, other._suffixScript)
                && Objects.equals(_suffixScriptText, other._suffixScriptText)
                && super.equals(other);
    }

}
