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

import org.seasar.maya.cycle.script.resolver.ScriptResolver;
import org.seasar.maya.provider.Parameterizable;

/**
 * スクリプトコンパイラ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ScriptCompiler extends Parameterizable {

    /**
     * ルートのスクリプトレゾルバの取得。
     * @return スクリプトレゾルバ。
     */
    ScriptResolver getScriptResolver();
    
    /**
     * スクリプト文字列ブロックの開きクオートに前置される識別文字列の設定。
     * デフォルトでは、JSP仕様と同じく、「$」。
     * @param blockSign 開きクオート前置文字列。
     */
    void setBlockSign(String blockSign);
    
    /**
     * 式文字列をコンパイルする。
     * @param script スクリプト。
     * @param expectedType 期待する結果型。Void.classの場合、リターンはなし。
     * @return コンパイル済みスクリプトオブジェクト。
     */
    CompiledScript compile(String script, Class expectedType);

}
