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
package org.seasar.mayaa.cycle.scope;

import java.util.Iterator;

import org.seasar.mayaa.ParameterAware;

/**
 * 名前つきでオブジェクトを保存できる「スコープ」概念インターフェイス。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface AttributeScope extends ParameterAware {

    /**
     * スコープ識別の取得を行う補助メソッド。
     * @return スコープ識別。
     */
    String getScopeName();

    /**
     * このスコープに保存されている名前をイテレートする。
     * @return 名前（String）の入ったイテレータ。
     */
    Iterator iterateAttributeNames();

    /**
     * このスコープ中に指定された名前に対応したオブジェクトがあるかをテストする。
     * @param name 指定オブジェクト名。
     * @return テスト結果。
     */
    boolean hasAttribute(String name);

    /**
     * このスコープ中から指定された名前に対応したオブジェクトを返す。
     * 名前文字列がnullもしくは空白文字列、もしくは該当オブジェクトが無い場合には、
     * UNDEFINEDを返す。
     * @param name 指定オブジェクト名。
     * @return 指定オブジェクト。
     */
    Object getAttribute(String name);

    /**
     * このスコープ中にオブジェクトの書き込みおよび削除ができるかを返す。
     * @return テスト結果。
     */
    boolean isAttributeWritable();

    /**
     * このスコープ中に、指定名でオブジェクトを保存する。
     * @param name 指定名。nullおよび空白文字列だと何もしない。
     * @param attribute 指定オブジェクト。
     */
    void setAttribute(String name, Object attribute);

    /**
     * このスコープ中の指定名オブジェクトを削除する。
     * @param name 指定名。nullおよび空白文字列だと何もしない。
     */
    void removeAttribute(String name);

    /**
     * このスコープ中に、指定クラス型のオブジェクトを生成する。
     * すでに同じ名前で違うクラス型のオブジェクトが存在する場合の処理や、
     * 名前やクラス型の引数チェックの仕様は定めない。実装毎の仕様となる。
     * @param name 指定名。
     * @param attributeClass 指定クラス型。
     * @return 指定オブジェクト。
     */
    Object newAttribute(String name, Class attributeClass);

}
