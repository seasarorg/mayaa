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
package org.seasar.mayaa.provider;

import java.io.Serializable;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.builder.PathAdjuster;
import org.seasar.mayaa.builder.SpecificationBuilder;
import org.seasar.mayaa.builder.TemplateBuilder;
import org.seasar.mayaa.builder.library.LibraryManager;
import org.seasar.mayaa.builder.library.TemplateAttributeReader;
import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.specification.ParentSpecificationResolver;

/**
 * アプリケーションスコープでのサービス提供オブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ServiceProvider extends ParameterAware, Serializable {

    /**
     * エンジンを設定する。
     * @param engine エンジン。
     */
    void setEngine(Engine engine);

    /**
     * エンジンを取得する。
     * @return エンジン
     */
    Engine getEngine();

    /**
     * スクリプト実行環境の設定。
     * @param environment スクリプト実行環境。
     */
    void setScriptEnvironment(ScriptEnvironment environment);

    /**
     * スクリプト実行環境の取得。
     * @return スクリプト実行環境。
     */
    ScriptEnvironment getScriptEnvironment();

    /**
     * ライブラリマネージャを設定する。
     * @param libraryManager ライブラリマネージャ。
     */
    void setLibraryManager(LibraryManager libraryManager);

    /**
     * ライブラリマネージャを取得する。
     * @return ライブラリマネージャ。
     */
    LibraryManager getLibraryManager();

    /**
     * 設定XMLのビルダを設定する。
     * @param specificationBuilder 設定XMLビルダ。
     */
    void setSpecificationBuilder(SpecificationBuilder specificationBuilder);

    /**
     * 設定XMLのビルダを取得する。
     * @return 設定XMLビルダ。
     */
    SpecificationBuilder getSpecificationBuilder();

    /**
     * HTMLテンプレートファイルのビルダを設定する。
     * @param templateBuilder テンプレートビルダ。
     */
    void setTemplateBuilder(TemplateBuilder templateBuilder);

    /**
     * HTMLテンプレートファイルのビルダを取得する。
     * @return テンプレートビルダ。
     */
    TemplateBuilder getTemplateBuilder();

    /**
     * HTMLタグ属性の相対パスを絶対パスに置換するオブジェクトを設定する。
     * @param pathAdjuster パスアジャスタ。
     */
    void setPathAdjuster(PathAdjuster pathAdjuster);

    /**
     * HTMLタグ属性の相対パスを絶対パスに置換するオブジェクトを取得する。
     * @return パスアジャスタ。
     */
    PathAdjuster getPathAdjuster();

    /**
     * テンプレートの属性を取得するオブジェクトを設定する。
     * @param templateAttributeReader テンプレートの属性を取得するオブジェクト。
     */
    void setTemplateAttributeReader(TemplateAttributeReader templateAttributeReader);

    /**
     * テンプレートの属性を取得するオブジェクトを取得する。
     * @return テンプレートの属性を取得するオブジェクト。
     */
    TemplateAttributeReader getTemplateAttributeReader();

    /**
     * Specificationの親を取得するオブジェクトを設定する。
     * @param parentSpecificationResolver Specificationの親を取得するオブジェクト。
     */
    void setParentSpecificationResolver(ParentSpecificationResolver parentSpecificationResolver);

    /**
     * Specificationの親を取得するオブジェクトを取得する。
     * @return Specificationの親を取得するオブジェクト。
     */
    ParentSpecificationResolver getParentSpecificationResolver();

}
