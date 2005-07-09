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

    String SCOPE_IMPLICIT = "implicit";
    String SCOPE_APPLICATION = "application";
    String SCOPE_SESSION = "session";
    String SCOPE_REQUEST = "request";
    String SCOPE_PAGE = "page";
    
    /**
     * 「application」スコープオブジェクトの取得。
     * @return 「application」スコープ。 
     */
    Application getApplication();
    
    /**
     * 「session」スコープオブジェクトの取得。
     * @return 「session」スコープ。
     */
    Session getSession();
    
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
     * @return 名前（String）の入ったイテレータ。
     */
    Iterator iterateAttributeNames(String scope);
    
    /**
     * 指定スコープ中より指定名のオブジェクトを取得する。
     * 該当するものが無い場合、nullを返す。
     * @param name 指定オブジェクト名。
     * @param scope 指定スコープ名。
     * @return 該当オブジェクトもしくはnull。
     */
    Object getAttribute(String name, String scope);
    
    /**
     * 指定スコープ中に、名前を指定してオブジェクトを保存する。
     * @param name 指定オブジェクト名。
     * @param attribute 保存オブジェクト。
     * @param scope 指定スコープ名。
     */
    void setAttribute(String name, Object attribute, String scope);
    
}
