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

/**
 * WEBアプリケーション全体で共有されるスコープ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Application extends AttributeScope, Underlyable {

    /**
     * リクエストURIのコンテキスト相対パスを、OS上のファイルパスに変換する。
     * @param contextRelatedPath コンテキスト相対パス。
     * @return OS上のファイルパス。
     */
    String getRealPath(String contextRelatedPath);
    
}
