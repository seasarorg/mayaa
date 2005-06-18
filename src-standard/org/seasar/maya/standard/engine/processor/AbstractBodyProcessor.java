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

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

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

    protected abstract int process(PageContext context, Object obj);
    
    protected void setValue(ProcessorProperty value) {
        _value = value;
    }
    
    protected void setDefault(ProcessorProperty defaultValue) {
        _defaultValue = defaultValue;
    }
    
    public void setBodyContent(PageContext context, BodyContent bodyContent) {
        if (context == null || bodyContent == null) {
            throw new IllegalArgumentException();
        }
        context.setAttribute(getBodyContentKey(),  bodyContent);
    }

    public int doStartProcess(PageContext context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        Object obj = null;
        if(_value != null) {
	        obj = _value.getValue(context);
	        if (obj == null && _defaultValue != null) {
                obj = _defaultValue.getValue(context);
	        }
        }
        if(obj != null) {
            context.setAttribute(getBodyContentKey(),  obj);
	        return Tag.SKIP_BODY;
        }
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    public int doEndProcess(PageContext context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        String key = getBodyContentKey();
        Object obj = context.getAttribute(key);
        if (obj instanceof BodyContent) {
            obj = ((BodyContent)obj).getString().trim();
            context.removeAttribute(key);
        }
        return process(context, obj);
    }

    public void doInitChildProcess(PageContext context) {
    }

    public int doAfterChildProcess(PageContext context) {
        return Tag.SKIP_BODY;
    }

    public void setChildEvaluation(boolean childEvaluation) {
        _childEvaluation = childEvaluation;
    }
    
    public boolean isChildEvaluation(PageContext context) {
        return _childEvaluation;
    }

    public boolean isIteration(PageContext context) {
        return true;
    }

}