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
package org.seasar.mayaa.cycle.script;

import java.util.Iterator;
import java.util.Map;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * スクリプトの実行環境。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ScriptEnvironment extends ParameterAware {

    /**
     * ユーザー定義スコープオブジェクトを追加する。
     * @param attrs ユーザー定義スコープ。
     */
    void addAttributeScope(AttributeScope attrs);

    /**
     * ユーザー定義スコープオブジェクトをイテレートする。
     * @return スコープオブジェクト（AttributeScope）のイテレータ。
     */
    Iterator iterateAttributeScope();

    /**
     * スクリプト文字列ブロックの開きクオートに前置される識別文字列の取得。
     * @return 開きクオート前置文字列。
     */
    String getBlockSign();

    /**
     * 式文字列をコンパイルする。
     * @param script スクリプト。
     * @param position スクリプトソースの位置情報。
     * @return コンパイル済みスクリプトオブジェクト。
     */
    CompiledScript compile(String script, PositionAware position);

    /**
     * スクリプトソースファイルを読み込んでコンパイルする。
     * @param source スクリプトソースファイル。
     * @param encoding スクリプトソースファイルのエンコーディング。
     * @return コンパイル済みスクリプトオブジェクト。
     */
    CompiledScript compile(SourceDescriptor source, String encoding);

    /**
     * テンプレート描画時に、プロセッサのスタートイベントと同期するためのメソッド。
     * カレントServiceCycleのページスコープを初期化する。
     */
    void initScope();

    /**
     * テンプレート描画時に、プロセッサのスタートイベントと同期するためのメソッド。
     * カレントServiceCycleのページスコープに、スクリプト変数をプッシュする。
     * @param variables カスタムスコープ変数。
     */
    void startScope(Map variables);

    /**
     * テンプレート描画時に、プロセッサのエンドイベントと同期するためのメソッド。
     * カレントServiceCycleのページスコープから、スクリプトのスコープオブジェクト
     * をポップする。
     */
    void endScope();

    /**
     * スクリプト内部表現オブジェクトから、Javaのオブジェクトに変換する。
     * @param scriptObject スクリプト内部表現オブジェクト。
     * @return Javaのオブジェクト。
     */
    Object convertFromScriptObject(Object scriptObject);

    /**
     * スクリプト内部表現オブジェクトから、Javaのオブジェクトに変換する。
     * @param scriptObject スクリプト内部表現オブジェクト。
     * @param expectedClass 変換後のクラス。
     * @return Javaのオブジェクト。
     * @since 1.1.11
     */
    Object convertFromScriptObject(Object scriptObject, Class expectedClass);

    /**
     * スクリプトとして空と見なせることを判定する。
     * @param scriptResult 判定するオブジェクト
     * @return 空と見なせるなら{@code true}
     */
    boolean isEmpty(Object scriptResult);

}
