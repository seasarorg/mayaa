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
package org.seasar.mayaa.cycle.scope;

import java.io.Serializable;
import java.util.Locale;

import org.seasar.mayaa.ContextAware;

/**
 * リクエストレベルのスコープ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface RequestScope
        extends Serializable, AttributeScope, ContextAware {

    /**
     * WEBアプリケーションコンテキストのパス部を返す。
     * @return コンテキストパス。
     */
    String getContextPath();

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
     * リクエストされたパスより類推できるMIME型を返す。
     * @return リクエストされたパスから類推されるMIME型。
     */
    String getMimeType();

    /**
     * リクエストのロケールを返す。
     * @return リクエストロケール。
     */
    Locale[] getLocales();

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
     * このリクエストは他からforwardされたものかを判定する。
     * @return 他からforwardされたならtrue
     * @since 1.1.8
     */
    boolean isForwarded();

}
