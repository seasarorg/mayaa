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
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.engine.processor.TryCatchFinallyProcessor;
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;

/**
 * @author maruo_syunsuke
 */
public class CatchProcessor extends TemplateProcessorSupport
        implements TryCatchFinallyProcessor {

    private static final long serialVersionUID = -6528283511342748578L;

    private String _var;
    
    public void setVar(String var) {
        _var = var;
    }

    public int doStartProcess(ServiceCycle cycle) {
        return EVAL_BODY_INCLUDE;
    }

    public boolean canCatch(ServiceCycle cycle) {
        return true;
    }

    public void doCatchProcess(ServiceCycle cycle, Throwable t) {
        AttributeValue val = AttributeValueFactory.create(_var);
        val.setValue(cycle, t);
    }

    public void doFinallyProcess(ServiceCycle cycle) {
    }

}
