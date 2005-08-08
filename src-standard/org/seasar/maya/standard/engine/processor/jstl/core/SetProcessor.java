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

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.BodyValueProcessor;

/**
 * @author maruo_syunsuke
 */
public class SetProcessor extends BodyValueProcessor {

    private static final long serialVersionUID = 6211369730749002956L;

    private ProcessorProperty _value;
    private ProcessorProperty _var;
    private String            _scope;
    
    protected ProcessStatus process() {
        String varName = (String)_var.getValue();
        if(StringUtil.hasValue(varName)) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            AttributeScope scope = cycle.getAttributeScope(_scope);
            scope.setAttribute(varName, getVarValue());
        }
        return EVAL_PAGE;
    }

    private Object getVarValue() {
        if( _value == null ){
            return getBodyValue();
        }
        return _value.getValue();
    }

    public void setValue(ProcessorProperty value) {
        _value = value ;
    }

    public void setVar(ProcessorProperty var) {
        _var = var;
    }
    
    public void setScope(String scope) {
        _scope = scope;
    }

}