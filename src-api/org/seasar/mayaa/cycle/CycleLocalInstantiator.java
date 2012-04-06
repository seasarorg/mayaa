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
 * サイクル内スレッドローカルオブジェクト生成。
 * @author Taro Kato (Gluegent, Inc.)
 */
public interface CycleLocalInstantiator {

    /**
     * オブジェクトを生成する。
     * サイクルが有効な間、グローバルな共有オブジェクトとなる。
     * @param params インスタンス作成に必要なパラメータ。
     * @return インスタンス。
     */
    Object create(Object[] params);

    /**
     * あるオブジェクトの所有物としてオブジェクトを生成する。
     * サイクルが有効な間、フィールド的なオブジェクトとなる。
     * @param owner 所有者となるオブジェクト。
     * @param params インスタンス作成に必要なパラメータ。
     * @return インスタンス。
     */
    Object create(Object owner, Object[] params);

    /**
     * オブジェクトを破棄する。
     * @param instance createされたオブジェクト。
     */
    void destroy(Object instance);

}

