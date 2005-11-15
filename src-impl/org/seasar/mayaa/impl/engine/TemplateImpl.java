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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.seasar.mayaa.builder.TemplateBuilder;
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
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateImpl 
		extends SpecificationImpl implements Template, CONST_IMPL {

	private static final long serialVersionUID = -5368325487192629078L;

    private Page _page;
    private String _suffix ;
    private String _extension;
    private List _childProcessors = new ArrayList();
    
    public TemplateImpl(Page page, String suffix, String extension) {
        if(page == null || suffix == null || extension == null) {
            throw new IllegalArgumentException();
        }
        _page = page;
        _suffix = suffix;
        _extension = extension;
    }
    
    public Page getPage() {
        return _page;
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

    protected String getContentType(Page topLevelPage) {
        String contentType = findMayaaAttribute(topLevelPage, QM_CONTENT_TYPE);
        if (contentType != null) {
            return contentType;
        }

        RequestScope request = CycleUtil.getRequestScope();
        String ret = request.getMimeType();
        if(ret == null) {
            ret = "text/html; charset=UTF-8";
        }
        return ret;
    }

    protected boolean isNoCache(Page topLevelPage) {
        String noCache = findMayaaAttribute(topLevelPage, QM_NO_CACHE);
        if (noCache != null) {
            return ObjectUtil.booleanValue(noCache, false);
        }

        return false;
    }

    protected void prepareCycle(Page topLevelPage) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Response response = cycle.getResponse();
        String contentType = getContentType(topLevelPage);
        response.setContentType(contentType);
        if(isNoCache(topLevelPage)) {
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
            response.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
        }
    }

    public ProcessStatus doTemplateRender(Page topLevelPage) {
        RenderUtil.saveToCycle(this);
        prepareCycle(topLevelPage);
        ProcessStatus ret = 
            RenderUtil.renderProcessorTree(topLevelPage, this);
        return ret;
    }
    
    public void clear() {
        synchronized(this) {
            _childProcessors.clear();
            super.clear();
        }
    }

    protected void parseSpecification() {
        setTimestamp(new Date());
        clear();
        TemplateBuilder builder = ProviderUtil.getTemplateBuilder();
        builder.build(this);
    }

    protected void checkTimestamps() {
        Date templateTime = getTimestamp();
        if(templateTime != null) {
            Page page = getPage();
            Date pageTime = page.getTimestamp();
            Date engineTime = ProviderUtil.getEngine().getTimestamp();
            if(pageTime.after(templateTime) || engineTime.after(templateTime)) {
                setTimestamp(null);
            }
        }
        synchronized(this) {
            if(isSourceNotExists() || isOldSpecification()) {
                parseSpecification();
            }
        }
    }

    // ProcessorTreeWalker implements --------------------------------
    
    public Map getVariables() {
        return null;
    }

    public void setParentProcessor(ProcessorTreeWalker parent, int index) {
        throw new IllegalStateException();
    }

    public ProcessorTreeWalker getParentProcessor() {
        return null;
    }

    public int getIndex() {
        return 0;
    }

	public void addChildProcessor(ProcessorTreeWalker child) {
        if(child == null) {
            throw new IllegalArgumentException();
        }
        synchronized(_childProcessors) {
            _childProcessors.add(child);
            child.setParentProcessor(this, _childProcessors.size() - 1);
        }
    }

    public int getChildProcessorSize() {
        checkTimestamps();
        return _childProcessors.size();
    }

    public ProcessorTreeWalker getChildProcessor(int index) {
        checkTimestamps();
        return (ProcessorTreeWalker)_childProcessors.get(index);
    }

    // PositionAware implements ------------------------------------

    public boolean isOnTemplate() {
        return true;
    }

}
