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
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;

/**
 * ボディーの情報>属性値>デフォルト値の三つの値を持つ可能性のあるタグの処理をおこなう。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractBodyProcessor extends TemplateProcessorSupport
        implements ChildEvaluationProcessor {

    private boolean _childEvaluation = true;
    private ProcessorProperty _value;
    private ProcessorProperty _defaultValue;

    private String getBodyContentKey() {
        return getClass().getName() + "@" + hashCode();
    }

    protected abstract ProcessStatus process(ServiceCycle cycle, Object obj);
    
    protected void setValue(ProcessorProperty value) {
        _value = value;
    }
    
    protected void setDefault(ProcessorProperty defaultValue) {
        _defaultValue = defaultValue;
    }
    
    public void setBodyContent(ServiceCycle cycle, CycleWriter body) {
        if (cycle == null || body == null) {
            throw new IllegalArgumentException();
        }
        cycle.setAttribute(getBodyContentKey(),  body);
    }

    public ProcessStatus doStartProcess(ServiceCycle cycle) {
        if (cycle == null) {
            throw new IllegalArgumentException();
        }
        Object obj = null;
        if(_value != null) {
	        obj = _value.getValue(cycle);
	        if (obj == null && _defaultValue != null) {
                obj = _defaultValue.getValue(cycle);
	        }
        }
        if(obj != null) {
            cycle.setAttribute(getBodyContentKey(),  obj);
	        return SKIP_BODY;
        }
        return EVAL_BODY_BUFFERED;
    }

    public ProcessStatus doEndProcess(ServiceCycle cycle) {
        if (cycle == null) {
            throw new IllegalArgumentException();
        }
        
        // FIXME なんか変なので後で見直す。
        
        String key = getBodyContentKey();
        Object obj = cycle.getAttribute(key);
        if (obj instanceof CycleWriter) {
            obj = ((CycleWriter)obj).getString();
            cycle.removeAttribute(key);
        }
        return process(cycle, obj);
    }

    public void doInitChildProcess(ServiceCycle cycle) {
    }

    public ProcessStatus doAfterChildProcess(ServiceCycle cycle) {
        return SKIP_BODY;
    }

    public void setChildEvaluation(boolean childEvaluation) {
        _childEvaluation = childEvaluation;
    }
    
    public boolean isChildEvaluation(ServiceCycle cycle) {
        return _childEvaluation;
    }

    public boolean isIteration(ServiceCycle cycle) {
        return true;
    }

}