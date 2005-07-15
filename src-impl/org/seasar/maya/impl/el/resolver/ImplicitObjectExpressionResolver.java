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
package org.seasar.maya.impl.el.resolver;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.el.resolver.ExpressionChain;
import org.seasar.maya.el.resolver.ExpressionResolver;
import org.seasar.maya.impl.el.PropertyNotWritableException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ImplicitObjectExpressionResolver implements ExpressionResolver {

    public Object getValue(ServiceCycle cycle, 
            Object base, Object property, ExpressionChain chain) {
        if(cycle == null) {
            throw new NullPointerException();
        }
        if(base == null) {
            Object obj = cycle.getAttribute(property.toString(), ServiceCycle.SCOPE_IMPLICIT);
            if(obj != null) {
                return obj;
            }
        }
        return chain.getValue(cycle, base, property);
    }

    public void setValue(ServiceCycle cycle, 
            Object base, Object property, Object value, ExpressionChain chain) {
        if(cycle == null) {
            throw new NullPointerException();
        }
        if(base == null) {
            Object obj = cycle.getAttribute(property.toString(), ServiceCycle.SCOPE_IMPLICIT);
            if(obj != null) {
                throw new PropertyNotWritableException(base, property);
            }
        }
        chain.setValue(cycle, base, property, value);
    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

}