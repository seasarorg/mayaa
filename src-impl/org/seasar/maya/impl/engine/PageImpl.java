/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.engine;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor.ProcessStatus;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.cycle.script.ScriptUtil;
import org.seasar.maya.impl.engine.specification.SpecificationImpl;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class PageImpl extends SpecificationImpl
        implements Page, Serializable, CONST_IMPL {

	private static final long serialVersionUID = -8688634709901129128L;

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
        String extendsPath = SpecificationUtil.getMayaAttributeValue(
                this, QM_EXTENDS);
        if(StringUtil.hasValue(extendsPath)) {
            Engine engine = EngineUtil.getEngine();
            String suffixSeparator = engine.getParameter(SUFFIX_SEPARATOR);
            String[] pagePath = StringUtil.parsePath(extendsPath, suffixSeparator);
            _superPage = engine.getPage(pagePath[0]);  
            _superSuffix = pagePath[1];
            _superExtension = pagePath[2];
        }
    }
    
    public Page getSuperPage() {
        synchronized(this) {
            if(_superPage == null) {
            	prepareSuper();
            }
        }
        return _superPage;
    }

    public String getSuperSuffix() {
        synchronized(this) {
            if(_superPage == null) {
            	prepareSuper();
            }
        }
        return _superSuffix;
    }

    public String getSuperExtension() {
        synchronized(this) {
            if(_superPage == null) {
            	prepareSuper();
            }
        }
        return _superExtension;
    }
    
    public String getPageName() {
        return _pageName;
    }

    public CompiledScript getSuffixScript() {
        String value = SpecificationUtil.getMayaAttributeValue(
                this, QM_TEMPLATE_SUFFIX);
        if(StringUtil.isEmpty(value)) {
            value = SpecificationUtil.getMayaAttributeValue(
                    EngineUtil.getEngine(), QM_TEMPLATE_SUFFIX);
        }
        if(StringUtil.isEmpty(value)) {
            value = "";
        }        
        return ScriptUtil.compile(value, String.class);
    }


    private boolean match(Template template,  String suffix) {
        String templateSuffix = template.getSuffix();
        if(templateSuffix.equals(suffix)) {
            return true;
        }
        return false;
    }
    
    protected Template getTemplateImpl(String suffix, String extension) {
        if(suffix == null) {
            throw new IllegalArgumentException();
        }
        Template template = null;
        synchronized(this) {
            if(_templates != null) {
                for(Iterator it = new ChildSpecificationsIterator(_templates); 
                        it.hasNext(); ) {
                    Object obj = it.next();
                    if(obj instanceof Template) {
                        Template test = (Template)obj;
                        if(match(test, suffix)) {
                            template = test;
                            break;
                        }
                    }
                }
            }
            if(template == null) {
                StringBuffer name = new StringBuffer(_pageName);
                if(StringUtil.hasValue(suffix)) {
                    String separator = EngineUtil.getEngineSetting(
                            SUFFIX_SEPARATOR, "$");
                    name.append(separator).append(suffix);
                }
                if(StringUtil.hasValue(extension)) {
                    name.append(".").append(extension);
                }
                ServiceProvider provider = ProviderFactory.getServiceProvider();
                SourceDescriptor source = 
                    provider.getPageSourceDescriptor(name.toString());
                if(source.exists()) {
                    template = new TemplateImpl(this, suffix, extension);
                    template.setSource(source);
                    if (_templates == null) {
                        _templates = new ArrayList();
                    }
                    _templates.add(new SoftReference(template));
                }
            }
        }
        return template;
    }
    
    public Template getTemplate(String suffix, String extension) {
        Template template = getTemplateImpl(suffix, extension);
        if(template == null && StringUtil.hasValue(suffix)) {
            template = getTemplateImpl("", extension);
        }
        if(template == null) {
            throw new PageNotFoundException(_pageName, extension);
        }
        return template;
    }
    
    protected void saveToCycle() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(this);
        cycle.setInjectedNode(this);
    }

    public ProcessStatus doPageRender(
    		String requestedSuffix, String extension) throws PageForwarded {
        saveToCycle();
        Page page = this;
        String suffix = null;
        while(page.getSuperPage() != null) {
            page = page.getSuperPage();
            suffix = page.getSuperSuffix();
            extension = page.getSuperExtension();
        }
        Object model = SpecificationUtil.getSpecificationModel(page);
        SpecificationUtil.startScope(model, null);
        SpecificationUtil.execEvent(page, QM_BEFORE_RENDER);
        ProcessStatus ret = null;
        if("maya".equals(extension) == false) {
            if(StringUtil.isEmpty(suffix)) {
            	if(StringUtil.isEmpty(requestedSuffix)) {
	                CompiledScript script = getSuffixScript();
	                suffix = (String)script.execute();
            	} else {
            		suffix = requestedSuffix;
            	}
            }
            Template template = page.getTemplate(suffix, extension);
            ret = template.doTemplateRender();
            saveToCycle();
        }
        SpecificationUtil.execEvent(page, QM_AFTER_RENDER);
        SpecificationUtil.endScope();
        return ret;
    }

}
