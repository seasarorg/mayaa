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
package org.seasar.maya.impl.cycle.el;

import org.seasar.maya.cycle.el.CompiledExpression;
import org.seasar.maya.impl.util.StringUtil;

/**
 * リテラルテキストのCompiledExpression。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LiteralExpression  implements CompiledExpression {

	private static final long serialVersionUID = -3791475287481727514L;

	private String _expression;
    private Class _expectedType;
    
    public LiteralExpression(String expression, Class expectedType) {
        if(StringUtil.isEmpty(expression)) {
            throw new IllegalArgumentException();
        }
        _expression = expression;
        _expectedType = expectedType;
    }
    
    public Class getExpectedType() {
        return _expectedType;
    }
    
    public Object getValue() {
        if(_expectedType != Void.class &&
                _expectedType != String.class &&
                _expectedType != Object.class) {
            throw new ConversionException(_expectedType, _expression);
        }
        if(_expectedType == Void.class) {
            return null;
        }
        return _expression;
    }
    
    public void setValue(Object value) {
        throw new IllegalStateException();
    }

    public String getExpression() {
        return _expression;
    }

    public boolean isLiteralText() {
        return true;
    }
    
}
