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

import org.seasar.mayaa.ContextAware;

/**
 * WEBアプリケーション全体で共有されるスコープ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ApplicationScope extends AttributeScope, ContextAware {

    /**
     * MIME型をSystemIDから類推して返す。
     * @param systemID テストするSysteｍID。
     * @return MIME型。
     */
    String getMimeType(String systemID);

    /**
     * リクエストURIのコンテキスト相対パスを、OS上のファイルパスに変換する。
     * @param contextRelatedPath コンテキスト相対パス。
     * @return OS上のファイルパス。
     */
    String getRealPath(String contextRelatedPath);

}
