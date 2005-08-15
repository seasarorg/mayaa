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
import javax.servlet.jsp.el.VariableResolver;

import org.seasar.maya.cycle.el.CompiledExpression;
import org.seasar.maya.impl.util.ExpressionUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleVariableResolver implements VariableResolver {

    private static VariableResolver _instance = new CycleVariableResolver();

    public static VariableResolver getInstance() {
        return _instance;
    }

    public Object resolveVariable(String pName) throws ELException {
        CompiledExpression exp = ExpressionUtil.parseExpression(pName, Object.class);
        return ExpressionUtil.expressGetValue(exp);
    }
    
}
