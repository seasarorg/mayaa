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
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * ただの文字列をスクリプトとして扱うためのクラス。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LiteralScript implements CompiledScript {

    private static final long serialVersionUID = -3791475287481727514L;

    public static final LiteralScript NULL_LITERAL_SCRIPT = new LiteralScript("");

    private String _text;
    private Class _expectedClass = Object.class;

    public LiteralScript(String text) {
        if (text == null) {
            throw new IllegalArgumentException();
        }
        _text = text;
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
        if (_expectedClass == Void.class) {
            return null;
        }
        if (NULL_LITERAL_SCRIPT == this || StringUtil.isEmpty(_text)) {
            return NULL_LITERAL_SCRIPT._text;
        }
        if (String.class.equals(_expectedClass) || Object.class.equals(_expectedClass)) {
            return _text;
        }
        return ObjectUtil.convert(_expectedClass, _text);
    }

    public void setMethodArgClasses(Class[] methodArgClasses) {
        // do nothing.
    }

    public Class[] getMethodArgClasses() {
        return null;
    }

    public boolean isLiteral() {
        return true;
    }

    public String getScriptText() {
        return _text;
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
