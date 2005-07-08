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
 * 名前つきでオブジェクトを保存できる「スコープ」概念インターフェイス。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface AttributeScope {

    /**
     * スコープ名の取得を行う補助メソッド。
     * @return スコープ名。
     */
    String getScopeName();
    
    /**
     * このスコープ中から指定された名前に対応したオブジェクトを返す。
     * 該当オブジェクトが無い場合には、nullを返す。
     * @param name 指定オブジェクト名。
     * @return 指定オブジェクト。
     */
    Object getAttribute(String name);
    
    /**
     * このスコープ中に、指定名でオブジェクトを保存する。
     * @param name 指定名。空白文字列だと例外。
     * @param attribute 指定オブジェクト。nullの場合は、スコープより削除される。
     */
    void setAttribute(String name, Object attribute);

}
