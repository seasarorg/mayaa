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
package org.seasar.maya.standard.engine.processor.jstl.core;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;

/**
 * @author maruo_syunsuke
 */
public class ForLoopProcessor extends TemplateProcessorSupport implements IterationProcessor {

    private static final long serialVersionUID = 5575180013360728994L;

	private static final int DEFAULT_STEP_START_VALUE = 1;
    private static final int DEFAULT_INDEX_START_VALUE = 0;
    
    private String _var   = null ;
    private ProcessorProperty _begin = null ;
    private ProcessorProperty _end   = null ;
    private ProcessorProperty _step  = null ;
    
    private static final String LOCAL_INDEX = "LOCAL_INDEX" ;
    private static final String LOCAL_VAR   = "LOCAL_VAR" ;
    private static final String LOCAL_STEP  = "LOCAL_STEP" ;
    private static final String LOCAL_START = "LOCAL_START" ;
    private static final String LOCAL_END   = "LOCAL_END" ;
    
    protected boolean nextArrayItem() {
        if (getEndValue() >= getIndexValue()) {
            getVarAttribute().setValue(getCurrentItem());
            setIndexValue(getIndexValue() + getStepValue());
            return true;
        }
        getVarAttribute().remove();
        return false;
    }

    protected Object getCurrentItem() {
        return new Integer(getIndexValue());
    }

    private String getKey() {
        return getClass().getName() + "@" + hashCode();
    }

    public ProcessStatus doStartProcess() {
        initParameter();
        return nextArrayItem() ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }

    private void initParameter() {
        AttributeValue varAttribute = AttributeValueFactory.create(_var);
        setVarAttribute(varAttribute);
        setIndexValue(initBeginParameter());
        setStepValue(initStepParameter());
        setEndValue(initEndParameter());
        varAttribute.setValue(new Integer(getIndexValue()));
    }

    protected int initBeginParameter() {
        if (_begin != null) {
            Object value = _begin.getValue();
            if (value != null) {
                if (value instanceof Integer) {
                    Integer beginValue = (Integer) value;
                    return beginValue.intValue();
                } else if (value != null && value instanceof String) {
                    return Integer.valueOf((String) value).intValue();
                }
            }
        }
        return DEFAULT_INDEX_START_VALUE;
    }

    protected int initStepParameter() {
        if (_step != null) {
            Object value = _step.getValue();
            if (value != null && value instanceof Integer) {
                Integer stepValue = (Integer) value;
                return stepValue.intValue();
            } else if (value != null && value instanceof String) {
                return Integer.valueOf((String) value).intValue();
            }
        }
        return DEFAULT_STEP_START_VALUE;
    }

    protected int initEndParameter() {
        Integer endValue = getEndParameterValue();
        if (endValue != null) {
            return endValue.intValue();
        }
        return 0;
    }

    protected Integer getEndParameterValue() {
        Integer endValue = null;
        if (_end != null) {
            Object value = _end.getValue();
            if (value != null) {
                if (value instanceof Integer) {
                    endValue = (Integer) value;
                } else if (value instanceof String) {
                    endValue = new Integer((String) value);
                }
            }
        }
        return endValue;
    }

    public ProcessStatus doAfterChildProcess() {
        return nextArrayItem() ? EVAL_BODY_AGAIN : SKIP_BODY;
    }

    public ProcessStatus doEndProcess() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.removeAttribute(getKey());
        return EVAL_PAGE;
    }

    public boolean isIteration() {
        return true;
    }

    // Local Value
    protected int getIntValue(String name) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Integer indexObject = (Integer)cycle.getAttribute(createAttributeKey(name));
        if( indexObject == null )throw new IllegalArgumentException("name is illegal.");
        return indexObject.intValue();
    }

    protected void setIntValue(String name, int value) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setAttribute(createAttributeKey(name), new Integer(value));
    }

    protected int getIndexValue() {
        return getIntValue(LOCAL_INDEX);
    }

    protected void setIndexValue(int index) {
        setIntValue(LOCAL_INDEX, index);
    }

    protected int getStepValue() {
        return getIntValue(LOCAL_STEP);
    }

    protected void setStepValue(int step) {
        setIntValue(LOCAL_STEP, step);
    }

    protected int getSartValue() {
        return getIntValue(LOCAL_START);
    }

    protected void setStartValue(int step) {
        setIntValue(LOCAL_START, step);
    }

    protected int getEndValue() {
        return getIntValue(LOCAL_END);
    }

    protected void setEndValue(int step) {
        setIntValue(LOCAL_END, step);
    }

    protected AttributeValue getVarAttribute() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        return AttributeValueFactory.create(
                (String)cycle.getAttribute(createAttributeKey(LOCAL_VAR)));
    }

    protected void setVarAttribute(AttributeValue value) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setAttribute(createAttributeKey(LOCAL_VAR), value.getName());
    }

    private String createAttributeKey(String postFix) {
        return getClass().getName() + "@" + hashCode() +":"+ postFix;
    }

    public void setBegin(ProcessorProperty beginParameter) {
        _begin = beginParameter;
    }

    public void setEnd(ProcessorProperty endParameter) {
        _end = endParameter;
    }

    public void setStep(ProcessorProperty stepParameter) {
        _step = stepParameter;
    }

    public void setVar(String varParameter) {
        _var = varParameter;
    }

}

