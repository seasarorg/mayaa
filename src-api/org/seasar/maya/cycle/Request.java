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
import java.util.Locale;

/**
 * リクエストレベルのスコープ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Request extends Serializable, AttributeScope, Underlyable {

	/**
	 * リクエストされたパス文字列を取得する。
	 * @return パス文字列。
	 */
	String getRequestedPath();
	
    /**
     * リクエストされたページ名を取得する。
     * @return リクエストページ名。
     */
    String getPageName();

    /**
     * リクエストで強制されるページ接尾辞を取得する。
     * @return リクエスト接尾辞、もしくはnull。
     */
    String getRequestedSuffix();
    
    /**
     * リクエストされたページ拡張子を取得する。
     * @return リクエスト拡張子。
     */
    String getExtension();
    
    /**
     * リクエストパラメータを含むスコープを取得する。内包するオブジェクトはStringの配列。
     * @return クエリパラメータスコープ。
     */
    AttributeScope getParamValues();

    /**
     * リクエストヘッダを含むスコープを取得する。内包するオブジェクトはStringの配列。
     * @return クエリパラメータスコープ。
     */
    AttributeScope getHeaderValues();

    /**
     * リクエストのロケールを返す。
     * @return リクエストロケール。
     */
    Locale[] getLocales();

    /**
     * フォワード先のパスを設定する。
     * @param relativeUrlPath コンテキスト相対のパス。
     */
    void setForwardPath(String relativeUrlPath);
    
}
