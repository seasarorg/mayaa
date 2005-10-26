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
package org.seasar.maya.impl.engine;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.TemplateRenderer;
import org.seasar.maya.engine.processor.ChildEvaluationProcessor;
import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.ProcessStatus;
import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.processor.TryCatchFinallyProcessor;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.engine.processor.ElementProcessor;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RenderUtil implements CONST_IMPL {

    private static final ProcessStatus SKIP_BODY = 
            ProcessStatus.SKIP_BODY;
    private static final ProcessStatus EVAL_BODY_INCLUDE = 
            ProcessStatus.EVAL_BODY_INCLUDE;
    private static final ProcessStatus SKIP_PAGE = 
            ProcessStatus.SKIP_PAGE;
    private static final ProcessStatus EVAL_PAGE = 
            ProcessStatus.EVAL_PAGE;
    private static final ProcessStatus EVAL_BODY_AGAIN =
            ProcessStatus.EVAL_BODY_AGAIN;
    private static final ProcessStatus EVAL_BODY_BUFFERED =
            ProcessStatus.EVAL_BODY_BUFFERED;

    private RenderUtil() {
        // no instantiation.
    }

    public static boolean isEvaluation(TemplateProcessor current) {
        return current instanceof ChildEvaluationProcessor && 
        		((ChildEvaluationProcessor)current).isChildEvaluation();
    }

    public static ChildEvaluationProcessor getEvaluation(
            TemplateProcessor current) {
        return (ChildEvaluationProcessor)current;
    }

    public static boolean isIteration(TemplateProcessor current) {
        return current instanceof IterationProcessor &&
        		((IterationProcessor)current).isIteration();
    }

    public static IterationProcessor getIteration(
            TemplateProcessor current) {
        return (IterationProcessor)current;
    }

    public static boolean isDuplicated(TemplateProcessor current) {
        return current instanceof ElementProcessor &&
        		((ElementProcessor)current).isDuplicated(); 
    }

    public static boolean isTryCatchFinally(TemplateProcessor current) {
        if( current instanceof TryCatchFinallyProcessor ){
            TryCatchFinallyProcessor tryCatchFinallyProcessor 
                                        = (TryCatchFinallyProcessor)current;
            return tryCatchFinallyProcessor.canCatch();
        }
        return false ;
    }

    public static TryCatchFinallyProcessor getTryCatchFinally(
            TemplateProcessor current) {
        return (TryCatchFinallyProcessor)current;
    }

    public static void saveToCycle(ProcessorTreeWalker current) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setProcessor(current);
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

    // main rendering method
    public static ProcessStatus renderTemplateProcessor(
            Page topLevelPage, TemplateProcessor current) {
        if(current == null) {
            throw new IllegalArgumentException();
        }
        saveToCycle(current);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        ProcessStatus ret = EVAL_PAGE;
        try { 
            SpecificationUtil.startScope(current.getVariables());
            ProcessStatus startRet = EVAL_BODY_INCLUDE;
        	startRet = current.doStartProcess(topLevelPage);
            if(startRet == SKIP_PAGE) {
                return SKIP_PAGE;
            }
            boolean buffered = false;
            if(startRet == EVAL_BODY_BUFFERED && isEvaluation(current)) {
                buffered = true;
                getEvaluation(current).setBodyContent(
                        cycle.getResponse().pushWriter());
                getEvaluation(current).doInitChildProcess();
            }
            if(startRet == EVAL_BODY_INCLUDE || 
                    startRet == EVAL_BODY_BUFFERED) {
            	ProcessStatus afterRet;
                do {
                    for(int i = 0; i < current.getChildProcessorSize(); i++) {
                        ProcessorTreeWalker child = current.getChildProcessor(i);
                        if(child instanceof TemplateProcessor) {
                            TemplateProcessor childProc =
                                (TemplateProcessor)child;
                            final ProcessStatus childRet = 
                                renderTemplateProcessor(topLevelPage, childProc);
                            if(childRet == SKIP_PAGE) {
                                return SKIP_PAGE;
                            }
                        } else {
                            throw new IllegalStateException();
                        }
                    }
                    afterRet = SKIP_BODY;
                    saveToCycle(current);
                    if(isIteration(current)) {
                        afterRet = getIteration(current).doAfterChildProcess();
                        ProcessorTreeWalker parent = current.getParentProcessor();
                        if(parent instanceof TemplateProcessor) {
                            TemplateProcessor parentProc =
                                (TemplateProcessor)parent;
                        	if(afterRet == EVAL_BODY_AGAIN &&
                                    isDuplicated(parentProc)) {
                                saveToCycle(parentProc);
                                parentProc.doEndProcess();
                                parentProc.doStartProcess(null);
                        	}
                        }
                    }
                } while(afterRet == EVAL_BODY_AGAIN);
            }
            if(buffered) {
                cycle.getResponse().popWriter();
            }
            saveToCycle(current);
            ret = current.doEndProcess();
            SpecificationUtil.endScope();
        } catch (RuntimeException e) {
            if(isTryCatchFinally(current)) {
                getTryCatchFinally(current).doCatchProcess(e);
                SpecificationUtil.endScope();
            } else {
                throw e;
            }
        } finally {
            if(isTryCatchFinally(current)) {
                getTryCatchFinally(current).doFinallyProcess();
            }
        }
        return ret;
    }
    
    // Rendering entry point
    public static ProcessStatus renderProcessorTree(
            Page topLevelPage, ProcessorTreeWalker root) {
        for(int i = 0; i < root.getChildProcessorSize(); i++) {
            ProcessorTreeWalker child = root.getChildProcessor(i);
            if(child instanceof TemplateProcessor) {
                TemplateProcessor childProc = (TemplateProcessor)child;
                final ProcessStatus childRet = 
                    renderTemplateProcessor(topLevelPage, childProc);
                if(childRet == SKIP_PAGE) {
                    return SKIP_PAGE;
                }
            } else {
                throw new IllegalStateException();
            }
        }
        return EVAL_PAGE;
    }
    
    public static void saveToCycle(Page page) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(page);
        cycle.setInjectedNode(page);
    }

    protected static Template getTemplate(String requestedSuffix,
            Page page, String suffix, String extension) {
        boolean maya = "maya".equals(extension);
        if(maya) {
            SourceDescriptor source = page.getSource();
            if(source.exists() == false) {
                String pageName = page.getPageName();
                throw new PageNotFoundException(pageName, extension);
            }
        }
        if(maya == false) {
            if(StringUtil.isEmpty(suffix)) {
                if(StringUtil.isEmpty(requestedSuffix)) {
                    CompiledScript script = page.getSuffixScript();
                    suffix = (String)script.execute(null);
                } else {
                    suffix = requestedSuffix;
                }
            }
            return page.getTemplate(suffix, extension);
        }
        return null;
    }
    
    //TODO コンポーネントのクライアント側スクリプト、CSSの機能の検討。
    //TODO レイアウトやコンポーネントの直接実行を制限する機能の検討。
    public static ProcessStatus renderPage(boolean fireEvent, 
            TemplateRenderer renderer, Map variables, 
            Page topLevelPage, String requestedSuffix, String extension) {
        if(renderer == null || topLevelPage == null) {
            throw new IllegalArgumentException();
        }
        Page page = topLevelPage;
        String suffix = null;
        saveToCycle(page);
        List pageStack = fireEvent ? new LinkedList() : null;
        List templateStack = new LinkedList();
        do {
            if(fireEvent) {
                // stack for afterRender event.
                pageStack.add(0, page);
                SpecificationUtil.startScope(variables);
                SpecificationUtil.execEvent(page, QM_BEFORE_RENDER);
            }
            Template template = 
                getTemplate(requestedSuffix, page, suffix, extension);
            if(template != null) {
                // LIFO access
                templateStack.add(0, template);
            }
            suffix = page.getSuperSuffix();
            extension = page.getSuperExtension();
            page = page.getSuperPage();
            variables = null;
        } while(page != null);
        ProcessStatus ret = null;
        int templateSize = templateStack.size();
        if(templateSize > 0) {
            Template[] templates = (Template[])
                    templateStack.toArray(new Template[templateSize]);
            ret = renderer.renderTemplate(topLevelPage, templates);
            saveToCycle(page);
        }
        if(fireEvent) {
            for(int i = 0; i < pageStack.size(); i++) {
                page = (Page)pageStack.get(i);
                saveToCycle(page);
                SpecificationUtil.execEvent(page, QM_AFTER_RENDER);
                SpecificationUtil.endScope();
            }
        }
        return ret;
    }

}
