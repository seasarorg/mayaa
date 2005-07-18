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

import java.util.Iterator;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractIteratorProcessor extends TemplateProcessorSupport
		implements IterationProcessor, CONST_IMPL {
    
    private ProcessorProperty _expression;
    private String _var;
    private String _index;
    
    protected void setExpression(ProcessorProperty expression) {
        if(expression == null) {
            throw new IllegalArgumentException();
        }
        _expression = expression;
    }

    protected void setVar(String var) {
    	_var = var;
    }
    
    protected void setIndex(String index) {
    	_index = index;
    }
    
    public boolean isIteration(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        return true;
    }
    
    private String getKey() {
        return getClass().getName() + "@" + hashCode();
    }
    
    protected boolean nextArrayItem(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        Iterator it = getIterator(cycle);
        if(it.hasNext()) {
	        Object next = it.next();
	        setVarItem(cycle, next);
	        incrimentIndex(cycle);
	        return true;
        }
        removeVarItem(cycle);
        removeIndexValue(cycle);
        return false;
    }
    
    protected final Iterator getIterator(ServiceCycle cycle) {
		Iterator it = (Iterator)cycle.getAttribute(getKey());
		return it;
	}

	protected final void removeIndexValue(ServiceCycle cycle) {
		if(StringUtil.hasValue(_index)) {
            cycle.setAttribute(_index, null);
        }
	}

    protected final void removeVarItem(ServiceCycle cycle) {
		if(StringUtil.hasValue(_var)) {
            cycle.setAttribute(_var, null);
        }
	}

    protected final void incrimentIndex(ServiceCycle cycle) {
		if(StringUtil.hasValue(_index)) {
		    Integer index = (Integer)cycle.getAttribute(_index);
		    if(index == null) {
		        cycle.setAttribute(_index, new Integer(0));
		    } else {
		        cycle.setAttribute(_index, new Integer(index.intValue() + 1));
		    }
		}
	}

    protected final void setVarItem(ServiceCycle cycle, Object next) {
		if(StringUtil.hasValue(_var)) {
		    cycle.setAttribute(_var, next);
		}
	}

	protected abstract Iterator createIterator(ServiceCycle cycle, Object eval);
    
    public ProcessStatus doStartProcess(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        Object eval = _expression.getValue(cycle);
        Iterator it = createIterator(cycle, eval);
        if(it == null) {
            it = NullIterator.getInstance();
        }
		cycle.setAttribute(getKey(), it);
        return nextArrayItem(cycle) ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }
    
	public ProcessStatus doAfterChildProcess(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        return nextArrayItem(cycle) ? EVAL_BODY_AGAIN : SKIP_BODY;
    }
    
    public ProcessStatus doEndProcess(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        cycle.setAttribute(getKey(), null);
        return EVAL_PAGE;
    }

}
