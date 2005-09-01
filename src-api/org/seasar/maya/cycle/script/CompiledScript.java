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
     * @return 実行結果の値。
     */
    Object execute();
    
    /**
     * スクリプト実行結果への期待型の取得。
     * @return 実行結果に期待されるクラス型。
     */
    Class getExpectedType();

    /**
     * スクリプト実行結果への期待型の設定。
     * @param expectedType 実行結果に期待するクラス型。
     */
    void setExpectedType(Class expectedType);
    
    /**
     * リテラルテキストかどうか。
     * @return コンパイル結果が、リテラルだったらtrue。
     */
    boolean isLiteral();
    
    /**
     * スクリプトソースコードの取得。ソースが外部ファイルの場合は、SystemID。
     * @return スクリプトソースコードもしくは外部ソースコードファイルのSystemID。
     */
    String getScript();
    
}
