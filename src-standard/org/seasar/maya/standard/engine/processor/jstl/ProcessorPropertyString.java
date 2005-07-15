package org.seasar.maya.standard.engine.processor.jstl;

import org.seasar.maya.cycle.ServiceCycle;
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

    public String getLiteral() {
        return _value.toString() ;
    }

    public boolean isDynamic() {
        return false;
    }

    public Object getValue(ServiceCycle cycle) {
        return _value.toString();
    }

    public void setValue(ServiceCycle cycle, Object value) {
        _value = value ;
    }

}
