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
package org.seasar.maya.el.resolver;

import org.seasar.maya.cycle.ServiceCycle;

/**
 * 式評価リゾルバチェイン。アプリケーションスコープにて共有されるので、
 * スレッドセーフに実装することが求められる。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ExpressionChain {

    /**
     * 値の取得のためのチェインメソッド。
     * @param cycle サービスサイクルコンテキスト。
     * @param base 評価のベースとなるオブジェクト。
     * @param property 値取得するプロパティ。文字列もしくはインデックス値。
     * @return 評価結果。
     */
    Object getValue(ServiceCycle cycle, Object base, Object property);

    /**
     * 値設定のためのチェインメソッド。
     * @param cycle サービスサイクルコンテキスト。
     * @param base 評価のベースとなるオブジェクト。
     * @param property 値設定するプロパティ。文字列もしくはインデックス値。
     * @param value 設定値。
     */
    void setValue(ServiceCycle cycle, Object base, Object property, Object value);
    
}
