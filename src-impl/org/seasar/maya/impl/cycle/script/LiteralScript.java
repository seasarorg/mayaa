/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.cycle.script;

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LiteralScript  implements CompiledScript {

	private static final long serialVersionUID = -3791475287481727514L;
    
	private String _text;
    private Class _expectedType = Object.class;
    
    public LiteralScript(String text) {
        if(text == null) {
            throw new IllegalArgumentException();
        }
        _text = text;
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
    
    public Object execute(Object[] args) {
        if(_expectedType == Void.class) {
            return null;
        }
        if (StringUtil.isEmpty(_text)) {
            return "";
        }
        return ObjectUtil.convert(_expectedType, _text);
    }

    public void setMethodArgTypes(Class[] methodArgTypes) {
    }
    
    public Class[] getMethodArgTypes() {
        return null;
    }

    public boolean isLiteral() {
        return true;
    }

    public boolean isReadOnly() {
        return true;
    }

    public void assignValue(Object value) {
        throw new ReadOnlyScriptBlockException(toString());
    }

    public String toString() {
        return _text;
    }
    
}
