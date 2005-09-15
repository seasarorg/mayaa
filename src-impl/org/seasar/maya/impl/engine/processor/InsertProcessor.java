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
import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.engine.EngineUtil;
import org.seasar.maya.impl.engine.PageNotFoundException;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class InsertProcessor
        extends AbstractAttributableProcessor	implements CONST_IMPL {

	private static final long serialVersionUID = -1240398725406503403L;
	private String _path;
    private String _name;
    private Page _page;
    
    // MLD property, required
    public void setPath(String path) {
        if(StringUtil.isEmpty(path)) {
            throw new IllegalArgumentException();
        }
        _path = path;
    }
    
    // MLD property
    public void setName(String name) {
        _name = name;
    }
    
    protected Page preparePage() {
        if(StringUtil.isEmpty(_path)) {
            throw new IllegalStateException();
        }
        Engine engine = EngineUtil.getEngine();
        String suffixSeparator = engine.getParameter(SUFFIX_SEPARATOR);
        String[] pagePath = StringUtil.parsePath(_path, suffixSeparator);
        Page page = engine.getPage(getTemplate(), pagePath[0], pagePath[2]);  
        return page;
    }
    
    protected DoRenderProcessor findDoRender(ProcessorTreeWalker proc) {
        DoRenderProcessor doRender = null;
        for(int i = 0; i < proc.getChildProcessorSize(); i++) {
            ProcessorTreeWalker child = proc.getChildProcessor(i);
            if(child instanceof DoRenderProcessor) {
           		doRender =  (DoRenderProcessor)child;
               	if(StringUtil.isEmpty(_name) || 
               			_name.equals(doRender.getName())) {
           			break;
            	}
            }
            doRender = findDoRender(child);
            if(doRender != null) {
                break;
            }
        }
        return doRender;
    }
    
    public ProcessStatus doBase() {
        Template template = getTemplate();
        return template.doTemplateRender(this);
    }

    protected void saveToCycle() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(_page);
        cycle.setInjectedNode(_page);
    }
    
    protected ProcessorTreeWalker getRenderRoot(Template template) {
        DoRenderProcessor doRender = findDoRender(template);
        if(doRender == null) {
            throw new DoRenderNotFoundException();
        }
        if(doRender.isRendered()) {
            ProcessorTreeWalker duplecated = doRender.getParentProcessor();
            if(duplecated == null) {
                throw new IllegalStateException();
            }
            ProcessorTreeWalker root = duplecated.getParentProcessor();
            if(root == null) {
                throw new IllegalStateException();
            }
            return root;
        }
        return doRender;
    }
    
    protected ProcessStatus renderTemplate() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        String requiredSuffix = cycle.getRequest().getRequestedSuffix();
        Template template = _page.getTemplate(requiredSuffix);
        if(template == null) {
            throw new PageNotFoundException(
                    _page.getPageName(), requiredSuffix, _page.getExtension());
        }
        template.setParentProcessor(this, 0);
        ProcessorTreeWalker root = getRenderRoot(template);
        ProcessStatus ret = template.doTemplateRender(root);
        if(ret == EVAL_PAGE) {
            ret = SKIP_BODY;
        }
        return ret;
    }
    
	protected ProcessStatus writeStartElement() {
        synchronized(this) {
            if(_page == null) {
                _page = preparePage();
            }
        }
        saveToCycle();
        Object model = SpecificationUtil.getSpecificationModel(_page);
        SpecificationUtil.startScope(model);
        SpecificationUtil.execEvent(_page, QM_BEFORE_RENDER);
        ProcessStatus startRet = SKIP_BODY; 
        if("maya".equals(_page.getExtension()) == false) {
            startRet = renderTemplate();
        }
        saveToCycle();
        SpecificationUtil.execEvent(_page, QM_AFTER_RENDER);
        SpecificationUtil.endScope();
        return startRet;
    }
    
	protected void writeEndElement() {
	}

}
