package org.seasar.maya.standard.util;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.impl.util.StringUtil;

/**
 * @author maruo_syunsuke
 */
public class AttributeScopeUtil {
    public static int convertScopeStringToInt(String scopeString){
        if( StringUtil.isEmpty(scopeString) ) 
            return PageContext.PAGE_SCOPE ;
        scopeString = scopeString.toLowerCase();
        if( "session".equals(scopeString) )
            return PageContext.SESSION_SCOPE;
        if( "request".equals(scopeString) )
            return PageContext.REQUEST_SCOPE ;
        if( "application".equals(scopeString) )
            return PageContext.APPLICATION_SCOPE ;
        if( "page".equals(scopeString) )
            return PageContext.PAGE_SCOPE;
        return PageContext.PAGE_SCOPE;
    }
}
