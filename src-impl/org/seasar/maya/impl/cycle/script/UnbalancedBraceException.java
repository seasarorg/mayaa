/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.cycle.script;

/**
 * 式ブロックの開き・閉じのバランスが悪いときの例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class UnbalancedBraceException extends ScriptException {

	private static final long serialVersionUID = 9098125536269480736L;

	private String _expression;
	private int _offset;
	
	/**
	 * @param expression 問題の式文字列。
	 * @param offset アンバランスの発生箇所。
	 */
	public UnbalancedBraceException(String expression, int offset) {
		_expression = expression;
		_offset = offset;
	}
	
	/**
	 * 式文字列の取得。
	 * @return 式文字列。
	 */
	public String getExpression() {
		return _expression;
	}
	
	/**
	 * アンバランス発生箇所の取得。
	 * @return 発生箇所オフセット。
	 */
	public int getOffset() {
		return _offset;
	}
	
}
