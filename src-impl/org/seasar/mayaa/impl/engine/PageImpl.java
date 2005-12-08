/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.script.ScriptUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.source.SourceUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class PageImpl extends SpecificationImpl
        implements Page, Serializable, CONST_IMPL {

    private static final long serialVersionUID = -5345416943673041700L;

    protected static final QName QM_EXTENDS =
        SpecificationUtil.createQName("extends");

    private Page _superPage;
    private String _superSuffix;
    private String _superExtension;
    private String _pageName;
    private List _templates;

    public PageImpl(String pageName) {
        if(StringUtil.isEmpty(pageName)) {
            throw new IllegalArgumentException();
        }
        _pageName = pageName;
    }

    protected void clear() {
        synchronized(this) {
            _superPage = null;
            super.clear();
        }
    }

    protected void prepareSuper() {
        synchronized(this) {
            if(_superPage != null) {
                return;
            }
            String extendsPath =
                SpecificationUtil.getMayaaAttributeValue(this, QM_EXTENDS);
            if(StringUtil.isEmpty(extendsPath)) {
                return;
            }
            Engine engine = ProviderUtil.getEngine();
            String suffixSeparator = engine.getParameter(SUFFIX_SEPARATOR);
            String[] pagePath = StringUtil.parsePath(extendsPath, suffixSeparator);
            _superPage = engine.getPage(pagePath[0]);
            _superSuffix = pagePath[1];
            _superExtension = pagePath[2];
        }
    }

    public void checkTimestamps() {
        if(isOldSpecification()) {
            parseSpecification();
        }
    }

    public Page getSuperPage() {
        prepareSuper();
        if (_superPage != null) {
            _superPage.checkTimestamp();
        }
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
        if(StringUtil.isEmpty(value)) {
            value = SpecificationUtil.getMayaaAttributeValue(
                    ProviderUtil.getEngine(), QM_TEMPLATE_SUFFIX);
        }
        if(StringUtil.isEmpty(value)) {
            value = "";
        }
        return ScriptUtil.compile(value, String.class);
    }

    protected Template findTemplateFromCache(
            String suffix, String extension) {
        if(_templates != null) {
            for(Iterator it = new ChildSpecificationsIterator(_templates);
                    it.hasNext(); ) {
                Object obj = it.next();
                if(obj instanceof Template == false) {
                    throw new IllegalStateException();
                }
                Template template = (Template)obj;
                String templateSuffix = template.getSuffix();
                String templateExtension = template.getExtension();
                if(templateSuffix.equals(suffix) &&
                        templateExtension.equals(extension)) {
                    return template;
                }
            }
        }
        return null;
    }

    protected Template createNewTemplate(String suffix, String extension) {
        StringBuffer name = new StringBuffer(_pageName);
        if(StringUtil.hasValue(suffix)) {
            String separator = EngineUtil.getEngineSetting(
                    SUFFIX_SEPARATOR, "$");
            name.append(separator).append(suffix);
        }
        if(StringUtil.hasValue(extension)) {
            name.append(".").append(extension);
        }
        SourceDescriptor source = 
            SourceUtil.getSourceDescriptor(name.toString());
        if(source.exists()) {
            Template template = new TemplateImpl(this, suffix, extension);
            template.setSource(source);
            return template;
        }
        return null;
    }

    protected Template getTemplateFromFixedSuffix(
            String suffix, String extension) {
        if(suffix == null || extension == null) {
            throw new IllegalArgumentException();
        }
        synchronized(this) {
            Template template = findTemplateFromCache(suffix, extension);
            if(template != null) {
                if (template.getSource().exists()) {
                    return template;
                }
                return null;
            }
            template = createNewTemplate(suffix, extension);
            if(template == null) {
                return null;
            }

            if (_templates == null) {
                _templates = new ArrayList();
            }
            _templates.add(new SoftReference(template));
            return template;
        }
    }

    public Template getTemplate(String suffix, String extension) {
        if(suffix == null) {
            suffix = "";
        }
        if(extension == null) {
            extension = "";
        }
        Template template = getTemplateFromFixedSuffix(suffix, extension);
        if(template == null && StringUtil.hasValue(suffix)) {
            template = getTemplateFromFixedSuffix("", extension);
        }
        if(template == null) {
            throw new PageNotFoundException(_pageName, extension);
        }
        return template;
    }

    public ProcessStatus doPageRender(
            String requestedSuffix, String extension) {
        return RenderUtil.renderPage(true, this, null,
                this, requestedSuffix, extension);
    }

    // TemplateRenderer implements ----------------------------------

    public ProcessStatus renderTemplate(
            Page topLevelPage, Template[] templates) {
        if(topLevelPage == null || templates == null || templates.length == 0) {
            throw new IllegalArgumentException();
        }
        return templates[0].doTemplateRender(topLevelPage);
    }

}
