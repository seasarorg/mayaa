package org.seasar.maya.standard.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;

/**
 * @author maruo_syunsuke
 */
public interface ChildParamReciever {
    public void addChildParam(ServiceCycle cycle, String name, String value);
}
