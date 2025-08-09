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
package org.seasar.mayaa.impl.cycle.jsp;

import java.io.IOException;

import jakarta.servlet.jsp.JspWriter;

import org.seasar.mayaa.cycle.CycleWriter;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspWriterImpl extends JspWriter {

    private static final String LINE_SEPARATOR =
        System.getProperty("line.separator");

    private boolean _closed;
    private boolean _flushed;
    private CycleWriter _writer;

    public JspWriterImpl(CycleWriter writer) {
        super(DEFAULT_BUFFER, false);
        if (writer == null) {
            throw new IllegalArgumentException();
        }
        _writer = writer;
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        if (_closed) {
            throw new IOException();
        }
        if (len == 0) {
            return;
        }
        _writer.write(cbuf, off, len);
    }

    public final void clear() throws IOException {
        if (_closed || _flushed) {
            throw new IOException();
        }
        // lazy implementation
        _writer.clearBuffer();
    }

    public void clearBuffer() {
        // lazy implementation
        _writer.clearBuffer();
    }

    public void close() {
        _closed = true;
    }

    public void flush() throws IOException {
        if (_closed) {
            throw new IOException();
        }
        // lazy implementation
        _writer.flush();
        _flushed = true;
    }

    public int getRemaining() {
        return 0;
    }

    public void newLine() throws IOException {
        write(LINE_SEPARATOR);
    }

    // ----------------------------------------------------------
    public void print(boolean b) throws IOException {
        write(Boolean.toString(b));
    }

    public void print(char c) throws IOException {
        write(Character.toString(c));
    }

    public void print(char[] c) throws IOException {
        write(c);
    }

    public void print(double d) throws IOException {
        write(Double.toString(d));
    }

    public void print(float f) throws IOException {
        write(Float.toString(f));
    }

    public void print(int i) throws IOException {
        write(Integer.toString(i));
    }

    public void print(long l) throws IOException {
        write(Long.toString(l));
    }

    public void print(Object o) throws IOException {
        write((o == null) ? "null" : o.toString());
    }

    public void print(String s) throws IOException {
        write((s == null) ? "null" : s);
    }

    // ----------------------------------------------------------
    public void println() throws IOException {
        newLine();
    }

    public void println(boolean b) throws IOException {
        print(b);
        newLine();
    }

    public void println(char c) throws IOException {
        print(c);
        newLine();
    }

    public void println(char[] c) throws IOException {
        print(c);
        newLine();
    }

    public void println(double d) throws IOException {
        print(d);
        newLine();
    }

    public void println(float f) throws IOException {
        print(f);
        newLine();
    }

    public void println(int i) throws IOException {
        print(i);
        newLine();
    }

    public void println(long l) throws IOException {
        print(l);
        newLine();
    }

    public void println(Object o) throws IOException {
        print(o);
        newLine();
    }

    public void println(String s) throws IOException {
        print(s);
        newLine();
    }

}
