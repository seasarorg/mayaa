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
package org.seasar.maya.impl.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.PageNotFoundException;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ComponentPageProcessor extends AbstractAttributableProcessor
		implements CONST_IMPL {

	private static final long serialVersionUID = -1240398725406503403L;
	private String _path;
    private String _namespaceURI;
    private Page _page;
    
    // MLD property
    public void setPath(String path) {
        if(StringUtil.isEmpty(path)) {
            throw new IllegalArgumentException();
        }
        _path = path;
    }
    
    // MLD property
    public void setNamespaceURI(String namespaceURI) {
        if(StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        _namespaceURI = namespaceURI;
    }
    
    public String getInformalAttrituteURI() {
        if(StringUtil.isEmpty(_namespaceURI)) {
            return URI_MAYA;
        }
        return _namespaceURI;
    }
    
    protected Page preparePage() {
        if(StringUtil.isEmpty(_path)) {
            throw new IllegalStateException();
        }
        Engine engine = SpecificationUtil.getEngine();
        String suffixSeparator = engine.getParameter(SUFFIX_SEPARATOR);
        String[] pagePath = StringUtil.parsePath(_path, suffixSeparator);
        Page page = engine.getPage(getTemplate(), pagePath[0], pagePath[2]);  
        return page;
    }
    
    protected StartComponentProcessor findStart(TemplateProcessor processor) {
        for(int i = 0; i < processor.getChildProcessorSize(); i++) {
            TemplateProcessor childProcessor = processor.getChildProcessor(i);
            if(childProcessor instanceof StartComponentProcessor) {
                return (StartComponentProcessor)childProcessor;
            }
            StartComponentProcessor doContentsProcessor = findStart(childProcessor);
            if(doContentsProcessor != null) {
                return doContentsProcessor;
            }
        }
        return null;
    }
    
    public ProcessStatus renderChildren() {
        Template template = getTemplate();
        for(int i = 0; i < getChildProcessorSize(); i++) {
            TemplateProcessor processor = getChildProcessor(i);
            if(template.doTemplateRender(processor) == SKIP_PAGE) {
                return SKIP_PAGE;
            }
        }
        return EVAL_PAGE;
    }
    
	protected ProcessStatus writeStartElement() {
        synchronized(this) {
            if(_page == null) {
                _page = preparePage();
            }
        }
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        String requiredSuffix = cycle.getRequest().getRequestedSuffix();
        Template template = _page.getTemplate(requiredSuffix);
        if(template == null) {
            throw new PageNotFoundException(
                    _page.getPageName(), requiredSuffix, _page.getExtension());
        }
        template.setParentProcessor(this, 0);
        StartComponentProcessor start = findStart(template);
        if(start != null) {
            template.doTemplateRender(start);
            return SKIP_BODY;
        }
        throw new StartComponentNotFoundException();
    }
    
	protected void writeEndElement() {
	}

}
