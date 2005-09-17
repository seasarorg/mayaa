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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.seasar.maya.builder.TemplateBuilder;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.engine.processor.TemplateProcessor.ProcessStatus;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.engine.specification.SpecificationImpl;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateImpl extends SpecificationImpl
		implements Template, CONST_IMPL {

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
    
    protected String getContentType() {
        SpecificationNode maya = SpecificationUtil.getMayaNode(this);
        if(maya != null) {
            String contentType = SpecificationUtil.getAttributeValue(
                    maya, QM_CONTENT_TYPE);
            if(StringUtil.hasValue(contentType)) {
                return contentType;
            }
        }
        Request request = CycleUtil.getRequest();
        String ret = request.getMimeType();
        if(ret == null) {
            ret = "text/html; charset=UTF-8";
        }
        return ret ;
    }
    
    protected void prepareCycle() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Response response = cycle.getResponse();
        String contentType = getContentType();
        response.setContentType(contentType);
    }
    
    public ProcessStatus doTemplateRender() {
        RenderUtil.saveToCycle(this);
        prepareCycle();
        ProcessStatus ret = RenderUtil.render(this);
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
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        TemplateBuilder builder = provider.getTemplateBuilder();
        builder.build(this);
    }

    protected void checkTimestamps() {
        Date templateTime = getTimestamp();
        if(templateTime != null) {
            Page page = getPage();
            Date pageTime = page.getTimestamp();
            Date engineTime = EngineUtil.getEngine().getTimestamp();
            if(pageTime.after(templateTime) || engineTime.after(templateTime)) {
                setTimestamp(null);
            }
        }
        synchronized(this) {
            if(isOldSpecification()) {
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
    
}
