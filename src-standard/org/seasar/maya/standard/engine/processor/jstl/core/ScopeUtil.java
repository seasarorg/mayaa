package org.seasar.maya.standard.engine.processor.jstl.core;

import javax.servlet.jsp.PageContext;

/**
 * @author maruo_syunsuke
 */
public class ScopeUtil {
    public static boolean isScopeValue(int scope){
        return( PageContext.PAGE_SCOPE <= scope && PageContext.APPLICATION_SCOPE >= scope ); 
    }
}
