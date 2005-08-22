package org.seasar.maya.standard.engine.processor.jstl;

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.specification.QName;

/**
 * @author maruo_syunsuke
 */
public class ProcessorPropertyString implements ProcessorProperty {

    private Object _value ; 
    
    public ProcessorPropertyString(String str) {
        _value = str ;
    }
    
    public QName getQName() {
        return null;
    }

    public String getPrefix() {
        return null;
    }

    public boolean isStatic() {
        return true;
    }

    public Object getValue() {
        return _value.toString();
    }

    public void setValue(Object value) {
        _value = value ;
    }

}
