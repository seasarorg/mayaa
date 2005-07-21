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

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.seasar.maya.cycle.CycleWriter;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleWriterImpl extends CycleWriter {
    
    private boolean _closed;
    private boolean _flushed;
    private CycleWriter _enclosingWriter;
    private char[] _buffer;
    private int _blockSize;
    private int _maxBlockNum;
    private int _currentBlockNum;
    private int _bufferOffset;
    private int _usedBufferSize;

    public CycleWriterImpl(CycleWriter enclosingWriter, int blockSize, int maxBlockNum) {
        if(blockSize <= 0 || maxBlockNum <= 0) {
            throw new IllegalArgumentException();
        }
        _enclosingWriter = enclosingWriter;
        _blockSize = blockSize;
        _maxBlockNum = maxBlockNum;
        _currentBlockNum = 1;
        _buffer = new char[_blockSize];
    }
    
    public CycleWriterImpl(CycleWriter enclosingWriter) {
        this(enclosingWriter, BLOCK_SIZE, MAX_BLOCK_NUM);
    }
    
    public CycleWriter getEnclosingWriter() {
        return _enclosingWriter;
    }
    
    public void clearBuffer() {
        check();
        _bufferOffset = 0;
		_usedBufferSize = 0;
	}
	
	public void close() {
        _closed = true;
        _bufferOffset = 0;
        _usedBufferSize = 0;
        _buffer = null;
	}

    private void check() {
        if(_closed) {
            throw new AlreadyClosedException();
        }
        if(_flushed) {
            throw new AlreadyFlushedException();
        }
    }
    
    // write out to enclosing-writer.
    private void writeToEnclosing() throws IOException {
        if(_enclosingWriter != null) {
            _enclosingWriter.write(_buffer, _bufferOffset, _usedBufferSize);
            _bufferOffset = _usedBufferSize;
        }
    }
    
	public void flush() throws IOException {
	    check();
        _flushed = true;
        writeToEnclosing();
	}

	protected int prepareBuffer(int len) throws IOException {
		int newUsed = _usedBufferSize + len;
		int currentSize = _buffer.length;
		if(newUsed <= currentSize) {
			return currentSize;
		}
		for(int blockNum = _currentBlockNum; blockNum <= _maxBlockNum; blockNum++) {
			int newSize = blockNum * _blockSize;
			if(newSize >= newUsed) {
				char[] newBuffer = new char[newSize];
                System.arraycopy(_buffer, 0, newBuffer, 0, _usedBufferSize);
                _buffer = newBuffer;
				return newSize;
			}
		}
		writeToEnclosing();
        clearBuffer();
        return prepareBuffer(len);
	}
	
	public void write(char[] cbuf, int off, int len) throws IOException {
        check();
        if (len == 0) {
            return;
        }
        prepareBuffer(len);
        System.arraycopy(cbuf, off, _buffer, _usedBufferSize, len);
        _usedBufferSize += len;
    }

    public Reader getReader() {
        return new CharArrayReader(_buffer, 0, _usedBufferSize);
    }

    public String getString() {
        return new String(_buffer, 0, _usedBufferSize);
    }

    public void writeOut(Writer writer) throws IOException {
        if(writer == null) {
            throw new IllegalArgumentException();
        }
        writer.write(_buffer, 0, _usedBufferSize);
        writer.flush();
    }
    
}
