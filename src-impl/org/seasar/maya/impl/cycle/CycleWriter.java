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
import java.io.Writer;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleWriter extends Writer {

	private ByteArrayOutputStream _outputStream;
	
	public CycleWriter(ByteArrayOutputStream outputStream) {
		if(outputStream == null) {
			throw new IllegalArgumentException();
		}
		_outputStream = outputStream;
	}

	public void clearBuffer() {
		_outputStream.reset();
	}
	
	public byte[] getBuffer() {
		return _outputStream.toByteArray();
	}
	
	public void close() throws IOException {
		_outputStream.close();
	}

	public void flush() throws IOException {
		_outputStream.flush();
	}

	public void write(char[] cbuf, int off, int len) throws IOException {
		for(int i = off; i < len; i++) {
			_outputStream.write(cbuf[i]);
		}
	}
	
}
