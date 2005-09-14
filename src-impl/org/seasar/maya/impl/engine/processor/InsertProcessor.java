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
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.engine.EngineUtil;
import org.seasar.maya.impl.engine.PageNotFoundException;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class InsertProcessor
        extends AbstractAttributableProcessor	implements CONST_IMPL {

	private static final long serialVersionUID = -1240398725406503403L;
	private String _path;
    private String _name = "";
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
        if(name == null) {
            name = "";
        }
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
    
    protected DoRenderProcessor findDoRender(
            ProcessorTreeWalker processor) {
        for(int i = 0; i < processor.getChildProcessorSize(); i++) {
            ProcessorTreeWalker child = processor.getChildProcessor(i);
            if(child instanceof DoRenderProcessor) {
                DoRenderProcessor doRender =  (DoRenderProcessor)child;
                if(_name.equals(doRender.getName())) {
                    return doRender;
                }
            }
            DoRenderProcessor doRender = findDoRender(child);
            if(doRender != null) {
                return doRender;
            }
        }
        return null;
    }
    
    public ProcessStatus doBody() {
        Template template = getTemplate();
        for(int i = 0; i < getChildProcessorSize(); i++) {
            ProcessorTreeWalker child = getChildProcessor(i);
            if(child instanceof TemplateProcessor) {
                TemplateProcessor proc = (TemplateProcessor)child;
                if(template.doTemplateRender(proc) == TemplateProcessor.SKIP_PAGE) {
                    return TemplateProcessor.SKIP_PAGE;
                }
            } else {
                throw new IllegalStateException();
            }
        }
        return TemplateProcessor.EVAL_PAGE;
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
        // TODO setParentProcessor‚µ‚È‚¢‚Å‚àAOK‚È‚æ‚¤‚É‚·‚éB
        template.setParentProcessor(this, 0);
        DoRenderProcessor start = findDoRender(template);
        if(start != null) {
            if(start.isRendered()) {
                ProcessorTreeWalker duplecated = start.getParentProcessor();
                if(duplecated instanceof TemplateProcessor) {
                    TemplateProcessor proc = (TemplateProcessor)duplecated;
                    template.doTemplateRender(proc);
                }
            } else {
                template.doTemplateRender(start);
            }
            return TemplateProcessor.SKIP_BODY;
        }
        throw new DoRenderNotFoundException();
    }
    
	protected void writeEndElement() {
	}

}
