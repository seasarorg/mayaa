/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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

import java.util.Iterator;
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
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class GetterScriptFactory {

    // TODO 多段階への対応
    // getterとして解釈するパターンと、順番に抽出していくパターンを分ける。
    /** GetterScriptとして解釈するパターン */
    protected static final Pattern GETTER_PATTERN =
        Pattern.compile("\\s*([a-zA-Z_][a-zA-Z0-9_]*)" +
                "(\\s*\\.\\s*([a-zA-Z_][a-zA-Z0-9_]*)|" +
                "\\s*\\[\\s*'\\s*([a-zA-Z0-9_]*)\\s*'\\s*\\]|" +
                "\\s*\\[\\s*\"\\s*([a-zA-Z0-9_]*)\\s*\"\\s*\\]|" +
                "\\s*\\.\\s*getAttribute\\s*\\(\\s*'\\s*([a-zA-Z0-9_]*)\\s*'\\s*\\)|" +
                "\\s*\\.\\s*getAttribute\\s*\\(\\s*\"\\s*([a-zA-Z0-9_]*)\\s*\"\\s*\\)" +
                "\\s*)?" +
                "(\\s*\\.\\s*([a-zA-Z_][a-zA-Z0-9_]*)|" +
                "\\s*\\[\\s*'\\s*([a-zA-Z0-9_]*)\\s*'\\s*\\]|" +
                "\\s*\\[\\s*\"\\s*([a-zA-Z0-9_]*)\\s*\"\\s*\\]|" +
                "\\s*\\.\\s*getAttribute\\s*\\(\\s*'\\s*([a-zA-Z0-9_]*)\\s*'\\s*\\)|" +
                "\\s*\\.\\s*getAttribute\\s*\\(\\s*\"\\s*([a-zA-Z0-9_]*)\\s*\"\\s*\\)" +
                "\\s*)?" +
                "[\\s;]*"
                );

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
        return createGetterScript(script, position, offsetLine, splited[0], splited[1], splited[2]);
    }

    /**
     * スクリプトをスコープと変数名、プロパティ名に分割します。
     *
     * @param script
     * @return [スコープ名, 変数名, プロパティ名]のString配列。GetterScriptでない場合はnull。
     */
    protected static String[] splitScope(String script) {
        Matcher matcher = GETTER_PATTERN.matcher(script);
        if (matcher.matches() == false) {
            return null;
        }

        String[] result = new String[3];

        // 1, 3-7, 9-13 番目が名前
        final int firstMatch = 1;
        final int secondMatch = 2;
        final int secondRangeStart = 3;
        final int secondRangeEnd = 7;
        final int thirdMatch = 8;
        final int thirdRangeStart = 9;
        final int thirdRangeEnd = 13;

        String part1 = matcher.group(firstMatch);
        if (isScopeName(part1)) {
            result[0] = part1;
            if (matcher.group(secondMatch) != null) {
                result[1] = getFromRange(matcher, secondRangeStart, secondRangeEnd);
            }
            if (matcher.group(thirdMatch) != null) {
                result[2] = getFromRange(matcher, thirdRangeStart, thirdRangeEnd);
            }
        } else if (isReserved(part1) == false && matcher.group(thirdMatch) == null) {
            result[0] = WalkStandardScope.SCOPE_NAME;
            result[1] = part1;
            if (matcher.group(secondMatch) != null) {
                result[2] = getFromRange(matcher, secondRangeStart, secondRangeEnd);
            }
        }

        return result;
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
            String script, PositionAware position, int offsetLine,
            String scopeName, String attributeName, String propertyName) {
        if (containsRhinoSpecialProperty(attributeName, propertyName)) {
            return null;
        }

        if (WalkStandardScope.SCOPE_NAME.equals(scopeName)) {
            return new StandardGetterScript(
                    script, position, offsetLine, attributeName, propertyName);
        } else if (ServiceCycle.SCOPE_PAGE.equals(scopeName) || "this".equals(scopeName)) {
            return new PageGetterScript(
                    script, position, offsetLine, attributeName, propertyName);
        } else if (ServiceCycle.SCOPE_REQUEST.equals(scopeName)) {
            return new RequestGetterScript(
                    script, position, offsetLine, attributeName, propertyName);
        } else if (ServiceCycle.SCOPE_SESSION.equals(scopeName)) {
            return new SessionGetterScript(
                    script, position, offsetLine, attributeName, propertyName);
        } else if (ServiceCycle.SCOPE_APPLICATION.equals(scopeName)) {
            return new ApplicationGetterScript(
                    script, position, offsetLine, attributeName, propertyName);
        } else {
            Iterator it = ProviderUtil.getScriptEnvironment().iterateAttributeScope();
            while (it.hasNext()) {
                AttributeScope scope = (AttributeScope) it.next();
                if (scope.getScopeName().equals(scopeName)) {
                    return new AttributeScopeGetterScript(
                            script, position, offsetLine,
                            attributeName, propertyName, scopeName);
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
