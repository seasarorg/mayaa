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
package org.seasar.maya.impl.util;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.el.CompiledExpression;
import org.seasar.maya.el.ExpressionFactory;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ServiceProviderFactory;

/**
 * TODO ServiceCycle
 * 
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ExpressionUtil implements CONST_IMPL {

	private ExpressionUtil() {
	}

    public static CompiledExpression parseExpression(
            String expressionString, Class expectedType) {
        if(expectedType == null) {
        	throw new IllegalArgumentException();
        }
        if(StringUtil.hasValue(expressionString)) {
            ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
	        ExpressionFactory expressionFactory = provider.getExpressionFactory();
            return expressionFactory.createExpression(expressionString, expectedType);
        }
        return null;
    }
    
    public static Object expressGetValue(PageContext context, Object expression) {
        Object value = null;
        if (expression instanceof CompiledExpression) {
            CompiledExpression compiledExpression = (CompiledExpression)expression;
            value = compiledExpression.getValue(context);
        } else {
            value = expression;
        }
        return value;
    }

    public static void expressSetValue(PageContext context,
            Object expression, Object value) {
        if (expression instanceof CompiledExpression) {
            CompiledExpression compiledExpression = (CompiledExpression)expression;
            compiledExpression.setValue(context, value);
        }
    }
    
    public static  void execEvent(Specification specification, QName eventName,
            PageContext context) {
        if(specification == null || eventName == null || context == null) {
            throw new IllegalArgumentException();
        }
        SpecificationNode maya = SpecificationUtil.getMayaNode(specification);
        if(maya != null) {
	        NodeAttribute attr = maya.getAttribute(eventName);
	        if(attr != null) {
	        	String expression = attr.getValue();
	        	if(StringUtil.hasValue(expression)) {
		            Object obj = ExpressionUtil.parseExpression(expression, Void.class);
		            ExpressionUtil.expressGetValue(context, obj);
	        	}
	        }
        }
    }

}
