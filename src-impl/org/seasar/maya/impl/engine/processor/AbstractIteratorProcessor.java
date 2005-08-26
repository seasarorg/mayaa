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
package org.seasar.maya.impl.engine.processor;

import java.util.Iterator;

import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractIteratorProcessor extends TemplateProcessorSupport
		implements IterationProcessor, CONST_IMPL {
    
    private ProcessorProperty _property;
    private String _var;
    private String _index;
    
    protected void setProperty(ProcessorProperty property) {
        if(property == null) {
            throw new IllegalArgumentException();
        }
        _property = property;
    }

    protected void setVar(String var) {
    	_var = var;
    }
    
    protected void setIndex(String index) {
    	_index = index;
    }
    
    public boolean isIteration() {
        return true;
    }
    
    private String getKey() {
        return getClass().getName() + "@" + hashCode();
    }
    
    protected boolean nextArrayItem() {
        Iterator it = getIterator();
        if(it.hasNext()) {
	        Object next = it.next();
	        setVarItem(next);
	        incrimentIndex();
	        return true;
        }
        removeVarItem();
        removeIndexValue();
        return false;
    }
    
    protected final Iterator getIterator() {
		Iterator it = (Iterator)CycleUtil.getAttribute(getKey());
		return it;
	}

	protected final void removeIndexValue() {
		if(StringUtil.hasValue(_index)) {
            CycleUtil.removeAttribute(_index);
        }
	}

    protected final void removeVarItem() {
		if(StringUtil.hasValue(_var)) {
            CycleUtil.removeAttribute(_var);
        }
	}

    protected final void incrimentIndex() {
		if(StringUtil.hasValue(_index)) {
		    Integer index = (Integer)CycleUtil.getAttribute(_index);
		    if(index == null) {
		        CycleUtil.setAttribute(_index, new Integer(0));
		    } else {
		        CycleUtil.setAttribute(_index, new Integer(index.intValue() + 1));
		    }
		}
	}

    protected final void setVarItem(Object next) {
		if(StringUtil.hasValue(_var)) {
		    CycleUtil.setAttribute(_var, next);
		}
	}

	protected abstract Iterator createIterator(Object eval);
    
    public ProcessStatus doStartProcess() {
        Object eval = _property.getValue();
        Iterator it = createIterator(eval);
        if(it == null) {
            it = NullIterator.getInstance();
        }
		CycleUtil.setAttribute(getKey(), it);
        return nextArrayItem() ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }
    
	public ProcessStatus doAfterChildProcess() {
        return nextArrayItem() ? EVAL_BODY_AGAIN : SKIP_BODY;
    }
    
    public ProcessStatus doEndProcess() {
        CycleUtil.removeAttribute(getKey());
        return EVAL_PAGE;
    }

}
