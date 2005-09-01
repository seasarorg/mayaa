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

import org.seasar.maya.engine.specification.SpecificationNode;

/**
 * サービスのライフサイクルオブジェクト。HTTPリクエストの期間、サービスのコンテキストとなる。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ServiceCycle extends Serializable {
    
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
    void setOriginalNode(SpecificationNode node);
    
    /**
     * 現在処理中のテンプレート上ノード情報の取得。
     * @return テンプレートノード。
     */
    SpecificationNode getOriginalNode();
    
    /**
     * 現在処理中のインジェクションされたノード情報の設定。
     * @param node インジェクトされたノード。
     */
    void setInjectedNode(SpecificationNode node);
    
    /**
     * 現在処理中のインジェクションされたノード情報の取得。
     * @return インジェクトされたノード。
     */    
    SpecificationNode getInjectedNode();
    
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
   
}
