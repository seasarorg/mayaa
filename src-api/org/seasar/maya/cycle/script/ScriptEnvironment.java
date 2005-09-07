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

import org.seasar.maya.provider.Parameterizable;
import org.seasar.maya.source.SourceDescriptor;

/**
 * スクリプトの実行環境。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ScriptEnvironment extends Parameterizable {
    
    /**
     * ユーザー定義スコープオブジェクトをイテレートする。
     * @return スコープオブジェクト（AttributeScope）のイテレータ。
     */
    Iterator iterateAttributeScope();

    /**
     * スクリプト文字列ブロックの開きクオートに前置される識別文字列の設定。
     * デフォルトでは、JSP仕様と同じく、「$」。
     * @param blockSign 開きクオート前置文字列。
     */
    void setBlockSign(String blockSign);
    
    /**
     * 式文字列をコンパイルする。
     * @param script スクリプト。
     * @param sourceName ソースファイル名。
     * @param lineno 開始行番号。
     * @return コンパイル済みスクリプトオブジェクト。
     */
    CompiledScript compile(String script, String sourceName, int lineno);

    /**
     * ソースがコンパイル可能かどうかを返す。
     * @param source テストするソース。
     * @return コンパイル可能であれば、true。
     */
    boolean canCompile(SourceDescriptor source);

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
     * カレントServiceCycleのページスコープに、スクリプトのスコープオブジェクトを
     * プッシュする。
     * @param model モデルオブジェクト、もしくはnull。
     */
    void startScope(Object model);
    
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
