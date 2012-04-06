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
package org.seasar.mayaa.engine;

import java.util.Map;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.engine.error.ErrorHandler;
import org.seasar.mayaa.engine.specification.Specification;

/**
 * ランタイムエンジン。ホストサーブレットからservice()が呼び出される。
 * リクエストに対して、ステートレスに実装。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Engine extends ParameterAware, Specification {

    /**
     * エラーハンドラの設定。
     * @param errorHandler エラーハンドラ。
     */
    void setErrorHandler(ErrorHandler errorHandler);

    /**
     * エラーハンドラの取得。
     * @return エラーハンドラ。
     */
    ErrorHandler getErrorHandler();

    /**
     * ページやテンプレートなどのスペック情報のインスタンスが
     * メモリキャッシュに存在する場合は返却する。
     * @param systemID システムID
     * @return スペック情報インスタンス
     */
    Specification findSpecificationFromCache(String systemID);

    /**
     * Pageオブジェクトのインスタンスを返す。
     * @param pageName ページ名。
     * @return Pageオブジェクト。
     */
    Page getPage(String pageName);

    /**
     * レンダリング可能なページを示すリクエストかどうかを判定する。
     * @return mayaaがレンダリング対象とする場合はtrue
     */
    boolean isPageRequested();

    /**
     * サービスメソッド。
     * @param pageScopeValues PAGEスコープのトップに含めるもの。
     * @param pageFlush テンプレート出力を自動でフラッシュするかどうか。
     */
    void doService(Map pageScopeValues, boolean pageFlush);

    /**
     * エラーハンドルページの表示。
     * @param t 発生した例外。
     * @param pageFlush テンプレート出力を自動でフラッシュするかどうか。
     */
    void handleError(Throwable t, boolean pageFlush);

    /**
     * ページのインスタンスを生成しソースビルドを行ってから返す。
     * @param pageName ページ名
     * @return ページ
     */
    Page createPageInstance(String pageName);

    /**
     * テンプレートのインスタンスを生成しソースビルドを行ってから返す。
     * @param page ページ
     * @param suffix テンプレートの接尾子。
     * @param extension ページの拡張子。
     * @return テンプレート
     */
    Template createTemplateInstance(Page page, String suffix, String extension);

    /**
     * テンプレートの示すシステムIDを返す。
     * @param page 属するページ
     * @param suffix サフィックス
     * @param extension 拡張子
     * @return テンプレートのシステムID
     */
    String getTemplateID(Page page, String suffix, String extension);

    /**
     * Engineを破棄します。
     * destroy()が呼ばれた後のEngineの動作は保証されません。
     */
    void destroy();

}
