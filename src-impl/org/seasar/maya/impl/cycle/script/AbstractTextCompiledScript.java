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

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractTextCompiledScript 
        implements CompiledScript {

    private static final Class[] ZERO_ARGS_TYPE = new Class[0];

    private String _text;
    private Class _expectedType = Object.class;
    private Class[] _methodArgTypes;
    
    public AbstractTextCompiledScript(String text) {
        if(text == null) {
            throw new IllegalArgumentException();
        }
        _text = text;
    }

    protected String getText() {
        return _text;
    }
    
    public void setExpectedType(Class expectedType) {
        if(expectedType == null) {
            expectedType = Object.class;
        }
        _expectedType = expectedType;
    }

    public Class getExpectedType() {
        return _expectedType;
    }

    public void setMethodArgTypes(Class[] methodArgTypes) {
        if(methodArgTypes == null) {
            methodArgTypes = ZERO_ARGS_TYPE;
        }
        _methodArgTypes = methodArgTypes;
    }
    
    public Class[] getMethodArgTypes() {
        return _methodArgTypes;
    }
    
    public boolean isLiteral() {
        return false;
    }

    public boolean isReadOnly() {
        return false;
    }

    public String toString() {
        return ScriptUtil.getBlockSignedText(_text);
    }
    
}
