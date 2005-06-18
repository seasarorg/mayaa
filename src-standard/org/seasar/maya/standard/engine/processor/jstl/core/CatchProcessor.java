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
package org.seasar.maya.standard.engine.processor.jstl.core;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.engine.processor.TryCatchFinallyProcessor;
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;

/**
 * @author maruo_syunsuke
 */
public class CatchProcessor extends TemplateProcessorSupport implements TryCatchFinallyProcessor {

    private String _var;
    
    public void setVar(String var) {
        _var = var;
    }

    public int doStartProcess(PageContext context) {
        return Tag.EVAL_BODY_INCLUDE;
    }

    public boolean canCatch(PageContext context) {
        return true;
    }

    public void doCatchProcess(PageContext context, Throwable t) {
        AttributeValue val = AttributeValueFactory.create(_var);
        val.setValue(context, t);
    }

    public void doFinallyProcess(PageContext context) {
    }
}
