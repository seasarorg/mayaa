package org.seasar.maya.standard.engine.processor;

import java.io.Serializable;

import javax.servlet.jsp.PageContext;

/**
 * @author maruo_syunsuke
 */
public interface AttributeValue extends Serializable{
    public String getName();
    public void setValue(PageContext context, Object value);
    public Object getValue(PageContext context);
    public void remove(PageContext context);
}

