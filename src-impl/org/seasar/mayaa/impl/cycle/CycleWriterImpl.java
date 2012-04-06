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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;

import org.seasar.mayaa.cycle.CycleWriter;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleWriterImpl extends CycleWriter {

    // インスタンス数が多いため、初期バッファサイズは大きすぎないように
    private static final int DEFAULT_BUFFER_SIZE = 512;

    private CycleWriter _enclosingWriter;
    private CharArrayWriter _buffer;
    private boolean _flushed;
    private boolean _flushToWriteOut;

    /**
     * @param enclosingWriter 内部Writer
     */
    public CycleWriterImpl(CycleWriter enclosingWriter) {
        this(enclosingWriter, true);
    }

    /**
     * @param enclosingWriter 内部Writer
     */
    public CycleWriterImpl(CycleWriter enclosingWriter, boolean flushToWriteOut) {
        _enclosingWriter = enclosingWriter;
        _buffer = new CharArrayWriter(DEFAULT_BUFFER_SIZE);
        _flushToWriteOut = flushToWriteOut;
    }

    public CycleWriter getEnclosingWriter() {
        return _enclosingWriter;
    }

    public void clearBuffer() {
        _buffer.reset();
    }

    public String getString() {
        return _buffer.toString();
    }

    public boolean isDirty() {
        return _flushed || _buffer.size() > 0;
    }

    public void writeOut(Writer writer) throws IOException {
        if (writer == null) {
            return;
        }
        // don't flush.
        writer.write(_buffer.toCharArray());
        _buffer.reset();
        _flushed = true;
    }

    // Writer implemtents --------------------------------------------

    public void write(char[] cbuf, int off, int len) {
        if (len == 0) {
            return;
        }
        _buffer.write(cbuf, off, len);
    }

    public void write(String str, int off, int len) {
        if (len == 0) {
            return;
        }
        _buffer.write(str, off, len);
    }

    public void flush() throws IOException {
    	if (_flushToWriteOut) {
    		writeOut(_enclosingWriter);
    	}
    }

    public void close() {
        // do nothing.
    }

}
