/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.test.util.servlet.jsp;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.jsp.JspWriter;

import org.seasar.maya.impl.CONST_IMPL;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TestJspWriter extends JspWriter implements CONST_IMPL {

    private static final int UNBOUNDED_BUFFER_SIZE = 512;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    private boolean _initOut;
    private Writer _out;
    private char[] _buffer;
    private int _bufferUsedSize;
    private boolean _flushed;
    private boolean _closed;
    private boolean _unbound;

    public TestJspWriter(int bufferSize, boolean autoFlush) {
        super(bufferSize, autoFlush);
        if(bufferSize == DEFAULT_BUFFER) {
            bufferSize = UNBOUNDED_BUFFER_SIZE;
        } else if(bufferSize == UNBOUNDED_BUFFER) {
            bufferSize = UNBOUNDED_BUFFER_SIZE;
            _unbound = true;
        } else if(bufferSize < UNBOUNDED_BUFFER) {
            // Buffer size < -2
            throw new IllegalArgumentException();
        }
        _buffer = new char[bufferSize];
        _bufferUsedSize = 0;
    }
    
    private boolean notUseBuffer() {
        return getBufferSize() == 0; 
    }

    private void flushBuffer() throws IOException {
        if (notUseBuffer()) {
            return;
        }
        checkWriter();
        _flushed = true;
        if (_bufferUsedSize == 0) {
            return;
        }
        _out.write(_buffer, 0, _bufferUsedSize);
        _bufferUsedSize = 0;
    }

    private void checkWriter() throws IOException {
        if (_out == null || _closed) {
            // Stream closed
            throw new IOException();
        }
    }
    
    public char[] getBuffer() {
        return _buffer;
    }
    
    public boolean isInitOut() {
        return _initOut;
    }
    
    public void setOut(Writer out) {
        if(out == null) {
            throw new IllegalArgumentException();
        }
        _out = out;
        _initOut = true;
    }

    public void write(char cbuf[], int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        if (notUseBuffer()) {
            _out.write(cbuf, off, len);
        } else { 
	        if (off < 0 || off > cbuf.length || len < 0 || (off + len) > cbuf.length) {
	            throw new IndexOutOfBoundsException();
	        }
	        if (len >= getBufferSize()) {
	            if (autoFlush) {
	                flushBuffer();
	            } else if(_unbound) {
	                bufferSize = len + UNBOUNDED_BUFFER_SIZE; 
	                char[] newBuffer = new char[bufferSize];
	                System.arraycopy(_buffer, 0, newBuffer, 0, _bufferUsedSize);
	                _buffer = newBuffer;
	            } else {
	                //buffer overflow
	                throw new IOException();
	            }
	            _out.write(cbuf, off, len);
	        } else {
		        int currentPos = off;
		        final int endPos = off + len;
		        while (currentPos < endPos) {
		            int deltaSize = Math.min(getRemaining(), endPos - currentPos);
		            System.arraycopy(cbuf, currentPos, _buffer, _bufferUsedSize, deltaSize);
		            currentPos += deltaSize;
		            _bufferUsedSize += deltaSize;
		            if (_bufferUsedSize >= getBufferSize()) {
		                if (autoFlush) {
		                    flushBuffer();
		                } else if(_unbound) {
			                bufferSize += UNBOUNDED_BUFFER_SIZE;
			                char[] newBuffer = new char[bufferSize];
			                System.arraycopy(_buffer, 0, newBuffer, 0, _bufferUsedSize);
			                _buffer = newBuffer;
		                } else {
			                //buffer overflow
			                throw new IOException();
		                }
		            }
		        }
	        }
    	}
    }

    public final void clear() throws IOException {
        if (notUseBuffer() && !_closed) {
            throw new IllegalStateException();
        }
        if (_flushed) {
            throw new IOException();
        }
        checkWriter();
        _bufferUsedSize = 0;
    }

    public void clearBuffer() throws IOException {
        if (notUseBuffer()) {
            throw new IllegalStateException();
        }
        checkWriter();
        _bufferUsedSize = 0;
    }

    public void close() throws IOException {
        if (_out != null && !_closed) {
            flush();
            _out.close();
            _out = null;
            _closed = true;
        }
    }

    public void flush() throws IOException {
        flushBuffer();
        if (!_closed) {
            _out.flush();
        }
    }

    public int getRemaining() {
        return getBufferSize() - _bufferUsedSize;
    }

    public void newLine() throws IOException {
        write(LINE_SEPARATOR);
    }

    // 以下、printのバリエーション。write(String)を利用する。---------------------------------
    public void print(boolean b) throws IOException {
        write(Boolean.toString(b));
    }

    public void print(char c) throws IOException {
        write(Character.toString(c));
    }

    public void print(char c[]) throws IOException {
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
        write(String.valueOf(o));
    }

    public void print(String s) throws IOException {
        write(s);
    }

    // 以下、printlnのバリエーション。print(xxx)とnewLine()を利用する。----------------------
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

    public void println(char c[]) throws IOException {
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