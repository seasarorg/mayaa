/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;

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
    
    public boolean isIteration(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        return true;
    }
    
    private String getKey() {
        return getClass().getName() + "@" + hashCode();
    }
    
    protected boolean nextArrayItem(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        Iterator it = getIterator(context);
        if(it.hasNext()) {
	        Object next = it.next();
	        setVarItem(context, next);
	        incrimentIndex(context);
	        return true;
        }
        removeVarItem(context);
        removeIndexValue(context);
        return false;
    }
    
    protected final Iterator getIterator(PageContext context) {
		Iterator it = (Iterator)context.getAttribute(getKey());
		return it;
	}

	protected final void removeIndexValue(PageContext context) {
		if(StringUtil.hasValue(_index)) {
            context.removeAttribute(_index);
        }
	}

    protected final void removeVarItem(PageContext context) {
		if(StringUtil.hasValue(_var)) {
            context.removeAttribute(_var);
        }
	}

    protected final void incrimentIndex(PageContext context) {
		if(StringUtil.hasValue(_index)) {
		    Integer index = (Integer)context.getAttribute(_index);
		    if(index == null) {
		        context.setAttribute(_index, new Integer(0));
		    } else {
		        context.setAttribute(_index, new Integer(index.intValue() + 1));
		    }
		}
	}

    protected final void setVarItem(PageContext context, Object next) {
		if(StringUtil.hasValue(_var)) {
		    context.setAttribute(_var, next);
		}
	}

	protected abstract Iterator createIterator(PageContext context, Object eval);
    
    public int doStartProcess(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        Object eval = _expression.getValue(context);
        Iterator it = createIterator(context, eval);
        if(it == null) {
            it = NullIterator.getInstance();
        }
		context.setAttribute(getKey(), it);
        return nextArrayItem(context) ? 
                Tag.EVAL_BODY_INCLUDE : Tag.SKIP_BODY;
    }
    
	public int doAfterChildProcess(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        return nextArrayItem(context) ?
                IterationTag.EVAL_BODY_AGAIN : Tag.SKIP_BODY;
    }
    
    public int doEndProcess(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        context.removeAttribute(getKey());
        return Tag.EVAL_PAGE;
    }

}
