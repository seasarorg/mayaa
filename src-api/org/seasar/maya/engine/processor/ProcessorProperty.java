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
package org.seasar.maya.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.specification.QName;

/**
 * プロセッサに設定するプロパティのランタイムオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ProcessorProperty {
    
    /**
     * プロセッサにセットすべきプロパティとテンプレートや設定XML上の属性とのバインディング名。
     * @return バインディング名。
     */
    QName getQName();

    /**
     * 名前空間URIにマッピングされたプレフィックス文字列の取得。
     * @return プレフィックス文字列。
     */
    String getPrefix();
    
    /**
     * 静的なプロパティ値取得を行う。
     * @return プロパティ値。
     */
    String getLiteral();
    
    /**
     * 動的値かどうかを返す。
     * @return trueだと動的値。
     */
    boolean isDynamic();
    
    /**
     * プロセッサの実行時に、プロパティ値取得を行う。
     * @param cycle サービスサイクルコンテキスト。
     * @return プロパティ値。
     */
    Object getValue(ServiceCycle cycle);
 
    /**
     * プロセッサの実行時にモデルへの値設定を行う。
     * @param cycle サービスサイクルコンテキスト。
     * @param value 設定値。
     */
    void setValue(ServiceCycle cycle, Object value);
    
}
