/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.cycle;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Stack;

import org.seasar.mayaa.cycle.CycleWriter;
import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractResponse implements Response {

    private String _encoding = "UTF-8";
    private Stack _stack;
    private boolean _flushed;

    public AbstractResponse() {
        _stack = new Stack();
        _stack.push(new CycleWriterImpl(null));
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

    protected abstract void setContentTypeToUnderlyingObject(
            String contentType);

    public void setContentType(String contentType) {
        if (StringUtil.isEmpty(contentType)) {
            throw new IllegalArgumentException();
        }
        _encoding = parseCharacterEncoding(contentType);
        setContentTypeToUnderlyingObject(contentType);
    }

    public CycleWriter getWriter() {
        return (CycleWriter) _stack.peek();
    }

    public void clearBuffer() {
        _stack.clear();
        _stack.push(new CycleWriterImpl(null));
    }

    public CycleWriter pushWriter() {
        CycleWriter writer = new CycleWriterImpl(getWriter());
        _stack.push(writer);
        return writer;
    }

    public CycleWriter popWriter() {
        if (_stack.size() > 1) {
            return (CycleWriter) _stack.pop();
        }
        throw new IllegalStateException();
    }

    public void write(char[] cbuf) {
        if (cbuf != null) {
            write(cbuf, 0, cbuf.length);
        }
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
        if (str != null) {
            write(str, 0, str.length());
        }
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

    public void flush() {
        try {
            if (_stack.size() == 1) {
                CycleWriter writer = (CycleWriter) _stack.peek();
                Writer underlyingWriter = new OutputStreamWriter(
                        getOutputStream(), _encoding);
                writer.writeOut(underlyingWriter);
                underlyingWriter.flush();
            } else {
                getWriter().flush();
            }
            _flushed = true;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isFlushed() {
        return _flushed;
    }

}
