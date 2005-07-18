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
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;

/**
 * @author maruo_syunsuke
 */
public class SetAttributeProcessor extends AbstractBodyProcessor {

    private static final long serialVersionUID = -8774293192056971213L;

    private ProcessorProperty _var;
    private String _scope;
    
    public void setVar(ProcessorProperty var) {
        _var = var;
    }
    
    public void setValue(ProcessorProperty value) {
        super.setValue(value);
    }
    
    public void setScope(String scope) {
        _scope = scope;
    }

    public ProcessStatus process(ServiceCycle cycle, Object obj) {
        String varName = (String)_var.getValue(cycle);
        if(StringUtil.hasValue(varName)) {
            cycle.setAttribute(varName, obj, _scope);
        }
        return EVAL_PAGE;
    }

}