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
package org.seasar.maya.standard.cycle;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;

import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.test.util.servlet.jsp.TestJspWriter;

/**
 * TODO Cycle対応実装（今のところ、既存からコピペしただけ）
 * 
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleBodyContent extends BodyContent implements CONST_IMPL {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    private TestJspWriter _out;
    private Writer _writer;
    
    public CycleBodyContent(JspWriter enclosingWriter) {
        super(enclosingWriter);
        _out = new TestJspWriter(UNBOUNDED_BUFFER, false);
        _out.setOut(enclosingWriter);
    }

    /**
     * JSP2.0からの非JspContext#pushBody(Writer)対応。
     * @param writer JspContext#pushBody(Writer)から渡されるWriter。 
     */
    public void setWriter(Writer writer) {
        _writer = writer;
    }
    
    public int getBufferSize() {
        if(_writer == null) {
            return _out.getBufferSize();
        }
        return NO_BUFFER;
    }

    public Reader getReader() {
        if(_writer == null) {
            return new CharArrayReader(_out.getBuffer());
        }
        return null;
    } 
    
    public String getString() {
        if(_writer == null) {
            return new String(_out.getBuffer());
        }
        return null;
    }

    public void clear() throws IOException {
        if(_writer == null) {
            _out.clear();
        }
    }
    
    public void clearBuffer() throws IOException {
        if(_writer == null) {
            _out.clearBuffer();
        }
    }
    
    public void close() throws IOException {
        if(_writer == null) {
            _out.close();
        } else {
            _writer.close();
        }
    }
    
    public int getRemaining() {
        if(_writer == null) {
            return _out.getRemaining();
        }
        return NO_BUFFER;
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        if(_writer == null) {
            _out.write(cbuf, off, len);
        } else {
            _writer.write(cbuf, off, len);
        }
    }
    
    public void writeOut(Writer out) throws IOException {
        if(_writer == null) {
	        char[] buffer = _out.getBuffer();
	        out.write(buffer);
        }
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
