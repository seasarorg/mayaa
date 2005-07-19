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
package org.seasar.maya.cycle.el;

import java.io.Serializable;

import org.seasar.maya.cycle.ServiceCycle;

/**
 * コンパイル済みの式言語オブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface CompiledExpression extends Serializable {

    /**
     * 式を実行して値を取得する。
     * @param cycle サービスサイクルコンテキスト。
     * @return 実行結果の値。
     */
    Object getValue(ServiceCycle cycle);
    
    /**
     * 式により、値を設定する
     * @param cycle サービスサイクルコンテキスト。
     * @param value 設定する値。
     */
    void setValue(ServiceCycle cycle, Object value);

    /**
     * 式の取得。
     * @return 式文字列。
     */
    String getExpression();
    
    /**
     * 式実行結果への期待型。
     * @return 期待されるクラス型。
     */
    Class getExpectedType();
 
    /**
     * リテラルテキストかどうか。
     * @return コンパイル結果が、リテラルだったらtrue。
     */
    boolean isLiteralText();
    
}
