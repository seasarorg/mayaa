/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://homepage3.nifty.com/seasar/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.cycle.el.ognl2;

import ognl.Ognl;
import ognl.OgnlException;
import ognl.OgnlRuntime;

import org.seasar.maya.cycle.el.CompiledExpression;
import org.seasar.maya.impl.cycle.el.AbstractExpressionFactory;
import org.seasar.maya.impl.cycle.el.ExpressionBlock;
import org.seasar.maya.impl.cycle.el.LiteralExpression;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class Ognl2ExpressionFactory extends AbstractExpressionFactory {

    public Ognl2ExpressionFactory() {
        OgnlRuntime.setPropertyAccessor(Object.class, 
                new Ognl2PropertyAccessor(getExpressionResolver()));
        OgnlRuntime.setMethodAccessor(Object.class, new Ognl2MethodAccessor());
    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    private String prepareOgnlExpression(String expression) {
        String ret = expression;
        ret = ret.replace('\r', ' ');
        ret = ret.replace('\n', ' ');
        return ret;
    }
    
    protected CompiledExpression compile(
            ExpressionBlock expressionBlock, Class expectedType) {
        if(expressionBlock == null || expectedType == null) {
            throw new IllegalArgumentException();
        }
        String expression = expressionBlock.getBlockString();
        if(expressionBlock.isLiteral()) {
            return new LiteralExpression(expression, expectedType);
        }
        try {
            Object exp = Ognl.parseExpression(prepareOgnlExpression(expression));
            return new Ognl2CompiledExpression(exp, expression, expectedType);
        } catch(OgnlException e) {
            throw new RuntimeException(e);
        }
    }
    
}
