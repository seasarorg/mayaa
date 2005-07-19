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
 * QNameを取得できるオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface QNameable extends Namespaceable {

    /**
     * 名前空間URIおよびローカル名の組み合わせの取得。
     * @return 名前空間URIおよびローカル名の組み合わせ。 
     */
    QName getQName();

    /**
     * テンプレートや設定XMLに記述されているプレフィックスを取得。
     * @return プレフィックス文字列もしくはnull。
     */
    String getPrefix();
    
}
