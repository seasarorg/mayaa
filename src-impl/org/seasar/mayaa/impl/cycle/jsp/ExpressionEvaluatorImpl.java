/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.cycle.jsp;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.Expression;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.VariableResolver;

import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.impl.cycle.script.ScriptUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ExpressionEvaluatorImpl extends ExpressionEvaluator {

    private static ExpressionEvaluator _instance =
        new ExpressionEvaluatorImpl();

    public static ExpressionEvaluator getInstance() {
        return _instance;
    }

    public Object evaluate(String expression, Class expectedClass,
            VariableResolver vResolver, FunctionMapper fMapper)
            throws ELException {
        Expression exp = parseExpression(expression, expectedClass, fMapper);
        return exp.evaluate(vResolver);
    }

    public Expression parseExpression(String expression,
            Class expectedClass, FunctionMapper fMapper) {
        CompiledScript script = ScriptUtil.compile(expression, expectedClass);
        return new ExpressionImpl(script);
    }

}
