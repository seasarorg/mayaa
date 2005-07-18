package org.seasar.maya.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.specification.QName;

public class NullProcessorProperty implements ProcessorProperty {
    public final static ProcessorProperty NULL = new NullProcessorProperty();
    
    private NullProcessorProperty(){
    }
    
    public QName getQName() {
        return null;
    }
    public String getPrefix() {
        return null;
    }
    public String getLiteral() {
        return null;
    }
    public boolean isDynamic() {
        return false;
    }
    public Object getValue(ServiceCycle cycle) {
        return null;
    }
    public void setValue(ServiceCycle cycle, Object value) {
    }
}
