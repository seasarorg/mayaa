/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.standard.engine.processor;

import org.seasar.maya.cycle.CycleWriter;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.ChildEvaluationProcessor;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;

/**
 * ボディーの情報のあるタグの処理をおこなう。
 * @author maruo_syunsuke
 */
public class BodyValueProcessor 
                        extends TemplateProcessorSupport 
                        implements ChildEvaluationProcessor {
    
	private static final long serialVersionUID = 5795848587818700175L;

	protected ProcessStatus process(ServiceCycle cycle){
        return EVAL_PAGE;
    }
    
    private static String BODY_VALUE_NAME 
            = BodyValueProcessor.class.getName() + "@bodyValue" ;
    
    public void setBodyContent(ServiceCycle cycle, CycleWriter body) {
        if (cycle == null || body == null) {
            throw new IllegalArgumentException();
        }
        ProcessorLocalValueUtil.setObject(cycle,this,BODY_VALUE_NAME,body);
    }

    public Object getBodyValue(ServiceCycle cycle){
        return ProcessorLocalValueUtil.getObject(cycle,this,BODY_VALUE_NAME);
    }
    
    public ProcessStatus doStartProcess(ServiceCycle cycle) {
        return EVAL_BODY_BUFFERED;
    }

    public ProcessStatus doEndProcess(ServiceCycle cycle) {
        if (cycle == null) {
            throw new IllegalArgumentException();
        }
        return process(cycle);
    }
    public void doInitChildProcess(ServiceCycle cycle) {
    }
    public ProcessStatus doAfterChildProcess(ServiceCycle cycle) {
        return SKIP_BODY;
    }
    public boolean isChildEvaluation(ServiceCycle cycle) {
        return true;
    }
    public boolean isIteration(ServiceCycle cycle) {
        return false;
    }
}