package org.seasar.maya.standard.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;

public class ObjectAttributeUtil {
    public static void setAttribute(ServiceCycle cycle, Object keyObject, String keyString, Object value){
        cycle.setAttribute(getIdentityKeyString(keyObject,keyString),value);
    }
    public static Object getAttribute(ServiceCycle cycle, Object keyObject, String keyString){
        return cycle.getAttribute(getIdentityKeyString(keyObject,keyString));
    }
    
    public static String getIdentityKeyString(Object keyObject, String name){
        return keyObject.getClass().getName() +"@"+ System.identityHashCode(keyObject) +"@"+ name ;
    }
}
