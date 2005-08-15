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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptBlockIterator implements Iterator {

    public static final String BLOCK_START_JSP = "${";
    public static final String BLOCK_END_JSP = "}";
    public static final String BLOCK_START_JSF = "#{";
    public static final String BLOCK_END_JSF = "}";

	private String _script;
	private String _blockStart;
	private String _blockEnd;
	private int _offset;

	public ScriptBlockIterator(String expression, 
	        String blockStart, String blockEnd) {
		if (StringUtil.isEmpty(expression) || 
		        StringUtil.isEmpty(blockStart) || StringUtil.isEmpty(blockEnd)) {
			throw new IllegalArgumentException();
		}
		_script = expression;
		_blockStart = blockStart;
		_blockEnd = blockEnd;
		_offset = 0;
	}

	public boolean hasNext() {
		return _offset < _script.length();
	}

	public Object next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		int start = _script.indexOf(_blockStart, _offset);
		int end = _script.indexOf(_blockEnd, _offset);
		if (start == -1) {
			String lastLiteralBlock = _script.substring(_offset);
			_offset = _script.length();
			return new ScriptBlock(lastLiteralBlock, true);
		} else if (start == _offset) {
			if (end == -1) {
				throw new UnbalancedBraceException(_script, _script.length());
			}
			String expressionBlock = _script.substring(_offset + _blockStart.length(), end);
			_offset = end + _blockEnd.length();
			return new ScriptBlock(expressionBlock.trim(), false);
		} else if(end < start) {
			int errorOffset = start != -1 ? start : end;
			throw new UnbalancedBraceException(_script, errorOffset);
		}
		String literalBlock = _script.substring(_offset, start);
		_offset = start;
		return new ScriptBlock(literalBlock, true);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}