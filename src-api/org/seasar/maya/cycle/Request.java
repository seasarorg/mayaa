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
import java.util.Locale;

/**
 * リクエストレベルのスコープ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Request extends Serializable, AttributeScope, Underlyable {
    
    /**
     * リクエストされたURIパスを取得する。
     * @return リクエストURI。
     */
    String getPath();

    /**
     * リクエストのクエリパラメータの名前一覧。
     * @return クエリパラメータ名（String）を保持するイテレータ。
     */
    Iterator iterateParameterNames();

    /**
     * 指定名のリクエストのクエリパラメータ値を返す。
     * 該当するクエリパラメータ名が無い場合はnullを返す。
     * @param name 指定クエリパラメータ名。
     * @return 指定クエリパラメータ値。
     */
    String[] getParameterValues(String name);

    /**
     * リクエストヘッダの名前一覧。
     * @return ヘッダ名（String）を保持するイテレータ。
     */
    Iterator iterateHeaderNames();

    /**
     * 指定名のリクエストヘッダ値を返す。
     * 該当するヘッダ名が無い場合はnullを返す。
     * @param name 指定ヘッダ名。
     * @return 指定ヘッダの値。
     */
    String[] getHeaderValues(String name);

    /**
     * リクエストのロケールを返す。
     * @return リクエストロケール。
     */
    Locale[] getLocales();
    
    /**
     * セッションの取得。
     * @return セッションオブジェクト。
     */
    Session getSession();
    
}
