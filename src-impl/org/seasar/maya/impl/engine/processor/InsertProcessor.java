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
package org.seasar.maya.impl.engine.processor;

import java.util.ArrayList;
import java.util.List;

import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.TemplateRenderer;
import org.seasar.maya.engine.processor.InformalPropertyAcceptable;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.engine.EngineUtil;
import org.seasar.maya.impl.engine.RenderNotCompletedException;
import org.seasar.maya.impl.engine.RenderUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class InsertProcessor extends TemplateProcessorSupport
        implements InformalPropertyAcceptable, 
            TemplateRenderer, CONST_IMPL {

	private static final long serialVersionUID = -1240398725406503403L;
	
    private String _path;
    private String _name;
    private Page _page;
    private String _suffix;
    private String _extension;
    private List _attributes;
    
    // MLD property, required
    public void setPath(String path) {
        _path = path;
    }
    
    // MLD property
    public void setName(String name) {
        _name = name;
    }
    
    // MLD method
    public void addInformalProperty(ProcessorProperty attr) {
        if(_attributes == null) {
            _attributes = new ArrayList();
        }
        _attributes.add(attr);
    }
    
    public List getInformalProperties() {
        if(_attributes == null) {
            _attributes = new ArrayList();
        }
        return _attributes;
    }
    
    protected void preparePage() {
        if(_page == null && StringUtil.hasValue(_path)) {
            Engine engine = EngineUtil.getEngine();
            String suffixSeparator = engine.getParameter(SUFFIX_SEPARATOR);
            String[] pagePath = StringUtil.parsePath(_path, suffixSeparator);
            _page = engine.getPage(pagePath[0]);  
            _suffix = pagePath[1];
            _extension = pagePath[2];
        }
    }
    
    public ProcessStatus doStartProcess(Page topLevelPage) {
        synchronized(this) {
            preparePage();
        }
        Page renderPage = _page;
        String requestedSuffix = _suffix;
        String extension = _extension;
        boolean findSuper = true;
        if(renderPage == null) {
        	ServiceCycle cycle = CycleUtil.getServiceCycle();
            renderPage = topLevelPage;
            Request request = cycle.getRequest();
            requestedSuffix = request.getRequestedSuffix();
            extension = request.getExtension();
            findSuper = false;
        }
        if(renderPage == null) {
            throw new IllegalStateException();
        }
        ProcessStatus ret = RenderUtil.renderPage(
                findSuper, this, getVariables(),
                renderPage, requestedSuffix, extension);
        if(ret == null) {
            Response response = CycleUtil.getResponse();
            if(response.getWriter().isDirty() == false) {
                throw new RenderNotCompletedException(
                        renderPage.getPageName(), extension);
            }
        }
        if(ret == EVAL_PAGE) {
            ret = SKIP_BODY;
        }
        return ret;
    }
    
    // TemplateRenderer implements ----------------------------------

    protected DoRenderProcessor findDoRender(
            ProcessorTreeWalker proc, String name) {
        DoRenderProcessor doRender = null;
        for(int i = 0; i < proc.getChildProcessorSize(); i++) {
            ProcessorTreeWalker child = proc.getChildProcessor(i);
            if(child instanceof DoRenderProcessor) {
                doRender =  (DoRenderProcessor)child;
                if(StringUtil.isEmpty(name) || 
                        name.equals(doRender.getName())) {
                    break;
                }
            }
            doRender = findDoRender(child, name);
            if(doRender != null) {
                break;
            }
        }
        return doRender;
    }
    
    protected TemplateProcessor getRenderRoot(
            DoRenderProcessor doRender) {
        if(doRender.isRendered()) {
            ProcessorTreeWalker duplecated = doRender.getParentProcessor();
            if(duplecated == null ||
                    duplecated instanceof TemplateProcessor == false) {
                throw new IllegalStateException();
            }
            return (TemplateProcessor)duplecated;
        }
        return doRender;
    }
    
    public ProcessStatus renderTemplate(
            Page topLevelPage, Template template) {
        DoRenderProcessor doRender = findDoRender(template, _name);
        if(doRender == null) {
            String systemID = template.getSource().getSystemID();
            throw new DoRenderNotFoundException(systemID, _name);
        }
        TemplateProcessor insertRoot = getRenderRoot(doRender);
        doRender.pushInsertProcessor(this);
        ProcessStatus ret = 
            RenderUtil.renderTemplateProcessor(topLevelPage, insertRoot); 
        doRender.popInsertProcessor();
        return ret;
    }    

}
