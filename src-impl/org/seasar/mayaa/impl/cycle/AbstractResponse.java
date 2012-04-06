/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.CharsetConverter;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractResponse implements Response, CONST_IMPL {

    private static final long serialVersionUID = -7175816678322765773L;

    private String _encoding = TEMPLATE_DEFAULT_CHARSET;
    private Stack _stack;
    private boolean _flushed;

    public AbstractResponse() {
        _stack = new Stack();
        _stack.push(new CycleWriterImpl(null));
    }

    protected abstract void setContentTypeToUnderlyingObject(
            String contentType);

    public void setContentType(String contentType) {
        if (StringUtil.isEmpty(contentType)) {
            throw new IllegalArgumentException();
        }
        _encoding = CharsetConverter.extractEncoding(contentType);
        setContentTypeToUnderlyingObject(
                CharsetConverter.convertContentType(contentType));
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

    public CycleWriter pushNoFlushWriter() {
        CycleWriter writer = new CycleWriterImpl(getWriter(), false);
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
