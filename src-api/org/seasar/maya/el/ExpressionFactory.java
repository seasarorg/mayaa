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
package org.seasar.maya.el;

import org.seasar.maya.el.resolver.ExpressionResolver;
import org.seasar.maya.provider.Parameterizable;

/**
 * Expressionのファクトリ。式をコンパイルする。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ExpressionFactory extends Parameterizable {

    /**
     * ルートの式レゾルバの取得。
     * @return 式レゾルバ。
     */
    ExpressionResolver getExpressionResolver();
    
    /**
     * 式言語文字列の開きクオート文字列の設定。
     * @param blockStart 開きクオート文字列。
     */
    void setBlockStart(String blockStart);
    
    /**
     * 式言語文字列の閉じクオート文字列の設定。
     * @param blockEnd 閉じクオート文字列。
     */
    void setBlockEnd(String blockEnd);
    
    /**
     * 式文字列をコンパイルする。
     * @param expression 式文字列。
     * @param expectedType 期待する結果型。Void.classの場合、リターンはnull。
     * @return コンパイル済み式オブジェクト。
     */
    CompiledExpression createExpression(String expression, Class expectedType);

}
