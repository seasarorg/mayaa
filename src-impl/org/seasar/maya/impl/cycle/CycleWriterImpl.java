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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.seasar.maya.cycle.CycleWriter;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleWriterImpl extends CycleWriter {

    private boolean _flushed;
    private CycleWriter _enclosingWriter;
	private ByteArrayOutputStream _outputStream = new ByteArrayOutputStream();
	
    public CycleWriterImpl(CycleWriter enclosingWriter) {
        _enclosingWriter = enclosingWriter;
    }
    
    public CycleWriter getEnclosingWriter() {
        return _enclosingWriter;
    }
    
    public void clearBuffer() {
        _flushed = false;
		_outputStream.reset();
	}
	
    public byte[] getBuffer() {
		return _outputStream.toByteArray();
	}
	
	public void close() throws IOException {
	}

	public void flush() throws IOException {
	    if(_flushed) {
	        throw new AlreadyFlushedException();
        }
        // write out to enclosing-writer.
        if(_enclosingWriter != null) {
            _flushed = true;
            byte[] buffer = getBuffer();
	        for(int i = 0; i < buffer.length; i++) {
	            _enclosingWriter.write(buffer[i]);
            }
        }
	}

	public void write(char[] cbuf, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
		for(int i = off; i < len; i++) {
			_outputStream.write(cbuf[i]);
		}
	}
    
    public OutputStream getOutputStream() {
        return _outputStream;
    }
	
}
