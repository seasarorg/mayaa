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

    private static final long serialVersionUID = -4686149451300693410L;

    private CompiledScript[] _compiled;

    public ComplexScript(CompiledScript[] compiled) {
        if (compiled == null) {
            throw new IllegalArgumentException();
        }
        _compiled = compiled;
    }

    @Override
    public Object execute(Class<?> expectedClass, Object[] args) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < _compiled.length; i++) {
            Object ret = _compiled[i].execute(expectedClass, null);
            if (ret != null) {
                buffer.append(ret);
            }
        }
        if (expectedClass == Void.class) {
            return null;
        }
        return ObjectUtil.convert(expectedClass, buffer.toString());
    }

    public void setMethodArgClasses(Class<?>[] methodArgClasses) {
        // do nothing.
    }

    public Class<?>[] getMethodArgClasses() {
        return null;
    }

    public String getScriptText() {
        StringBuilder buffer = new StringBuilder();
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
