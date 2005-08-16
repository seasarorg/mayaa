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
package org.seasar.maya.impl.cycle.script;

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LiteralScript  implements CompiledScript {

	private static final long serialVersionUID = -3791475287481727514L;

	private String _script;
    private Class _expectedType;
    
    public LiteralScript(String expression, Class expectedType) {
        if(StringUtil.isEmpty(expression)) {
            throw new IllegalArgumentException();
        }
        _script = expression;
        _expectedType = expectedType;
    }
    
    public Class getExpectedType() {
        return _expectedType;
    }
    
    public Object exec() {
        if(_expectedType != Void.class &&
                _expectedType != String.class &&
                _expectedType != Object.class) {
            throw new ConversionException(this, String.class);
        }
        if(_expectedType == Void.class) {
            return null;
        }
        return _script;
    }
    
    public String getText() {
        return _script;
    }

    public boolean isLiteral() {
        return true;
    }
    
}
