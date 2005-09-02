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

import org.seasar.maya.builder.TemplateBuilder;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.ChildEvaluationProcessor;
import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.processor.TryCatchFinallyProcessor;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.processor.ElementProcessor;
import org.seasar.maya.impl.engine.specification.NodeNamespaceImpl;
import org.seasar.maya.impl.engine.specification.SpecificationImpl;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.ScriptUtil;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateImpl extends SpecificationImpl
		implements Template, CONST_IMPL {

	private static final long serialVersionUID = -5368325487192629078L;
	private static final NodeNamespace NS_HTML = 
        new NodeNamespaceImpl("", URI_HTML);
    
    private String _suffix ;
    private TemplateProcessor _parentNode;
    private int _index;
    private List _childProcessors = new ArrayList();

    public TemplateImpl(Page page, String suffix) {
        super(QM_TEMPLATE, page);
        if(suffix == null) {
            throw new IllegalArgumentException();
        }
        _suffix = suffix;
    }
    
    public void clear() {
        synchronized(this) {
            _childProcessors.clear();
            super.clear();
        }
    }
    
    public NodeNamespace getDefaultNamespace() {
        return NS_HTML;
    }
    
    public Page getPage() {
        return (Page)getParentSpecification();
    }

    protected void parseSpecification() {
        setTimestamp(new Date());
        clear();
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        TemplateBuilder builder = provider.getTemplateBuilder();
        builder.build(this);
    }

    public String getSuffix() {
        return _suffix;
    }
    
    private boolean isEvaluation(TemplateProcessor current) {
        return current instanceof ChildEvaluationProcessor && 
        		((ChildEvaluationProcessor)current).isChildEvaluation();
    }

    private ChildEvaluationProcessor getEvaluation(TemplateProcessor current) {
        return (ChildEvaluationProcessor)current;
    }
    
    private boolean isIteration(TemplateProcessor current) {
        return current instanceof IterationProcessor &&
        		((IterationProcessor)current).isIteration();
    }
    
    private IterationProcessor getIteration(TemplateProcessor current) {
        return (IterationProcessor)current;
    }
    
    private boolean isDuplicated(TemplateProcessor current) {
        return current instanceof ElementProcessor &&
        		((ElementProcessor)current).isDuplicated(); 
    }
    
    private boolean isTryCatchFinally(TemplateProcessor current) {
        if( current instanceof TryCatchFinallyProcessor ){
            TryCatchFinallyProcessor tryCatchFinallyProcessor 
                                        = (TryCatchFinallyProcessor)current;
            return tryCatchFinallyProcessor.canCatch();
        }
        return false ;
    }
    
    private TryCatchFinallyProcessor getTryCatchFinally(TemplateProcessor current) {
        return (TryCatchFinallyProcessor)current;
    }
    
    // main rendering method
    private ProcessStatus render(TemplateProcessor current) {
        if(current == null) {
            throw new IllegalArgumentException();
        }
        saveToCycle(current);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        ProcessStatus ret = EVAL_PAGE;
        try { 
            ScriptEnvironment scriptEnvironment = ScriptUtil.getScriptEnvironment();
            scriptEnvironment.startScope(null);
        	ProcessStatus startRet = current.doStartProcess();
            if(startRet == SKIP_PAGE) {
                return SKIP_PAGE;
            }
            boolean buffered = false;
            if(startRet == ChildEvaluationProcessor.EVAL_BODY_BUFFERED && 
                    isEvaluation(current)) {
                buffered = true;
                getEvaluation(current).setBodyContent(cycle.getResponse().pushWriter());
                getEvaluation(current).doInitChildProcess();
            }
            if(startRet == EVAL_BODY_INCLUDE || 
                    startRet == ChildEvaluationProcessor.EVAL_BODY_BUFFERED) {
            	ProcessStatus afterRet;
                do {
                    for(int i = 0; i < current.getChildProcessorSize(); i++) {
                        final ProcessStatus child = render(current.getChildProcessor(i));
                        if(child == SKIP_PAGE) {
                            return SKIP_PAGE;
                        }
                    }
                    afterRet = SKIP_BODY;
                    saveToCycle(current);
                    if(isIteration(current)) {
                        afterRet = getIteration(current).doAfterChildProcess();
                    	TemplateProcessor parent = current.getParentProcessor();
                    	if(afterRet == IterationProcessor.EVAL_BODY_AGAIN &&
                                isDuplicated(parent)) {
                            saveToCycle(parent);
                            parent.doEndProcess();
                            parent.doStartProcess();
                    	}
                    }
                } while(afterRet == IterationProcessor.EVAL_BODY_AGAIN);
            }
            if(buffered) {
                cycle.getResponse().popWriter();
            }
            saveToCycle(current);
            ret = current.doEndProcess();
            scriptEnvironment.endScope();
        } catch (RuntimeException e) {
            saveToCycle(current);
            if(isTryCatchFinally(current)) {
                getTryCatchFinally(current).doCatchProcess(e);
            } else {
                throw e;
            }
        } finally {
            saveToCycle(current);
            if(isTryCatchFinally(current)) {
                getTryCatchFinally(current).doFinallyProcess();
            }
        }
        return ret;
    }
    
    private String getContentType() {
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
    
    private void prepareCycle() {
    	ServiceCycle cycle = CycleUtil.getServiceCycle();
    	Response response = cycle.getResponse();
        String contentType = getContentType();
        response.setContentType(contentType);
    }

    private void saveToCycle(TemplateProcessor processor) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(processor.getOriginalNode());
        cycle.setInjectedNode(processor.getInjectedNode());
    }
    
    public ProcessStatus doTemplateRender(TemplateProcessor renderRoot) {
        saveToCycle(this);
        TemplateProcessor processor = renderRoot;
        if(renderRoot == null) {
            processor = this;
        }
        if(getParentProcessor() == null) {
            prepareCycle();
        }
        Object model = SpecificationUtil.findSpecificationModel(this);
        ScriptUtil.startScope(model);
        ScriptUtil.execEvent(this, QM_BEFORE_RENDER);
        ProcessStatus ret = render(processor);
        saveToCycle(this);
        ScriptUtil.execEvent(this, QM_AFTER_RENDER);
        ScriptUtil.endScope();
        return ret;
    }

    private void checkTemplate() {
        Date templateTime = getTimestamp();
        if(templateTime != null) {
            Page page = getPage();
            Date pageTime = page.getTimestamp();
            Date engineTime = page.getEngine().getTimestamp();
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

	public void addChildProcessor(TemplateProcessor child) {
        if(child == null) {
            throw new IllegalArgumentException();
        }
        synchronized(_childProcessors) {
            _childProcessors.add(child);
            child.setParentProcessor(this, _childProcessors.size() - 1);
        }
    }

    public TemplateProcessor getChildProcessor(int index) {
        checkTemplate();
        return (TemplateProcessor)_childProcessors.get(index);
    }

    public int getChildProcessorSize() {
        checkTemplate();
        return _childProcessors.size();
    }

    public int getIndex() {
        return _index;
    }

    public void setParentProcessor(TemplateProcessor parent, int index) {
        if(parent == null || index < 0) {
            throw new IllegalArgumentException();
        }
        _parentNode = parent;
        _index = index;
    }

    public TemplateProcessor getParentProcessor() {
        return _parentNode;
    }
    
    public ProcessStatus doStartProcess() {
        return EVAL_BODY_INCLUDE;
    }

    public ProcessStatus doEndProcess() {
        return EVAL_PAGE;
    }

    public void setOriginalNode(SpecificationNode node) {
        throw new IllegalStateException();
    }

    public SpecificationNode getOriginalNode() {
        return this;
    }

    public void setInjectedNode(SpecificationNode node) {
        throw new IllegalStateException();
    }

    public SpecificationNode getInjectedNode() {
        return this;
    }
    
}
