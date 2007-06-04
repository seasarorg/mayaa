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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.impl.cycle.script.rhino.WalkStandardScope;

/**
 * スクリプトがGetterScriptだと解釈できるなら対応するGetterScriptを
 * 作成して返すFactory。
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class GetterScriptFactory {

    /** GetterScriptとして解釈するパターン */
    protected static final Pattern GETTER_PATTERN =
        Pattern.compile("\\s*([a-zA-Z_][a-zA-Z0-9_]*)" +
                "\\s*(\\.\\s*([a-zA-Z_][a-zA-Z0-9_]*)|" +
                "\\[\\s*'\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*'\\s*\\]|" +
                "\\[\\s*\"\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*\"\\s*\\])?[\\s;]*"
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
        return createGetterScript(script, position, offsetLine, splited[0], splited[1]);
    }

    /**
     * スクリプトをスコープと変数名に分割します。
     *
     * @param trimed
     * @return [スコープ名, 変数名]のString配列。GetterScriptでない場合はnull。
     */
    protected static String[] splitScope(String trimed) {
        Matcher matcher = GETTER_PATTERN.matcher(trimed);
        if (matcher.matches() == false) {
            return null;
        }

        String[] result = new String[2];

        String part1 = matcher.group(1);
        String part2 = matcher.group(2);
        if (part2 == null) {
            result[0] = WalkStandardScope.SCOPE_NAME;
            result[1] = part1;
        } else {
            result[0] = part1;
            result[1] = matcher.group(3);
            if (result[1] == null) {
                result[1] = matcher.group(4);
            }
            if (result[1] == null) {
                result[1] = matcher.group(5);
            }
        }

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
            String scopeName, String attributeName) {
        if (WalkStandardScope.SCOPE_NAME.equals(scopeName)) {
            return new StandardGetterScript(script, position, offsetLine, attributeName);
        } else if (ServiceCycle.SCOPE_PAGE.equals(scopeName)) {
            return new PageGetterScript(script, position, offsetLine, attributeName);
        } else if (ServiceCycle.SCOPE_REQUEST.equals(scopeName)) {
            return new RequestGetterScript(script, position, offsetLine, attributeName);
        } else if (ServiceCycle.SCOPE_SESSION.equals(scopeName)) {
            return new SessionGetterScript(script, position, offsetLine, attributeName);
        } else if (ServiceCycle.SCOPE_APPLICATION.equals(scopeName)) {
            return new ApplicationGetterScript(script, position, offsetLine, attributeName);
        }
        return null;
    }

}
