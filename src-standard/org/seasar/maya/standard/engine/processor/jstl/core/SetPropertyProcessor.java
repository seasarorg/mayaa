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
import org.seasar.maya.engine.processor.NullProcessorProperty;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.standard.engine.processor.BodyValueProcessor;

/**
 * @author maruo_syunsuke
 */
class SetPropertyProcessor extends BodyValueProcessor {

    private static final long serialVersionUID = 8123151421552810350L;

    private ProcessorProperty _value    = NullProcessorProperty.NULL; 
    private ProcessorProperty _target   = null;
    private ProcessorProperty _property = null;

    
    protected ProcessStatus process(ServiceCycle cycle){
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        if(_target == null || _property == null) {
            throw new IllegalStateException();
        }
        Object value = getValue(cycle);
        Object bean  = _target.getValue(cycle);
        String prop  = (String)_property.getValue(cycle);
        if(bean != null && prop != null) {
            ObjectUtil.setProperty(bean, prop, value);
        }
        return EVAL_PAGE;
    }

    private Object getValue(ServiceCycle cycle) {
        Object value = _value.getValue(cycle);
        if( value == null ){
            value = getBodyValue(cycle);
        }
        return value;
    }

    // MLD property (dynamic, requested) 
    public void setTarget(ProcessorProperty target) {
        if(target == null) {
            throw new IllegalArgumentException();
        }
        _target = target;
    }
    
    // MLD property (dynamic=java.lang.String, requested) 
    public void setProperty(ProcessorProperty property) {
        if(property == null) {
            throw new IllegalArgumentException();
        }
        _property = property;
    }
    
    // MLD property (dynamic, requested) 
    public void setValue(ProcessorProperty value) {
        if( value == null ) return ;
        _value = value ;
    }
    
}