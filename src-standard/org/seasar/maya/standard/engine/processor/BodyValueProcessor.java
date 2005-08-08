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
import org.seasar.maya.impl.util.CycleUtil;

/**
 * ボディーの情報のあるタグの処理をおこなう。
 * AbstractBodyProcessorの簡素版
 * @author maruo_syunsuke
 */
public abstract class BodyValueProcessor extends TemplateProcessorSupport implements ChildEvaluationProcessor {
    
	private static final long serialVersionUID = 5795848587818700175L;

	protected abstract ProcessStatus process();
    
    private String getBodyContentKey() {
        return ObjectAttributeUtil.getIdentityKeyString(this,"BODY_VALUE");
    }
    
    public void setBodyContent(CycleWriter body) {
        if (body == null) {
            throw new IllegalArgumentException();
        }
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setAttribute(getBodyContentKey(),body);
    }

    public Object getBodyValue() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Object obj = cycle.getAttribute(getBodyContentKey());
        if (obj instanceof CycleWriter) {
            obj = ((CycleWriter)obj).getString();
        }
        return obj;
    }
    
    public ProcessStatus doStartProcess() {
        return EVAL_BODY_BUFFERED;
    }

    public ProcessStatus doEndProcess() {
        try{
            return process();
        } finally {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.removeAttribute(getBodyContentKey());
        }
    }
    public void doInitChildProcess() {
    }
    
    public ProcessStatus doAfterChildProcess() {
        return SKIP_BODY;
    }
    
    public boolean isChildEvaluation() {
        return true;
    }
    
    public boolean isIteration() {
        return false;
    }

}