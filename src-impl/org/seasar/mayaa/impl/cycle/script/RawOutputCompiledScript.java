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

/**
 * `${=...}` で生成された非エスケープ出力を示すためのラッパー。
 */
public class RawOutputCompiledScript implements CompiledScript {

    private static final long serialVersionUID = -4216755215482335032L;

    private final CompiledScript _delegate;

    public RawOutputCompiledScript(CompiledScript delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException();
        }
        _delegate = delegate;
    }

    public CompiledScript getDelegate() {
        return _delegate;
    }

    @Override
    public String getScriptText() {
        return _delegate.getScriptText();
    }

    @Override
    public boolean isLiteral() {
        return _delegate.isLiteral();
    }

    @Override
    public Object execute(Class<?> expectedClass, Object[] args) {
        return _delegate.execute(expectedClass, args);
    }

    @Override
    public void setMethodArgClasses(Class<?>[] methodArgClasses) {
        _delegate.setMethodArgClasses(methodArgClasses);
    }

    @Override
    public Class<?>[] getMethodArgClasses() {
        return _delegate.getMethodArgClasses();
    }

    @Override
    public boolean isReadOnly() {
        return _delegate.isReadOnly();
    }

    @Override
    public void assignValue(Object value) {
        _delegate.assignValue(value);
    }
}
