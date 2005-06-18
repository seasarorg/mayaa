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
package org.seasar.maya.impl.el.resolver;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.el.resolver.ExpressionChain;
import org.seasar.maya.el.resolver.ExpressionResolver;
import org.seasar.maya.impl.el.context.ImplicitObjectHolder;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScopedAttributeExpressionResolver implements ExpressionResolver {

    private static final Object unknown = new Object();
    
    private Object getScopedValue(PageContext context,
            String scopeName, Object property) {
        Map map = (Map)ImplicitObjectHolder.getImplicitObject(context, scopeName);
        if(map.containsKey(property)) {
            return map.get(property); 
        }
        return unknown;
    }
    
    private boolean setScopedValue(PageContext context, 
            String scopeName, Object property, Object value) {
        Map map = (Map)ImplicitObjectHolder.getImplicitObject(context, scopeName);
        if(map.containsKey(property)) {
            map.put(property, value);
            return true;
        }
        return false;
    }
    
    public Object getValue(PageContext context, 
            Object base, Object property, ExpressionChain chain) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        if(base == null) {
	        Object value = getScopedValue(context, ImplicitObjectHolder.PAGE_SCOPE, property);
	        if(value == unknown) {
	            value = getScopedValue(context, ImplicitObjectHolder.REQUEST_SCOPE, property);
	            if(value == unknown) {
	                value = getScopedValue(context, ImplicitObjectHolder.SESSION_SCOPE, property);
	                if(value == unknown) {
	                    value = getScopedValue(
	                            context, ImplicitObjectHolder.APPLICATION_SCOPE, property);
	                    if(value == unknown) {
	                        return chain.getValue(context, base, property);
	                    }
	                }
	            }
	        }
	        return value;
        }
        return chain.getValue(context, base, property);
    }

    public void setValue(PageContext context, 
            Object base, Object property, Object value, ExpressionChain chain) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        if(base == null) {
	        if(!setScopedValue(context, ImplicitObjectHolder.PAGE_SCOPE, property, value)) {
	            if(!setScopedValue(context, ImplicitObjectHolder.REQUEST_SCOPE, property, value)) {
	                if(!setScopedValue(context, ImplicitObjectHolder.SESSION_SCOPE, property, value)) {
	                    if(!setScopedValue(
	                            context, ImplicitObjectHolder.APPLICATION_SCOPE, property, value)) {
	                        Map pageScope = (Map)ImplicitObjectHolder.getImplicitObject(
	                                context, ImplicitObjectHolder.PAGE_SCOPE);
	                        pageScope.put(property, value);
	                    }
	                }
	            }
	        }
        }
        chain.setValue(context, base, property, value);
    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

}