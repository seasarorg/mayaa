/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.impl.el;

import org.seasar.maya.el.CompiledExpression;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractCompiledExpression implements CompiledExpression {

    private String _expression;
    private Class _expectedType;
    
    public AbstractCompiledExpression(String expression, Class expectedType) {
        if(StringUtil.isEmpty(expression) || expectedType == null) {
            throw new IllegalArgumentException();
        }
        _expression = expression;
        _expectedType = expectedType;
    }
    
    public Class getExpectedType() {
        return _expectedType;
    }

    public String getExpression() {
        return _expression;
    }
    
    public boolean isLiteralText() {
        return false;
    }

}
