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
package org.seasar.mayaa.builder.library;

import java.util.Iterator;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.builder.library.converter.PropertyConverter;
import org.seasar.mayaa.engine.specification.URI;


/**
 * MLDのlibraryエレメントのモデルオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface LibraryDefinition extends ParameterAware {

    /**
     * 当該ライブラリの名前空間URIを取得する。
     * @return 名前空間URI。
     */
    URI getNamespaceURI();

    /**
     * 当該ライブラリに、名前空間URIを追加アサインする。
     * @param assignedURI 追加アサインする名前空間URI。
     */
    void addAssignedURI(URI assignedURI);

    /**
     * 当該ライブラリに追加アサインされた名前空間URIをイテレートする。
     * @return 追加アサインされた名前空間URIのイテレータ。
     */
    Iterator iterateAssignedURI();

    /**
     * 登録コンバータの取得。
     * @param converterName コンバータ登録名。
     * @return 指定名のコンバータ、もしくはnull。
     */
    PropertyConverter getPropertyConverter(String converterName);

    /**
     * 登録コンバータのイテレータ。
     * @return コンバータイテレータ。
     */
    Iterator iteratePropertyConverters();

    /**
     * 登録コンバータの取得。
     * @param propertyClass コンバート対象型。
     * @return 指定型に対応したコンバータ、もしくはnull。
     */
    PropertyConverter getPropertyConverter(Class propertyClass);

    /**
     * 登録プロパティセットのイテレータ。
     * @return プロパティセットイテレータ。
     */
    Iterator iteratePropertySets();

    /**
     * 登録プロパティセットの取得。
     * @param name プロパティセット名。
     * @return 指定名のプロパティセット、もしくはnull。
     */
    PropertySet getPropertySet(String name);

    /**
     * 子のprocessorノードのイテレータ。
     * @return ノードイテレータ。
     */
    Iterator iterateProcessorDefinitions();

    /**
     * 指定QNameで該当するProcessorDefinitionの取得。
     * @param name 取得したいProcessorDefinitionの名前。
     * @return 該当ProcessorDefinitionもしくはnull。
     */
    ProcessorDefinition getProcessorDefinition(String name);

}
