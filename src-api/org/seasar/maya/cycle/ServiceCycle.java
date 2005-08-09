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

import org.seasar.maya.engine.specification.SpecificationNode;

/**
 * サービスのライフサイクルオブジェクト。HTTPリクエストの期間、サービスのコンテキストとなる。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ServiceCycle extends Serializable, AttributeScope {

    /**
     * 組み込みオブジェクト取得のための特別スコープ。
     */
    String SCOPE_IMPLICIT = "implicit";
    
    /**
     * ページレベルスコープ。 
     */
    String SCOPE_PAGE = "page";
    
    /**
     * リクエストレベルスコープ。
     */
    String SCOPE_REQUEST = "request";
    
    /**
     * セッションレベルスコープ。
     */
    String SCOPE_SESSION = "session";
    
    /**
     * アプリケーションレベルスコープ。
     */
    String SCOPE_APPLICATION = "application";
    
    /**
     * 「application」スコープオブジェクトの取得。
     * @return 「application」スコープ。 
     */
    Application getApplication();
    
    /**
     * 「request」スコープオブジェクトの取得。
     * @return 「request」スコープ。
     */
    Request getRequest();
    
    /**
     * レスポンスオブジェクトの取得。
     * @return レスポンス。
     */
    Response getResponse();
    
    /**
     * 指定スコープが存在するかをテストする。
     * @param scope スコープ名もしくはnull。nullの場合は「page」とする。
     * @return 指定スコープの存在テスト結果。
     */
    boolean hasAttributeScope(String scope);
    
    /**
     * スコープ空間オブジェクトの取得。
     * @param scope スコープ名もしくはnull。nullの場合は「page」とする。
     * @return スコープ空間オブジェクト。
     */
    AttributeScope getAttributeScope(String scope);
    
    /**
     * スコープ空間オブジェクトの追加。エンジンカスタマイズ用のAPI。
     * @param scope スコープ名。非nullのみ許される。
     * @param attrScope スコープ空間オブジェクト。
     */
    void putAttributeScope(String scope, AttributeScope attrScope);

    /**
     * ページスコープ空間をリセットする。
     */
    void resetPageScope();
    
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
     * 現在処理中のノードを設定する。例外情報やデバッグなどで開発者に提供する情報。
     * @param node 処理中のノード。
     */    
    void setCurrentNode(SpecificationNode node);
    
    /**
     * 現在処理中のノードを取得する。例外情報やデバッグなどで開発者に提供する情報。
     * @return 処理中のノード。
     */
    SpecificationNode getCurrentNode();
    
}
