package org.seasar.maya.standard.engine.processor;

import javax.servlet.jsp.PageContext;

/**
 * @author maruo_syunsuke
 */
public interface AttributeValue{
    public String getName();
    public void setValue(PageContext context, Object value);
    public Object getValue(PageContext context);
    public void remove(PageContext context);
}

