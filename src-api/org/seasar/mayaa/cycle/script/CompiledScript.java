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

import java.io.Serializable;

/**
 * コンパイル済みのスクリプトオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface CompiledScript
        extends Serializable, ExpectedClassAware {

    /**
     * コンパイル可能およびリテラルを問わず、入力テキストを取得する。
     * @return 入力テキスト。
     */
    String getScriptText();

    /**
     * リテラルテキストかどうか。
     * @return コンパイル結果が、リテラルだったらtrue。
     */
    boolean isLiteral();

    /**
     * スクリプトを実行して値を取得する。
     * @param args nullもしくは、実行時引数（JSF等ミドルウェア対応）。
     * @return 実行結果の値。
     */
    Object execute(Object[] args);

    /**
     * JSF等の式言語様式実行ミドルウェア対応。
     * 式言語様式のメソッドコールのための引数型を設定する。
     * @param methodArgClasses メソッド引数型配列。
     */
    void setMethodArgClasses(Class[] methodArgClasses);

    /**
     * JSF等の式言語様式実行ミドルウェア対応。
     * 式言語様式のメソッドコールのための引数型を取得する。
     * @return メソッド引数型配列。
     */
    Class[] getMethodArgClasses();

    /**
     * JSF等の式言語様式実行ミドルウェア対応。
     * スクリプトブロックの状態を調べ、読み取りのみかどうかを返す。
     * @return コンパイル結果が、値設定可能であればtrue。
     */
    boolean isReadOnly();

    /**
     * JSF等ミドルウェア対応機能。
     * スクリプトを実行して値設定する。
     * @param value 設定する値。
     */
    void assignValue(Object value);

}
