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
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ComplexScript implements CompiledScript {

 	private static final long serialVersionUID = -7356099026354564155L;

 	private String _script;
    private Class _expectedType = Object.class;
    private CompiledScript[] _compiled;
    
    public ComplexScript(String script,  CompiledScript[] compiled) {
        if(StringUtil.isEmpty(script) || compiled == null) {
            throw new IllegalArgumentException();
        }
        _script = script;
        _compiled = compiled;
    }
    
    public void setExpectedType(Class expectedType) {
        if(expectedType == null) {
            throw new IllegalArgumentException();
        }
        _expectedType = expectedType;
    }
    
    public Class getExpectedType() {
        return _expectedType;
    }
    
    public Object execute(Object root) {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < _compiled.length; i++) {
            buffer.append(_compiled[i].execute(root));
        }
        if(_expectedType == Void.class) {
            return null;
        }
        return ObjectUtil.convert(_expectedType, buffer.toString());
    }

    public String getText() {
        return _script;
    }

    public boolean isLiteral() {
        return false;
    }
    
}
