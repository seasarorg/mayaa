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
package org.seasar.mayaa.impl.cycle.script.rhino.direct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.NativeObject;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.script.rhino.NativeServiceCycle;
import org.seasar.mayaa.impl.cycle.script.rhino.WalkStandardScope;
import org.seasar.mayaa.impl.provider.ProviderUtil;

/**
 * スクリプトがGetterScriptだと解釈できるなら対応するGetterScriptを
 * 作成して返すFactory。
 * GetterScriptと呼んでいるのは、attributeまたはpropertyの取得が連なっている
 * だけのスクリプトのこと。
 * TODO 完成させる
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class GetterScriptFactory {

    /** GetterScriptとして解釈するパターン */
    protected static final Pattern GETTER_SCRIPT_PATTERN =
        Pattern.compile("\\s*([a-zA-Z_][a-zA-Z0-9_]*)" +
                "(\\s*\\.\\s*[a-zA-Z_][a-zA-Z0-9_]*|" +
                "\\s*\\[\\s*'\\s*[a-zA-Z0-9_]*\\s*'\\s*\\]|" +
                "\\s*\\[\\s*\"\\s*[a-zA-Z0-9_]*\\s*\"\\s*\\]" +
                "\\s*)*[\\s;]*");

    /** 個別getterのパターン */
    protected static final Pattern GETTER_PATTERN =
        Pattern.compile("\\s*\\.\\s*([a-zA-Z_][a-zA-Z0-9_]*)|" +
                "\\s*\\[\\s*'\\s*([a-zA-Z0-9_]*)\\s*'\\s*\\]|" +
                "\\s*\\[\\s*\"\\s*([a-zA-Z0-9_]*)\\s*\"\\s*\\]" +
                "\\s*");

    /**
     * 対応するGetterScriptを生成して返します。
     * GetterScriptでない場合はnullを返します。
     *
     * @param script 対象とするScript
     * @param position スクリプトのソースを表す情報
     * @param offsetLine スクリプトの行番号
     * @return 対応するGetterScriptまたはnull
     */
    public static CompiledScript create(
            String script, PositionAware position, int offsetLine) {
        String[] splited = splitScope(script);
        if (splited == null) {
            return null;
        }
        return createGetterScript(script, position, offsetLine, splited);
    }

    /**
     * スクリプトをスコープと変数名、プロパティ名に分割します。
     *
     * @param script
     * @return [スコープ名, 変数名, プロパティ名]のString配列。GetterScriptでない場合はnull。
     */
    protected static String[] splitScope(String script) {
        Matcher fullMatcher = GETTER_SCRIPT_PATTERN.matcher(script);
        if (fullMatcher.matches() == false) {
            return null;
        }

        List result = new ArrayList();

        String first = fullMatcher.group(1);
        if (isScopeName(first)) {
            result.add(first);
        } else {
            result.add(null);// standardScope
            result.add(first);
        }

        Matcher matcher = GETTER_PATTERN.matcher(script);
        int nextIndex = 0;
        while (matcher.find(nextIndex)) {
            result.add(getFromRange(matcher, 1, 3));
            nextIndex = matcher.end();
        }

        return (String[]) result.toArray(new String[result.size()]);
    }

    private static String getFromRange(Matcher matcher, int start, int end) {
        String result = null;
        int index = start;
        do {
            result = matcher.group(index++);
        } while (result == null && index <= end);
        return result;
    }

    /**
     * 対応するGetterScriptを生成して返します。
     * 対応していないスコープ名の場合はnullを返します。
     *
     * @param script 対象とするScript
     * @param position スクリプトのソースを表す情報
     * @param offsetLine スクリプトの行番号
     * @param scopeName 対象とするスコープ名
     * @param attributeName 変数名
     * @return 対応するGetterScriptまたはnull
     */
    protected static CompiledScript createGetterScript(
            String script, PositionAware position, int offsetLine, String[] params) {

        // TODO 無限段対応
        if (params.length > 3) {
            return null;// 無限段対応までは上限が3段階
        }

        String scopeName = params[0];
        String attributeName = null;
        String propertyName = null;
        if (params.length > 1) {
            attributeName = params[1];
        }
        if (params.length > 2) {
            propertyName = params[2];
        }

        if (containsRhinoSpecialProperty(attributeName, propertyName)) {
            return null;
        }

        if (scopeName == null || WalkStandardScope.SCOPE_NAME.equals(scopeName)) {
            return new StandardGetterScript(
                    script, position, offsetLine, scopeName, attributeName, propertyName);
        } else if (ServiceCycle.SCOPE_PAGE.equals(scopeName) || "this".equals(scopeName)) {
            return new PageGetterScript(
                    script, position, offsetLine, scopeName, attributeName, propertyName);
        } else if (ServiceCycle.SCOPE_REQUEST.equals(scopeName)) {
            return new RequestGetterScript(
                    script, position, offsetLine, scopeName, attributeName, propertyName);
        } else if (ServiceCycle.SCOPE_SESSION.equals(scopeName)) {
            return new SessionGetterScript(
                    script, position, offsetLine, scopeName, attributeName, propertyName);
        } else if (ServiceCycle.SCOPE_APPLICATION.equals(scopeName)) {
            return new ApplicationGetterScript(
                    script, position, offsetLine, scopeName, attributeName, propertyName);
        } else {
            Iterator it = ProviderUtil.getScriptEnvironment().iterateAttributeScope();
            while (it.hasNext()) {
                AttributeScope scope = (AttributeScope) it.next();
                if (scope.getScopeName().equals(scopeName)) {
                    return new AttributeScopeGetterScript(
                            script, position, offsetLine,
                            scopeName, attributeName, propertyName);
                }
            }
        }
        return null;
    }

    /**
     * 予約語かどうかを判定します。
     * ここでいう予約語とは、ServiceCycleに対する呼び出しになるものか、
     * あるいはRhinoの予約語です。
     *
     * @param word 判定する語
     * @return 予約語ならtrue
     */
    protected static boolean isReserved(String word) {
        if ("java".equals(word) || "Packages".equals(word)) {
            return true;
        }
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        NativeServiceCycle nc = new NativeServiceCycle(new NativeObject(), cycle);
        return nc.hasMember(word, nc);
    }

    /**
     * Rhinoの特殊なプロパティのうち、Javaでエミュレートしづらいものが含まれている
     * かどうかを判定します。
     * __parent__はスコープの場合に限り対応可能。__proto__は対応不能。
     *
     * @param attributeName 属性名
     * @param propertyName プロパティ名
     * @return 特殊な名前を含むならtrue
     */
    protected static boolean containsRhinoSpecialProperty(
            String attributeName, String propertyName) {
        return "__proto__".equals(attributeName) || "__proto__".equals(propertyName);
    }

    /**
     * スコープとして認識するかどうかを判定します。
     * ただしここで認識したスコープがすべて変換対象となるわけではありません。
     *
     * @param name 判定する名前
     * @return スコープとして認識する名前であればtrue
     */
    protected static boolean isScopeName(String name) {
        if (CycleUtil.getStandardScope().contains(name) || "this".equals(name)) {
            return true;
        }
        Iterator it = ProviderUtil.getScriptEnvironment().iterateAttributeScope();
        while (it.hasNext()) {
            AttributeScope scope = (AttributeScope) it.next();
            if (scope.getScopeName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
