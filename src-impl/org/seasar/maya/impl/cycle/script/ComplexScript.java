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
public class ComplexScript implements CompiledScript {

 	private static final long serialVersionUID = -7356099026354564155L;

 	private String _script;
    private Class _expectedType;
    private CompiledScript[] _compiled;
    
    public ComplexScript(
            String script,  Class expectedType, CompiledScript[] compiled) {
        if(StringUtil.isEmpty(script) || expectedType == null || compiled == null) {
            throw new IllegalArgumentException();
        }
        _script = script;
        _expectedType = expectedType;
        _compiled = compiled;
    }
    
    public Class getExpectedType() {
        return _expectedType;
    }
    
    public Object execute(Object root) {
        if(_expectedType != Void.class &&
                _expectedType != String.class &&
                _expectedType != Object.class) {
            throw new ConversionException(this, _expectedType);
        }
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < _compiled.length; i++) {
            buffer.append(_compiled[i].execute(root));
        }
        if(_expectedType == Void.class) {
            return null;
        }
         return buffer.toString();
    }

    public String getText() {
        return _script;
    }

    public boolean isLiteral() {
        return false;
    }
    
}
