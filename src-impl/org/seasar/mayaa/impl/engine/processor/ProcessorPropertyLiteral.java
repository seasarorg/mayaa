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
package org.seasar.mayaa.impl.engine.processor;

import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.impl.cycle.script.LiteralScript;

/**
 * 値の式を文字列として処理するプロパティランタイム。
 * @author Hisayoshi Sasaki (Gluegent, Inc.)
 */
public class ProcessorPropertyLiteral implements ProcessorProperty {

    private static final long serialVersionUID = -5156693929626730452L;

    private PrefixAwareName _name;
    private CompiledScript _compiled;

    public ProcessorPropertyLiteral(
            PrefixAwareName name, String value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException();
        }
        _name = name;
        if (value.length() == 0) {
            _compiled = LiteralScript.NULL_LITERAL_SCRIPT;
        } else {
            _compiled = new LiteralScript(value);
        }
    }

    public PrefixAwareName getName() {
        return _name;
    }

    public CompiledScript getValue() {
        return _compiled;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ProcessorProperty) {
            PrefixAwareName otherName = ((ProcessorProperty) obj).getName();
            return _name.getQName().equals(otherName.getQName());
        }
        return false;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public String toString() {
        return _name.toString() + "=\"" + _compiled.toString() + "\"";
    }
}
