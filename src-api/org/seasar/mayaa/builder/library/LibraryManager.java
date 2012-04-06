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
import org.seasar.mayaa.builder.library.scanner.SourceScanner;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.URI;

/**
 * MLDモデルオブジェクトのルート。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface LibraryManager extends ParameterAware {

    /**
     * プロパティ型コンバータの追加。
     * @param name コンバータ名、もしくはnullや空白文字列。
     * @param propertyConverter コンバータ。
     */
    void addPropertyConverter(
            String name, PropertyConverter propertyConverter);

    /**
     * プロパティ型コンバータの取得。
     * @param converterName コンバータ登録名。
     * @return 指定名のコンバータ、もしくはnull。
     */
    PropertyConverter getPropertyConverter(String converterName);

    /**
     * プロパティ型コンバータの取得。
     * @param propertyClass プロパティ型。
     * @return コンバータ。もしくはnull。
     */
    PropertyConverter getPropertyConverter(Class propertyClass);

    /**
     * プロパティ型コンバータのイテレータ。
     * @return コンバータイテレータ。
     */
    Iterator iteratePropertyConverters();

    /**
     * ライブラリ定義ソーススキャナの追加。
     * @param scanner ライブラリ定義ソーススキャナ。
     */
    void addSourceScanner(SourceScanner scanner);

    /**
     * ライブラリ定義ビルダの追加。
     * @param builder ライブラリ定義ビルダ。
     */
    void addDefinitionBuilder(DefinitionBuilder builder);

    /**
     * 必要ならライブラリの初期化処理を行う。
     */
    void prepareLibraries();

    /**
     * 全MLD設定（=<code>LibraryDefinition</code>）のイテレータ取得。
     * @return MLD設定のイテレータ。
     */
    Iterator iterateLibraryDefinition();

    /**
     * 指定URIで該当するMLD設定のイテレータ取得。
     * @param namespaceURI 取得したいMLDの名前空間URI。
     * @return MLD設定のイテレータ。
     */
    Iterator iterateLibraryDefinition(URI namespaceURI);

    /**
     * QNameで該当するプロセッサ定義（=<code>ProcessorDefinition</code>）を検索する。
     * 一番はじめに見つかったものを返す。
     * @param qName 取得したいプロセッサ定義の指定QName。
     * @return 指定QNameのプロセッサ定義。見つからない場合はnull。
     */
    ProcessorDefinition getProcessorDefinition(QName qName);

}
