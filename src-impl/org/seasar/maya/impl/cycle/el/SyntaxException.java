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
package org.seasar.maya.impl.cycle.el;

/**
 * 式コンパイル時の文法例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SyntaxException extends ExpressionException {

	private static final long serialVersionUID = 4167069503212250014L;

	private String _expression;
    
    /**
     * @param expression 式文字列。
     */
    public SyntaxException(String expression) {
        _expression = expression;
    }
    
    /**
     * 入力式文字列の取得。
     * @return 式文字列。
     */
    public String getExpression() {
        return _expression;
    }
    
}
