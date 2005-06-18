package org.seasar.maya.standard.engine.processor;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.util.AttributeScopeUtil;

/**
 * @author maruo_syunsuke
 */
public class AttributeValueFactory{
    public static AttributeValue create(String name){
        if( StringUtil.isEmpty(name)){
            return new AttributeValue_Null();
        }
        return new AttributeValue_Basic(name);
    }
    public static AttributeValue create(String name, int scope){
        if( StringUtil.isEmpty(name)){
            return new AttributeValue_Null();
        }
        return new AttributeValue_Basic(name,scope);
    }
    public static AttributeValue create(String name, String scopeString){
        if( StringUtil.isEmpty(name)){
            return new AttributeValue_Null();
        }
        return new AttributeValue_Basic(name,
                AttributeScopeUtil.convertScopeStringToInt(scopeString));
    }
}

class AttributeValue_Basic implements AttributeValue{
    private String _name ;
    private int    _scope ;
    public AttributeValue_Basic(String name){
        this(name,PageContext.PAGE_SCOPE);
    }
    public AttributeValue_Basic(String name,int scope){
        _name  = name ;
        _scope = scope ;
    }
    public String getName() {
        return _name;
    }
    public void setValue(PageContext context, Object value){
        context.setAttribute( _name, value, _scope );
    }
    public Object getValue(PageContext context){
        return context.getAttribute( _name );
    }
    public void remove(PageContext context){
        context.removeAttribute(_name);
    }
}

class AttributeValue_Null implements AttributeValue{
    public void setValue(PageContext context, Object value){
    }
    public void setValue(PageContext context, Object value, int scope){
    }
    public Object getValue(PageContext context){
        return null ;
    }
    public void remove(PageContext context){
    }
    public String getName() {
        return null;
    }
}

