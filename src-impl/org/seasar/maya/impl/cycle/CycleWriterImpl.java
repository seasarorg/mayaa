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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;

import org.seasar.maya.cycle.CycleWriter;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleWriterImpl extends CycleWriter {

    private CycleWriter _enclosingWriter;
    private CharArrayWriter _buffer;
    private boolean _flushed;

    public CycleWriterImpl(CycleWriter enclosingWriter) {
        _enclosingWriter = enclosingWriter;
        _buffer = new CharArrayWriter();
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
        if(writer == null) {
            throw new IllegalArgumentException();
        }
        // don't flush.
        writer.write(_buffer.toCharArray());
        _buffer.reset();
        _flushed = true;
    }

    // Writer implemtents --------------------------------------------
    
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        _buffer.write(cbuf, off, len);
    }
    
    public void flush() throws IOException {
        writeOut(_enclosingWriter);
    }
    
    public void close() {
    }
    
}
