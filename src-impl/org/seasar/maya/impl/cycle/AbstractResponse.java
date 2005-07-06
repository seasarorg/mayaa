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
package org.seasar.maya.impl.cycle;

import java.util.Stack;

import org.seasar.maya.cycle.Response;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractResponse implements Response {

    private String _encoding = "UTF-8";
    private Stack _stack;

    public AbstractResponse() {
        _stack = new Stack();
        _stack.push(new StringBuffer());
    }
    
    private String parseCharacterEncoding(String contentType) {
        if (StringUtil.hasValue(contentType)) {
            String lower = contentType.toLowerCase();
            int startPos = lower.indexOf("charset");
            if (startPos > 0) {
                startPos += 7; /*7 = "charset".length()*/
                final int eqPos = contentType.indexOf("=", startPos);
                if (eqPos > 0) {
                    int endPos = contentType.indexOf(";", eqPos);
                    if (endPos < 0) {
                        endPos = contentType.length();
                    }
                    return contentType.substring(eqPos + 1, endPos).trim();
                }
            }
        }
        return "UTF-8";
    }
    
    private StringBuffer getCurrentBuffer() {
        return (StringBuffer)_stack.peek();
    }

    protected abstract void setMimeTypeToUnderlyingObject(String mimeType);
    
    public void setMimeType(String mimeType) {
        if(StringUtil.isEmpty(mimeType)) {
            throw new IllegalArgumentException();
        }
        _encoding = parseCharacterEncoding(mimeType);
        setMimeTypeToUnderlyingObject(mimeType);
    }
    
    public void clearBuffer() {
        getCurrentBuffer().setLength(0);
    }

    public String getBufferedText() {
        return getCurrentBuffer().toString();
    }

    public void pushBuffer() {
        _stack.push(new StringBuffer());
    }

    public String popBuffer() {
        if(_stack.size() > 1) {
            return _stack.pop().toString();
        }
        throw new IllegalStateException();
    }

    public void write(String text) {
        getCurrentBuffer().append(text);
    }
    
    protected String getEncoding() {
        return _encoding;
    }

    protected abstract void writeToUnderlyingObject(String text);
    
    public void writeOut() {
        if(_stack.size() == 1) {
            writeToUnderlyingObject(_stack.peek().toString());
        } else {
            String text = popBuffer();
            getCurrentBuffer().append(text);
        }
    }

}
