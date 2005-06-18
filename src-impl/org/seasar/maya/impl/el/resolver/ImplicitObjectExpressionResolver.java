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

import javax.servlet.jsp.PageContext;

import org.seasar.maya.el.resolver.ExpressionChain;
import org.seasar.maya.el.resolver.ExpressionResolver;
import org.seasar.maya.impl.el.PropertyNotWritableException;
import org.seasar.maya.impl.el.context.ImplicitObjectHolder;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ImplicitObjectExpressionResolver implements ExpressionResolver {

    public Object getValue(PageContext context, 
            Object base, Object property, ExpressionChain chain) {
        if(context == null) {
            throw new NullPointerException();
        }
        if(base == null) {
            if(ImplicitObjectHolder.isImplicitObject(property)) {
                return ImplicitObjectHolder.getImplicitObject(context, property);
            }
        }
        return chain.getValue(context, base, property);
    }

    public void setValue(PageContext context, 
            Object base, Object property, Object value, ExpressionChain chain) {
        if(context == null) {
            throw new NullPointerException();
        }
        if(base == null) {
            if(ImplicitObjectHolder.isImplicitObject(property)) {
                throw new PropertyNotWritableException(base, property);
            }
        }
        chain.setValue(context, base, property, value);
    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

}