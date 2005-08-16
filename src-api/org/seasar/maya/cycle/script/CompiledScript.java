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
package org.seasar.maya.cycle.script;

import java.io.Serializable;

/**
 * コンパイル済みのスクリプトオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface CompiledScript extends Serializable {

    /**
     * スクリプトを実行して値を取得する。
     * @param root スクリプト実行環境の、ルートスコープオブジェクト。
     * @return 実行結果の値。
     */
    Object execute(Object root);
    
    /**
     * スクリプトの取得。
     * @return スクリプト文字列。
     */
    String getText();
    
    /**
     * スクリプト実行結果への期待型。
     * @return 期待されるクラス型。
     */
    Class getExpectedType();
 
    /**
     * リテラルテキストかどうか。
     * @return コンパイル結果が、リテラルだったらtrue。
     */
    boolean isLiteral();
    
}
