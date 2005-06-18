/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.impl.el;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.seasar.maya.impl.util.StringUtil;

/**
 * 式言語ブロックを式文字列から切り出すイテレータ。
 * イテレータの中身は、ExpressionBlock。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ExpressionBlockIterator implements Iterator {

	/**
	 * JSPライクな評価式ブロックの始まり文字列。
	 */
    public static final String BLOCK_START_JSP = "${";

	/**
	 * JSPライクな評価式ブロックの終わり文字列。
	 */
    public static final String BLOCK_END_JSP = "}";
	
	/**
	 * JSFライクな評価式ブロックの始まり文字列。
	 */
    public static final String BLOCK_START_JSF = "#{";

	/**
	 * JSFライクな評価式ブロックの終わり文字列。
	 */
    public static final String BLOCK_END_JSF = "}";

	private String _expression;
	private String _blockStart;
	private String _blockEnd;
	private int _offset;

	/**
	 * @param expression 式文字列。
	 * @param blockStart 評価式のブロックの始まりを示す文字列。
	 * @param blockEnd 評価式のブロックの終わりを示す文字列。
	 */
	public ExpressionBlockIterator(String expression, 
	        String blockStart, String blockEnd) {
		if (StringUtil.isEmpty(expression) || 
		        StringUtil.isEmpty(blockStart) || StringUtil.isEmpty(blockEnd)) {
			throw new IllegalArgumentException();
		}
		_expression = expression;
		_blockStart = blockStart;
		_blockEnd = blockEnd;
		_offset = 0;
	}

	public boolean hasNext() {
		return _offset < _expression.length();
	}

	public Object next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		int start = _expression.indexOf(_blockStart, _offset);
		int end = _expression.indexOf(_blockEnd, _offset);
		if (start == -1) {
			String lastLiteralBlock = _expression.substring(_offset);
			_offset = _expression.length();
			return new ExpressionBlock(lastLiteralBlock, true);
		} else if (start == _offset) {
			if (end == -1) {
				throw new UnbalancedBraceException(_expression, _expression.length());
			}
			String expressionBlock = _expression.substring(_offset + _blockStart.length(), end);
			_offset = end + _blockEnd.length();
			return new ExpressionBlock(expressionBlock.trim(), false);
		} else if(end < start) {
			int errorOffset = start != -1 ? start : end;
			throw new UnbalancedBraceException(_expression, errorOffset);
		}
		String literalBlock = _expression.substring(_offset, start);
		_offset = start;
		return new ExpressionBlock(literalBlock, true);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}