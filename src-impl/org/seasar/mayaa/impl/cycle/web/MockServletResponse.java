/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.cycle.web;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

import org.seasar.mayaa.impl.util.StringUtil;

/**
 * AutoPageBuilderで利用するServletResponseのモック。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockServletResponse implements ServletResponse {

    private boolean _commited;
    private String _contentType;
    private String _characterEncoding;
    private int _bufferSize;
    private Locale _locale;
    private MockServletOutputStream _outputStream = new MockServletOutputStream();

    public byte[] getBuffer() {
        return _outputStream.getBuffer();
    }

    public Locale getLocale() {
        return _locale;
    }

    public void setLocale(Locale locale) {
        if(locale == null) {
            throw new IllegalArgumentException();
        }
        _locale = locale;
    }

    public ServletOutputStream getOutputStream() {
        return _outputStream;
    }

    public PrintWriter getWriter() {
        return new PrintWriter(_outputStream);
    }

    public boolean isCommitted() {
        return _commited;
    }

    public void reset() {
        resetBuffer();
    }

    public void flushBuffer() {
        _commited = true;
        resetBuffer();
    }

    public void resetBuffer() {
        _outputStream.reset();
    }

    public int getBufferSize() {
        return _bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        _bufferSize = bufferSize;
    }

    public String getCharacterEncoding() {
        if(StringUtil.isEmpty(_characterEncoding)) {
            _characterEncoding = "UTF-8";
        }
        return _characterEncoding;
    }

    public void setCharacterEncoding(String encoding) {
        _characterEncoding = encoding;
    }

    public String getContentType() {
        return _contentType;
    }

    public void setContentType(String mimeType) {
        _contentType = mimeType;
    }

    public void setContentLength(int length) {
        // do nothing.
    }

    private class MockServletOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream _buffer = new ByteArrayOutputStream();

        protected MockServletOutputStream() {
            // do nothing.
        }

        public byte[] getBuffer() {
            return _buffer.toByteArray();
        }

        public int getBufferSize() {
            return _buffer.size();
        }

        public void reset() {
            _buffer.reset();
        }

        public void write(int b) {
            _buffer.write(b);
        }

    }

}
