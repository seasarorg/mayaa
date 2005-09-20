/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.cycle.script;

import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.provider.Parameterizable;
import org.seasar.maya.source.SourceDescriptor;

/**
 * スクリプトの実行環境。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ScriptEnvironment extends Parameterizable {
    
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
     * @param sourceName ソースファイル名。
     * @param lineno 開始行番号。
     * @return コンパイル済みスクリプトオブジェクト。
     */
    CompiledScript compile(String script, String sourceName, int lineno);

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
    
}
