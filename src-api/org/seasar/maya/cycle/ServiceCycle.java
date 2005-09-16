/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.cycle;

import java.io.Serializable;
import java.util.Iterator;

import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.engine.specification.NodeTreeWalker;
import org.seasar.maya.provider.Parameterizable;

/**
 * サービスのライフサイクルオブジェクト。HTTPリクエストの期間、サービスのコンテキストとなる。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ServiceCycle extends Parameterizable, Serializable {
    
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
     * @param relativeUrlPath パス文字列。
     */
    void forward(String relativeUrlPath);
    
    /**
     * リダイレクトを行う。
     * @param url リダイレクトを行うURL文字列。
     */
    void redirect(String url);

    /**
     * カレントのページスコープにてスクリプトを読み込み、実行する。
     * @param systemID スクリプトソースのSystemID。
     * @param encoding ソースエンコーディング。
     */
    void load(String systemID, String encoding);
    
    /**
     * アプリケーションスコープオブジェクトの取得。
     * @return アプリケーション。 
     */
    Application getApplication();
    
    /**
     * リクエストオブジェクトの取得。
     * @return リクエスト。
     */
    Request getRequest();
    
    /**
     * セッションの取得。
     * @return セッションオブジェクト。
     */
    Session getSession();
    
    /**
     * レスポンスオブジェクトの取得。
     * @return レスポンス。
     */
    Response getResponse();
    
    /**
     * スコープをイテレートする。
     * @return スコープ（AttributeScope）のイテレータ。
     */
    Iterator iterateAttributeScope();

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
     * 現在処理中の最上位ページの設定。
     * @param page 処理中のページ。
     */
    void setPage(Page page);
    
    /**
     * 現在処理中の最上位ページの取得。
     * @return 処理中のページ。もしくはnull。
     */
    Page getPage();
    
}
