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
package org.seasar.mayaa.impl.cycle.web;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * AutoPageBuilderで利用するServletResponseのモック。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockServletResponse implements ServletResponse, CONST_IMPL {

    private boolean _commited;
    private String _contentType;
    private String _characterEncoding;
    private int _bufferSize;
    private Locale _locale;
    private MockServletOutputStream _outputStream = new MockServletOutputStream();

    /**
     * コミット時に出力するOutputStreamをセットする。
     * 出力後はこのStreamをcloseする。
     *
     * @param onCommitOutputStream コミット時に出力するOutputStream
     */
    public void setOnCommitOutputStream(OutputStream onCommitOutputStream) {
        _outputStream.setOnCommitOutputStream(onCommitOutputStream);
    }

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
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(getOutputStream(), getCharacterEncoding()));
            return new PrintWriter(writer);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.toString());
        }
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
            _characterEncoding = TEMPLATE_DEFAULT_CHARSET;
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

    @Override
    public void setContentLengthLong(long length) {
        // do nothing.
    }

    private static class MockServletOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream _buffer = new ByteArrayOutputStream(5120);
        private OutputStream _onCommitOutputStream;

        protected MockServletOutputStream() {
            // do nothing.
        }

        @Override
        public boolean isReady() {
            // Always ready in this mock implementation
            return true;
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            // No-op for mock
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

        /**
         * コミット時に出力するOutputStreamをセットする。
         *
         * @param onCommitOutputStream コミット時に出力するOutputStream
         */
        public void setOnCommitOutputStream(OutputStream onCommitOutputStream) {
            _onCommitOutputStream = onCommitOutputStream;
        }

        /**
         * onCommitOutputStreamがセットされているなら、onCommitOutputStreamに
         * 対して内容を出力する。
         *
         * @see java.io.OutputStream#flush()
         */
        public void flush() {
            if (_onCommitOutputStream != null) {
                try {
                    final int bufferSize = 1024;
                    int offset = 0;
                    while (offset < getBufferSize()) {
                        if (offset + bufferSize > getBufferSize()) {
                            _onCommitOutputStream.write(
                                    getBuffer(), offset, getBufferSize() - offset);
                            offset = getBufferSize();
                        } else {
                            _onCommitOutputStream.write(getBuffer(), offset, bufferSize);
                            offset += bufferSize;
                        }
                    }
                    _onCommitOutputStream.flush();
                } catch (IOException e) {
                    Log log = LogFactory.getLog(MockServletResponse.class);
                    log.info(e.getMessage(), e);
                } finally {
                    IOUtil.close(_onCommitOutputStream);
                }
            }
        }

    }

}
