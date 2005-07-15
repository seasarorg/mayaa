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
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;
import org.seasar.maya.standard.engine.processor.ChildParamReciever;

/**
 * @author maruo_syunsuke
 */
public class ParamProcessor extends AbstractBodyProcessor {

    private static final long serialVersionUID = -26328314484459094L;

    private String _name ;

    protected int process(ServiceCycle cycle, Object obj) {
        String paramValue = getParamValue(obj);
        setParamValueToParentProcessor(paramValue);
        return EVAL_PAGE;
    }
    
    private void setParamValueToParentProcessor(String paramValue) {
        if(getParentProcessor() instanceof ChildParamReciever) {
            ChildParamReciever reciever = (ChildParamReciever)getParentProcessor();
            reciever.addChildParam(_name,paramValue);
        } else {
            throw new IllegalStateException();
        }
    }
    
    private String getParamValue(Object obj) {
        if( obj == null ) {
            return "";
        }
        return obj.toString();
    }
    
    public void setName(String name) {
        _name = name ;
    }

}