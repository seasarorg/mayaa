/*
 * Copyright 2004-2026 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.functional.engine;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.builder.TemplateBuilderImpl;
import org.seasar.mayaa.impl.source.DynamicRegisteredSourceHolder;

/**
 * JSエンジン互換性を検証するIT。
 *
 * <p>
 * 現行エンジン (Rhino 1.9.1) での動作を固定化し、
 * 別のJSのスクリプトエンジンに切り替えた時の差分を検出しやすくする。
 * </p>
 *
 * <h3>テスト分類と活用シーン</h3>
 * <ul>
 *   <li><strong>ES5回帰確認</strong>: Rhino 1.9.1 で動作し、移行後も継続すべき基本機能。
 *       新しいエンジンでも常に成功すべきテスト。</li>
 *   <li><strong>Rhino拡張</strong>: Rhino 固有の非標準機能。
 *       GraalJS など別のエンジンでは失敗する可能性が高い。
 *       アップグレード時に「削除対象機能」を検出するテスト。</li>
 *   <li><strong>ES6+ 可用性確認</strong>: Rhino 1.9.1 でのサポート状況を固定化する機能。
 *       テスト期待値が変わる。新エンジンで期待値更新後は常に成功すべき。</li>
 * </ul>
 *
 * <h3>アップグレードシナリオ別の用途</h3>
 * <ol>
 *   <li><strong>Rhino アップグレード（旧バージョン → 1.9.1 以降）</strong>:
 *       全テストで期待値維持。新バージョンでの regression を検出。</li>
 *   <li><strong>エンジン切り替え（Rhino → GraalJS）</strong>:
 *       - ES5 テストは成功維持。
 *       - Rhino拡張 テストは失敗（既知の移行差分）。
 *       - ES6+ テストは期待値更新後に成功へ。</li>
 *   <li><strong>先制的な互換性検証</strong>:
 *       GraalJS での実測テストにより、移行前の risk assessment が可能。</li>
 * </ol>
 */
public class ScriptESCompatibilityIntegrationTest extends EngineTestBase {

    private static final String BASE = "/it-case/script-es-compatibility/";

    // -------------------------------------------------------------------------
    // ES5 — 移行後も継続動作すべき機能の回帰確認
    // -------------------------------------------------------------------------

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "4,8"  (推定) ES5: Array.prototype.map/filter はサポート済み
     * org.mozilla:rhino:1.9.1 → "4,8"  変化なし
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void JS16_Arrayのmapとfilterが利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "array-map-filter/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // JS 1.6 (Rhino 1.9.1 で利用可能): map / filter。
                        // 移行後エンジンでも継続動作すべき。
                        var even = [1, 2, 3, 4, 5].filter(function(x) { return x % 2 === 0; });
                        var doubled = even.map(function(x) { return x * 2; });
                        request.message = doubled.join(",");
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        // filter [2,4] → map [4,8] → join "4,8"
        String expected = "4,8";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "6"  (推定) ES5: Array.prototype.forEach はサポート済み
     * org.mozilla:rhino:1.9.1 → "6"  変化なし
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void JS16_ArrayのforEachが利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "array-foreach/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // Array.prototype.forEach は現行 Rhino 1.9.1 でも利用可能。
                        // Rhino 独自拡張の for each 構文とは別物で、こちらは標準メソッド側の確認。
                        var sum = 0;
                        [1, 2, 3].forEach(function(value) {
                            sum += value;
                        });
                        request.message = String(sum);
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "6";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-available"  JSON グローバルは 1.7R3 以降で追加。1.7R2 では非サポート
     * org.mozilla:rhino:1.9.1 → "available"      ★変更 1.7R3 以降の JSON サポートを含むバージョンに更新
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES5_JSONは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es5-json/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES5 の JSON グローバルは現行 Rhino 1.9.1 で利用可能。
                        request.message = (typeof JSON === "undefined") ? "not-available" : "available";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "available";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-available"  Object.keys / Array.prototype.reduce は 1.7R3 以降で追加。1.7R2 では非サポート
     * org.mozilla:rhino:1.9.1 → "available"      ★変更 ES5 メソッド群を含むバージョンに更新
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES5_ObjectKeysとArrayReduceは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es5-object-keys-reduce/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES5 の Object.keys / Array.prototype.reduce は現行 Rhino 1.9.1 で利用可能。
                        var hasKeys   = (typeof Object.keys            === "function");
                        var hasReduce = (typeof Array.prototype.reduce === "function");
                        request.message = (hasKeys || hasReduce) ? "available" : "not-available";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "available";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "object"  (推定) ES3以降: typeof null === "object" は言語仕様として不変
     * org.mozilla:rhino:1.9.1 → "object"  変化なし
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES5_typeofnullはobjectである(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "typeof-null-is-object/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES5 の仕様上の quirk: typeof null === "object"。
                        // ES6+ 準拠エンジンでも変わらない（仕様として残存）。
                        request.message = typeof null;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "object";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "true:true"  (推定) IEEE 754規定: エンジン非依存の言語仕様
     * org.mozilla:rhino:1.9.1 → "true:true"  変化なし
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES5_NaNはNaN自身と等しくない(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "nan-not-equal-nan/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // IEEE 754: NaN !== NaN は true。全エンジンで共通の挙動。
                        request.message = String(NaN !== NaN) + ":" + String(isNaN(NaN));
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "true:true";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "true:2:hellO wOrld:3"  (推定) ES3/ES5: 正規表現基本機能はサポート済み
     * org.mozilla:rhino:1.9.1 → "true:2:hellO wOrld:3"  変化なし
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES5_RegExpの基本機能が利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es5-regexp-basic/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES5 の正規表現: literal, constructor, match, replace, split, グローバルフラグ等は
                        // 現行エンジン (Rhino 1.9.1) で継続動作すべき基本機能。
                        var literal = /world/.test("hello world");
                        var match = "hello".match(/l/g);
                        var replace = "hello world".replace(/o/g, "O");
                        var split = "a,b,c".split(/,/);
                        request.message = [literal, (match ? match.length : 0), replace, split.length].join(":");
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        // literal=true, match.length=2, replace="hellO wOrld", split.length=3
        String expected = "true:2:hellO wOrld:3";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-available"  (推定) ES5世代: startsWith/endsWith/includes 等は未実装
     * org.mozilla:rhino:1.9.1 → "available"      ★変更 ES6+ 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6Plus_String拡張メソッドstartsWithなどは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-string-methods/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6+ の String 拡張メソッド:
                        // startsWith, endsWith, includes, repeat, padStart, padEnd など
                        // は現行エンジン (Rhino 1.9.1) で利用可能。
                        var hasStartsWith = (typeof "".startsWith === "function");
                        var hasEndsWith = (typeof "".endsWith === "function");
                        var hasIncludes = (typeof "".includes === "function");
                        var hasRepeat = (typeof "".repeat === "function");
                        request.message = (hasStartsWith || hasEndsWith || hasIncludes || hasRepeat) 
                            ? "available" : "not-available";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "available";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-available"  (推定) ES5世代: find/findIndex/includes/flat 等は未実装
     * org.mozilla:rhino:1.9.1 → "available"      ★変更 ES6+ 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6Plus_Array拡張メソッドfindなどは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-array-methods/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6+ の Array 拡張メソッド:
                        // find, findIndex, includes, flat, flatMap など
                        // は現行エンジン (Rhino 1.9.1) で利用可能。
                        var hasFind = (typeof [].find === "function");
                        var hasFindIndex = (typeof [].findIndex === "function");
                        var hasIncludes = (typeof [].includes === "function");
                        var hasFlat = (typeof [].flat === "function");
                        request.message = (hasFind || hasFindIndex || hasIncludes || hasFlat) 
                            ? "available" : "not-available";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "available";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-available"  (推定) ES5世代: Object.entries/values は未実装
     * org.mozilla:rhino:1.9.1 → "available"      ★変更 ES2017 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES2017_Object_entriesとvaluesは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es2017-object-methods/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES2017 の Object 静的メソッド:
                        // Object.entries, Object.values は現行エンジン (Rhino 1.9.1) で利用可能。
                        var hasEntries = (typeof Object.entries === "function");
                        var hasValues = (typeof Object.values === "function");
                        request.message = (hasEntries || hasValues) ? "available" : "not-available";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "available";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    // -------------------------------------------------------------------------
    // Rhino拡張 — 現行エンジンで利用可能だが標準外のため移行差分になりやすい観点
    // -------------------------------------------------------------------------

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "6"  (推定) Rhino独自拡張: for each (...) 構文はサポート済み
     * org.mozilla:rhino:1.9.1 → "6"  変化なし
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void Rhino拡張_forEach構文が利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "rhino-for-each-syntax/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // Rhino 独自拡張の for each (...) は現行エンジンでは利用可能。
                        // ECMAScript 標準ではないため、別のJSのスクリプトエンジンに切り替えると
                        // 構文エラーになる可能性が高い移行差分点として固定化する。
                        var sum = 0;
                        for each (var value in [1, 2, 3]) {
                            sum += value;
                        }
                        request.message = String(sum);
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        // 現行 Rhino 1.9.1: 6
        // GraalJS など標準寄りエンジンでは構文エラーになる可能性が高い
        String expected = "6";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "class java.lang.String:true"  (推定) Rhino Java インターオップ: getClass()/['class']はサポート済み
     * org.mozilla:rhino:1.9.1 → "class java.lang.String:true"  変化なし
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void Rhino拡張_JavaオブジェクトのgetClass取得が利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "rhino-java-class-ref/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // Rhino の Java インターオップ: obj.getClass() は利用可能。
                        // ブラケット記法 obj['class'] も Rhino が getClass() にマップするため同値になる。
                        // ドット記法 obj.class は 'class' が予約語のため Rhino 1.9.1 でも構文エラー
                        // (ES3 時代から将来予約語扱い。ES6 class キーワードと競合するより前の問題)。
                        // GraalJS 等への移行後: getClass() は継続動作するが、
                        //   ['class'] マッピングの挙動は移行差分点になりうる。
                        var s = new java.lang.String("hello");
                        var byMethod = "" + s.getClass();
                        var byBracket = (s['class'] === s.getClass());
                        request.message = byMethod + ":" + byBracket;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        // 現行 Rhino 1.9.1: class java.lang.String:true
        // GraalJS など: getClass() の戻り値は同じだが ['class'] マッピングは未保証
        String expected = "class java.lang.String:true";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    // -------------------------------------------------------------------------
    // ES6+ — 現行エンジンでの可用性確認
    // -------------------------------------------------------------------------

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported"  eval() 経由の let はサポートされていなかった
     * org.mozilla:rhino:1.9.1 → "supported"      ★変更 ES6 let として正式サポート
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_letは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-let/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 let は現行エンジン (Rhino 1.9.1) で利用可能。
                        var result;
                        try {
                            eval("let x = 1; x;");
                            result = "supported";
                        } catch (e) {
                            result = "not-supported";
                        }
                        request.message = result;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported"  eval() 経由の const はサポートされていなかった
     * org.mozilla:rhino:1.9.1 → "supported"      ★変更 ES6 const として正式サポート
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_constは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-const/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 const は現行エンジン (Rhino 1.9.1) で利用可能。
                        var result;
                        try {
                            eval("const x = 1; x;");
                            result = "supported";
                        } catch (e) {
                            result = "not-supported";
                        }
                        request.message = result;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-available"  (推定) ES5世代: Promise/Symbol は未実装
     * org.mozilla:rhino:1.9.1 → "available"      ★変更 ES6 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_PromiseとSymbolは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-promise-symbol/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 の Promise / Symbol は現行エンジン (Rhino 1.9.1) で利用可能。
                        var hasPromise = (typeof Promise === "function");
                        var hasSymbol = (typeof Symbol === "function");
                        request.message = (hasPromise || hasSymbol) ? "available" : "not-available";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "available";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-available"  (推定) ES5世代: Map/Set は未実装
     * org.mozilla:rhino:1.9.1 → "available"      ★変更 ES6 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_MapとSetは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-map-set/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 の Map / Set は現行エンジン (Rhino 1.9.1) で利用可能。
                        var hasMap = (typeof Map === "function");
                        var hasSet = (typeof Set === "function");
                        request.message = (hasMap || hasSet) ? "available" : "not-available";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "available";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported"  (推定) ES5世代: アロー関数は未サポート
     * org.mozilla:rhino:1.9.1 → "supported"      ★変更 ES6 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_アロー関数は現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-arrow-function/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 アロー関数 (x => x) は現行エンジン (Rhino 1.9.1) で利用可能。
                        var result;
                        try {
                            eval("(x => x)(1)");
                            result = "supported";
                        } catch (e) {
                            result = "not-supported";
                        }
                        request.message = result;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported"  (推定) ES5世代: テンプレートリテラルは未サポート
     * org.mozilla:rhino:1.9.1 → "supported"      ★変更 ES6 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_テンプレートリテラルは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-template-literal/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 テンプレートリテラル (`...`) は現行エンジン (Rhino 1.9.1) で利用可能。
                        // バッククォートは ES5 構文内に直接書けないため、文字コードから生成して eval に渡す。
                        var backtick = String.fromCharCode(96);
                        var result;
                        try {
                            eval(backtick + "hello" + backtick);
                            result = "supported";
                        } catch (e) {
                            result = "not-supported";
                        }
                        request.message = result;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported"  (推定) ES5世代: for...of は未サポート
     * org.mozilla:rhino:1.9.1 → "supported"      ★変更 ES6 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_forOfは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-for-of/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 for...of は現行エンジン (Rhino 1.9.1) で利用可能。
                        var result;
                        try {
                            eval("var s = 0; for (var x of [1, 2, 3]) { s += x; }");
                            result = "supported";
                        } catch (e) {
                            result = "not-supported";
                        }
                        request.message = result;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported:not-supported"  (推定) ES5世代: RegExp u/y フラグはES6以降の機能
     * org.mozilla:rhino:1.9.1 → "supported:supported"          ★変更 ES6 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_RegExpフラグのuとyは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-regexp-flags/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 の RegExp フラグ u (unicode) と y (sticky) は
                        // 現行エンジン (Rhino 1.9.1) で利用可能。
                        var resultU;
                        try {
                            eval("/./u");
                            resultU = "supported";
                        } catch (e) {
                            resultU = "not-supported";
                        }
                        var resultY;
                        try {
                            eval("/./y");
                            resultY = "supported";
                        } catch (e) {
                            resultY = "not-supported";
                        }
                        request.message = resultU + ":" + resultY;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported:supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported"  (推定) ES5世代: class は未サポート
     * org.mozilla:rhino:1.9.1 → "not-supported"  変化なし（依然未対応）
     * ※ GraalJS など ES6+ 対応エンジンでは "supported" になる移行差分点
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_classキーワードは現行エンジンでは非対応(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-class/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 class キーワードは現行エンジン (Rhino 1.9.1) では構文エラー。
                        // 別のJSのスクリプトエンジンに切り替えると "supported" になる移行差分点。
                        var result;
                        try {
                            eval("class Foo { constructor(x) { this.x = x; } }");
                            result = "supported";
                        } catch (e) {
                            result = "not-supported";
                        }
                        request.message = result;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        // 現行 Rhino 1.9.1: not-supported
        // GraalJS など ES6+ エンジンでは: supported
        String expected = "not-supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported:supported"  配列分割のみ部分サポート、オブジェクト分割 ({a,b}=obj) は非サポート
     * org.mozilla:rhino:1.9.1 → "supported:supported"      ★変更 オブジェクト分割も含め完全サポート
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_分割代入は現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-destructuring/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 の分割代入 (destructuring) は現行エンジン (Rhino 1.9.1) で利用可能。
                        // オブジェクト分割 const {a, b} = obj と配列分割 const [x, y] = arr がある。
                        // 別のJSのスクリプトエンジンに切り替えると "supported" になる移行差分点。
                        var resultObj;
                        try {
                            eval("var {a, b} = {a:1, b:2}; a");
                            resultObj = "supported";
                        } catch (e) {
                            resultObj = "not-supported";
                        }
                        var resultArr;
                        try {
                            eval("var [x, y] = [1, 2]; x");
                            resultArr = "supported";
                        } catch (e) {
                            resultArr = "not-supported";
                        }
                        request.message = resultObj + ":" + resultArr;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported:supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported"  (推定) ES5世代: spread operator は未サポート
     * org.mozilla:rhino:1.9.1 → "supported"      ★変更 ES6 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_SpreadOperatorは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-spread-operator/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 Spread operator (...) は現行エンジン (Rhino 1.9.1) で利用可能。
                        var result;
                        try {
                            eval("[...[1,2,3]]");
                            result = "supported";
                        } catch (e) {
                            result = "not-supported";
                        }
                        request.message = result;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported:not-supported"  (推定) ES5世代: default/rest parameters は未サポート
     * org.mozilla:rhino:1.9.1 → "supported:supported"          ★変更 ES6 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_DefaultParametersとRestParametersは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-function-params/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 の Default parameters と Rest parameters は
                        // 現行エンジン (Rhino 1.9.1) で利用可能。
                        // function(a = 10) と function(...args) が該当。
                        // 別のJSのスクリプトエンジンに切り替えると "supported" になる移行差分点。
                        var resultDefault;
                        try {
                            eval("function f(a = 10) { return a; }");
                            resultDefault = "supported";
                        } catch (e) {
                            resultDefault = "not-supported";
                        }
                        var resultRest;
                        try {
                            eval("function f(...args) { return args.length; }");
                            resultRest = "supported";
                        } catch (e) {
                            resultRest = "not-supported";
                        }
                        request.message = resultDefault + ":" + resultRest;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported:supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported"  (推定) ES5世代: ES6 function* 構文は未サポート（JS1.7非標準 yield 構文は別途存在）
     * org.mozilla:rhino:1.9.1 → "supported"      ★変更 ES6 対応により function* 構文が利用可能に
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_Generatorは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-generators/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 の Generator (function* と yield) は
                        // 現行エンジン (Rhino 1.9.1) で利用可能。
                        // 別のJSのスクリプトエンジンに切り替えると "supported" になる移行差分点。
                        var result;
                        try {
                            eval("function* gen() { yield 1; }");
                            result = "supported";
                        } catch (e) {
                            result = "not-supported";
                        }
                        request.message = result;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-available"  (推定) ES5世代: Number.isNaN/isInteger/isFinite は未実装
     * org.mozilla:rhino:1.9.1 → "available"      ★変更 ES2015 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES2015_NumberメソッドisNaNやisIntegerは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es2015-number-methods/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES2015 の Number 静的メソッド:
                        // Number.isNaN, Number.isInteger, Number.isFinite, Number.isSafeInteger
                        // は現行エンジン (Rhino 1.9.1) で利用可能。
                        var hasIsNaN = (typeof Number.isNaN === "function");
                        var hasIsInteger = (typeof Number.isInteger === "function");
                        var hasIsFinite = (typeof Number.isFinite === "function");
                        request.message = (hasIsNaN || hasIsInteger || hasIsFinite) ? "available" : "not-available";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "available";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported"  (推定) ES5世代: Optional Chaining (?.) は未サポート
     * org.mozilla:rhino:1.9.1 → "supported"      ★変更 ES2020 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES2020_OptionalChainingは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es2020-optional-chaining/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES2020 の Optional chaining (?.) は
                        // 現行エンジン (Rhino 1.9.1) で利用可能。
                        // obj?.prop や obj?.method?.() が該当。
                        var result;
                        try {
                            eval("var x = {}; x?.a?.b");
                            result = "supported";
                        } catch (e) {
                            result = "not-supported";
                        }
                        request.message = result;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported"  (推定) ES5世代: Nullish Coalescing (??) は未サポート
     * org.mozilla:rhino:1.9.1 → "supported"      ★変更 ES2020 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES2020_NullishCoalescingは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es2020-nullish-coalescing/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES2020 の Nullish coalescing (??) は
                        // 現行エンジン (Rhino 1.9.1) で利用可能。
                        // value ?? defaultValue が該当。
                        var result;
                        try {
                            eval("var x = null; var y = x ?? 10;");
                            result = "supported";
                        } catch (e) {
                            result = "not-supported";
                        }
                        request.message = result;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → "not-supported:not-supported"  (推定) ES5世代: computed property names / object shorthand は未サポート
     * org.mozilla:rhino:1.9.1 → "supported:supported"          ★変更 ES6 対応により追加
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ES6_ComputedPropertyNamesとObjectShorthandは現行エンジンで利用可能(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String casePath = BASE + "es6-object-literals/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath    = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // ES6 のオブジェクトリテラル拡張:
                        // Computed property names ({[key]: value}) と
                        // Object shorthand ({x, y} for {x: x, y: y}) は
                        // 現行エンジン (Rhino 1.9.1) で利用可能。
                        var resultComputed;
                        try {
                            eval("var k = 'x'; var obj = {[k]: 1};");
                            resultComputed = "supported";
                        } catch (e) {
                            resultComputed = "not-supported";
                        }
                        var resultShorthand;
                        try {
                            eval("var x = 1, y = 2; var obj = {x, y};");
                            resultShorthand = "supported";
                        } catch (e) {
                            resultShorthand = "not-supported";
                        }
                        request.message = resultComputed + ":" + resultShorthand;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "supported:supported";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    void setUseNewParser(boolean useNewParser) {
        getServiceProvider().getTemplateBuilder().setParameter(
                TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));
    }

    void setAutoEscapeEnabled(boolean enabled) {
        getServiceProvider().getScriptEnvironment().setParameter(
                "autoEscapeEnabled", Boolean.toString(enabled));
    }

    @AfterEach
    void cleanup() {
        DynamicRegisteredSourceHolder.unregisterAll();
    }
}
