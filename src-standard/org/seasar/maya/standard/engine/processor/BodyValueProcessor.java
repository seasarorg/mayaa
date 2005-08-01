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
 * AbstractBodyProcessorの簡素版
 * @author maruo_syunsuke
 */
public abstract class BodyValueProcessor extends TemplateProcessorSupport implements ChildEvaluationProcessor {
    
	private static final long serialVersionUID = 5795848587818700175L;

	protected abstract ProcessStatus process(ServiceCycle cycle);
    
    private String getBodyContentKey() {
        return getClass().getName() + "@" + hashCode();
    }
    
    public void setBodyContent(ServiceCycle cycle, CycleWriter body) {
        if (cycle == null || body == null) {
            throw new IllegalArgumentException();
        }
        cycle.setAttribute(getBodyContentKey(),body);
    }

    public Object getBodyValue(ServiceCycle cycle){
        Object obj = cycle.getAttribute(getBodyContentKey());
        if (obj instanceof CycleWriter) {
            obj = ((CycleWriter)obj).getString();
        }
        return obj;
    }
    
    public ProcessStatus doStartProcess(ServiceCycle cycle) {
        return EVAL_BODY_BUFFERED;
    }

    public ProcessStatus doEndProcess(ServiceCycle cycle) {
        if (cycle == null) {
            throw new IllegalArgumentException();
        }
        try{
            return process(cycle);
        } finally {
            cycle.removeAttribute(getBodyContentKey());
        }
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