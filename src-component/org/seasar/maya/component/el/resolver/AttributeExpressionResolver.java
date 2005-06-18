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
package org.seasar.maya.component.el.resolver;

import java.util.Iterator;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.component.CONST_COMPONENT;
import org.seasar.maya.component.engine.processor.ComponentPageProcessor;
import org.seasar.maya.el.resolver.ExpressionChain;
import org.seasar.maya.el.resolver.ExpressionResolver;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.impl.el.PropertyNotFoundException;
import org.seasar.maya.impl.el.PropertyNotWritableException;
import org.seasar.maya.impl.el.context.ParamMap;
import org.seasar.maya.impl.util.EngineUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AttributeExpressionResolver
		implements ExpressionResolver, CONST_COMPONENT {

    private ProcessorProperty findProperty(Iterator it, Object property) {
        if(it == null || property == null) {
            throw new IllegalArgumentException();
        }
        while(it.hasNext()) {
            ProcessorProperty prop = (ProcessorProperty)it.next();
            if(prop.getQName().getLocalName().equals(property)) {
                return prop;
            }
        }
        return null;
    }
    
    public Object getValue(PageContext context, 
            Object base, Object property, ExpressionChain chain) {
        if (context == null || property == null || chain == null) {
            throw new NullPointerException();
        }
        if (base == null && "attribute".equals(property)) {
            Template template = EngineUtil.getTemplate(context);
            TemplateProcessor parent = template.getParentProcessor();
            if (parent == null) {
                return new ParamMap(context.getRequest());
            } else if (parent instanceof ComponentPageProcessor) {
                return parent;
            } else {
                throw new IllegalStateException();
            }
        } else if (base instanceof ComponentPageProcessor) {
            ComponentPageProcessor component = (ComponentPageProcessor) base;
            Iterator it = component.getInformalProperties().iterator();
            ProcessorProperty prop = findProperty(it, property);
            if(prop == null) {
                it = component.getProcesstimeProperties(context).iterator();
                prop = findProperty(it , property);
            }
            if(prop != null) {
                return prop.getValue(context);
            }
            throw new PropertyNotFoundException(base, property);
        }
        return chain.getValue(context, base, property);
    }

    public void setValue(PageContext context,
            Object base, Object property, Object value, ExpressionChain chain) {
        if (context == null || property == null || chain == null) {
            throw new NullPointerException();
        }
        if ((base == null && "attribute".equals(property))
                || (base instanceof ComponentPageProcessor)) {
            throw new PropertyNotWritableException(base, property);
        }
        chain.setValue(context, base, property, value);
    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

}