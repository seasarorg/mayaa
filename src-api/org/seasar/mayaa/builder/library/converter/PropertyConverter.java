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
package org.seasar.mayaa.builder.library.converter;

import java.io.Serializable;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.engine.specification.NodeAttribute;

/**
 * プロセッサ設定のプロパティ値を変換提供するコンバータ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface PropertyConverter extends ParameterAware {

    /**
     * このコンバータが処理を行うプロパティの静的型を取得する。
     * @return プロパティ型。
     */
    Class getPropetyClass();

    /**
     * プロパティの変換を行う。
     * @param attribute 設定属性。
     * @param value 変換する文字列値。
     * @param expectedClass 動的値の場合に期待する動的型。
     * @return 変換値。
     */
    Serializable convert(NodeAttribute attribute, String value, Class expectedClass);

}
