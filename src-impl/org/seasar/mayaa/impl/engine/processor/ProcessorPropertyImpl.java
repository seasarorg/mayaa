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
import org.seasar.mayaa.impl.cycle.script.ScriptUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProcessorPropertyImpl implements ProcessorProperty {
    private static final long serialVersionUID = 1794156997857284538L;

    private PrefixAwareName _name;
    private CompiledScript _compiled;

    public ProcessorPropertyImpl(
            PrefixAwareName name, String value, Class expectedClass) {
        if (name == null || expectedClass == null) {
            throw new IllegalArgumentException();
        }
        _name = name;
        _compiled = ScriptUtil.compile(value, expectedClass);
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
