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
package org.seasar.mayaa.cycle;

/**
 * リクエストサイクル中で有効な動的変数を制御する。
 * @author Taro Kato (Gluegent, Inc.)
 */
public interface CycleLocalVariables {

    /**
     * サイクル期間内で有効なスレッドローカルなグローバルオブジェクトの取得。
     * @param key 登録キー。
     * @param params 生成に必要なパラメータ。
     * @return オブジェクト。登録キーの示す初期化オブジェクトが登録されていなければエラー。
     */
    Object getGlobalVariable(String key, Object[] params);

    /**
     * サイクル期間内で有効なスレッドローカルなグローバルオブジェクトを無効化する。
     * @param key 登録キー。
     */
    void clearGlobalVariable(String key);

    /**
     * サイクル期間内で有効なスレッドローカルなグローバルオブジェクトへの設定。
     * @param key 登録キー。
     * @param value 設定値。
     */
    void setGlobalVariable(String key, Object value);

    /**
     * サイクル期間内で有効なスレッドローカルなオブジェクトの取得。
     * @param key 登録キー。
     * @param owner 所有者。
     * @param params インスタンスを新規生成する場合のパラメータ。
     * @return オブジェクト。登録キーの示す初期化オブジェクトが登録されていなければエラー。
     */
    Object getVariable(String key, Object owner, Object[] params);

    /**
     * サイクル期間内で有効なスレッドローカルなオブジェクトを無効化する。
     * @param key 登録キー。
     * @param owner 所有者。
     */
    void clearVariable(String key, Object owner);

    /**
     * サイクル期間内で有効なスレッドローカルなオブジェクトへの設定。
     * @param key 登録キー。
     * @param owner 所有者。
     * @param value 設定値。
     */
    void setVariable(String key, Object owner, Object value);

}

