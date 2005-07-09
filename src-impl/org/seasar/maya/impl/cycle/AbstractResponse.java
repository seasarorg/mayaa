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

import java.io.IOException;
import java.util.Stack;

import org.seasar.maya.cycle.CycleWriter;
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
        _stack.push(new CycleWriter(null));
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

    protected abstract void setMimeTypeToUnderlyingObject(String mimeType);
    
    public void setMimeType(String mimeType) {
        if(StringUtil.isEmpty(mimeType)) {
            throw new IllegalArgumentException();
        }
        _encoding = parseCharacterEncoding(mimeType);
        setMimeTypeToUnderlyingObject(mimeType);
    }
    
    public CycleWriter getWriter() {
        return (CycleWriter)_stack.peek();
    }
    
    public void clearBuffer() {
        getWriter().clearBuffer();
    }

    public String getBuffer() {
        return new String(getWriter().getBuffer());
    }

    public CycleWriter pushWriter() {
        CycleWriter writer = new CycleWriter(getWriter()); 
        _stack.push(writer);
        return writer;
    }

    public CycleWriter popWriter() {
        if(_stack.size() > 1) {
            return (CycleWriter)_stack.pop();
        }
        throw new IllegalStateException();
    }

    public void write(char[] cbuf) {
        write(cbuf, 0, cbuf.length);
    }

    public void write(char[] cbuf, int off, int len) {
        try {
            getWriter().write(cbuf, off, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(int b) {
        try {
            getWriter().write(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String str) {
        write(str, 0, str.length());
    }

    public void write(String str, int off, int len) {
        try {
            getWriter().write(str, off, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected String getEncoding() {
        return _encoding;
    }

    protected abstract void writeToUnderlyingObject(byte[] buffer);
    
    public void flush() {
        if(_stack.size() == 1) {
            CycleWriter writer = (CycleWriter)_stack.peek();
            writeToUnderlyingObject(writer.getBuffer());
        } else {
            try {
                getWriter().flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
