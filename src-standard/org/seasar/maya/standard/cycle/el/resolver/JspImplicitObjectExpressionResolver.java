/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.standard.cycle.el.resolver;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.el.resolver.ExpressionChain;
import org.seasar.maya.cycle.el.resolver.ExpressionResolver;
import org.seasar.maya.impl.cycle.el.PropertyNotWritableException;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.standard.cycle.JspImplicitScope;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspImplicitObjectExpressionResolver implements ExpressionResolver {

    private Object getImplicitObject(Object property) {
    	ServiceCycle cycle = CycleUtil.getServiceCycle();
        if(cycle.hasAttributeScope(JspImplicitScope.SCOPE_JSP_IMPLICIT) == false) {
            cycle.putAttributeScope(
                    JspImplicitScope.SCOPE_JSP_IMPLICIT, new JspImplicitScope());
        }
        AttributeScope scope = cycle.getAttributeScope(JspImplicitScope.SCOPE_JSP_IMPLICIT);
        return scope.getAttribute(property.toString());
    }
    
    public Object getValue(Object base, Object property, ExpressionChain chain) {
        if(base == null) {
            Object obj = getImplicitObject(property.toString());
            if(obj != null) {
                return obj;
            }
        }
        return chain.getValue(base, property);
    }

    public void setValue(Object base, Object property, Object value, ExpressionChain chain) {
        if(base == null) {
            Object obj = getImplicitObject(property.toString());
            if(obj != null) {
                throw new PropertyNotWritableException(base, property);
            }
        }
        chain.setValue(base, property, value);
    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

}