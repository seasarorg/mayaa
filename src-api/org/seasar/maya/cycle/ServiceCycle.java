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

/**
 * サービスのライフサイクルオブジェクト。HTTPリクエストの期間、サービスのコンテキストとなる。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ServiceCycle extends Serializable, AttributeScope {

    String SERVICE_CYCLE = "serviceCycle";
    String PARAM = "param";
    String PARAM_VALUES = "paramValues";
    String HEADER = "header";
    String HEADER_VALUES = "headerValues";

    String SCOPE_IMPLICIT = "implicit";
    String SCOPE_PAGE = "page";
    String SCOPE_REQUEST = "request";
    String SCOPE_SESSION = "session";
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
     * 指定スコープ中に保存されている名前をイテレートする。
     * @param scope 指定スコープ名。
     * @return 名前（String）の入ったイテレータ。nullもしくは空白文字列のときは"page"スコープ。
     */
    Iterator iterateAttributeNames(String scope);
    
    /**
     * 指定スコープ中より指定名のオブジェクトを取得する。
     * 該当するものが無い場合、nullを返す。
     * @param name 指定オブジェクト名。
     * @param scope 指定スコープ名。
     * @return 該当オブジェクトもしくはnull。nullもしくは空白文字列のときは"page"スコープ。
     */
    Object getAttribute(String name, String scope);
    
    /**
     * 指定スコープ中に、名前を指定してオブジェクトを保存する。
     * @param name 指定オブジェクト名。
     * @param attribute 保存オブジェクト。
     * @param scope 指定スコープ名。nullもしくは空白文字列のときは"page"スコープ。
     */
    void setAttribute(String name, Object attribute, String scope);

    /**
     * 渡されたURL文字列に、必要であればセッションIDを付加する。
     * @param url URL文字列。
     * @return セッションIDを付加した文字列。 
     */
    String encodeURL(String url);

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
     * インクルードを行う。
     * @param relativeUrlPath パス文字列。
     */
    void include(String relativeUrlPath);
    
}
