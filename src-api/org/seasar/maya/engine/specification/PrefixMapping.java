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
package org.seasar.maya.engine.specification;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface PrefixMapping extends NodeObject {

    /**
     * 宣言されている名前空間の設定。
     * @param namespace 宣言名前空間。
     */
    void setNamespace(Namespace namespace);
    
    /**
     * 宣言されている名前空間の取得。
     * @return 宣言名前空間。
     */
    Namespace getNamespace();

    /**
     * プレフィックス文字列の取得。
     * @return プレフィックス文字列。
     */
    String getPrefix();
    
    /**
     * 名前空間URIの取得。
     * @return 名前空間URI。
     */
    String getNamespaceURI();
    
}
