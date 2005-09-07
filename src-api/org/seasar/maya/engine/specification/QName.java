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
 * 名前空間URIとローカル名の組み合わせを保持する識別オブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface QName {

    /**
     * 名前空間URIの取得。
     * @return 名前空間URI。
     */
    String getNamespaceURI();

    /**
     * ローカル名の取得。
     * @return ローカル名。
     */
    String getLocalName();

}
