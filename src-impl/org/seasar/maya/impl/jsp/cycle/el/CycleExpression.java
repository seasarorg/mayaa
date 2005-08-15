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
package org.seasar.maya.impl.jsp.cycle.el;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.Expression;
import javax.servlet.jsp.el.VariableResolver;

import org.seasar.maya.cycle.el.CompiledExpression;
import org.seasar.maya.impl.util.ExpressionUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleExpression extends Expression {

    private CompiledExpression _exp;
    
    public CycleExpression(CompiledExpression exp) {
        _exp = exp;
    }
    
    public Object evaluate(VariableResolver vResolver) throws ELException {
        return ExpressionUtil.expressGetValue(_exp);
    }

}
