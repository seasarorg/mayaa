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

    public static final String BLOCK_SIGN_JSP = "$";
    public static final String BLOCK_SIGN_JSF = "#";

	private String _script;
	private String _blockSign;
	private int _offset;

	public ScriptBlockIterator(String expression, String blockSign) {
		if (StringUtil.isEmpty(expression) || StringUtil.isEmpty(blockSign)) {
			throw new IllegalArgumentException();
		}
		_script = expression;
		_blockSign = blockSign;
		_offset = 0;
	}

	public boolean hasNext() {
		return _offset < _script.length();
	}

    protected int scanBlockCloseOffset(int start) {
        char c = _script.charAt(start);
        if(c != '{') {
            throw new IllegalArgumentException();
        }
        int depth = 0;
        for(int i = start; i < _script.length(); i++) {
            c = _script.charAt(i);
            if(c == '{') {
                depth++;
            } else if(c == '}') {
                depth--;
                if(depth == 0) {
                    return i; 
                } else if(depth < 0) {
                    throw new UnbalancedBraceException(_script, i); 
                }
            }
        }
        return -1;
    }
    
	public Object next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
        String blockStart = _blockSign + "{";
		int sign = _script.indexOf(blockStart, _offset);
        if (sign != -1) {
            if(_offset == sign) {
                // script block
                int close = scanBlockCloseOffset(_offset + _blockSign.length());
                if (close == -1) {
                    throw new UnbalancedBraceException(_script, _script.length());
                }
                String expressionBlock = _script.substring(_offset + blockStart.length(), close);
                _offset = close + 1;
                return new ScriptBlock(expressionBlock, false);
            }
            // literal
            String lastLiteralBlock = _script.substring(_offset, sign);
            _offset = sign;
            return new ScriptBlock(lastLiteralBlock, true);
		}
        // tail literal
        String lastLiteralBlock = _script.substring(_offset);
        _offset = _script.length();
        return new ScriptBlock(lastLiteralBlock, true);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}