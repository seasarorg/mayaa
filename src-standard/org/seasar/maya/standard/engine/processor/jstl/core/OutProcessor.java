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
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;

/**
 * @author maruo_syunsuke
 */
public class OutProcessor extends AbstractBodyProcessor {

    private static final long serialVersionUID = 3988388279125884492L;

    private boolean _hasValue;
    private ProcessorProperty _escapeXml;

    // MLD property (dynamic, required)
    public void setValue(ProcessorProperty value) {
        if(value == null) {
            throw new IllegalArgumentException();
        }
        _hasValue = true;
        super.setValue(value);
    }
    
    // MLD property (dynamic)
    public void setDefault(ProcessorProperty defaultValue) {
        super.setDefault(defaultValue);
    }
    
    // MLD property (dynamic)
    public void setEscapeXml(ProcessorProperty escapeXml) {
        _escapeXml = escapeXml;
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

    protected int process(ServiceCycle cycle, Object obj) {
        if(_hasValue == false) {
            throw new IllegalStateException();
        }
        cycle.getResponse().write(escapeXml(cycle, obj));
        return EVAL_PAGE;
    }

}
