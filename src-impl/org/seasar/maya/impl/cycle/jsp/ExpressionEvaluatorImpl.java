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
package org.seasar.maya.impl.cycle.jsp;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.Expression;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.VariableResolver;

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.impl.cycle.ExpressionImpl;
import org.seasar.maya.impl.util.ScriptUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ExpressionEvaluatorImpl extends ExpressionEvaluator {

    private static ExpressionEvaluator _instance = new ExpressionEvaluatorImpl();
    
    public static ExpressionEvaluator getInstance() {
        return _instance;
    }
    
    public Object evaluate(String expression, Class expectedType,
            VariableResolver vResolver, FunctionMapper fMapper) throws ELException {
        Expression exp = parseExpression(expression, expectedType, fMapper);
        return exp.evaluate(vResolver);
    }

    public Expression parseExpression(String expression, Class expectedType, 
            FunctionMapper fMapper) throws ELException {
        CompiledScript script = ScriptUtil.compile(expression, expectedType);
        return new ExpressionImpl(script);
    }

}
