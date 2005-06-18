/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.el.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.PageContext;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ImplicitObjectHolder {

    public static final String PAGE_CONTEXT = "pageContext";
    public static final String PAGE_SCOPE = "pageScope";
    public static final String REQUEST_SCOPE = "requestScope";
    public static final String SESSION_SCOPE = "sessionScope";
    public static final String APPLICATION_SCOPE = "applicationScope";
    public static final String PARAM = "param";
    public static final String PARAM_VALUE = "paramValues";
    public static final String HEADER = "header";
    public static final String HEADER_VALUES = "headerValues";
    public static final String COOKIE = "cookie";
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    public static final String INIT_PARAM = "initParam";
    private static final String IMPLICIT_OBJECT_HOLDER =
        "org.seasar.maya.impl.el.context.ImplicitObjectHolder";
    private static Map _resolverMap;
    static {
        Map map = new HashMap();
        map.put(PAGE_CONTEXT, new PageContextResolver());
        map.put(PAGE_SCOPE, new PageScopeResolver());
        map.put(REQUEST_SCOPE, new RequestScopeResolver());
        map.put(SESSION_SCOPE, new SessionScopeResolver());
        map.put(APPLICATION_SCOPE, new ApplicationScopeResolver());
        map.put(PARAM, new ParamResolver());
        map.put(PARAM_VALUE, new ParamValuesResolver());
        map.put(HEADER, new HeaderResolver());
        map.put(HEADER_VALUES, new HeaderValuesResolver());
        map.put(COOKIE, new CookieResolver());
        map.put(REQUEST, new RequestResolver());
        map.put(RESPONSE, new ResponseResolver());
        map.put(INIT_PARAM, new InitParamResolver());
        _resolverMap = Collections.unmodifiableMap(map);
    }

    private static ImplicitObjectResolver getResolver(Object property) {
        return (ImplicitObjectResolver)_resolverMap.get(property);
    }
    
    private static ImplicitObjectHolder getImplicitObjectHolder(PageContext pageContext) {
        if(pageContext == null) {
            throw new IllegalArgumentException();
        }
        ImplicitObjectHolder holder = 
            (ImplicitObjectHolder)pageContext.getAttribute(IMPLICIT_OBJECT_HOLDER);
        if(holder == null) {
            holder = new ImplicitObjectHolder();
            pageContext.setAttribute(IMPLICIT_OBJECT_HOLDER, holder);
        }
        return holder;
    }
    
    public static Object getImplicitObject(PageContext context, Object property) {
        if(context == null || property == null) {
            throw new IllegalArgumentException();
        }
        ImplicitObjectHolder holder = getImplicitObjectHolder(context);
        return holder.getImplicit(context, property);
    }
    
    public static boolean isImplicitObject(Object property) {
        return _resolverMap.containsKey(property);
    }
        
    private Map _instanceMap = new HashMap();
    
    private Object getImplicit(PageContext context, Object property) {
        if(context == null || property == null) {
            throw new IllegalArgumentException();
        }
        Object object = _instanceMap.get(property);
        if(object == null) {
            ImplicitObjectResolver resolver = getResolver(property);
            if(resolver != null) {
                object = resolver.resolve(context);
                _instanceMap.put(property, object);
            }
        }
        return object;
    }

}
