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
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.BodyValueProcessor;

/**
 * @author maruo_syunsuke
 */
public class OutProcessor extends BodyValueProcessor {

    private static final long serialVersionUID = 3988388279125884492L;

    private ProcessorProperty _value = NullProcessorProperty.NULL;
    private ProcessorProperty _default;
    private ProcessorProperty _escapeXml;

    protected ProcessStatus process(ServiceCycle cycle){
        Object outputValue = getOutputObject(cycle);
        cycle.getResponse().write(escapeXml(cycle, outputValue));
        return EVAL_PAGE;
    }

    private boolean getBoolean(ServiceCycle cycle, ProcessorProperty value) {
        if(value == null) {
            return false;
        }
        Object obj = value.getValue(cycle);
        return ObjectUtil.booleanValue(obj, false);
    }
    
    private String escapeXml(ServiceCycle cycle, Object obj) {
        String plainString = String.valueOf(obj);
        if(getBoolean(cycle, _escapeXml)) {
            return StringUtil.escapeEntity(plainString);
        }
        return plainString;
    }

    private Object getOutputObject(ServiceCycle cycle) {
        Object outputValue = _value.getValue(cycle);

        if( outputValue == null ){
            outputValue = getBodyValue(cycle) ;
        }
        if( outputValue == null ){
            outputValue = _default.getValue(cycle) ;
        }
        return outputValue;
    }

    
    // MLD property (dynamic, required)
    public void setValue(ProcessorProperty value) {
        if( value == null ) return ;
        _value = value ;
    }

    // MLD property (dynamic)
    public void setDefault(ProcessorProperty defaultValue) {
        _default = defaultValue ;
    }
    
    // MLD property (dynamic)
    public void setEscapeXml(ProcessorProperty escapeXml) {
        _escapeXml = escapeXml;
    }
}
