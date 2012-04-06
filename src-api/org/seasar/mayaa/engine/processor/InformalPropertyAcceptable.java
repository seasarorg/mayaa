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
package org.seasar.mayaa.engine.processor;

import java.io.Serializable;

import org.seasar.mayaa.engine.specification.PrefixAwareName;

/**
 * あらかじめ、MLD（Mayaa Library Definition）ファイルに記述されてない
 * プロパティを受け入れる場合のインターフェイス。MLD記述されてるプロパティは
 * このメソッドを経由しないで、直接、Beanプロパティアクセスで設定される。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface InformalPropertyAcceptable
        extends ProcessorTreeWalker {

    /**
     * 非MLDなインフォーマルプロパティの受け入れメソッド。
     * @param name プロパティ名。
     * @param property インフォーマルプロパティ。
     */
    void addInformalProperty(PrefixAwareName name, Serializable property);

    /**
     * addInformalProperty()に渡されるインフォーマルプロパティに期待される型。
     * @return インフォーマルプロパティ型。
     * @deprecated 1.1.9 代わりに{@link #getInformalPropertyClass()}を使用してください。
     */
    Class getPropertyClass();

    /**
     * インフォーマルプロパティの予測される型を取得する。
     * @return インフォーマルプロパティの予測される型。
     * @deprecated 1.1.9 代わりに{@link #getInformalExpectedClass()}を使用してください。
     */
    Class getExpectedClass();

    /**
     * addInformalProperty()に渡されるインフォーマルプロパティに期待される型。
     * @return インフォーマルプロパティ型。
     */
    Class getInformalPropertyClass();

    /**
     * インフォーマルプロパティの予測される型を取得する。
     * @return インフォーマルプロパティの予測される型。
     */
    Class getInformalExpectedClass();

}
