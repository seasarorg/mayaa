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

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.util.CycleUtil;

public class ObjectAttributeUtil {
    
    public static void setAttribute(Object keyObject, String keyString, Object value) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setAttribute(getIdentityKeyString(keyObject,keyString),value);
    }
    
    public static Object getAttribute(Object keyObject, String keyString) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        return cycle.getAttribute(getIdentityKeyString(keyObject,keyString));
    }
    
    public static String getIdentityKeyString(Object keyObject, String name){
        return keyObject.getClass().getName() +"@"+ System.identityHashCode(keyObject) +"@"+ name ;
    }

}
