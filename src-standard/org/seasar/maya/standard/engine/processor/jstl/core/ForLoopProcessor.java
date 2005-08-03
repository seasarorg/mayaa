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
    
    protected boolean nextArrayItem(ServiceCycle context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        if (getEndValue(context) >= getIndexValue(context)) {
            getVarAttribute(context).setValue(context, getCurrentItem(context));
            setIndexValue(context, getIndexValue(context)
                    + getStepValue(context));
            return true;
        }
        getVarAttribute(context).remove(context);
        return false;
    }

    protected Object getCurrentItem(ServiceCycle context) {
        return new Integer(getIndexValue(context));
    }

    private String getKey() {
        return getClass().getName() + "@" + hashCode();
    }

    public ProcessStatus doStartProcess(ServiceCycle context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        initParameter(context);
        return nextArrayItem(context) ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }

    private void initParameter(ServiceCycle context) {
        AttributeValue varAttribute = AttributeValueFactory.create(_var);
        setVarAttribute(context, varAttribute);
        setIndexValue(context, initBeginParameter(context));
        setStepValue(context, initStepParameter(context));
        setEndValue(context, initEndParameter(context));
        varAttribute.setValue(context, new Integer(getIndexValue(context)));
    }

    protected int initBeginParameter(ServiceCycle context) {
        if (_begin != null) {
            Object value = _begin.getValue(context);
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

    protected int initStepParameter(ServiceCycle context) {
        if (_step != null) {
            Object value = _step.getValue(context);
            if (value != null && value instanceof Integer) {
                Integer stepValue = (Integer) value;
                return stepValue.intValue();
            } else if (value != null && value instanceof String) {
                return Integer.valueOf((String) value).intValue();
            }
        }
        return DEFAULT_STEP_START_VALUE;
    }

    protected int initEndParameter(ServiceCycle context) {
        Integer endValue = getEndParameterValue(context);
        if (endValue != null) {
            return endValue.intValue();
        }
        return 0;
    }

    protected Integer getEndParameterValue(ServiceCycle context) {
        Integer endValue = null;
        if (_end != null) {
            Object value = _end.getValue(context);
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

    public ProcessStatus doAfterChildProcess(ServiceCycle context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        return nextArrayItem(context) ? EVAL_BODY_AGAIN : SKIP_BODY;
    }

    public ProcessStatus doEndProcess(ServiceCycle context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        context.removeAttribute(getKey());
        return EVAL_PAGE;
    }

    public boolean isIteration(ServiceCycle context) {
        return true;
    }

    // Local Value
    protected int getIntValue(String name, ServiceCycle cycle) {
        Integer indexObject = (Integer)cycle.getAttribute(createAttributeKey(name));
        if( indexObject == null )throw new IllegalArgumentException("name is illegal.");
        return indexObject.intValue();
    }

    protected void setIntValue(String name, ServiceCycle cycle, int value) {
        cycle.setAttribute(createAttributeKey(name), new Integer(value));
    }

    protected int getIndexValue(ServiceCycle context) {
        return getIntValue(LOCAL_INDEX, context);
    }

    protected void setIndexValue(ServiceCycle context, int index) {
        setIntValue(LOCAL_INDEX, context, index);
    }

    protected int getStepValue(ServiceCycle context) {
        return getIntValue(LOCAL_STEP, context);
    }

    protected void setStepValue(ServiceCycle context, int step) {
        setIntValue(LOCAL_STEP, context, step);
    }

    protected int getSartValue(ServiceCycle context) {
        return getIntValue(LOCAL_START, context);
    }

    protected void setStartValue(ServiceCycle context, int step) {
        setIntValue(LOCAL_START, context, step);
    }

    protected int getEndValue(ServiceCycle context) {
        return getIntValue(LOCAL_END, context);
    }

    protected void setEndValue(ServiceCycle context, int step) {
        setIntValue(LOCAL_END, context, step);
    }

    protected AttributeValue getVarAttribute(ServiceCycle cycle) {
        return AttributeValueFactory.create(
                (String)cycle.getAttribute(createAttributeKey(LOCAL_VAR)));
    }

    protected void setVarAttribute(ServiceCycle cycle, AttributeValue value) {
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

