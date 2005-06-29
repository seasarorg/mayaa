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
package org.seasar.maya.impl.el;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.el.CompiledExpression;
import org.seasar.maya.impl.util.StringUtil;

/**
 * 複数のブロックからなる、CompiledExpression。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ComplexExpression implements CompiledExpression {

 	private static final long serialVersionUID = -7356099026354564155L;

 	private String _expression;
    private Class _expectedType;
    private CompiledExpression[] _compiled;
    
    public ComplexExpression(
            String expression,  Class expectedType, CompiledExpression[] compiled) {
        if(StringUtil.isEmpty(expression) || expectedType == null || compiled == null) {
            throw new IllegalArgumentException();
        }
        _expression = expression;
        _expectedType = expectedType;
        _compiled = compiled;
    }
    
    public Class getExpectedType() {
        return _expectedType;
    }
    
    public Object getValue(PageContext context) {
        if(_expectedType != Void.class &&
                _expectedType != String.class &&
                _expectedType != Object.class) {
            throw new ConversionException(_expectedType, _expression);
        }
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < _compiled.length; i++) {
            buffer.append(_compiled[i].getValue(context));
        }
        if(_expectedType == Void.class) {
            return null;
        }
         return buffer.toString();
    }
    
    public void setValue(PageContext context, Object value) {
        throw new IllegalStateException();
    }

    public String getExpression() {
        return _expression;
    }

    public boolean isLiteralText() {
        return false;
    }
    
}
