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
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractBodyTextProcessor 
                        extends TemplateProcessorSupport 
                        implements ChildEvaluationProcessor {

    private boolean _childEvaluation = true;

    private String getBodyContentKey() {
        return getClass().getName() + "@" + hashCode();
    }

    protected abstract int process(ServiceCycle cycle, String bodyString);
    
    public void setBodyContent(ServiceCycle context, CycleWriter body) {
        if (context == null || body == null) {
            throw new IllegalArgumentException();
        }
        context.setAttribute(getBodyContentKey(),  body);
    }

    public int doStartProcess(ServiceCycle cycle) {
        return EVAL_BODY_BUFFERED;
    }

    public int doEndProcess(ServiceCycle cycle) {
        if (cycle == null) {
            throw new IllegalArgumentException();
        }
        String key = getBodyContentKey();
        Object obj = cycle.getAttribute(key);
        if (obj instanceof CycleWriter) {
            String bodyString = new String(((CycleWriter)obj).getBuffer()).trim();
            cycle.setAttribute(key, null);
            return process(cycle, bodyString);
        }
        return process(cycle, null);
    }

    public void doInitChildProcess(ServiceCycle cycle) {
    }

    public int doAfterChildProcess(ServiceCycle cycle) {
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