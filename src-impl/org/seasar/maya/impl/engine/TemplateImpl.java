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
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.ChildEvaluationProcessor;
import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.processor.TryCatchFinallyProcessor;
import org.seasar.maya.engine.processor.TemplateProcessor.ProcessStatus;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.engine.processor.ElementProcessor;
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
    
    private String _suffix ;
    private int _index;
    private List _childProcessors = new ArrayList();

    public TemplateImpl(Page page, String suffix) {
        super(page);
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

    private ChildEvaluationProcessor getEvaluation(
            TemplateProcessor current) {
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
    
    private TryCatchFinallyProcessor getTryCatchFinally(
            TemplateProcessor current) {
        return (TryCatchFinallyProcessor)current;
    }
    
    private ProcessStatus renderRoot(ProcessorTreeWalker root) {
        for(int i = 0; i < root.getChildProcessorSize(); i++) {
            ProcessorTreeWalker child = root.getChildProcessor(i);
            if(child instanceof TemplateProcessor) {
                TemplateProcessor childProc = (TemplateProcessor)child;
                final ProcessStatus childRet = render(childProc);
                if(childRet == TemplateProcessor.SKIP_PAGE) {
                    return TemplateProcessor.SKIP_PAGE;
                }
            } else {
                throw new IllegalStateException();
            }
        }
        return TemplateProcessor.EVAL_PAGE;
    }
    
    // main rendering method
    private ProcessStatus render(TemplateProcessor current) {
        if(current == null) {
            throw new IllegalArgumentException();
        }
        saveToCycle(current);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        ProcessStatus ret = TemplateProcessor.EVAL_PAGE;
        try { 
            SpecificationUtil.startScope(null);
            ProcessStatus startRet = TemplateProcessor.EVAL_BODY_INCLUDE;
        	startRet = current.doStartProcess();
            if(startRet == TemplateProcessor.SKIP_PAGE) {
                return TemplateProcessor.SKIP_PAGE;
            }
            boolean buffered = false;
            if(startRet == ChildEvaluationProcessor.EVAL_BODY_BUFFERED && 
                    isEvaluation(current)) {
                buffered = true;
                getEvaluation(current).setBodyContent(
                        cycle.getResponse().pushWriter());
                getEvaluation(current).doInitChildProcess();
            }
            if(startRet == TemplateProcessor.EVAL_BODY_INCLUDE || 
                    startRet == ChildEvaluationProcessor.EVAL_BODY_BUFFERED) {
            	ProcessStatus afterRet;
                do {
                    for(int i = 0; i < current.getChildProcessorSize(); i++) {
                        ProcessorTreeWalker child = current.getChildProcessor(i);
                        if(child instanceof TemplateProcessor) {
                            TemplateProcessor childProc = (TemplateProcessor)child;
                            final ProcessStatus childRet = render(childProc);
                            if(childRet == TemplateProcessor.SKIP_PAGE) {
                                return TemplateProcessor.SKIP_PAGE;
                            }
                        } else {
                            throw new IllegalStateException();
                        }
                    }
                    afterRet = TemplateProcessor.SKIP_BODY;
                    saveToCycle(current);
                    if(isIteration(current)) {
                        afterRet = getIteration(current).doAfterChildProcess();
                        ProcessorTreeWalker parent = current.getParentProcessor();
                        if(parent instanceof TemplateProcessor) {
                            TemplateProcessor parentProc = (TemplateProcessor)parent;
                        	if(afterRet == IterationProcessor.EVAL_BODY_AGAIN &&
                                    isDuplicated(parentProc)) {
                                saveToCycle(parentProc);
                                parentProc.doEndProcess();
                                parentProc.doStartProcess();
                        	}
                        }
                    }
                } while(afterRet == IterationProcessor.EVAL_BODY_AGAIN);
            }
            if(buffered) {
                cycle.getResponse().popWriter();
            }
            saveToCycle(current);
            ret = current.doEndProcess();
            SpecificationUtil.endScope();
        } catch (RuntimeException e) {
            saveToCycle(current);
            if(isTryCatchFinally(current)) {
                getTryCatchFinally(current).doCatchProcess(e);
                SpecificationUtil.endScope();
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

    private void saveToCycle(ProcessorTreeWalker current) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if(current instanceof TemplateProcessor) {
            TemplateProcessor proc = (TemplateProcessor)current;
            cycle.setOriginalNode(proc.getOriginalNode());
            cycle.setInjectedNode(proc.getInjectedNode());
        } else if(current instanceof Template) {
            Template temp = (Template)current;
            cycle.setOriginalNode(temp);
            cycle.setInjectedNode(temp);
        }
    }
    
    public ProcessStatus doTemplateRender(ProcessorTreeWalker root) {
        if(root == null) {
            throw new IllegalArgumentException();
        }
        saveToCycle(this);
        if(root.getParentProcessor() == null) {
            prepareCycle();
        }
        Object model = SpecificationUtil.getSpecificationModel(this);
        SpecificationUtil.startScope(model);
        SpecificationUtil.execEvent(this, QM_BEFORE_RENDER);
        ProcessStatus ret;
        ret = renderRoot(root);
        saveToCycle(this);
        SpecificationUtil.execEvent(this, QM_AFTER_RENDER);
        SpecificationUtil.endScope();
        return ret;
    }

    private void checkTemplate() {
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

	public void addChildProcessor(ProcessorTreeWalker child) {
        if(child == null) {
            throw new IllegalArgumentException();
        }
        synchronized(_childProcessors) {
            _childProcessors.add(child);
            child.setParentProcessor(this, _childProcessors.size() - 1);
        }
    }

    public ProcessorTreeWalker getChildProcessor(int index) {
        checkTemplate();
        return (ProcessorTreeWalker)_childProcessors.get(index);
    }

    public int getChildProcessorSize() {
        checkTemplate();
        return _childProcessors.size();
    }

    public int getIndex() {
        return _index;
    }

    // TODO コンポーネントをキャッシュできなくなるので親を保持するべきでない?
    
    private ProcessorTreeWalker _parentProcessor;
    
    public void setParentProcessor(ProcessorTreeWalker parent, int index) {
        if(parent == null || index < 0) {
            throw new IllegalArgumentException();
        }
        _parentProcessor = parent;
        _index = index;
    }

    public ProcessorTreeWalker getParentProcessor() {
        return _parentProcessor;
    }
    
}
