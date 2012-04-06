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

import java.io.Serializable;
import java.util.Iterator;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.cycle.scope.SessionScope;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;

/**
 * サービスのライフサイクルオブジェクト。HTTPリクエストの期間、
 * サービスのコンテキストとなる。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ServiceCycle
        extends ParameterAware, Serializable {

    /**
     * アプリケーションレベルスコープ。
     */
    String SCOPE_APPLICATION = "application";

    /**
     * セッションレベルスコープ。
     */
    String SCOPE_SESSION = "session";

    /**
     * リクエストレベルスコープ。
     */
    String SCOPE_REQUEST = "request";

    /**
     * ページレベルスコープ。
     */
    String SCOPE_PAGE = "page";

    /**
     * フォワードを行う。
     * @param forwardPath パス文字列。
     */
    void forward(String forwardPath);

    /**
     * リダイレクトを行う。
     * @param url リダイレクトを行うURL文字列。
     */
    void redirect(String url);

    /**
     * エラーレスポンスを返す。
     * @param errorCode エラーコード。
     */
    void error(int errorCode);

    /**
     * メッセージありのエラーレスポンスを返す。
     * @param errorCode エラーコード。
     * @param message エラーメッセージ。
     */
    void error(int errorCode, String message);

    /**
     * カレントのページスコープにてスクリプトを読み込み、実行する。
     * ソースエンコーディングはUTF-8とする。
     * @param systemID スクリプトソースのSystemID。
     */
    void load(String systemID);

    /**
     * カレントのページスコープにてスクリプトを読み込み、実行する。
     * @param systemID スクリプトソースのSystemID。
     * @param encoding ソースエンコーディング。
     */
    void load(String systemID, String encoding);

    /**
     * 例外をthrow。
     * レンダリング中にJavaの例外をthrowしたい場合に利用する。
     * @param t 投げる例外。
     * @throws Throwable
     */
    void throwJava(Throwable t) throws Throwable;

    /**
     * アプリケーションスコープオブジェクトの取得。
     * @return アプリケーション。
     */
    ApplicationScope getApplicationScope();

    /**
     * リクエストオブジェクトの取得。
     * @return リクエスト。
     */
    RequestScope getRequestScope();

    /**
     * セッションの取得。
     * @return セッションオブジェクト。
     */
    SessionScope getSessionScope();

    /**
     * レスポンスオブジェクトの取得。
     * @return レスポンス。
     */
    Response getResponse();

    /**
     * 「page」スコープオブジェクトの設定。
     * @param page 「page」スコープ。
     */
    void setPageScope(AttributeScope page);

    /**
     * 「page」スコープオブジェクトの取得。
     * @return 「page」スコープ。
     */
    AttributeScope getPageScope();

    /**
     * 指定スコープを保持しているかをテストする。
     * @param scopeName 指定スコープ名。
     * @return テスト結果。指定スコープを保持しているときtrue。
     */
    boolean hasAttributeScope(String scopeName);

    /**
     * 指定スコープを取得する。
     * @param scopeName 指定スコープ名。
     * @return 指定スコープ。スコープが無い場合、例外。
     */
    AttributeScope getAttributeScope(String scopeName);

    /**
     * スコープをイテレートする。
     * @return スコープ（AttributeScope）のイテレータ。
     */
    Iterator iterateAttributeScope();

    /**
     * 現在処理中のテンプレート上ノード情報を設定する。
     * @param node テンプレートノード。
     */
    void setOriginalNode(NodeTreeWalker node);

    /**
     * 現在処理中のテンプレート上ノード情報の取得。
     * @return テンプレートノード。
     */
    NodeTreeWalker getOriginalNode();

    /**
     * 現在処理中のインジェクションされたノード情報の設定。
     * @param node インジェクトされたノード。
     */
    void setInjectedNode(NodeTreeWalker node);

    /**
     * 現在処理中のインジェクションされたノード情報の取得。
     * @return インジェクトされたノード。
     */
    NodeTreeWalker getInjectedNode();

    /**
     * 現在処理中のプロセッサの設定。
     * @param processor 処理中のプロセッサ。
     */
    void setProcessor(ProcessorTreeWalker processor);

    /**
     * 現在処理中のプロセッサの取得。
     * @return 処理中のプロセッサ。もしくはnull。
     */
    ProcessorTreeWalker getProcessor();

    /**
     * エラーハンドル時に参照する、発生した例外情報の取得。
     * @param t 発生した例外。
     */
    void setHandledError(Throwable t);

    /**
     * エラーハンドル時に、発生した例外情報を参照する。
     * @return 発生した例外。もしくはnull。
     */
    Throwable getHandledError();

    /**
     * デバッグモードかどうかを返す。
     * @return デバッグモードなら{@code true}、そうでなければ{@code false}。
     */
    boolean isDebugMode();

}
