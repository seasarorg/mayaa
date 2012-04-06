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
package org.seasar.mayaa.impl.cycle.script.rhino;

import java.util.Iterator;

import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.scope.ScopeNotWritableException;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * 現在スコープから標準のスコープ探索順をたどって、見つかればそれを返します。
 * 見つからなければnullを返します。
 * スコープ無指定での探索のうち、JavaScriptグローバルの領域を見に行かないもの。
 * 従って JavaScript の予約語、規定のオブジェクトなどは参照できません。
 * また、参照限定なため、このスコープを利用してオブジェクトをセットすることは
 * できません。
 *
 * 主な目的はRhinoの予約語と変数名がぶつかる場合のための回避です。
 * JavaScriptから利用する場合のスコープ名はアンダースコア("_" 例: _["class"])。
 *
 * @author Koji Suga (Gluegent, Inc.)
 */
public class WalkStandardScope extends ParameterAwareImpl implements AttributeScope {

    private static final long serialVersionUID = -2952128451664421957L;

    public static final String SCOPE_NAME = "_";

    // AttributeScope implements -------------------------------------

    public String getScopeName() {
        return SCOPE_NAME;
    }

    public boolean hasAttribute(String name) {
        return CycleUtil.findStandardAttributeScope(name) != null;
    }

    public Object getAttribute(String name) {
        AttributeScope scope = CycleUtil.findStandardAttributeScope(name);
        if (scope != null) {
            return scope.getAttribute(name);
        }
        return null;
    }

    public Iterator iterateAttributeNames() {
        return NullIterator.getInstance();
    }

    public boolean isAttributeWritable() {
        return false;
    }

    public void removeAttribute(String name) {
        throw new ScopeNotWritableException(getScopeName());
    }

    public void setAttribute(String name, Object attribute) {
        throw new ScopeNotWritableException(getScopeName());
    }

    public Object newAttribute(String name, Class attributeClass) {
        throw new UnsupportedOperationException();
    }

}
