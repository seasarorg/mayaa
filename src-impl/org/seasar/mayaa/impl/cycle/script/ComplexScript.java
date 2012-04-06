/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.cycle.script;

import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.impl.util.ObjectUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ComplexScript implements CompiledScript {

     private static final long serialVersionUID = -7356099026354564155L;

    private Class _expectedClass = Object.class;
    private CompiledScript[] _compiled;

    public ComplexScript(CompiledScript[] compiled) {
        if (compiled == null) {
            throw new IllegalArgumentException();
        }
        _compiled = compiled;
        for (int i = 0; i < compiled.length; i++) {
            compiled[i].setExpectedClass(String.class);
        }
    }

    public void setExpectedClass(Class expectedClass) {
        if (expectedClass == null) {
            throw new IllegalArgumentException();
        }
        _expectedClass = expectedClass;
    }

    public Class getExpectedClass() {
        return _expectedClass;
    }

    public Object execute(Object[] args) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < _compiled.length; i++) {
            Object ret = _compiled[i].execute(null);
            if (ret != null) {
                buffer.append(ret);
            }
        }
        if (_expectedClass == Void.class) {
            return null;
        }
        return ObjectUtil.convert(_expectedClass, buffer.toString());
    }

    public void setMethodArgClasses(Class[] methodArgClasses) {
        // do nothing.
    }

    public Class[] getMethodArgClasses() {
        return null;
    }

    public String getScriptText() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < _compiled.length; i++) {
            buffer.append(_compiled[i].getScriptText());
        }
        return buffer.toString();
    }

    public boolean isLiteral() {
        return false;
    }

    public boolean isReadOnly() {
        return true;
    }

    public void assignValue(Object value) {
        throw new ReadOnlyScriptBlockException(toString());
    }

    public String toString() {
        return getScriptText();
    }

}
