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
package org.seasar.maya.impl.el;

/**
 * 式中もしくは式実行結果の型変換時の例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ConversionException extends ExpressionException {

	private static final long serialVersionUID = -485430528770416298L;

	private Class _expectedType;
    private Object _expressed;
    
    /**
     * @param expectedType 期待された型。
     * @param expressed 式実行結果型。
     */
    public ConversionException(Class expectedType, Object expressed) {
        _expectedType = expectedType;
        _expressed = expressed;
    }
    
    /**
     * あらかじめ登録した期待の型を取得する。
     * @return 期待された型。
     */
    public Class getExpectedType() {
        return _expectedType;
    }
    
    /**
     * 式実行結果オブジェクトの取得。
     * @return 式実行結果。
     */
    public Object getExpressed() {
        return _expressed;
    }
    
}
