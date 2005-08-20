package org.seasar.maya.standard.engine.processor.util;

import org.seasar.maya.engine.processor.ProcessorProperty;

public class ProcessorPropertyUtil {
	public static Integer getInteger(ProcessorProperty prop) {
        if(prop == null) return null ;
        
        Object obj = prop.getValue();
        if(obj instanceof Integer) {
            return (Integer)obj;
        } else if(obj instanceof Number) {
        	return new Integer(((Number)obj).intValue());
        } else {
            try {
				return new Integer(obj.toString());
			} catch (NumberFormatException e) {
				return null ;
			}
        }
    }

    public static String getString(ProcessorProperty prop) {
        if(prop == null) return null ;
        
       	return prop.getValue().toString();
    }
    
}
