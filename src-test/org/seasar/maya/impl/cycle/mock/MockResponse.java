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
package org.seasar.maya.impl.cycle.mock;

import java.io.OutputStream;

import org.seasar.maya.impl.cycle.AbstractResponse;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockResponse extends AbstractResponse {

    private static final long serialVersionUID = -8914260934763222285L;

    private StringBuffer _underlyingBuffer = new StringBuffer();
    private String _mimeType;
    private String _encoding;
    
    public String getBufferedText() {
        return _underlyingBuffer.toString();
    }
    
    public Object getUnderlyingObject() {
        return _underlyingBuffer;
    }

    public String getMimeType() {
        return _mimeType;
    }
    
    protected void setMimeTypeToUnderlyingObject(String mimeType) {
        _mimeType = mimeType;
    }

    public String getEncoding() {
        return _encoding;
    }
    
    protected void writeToUnderlyingObject(String text, String encoding) {
        _encoding = encoding;
        _underlyingBuffer.append(text);
    }

    public OutputStream getOutputStream() {
        return new MockOutputStream(_underlyingBuffer);
    }

    private class MockOutputStream extends OutputStream {

        private StringBuffer _buffer;
        
        private MockOutputStream(StringBuffer buffer) {
            if(buffer == null) {
                throw new IllegalArgumentException();
            }
            _buffer = buffer;
        }
        
        public void write(int b) {
            _buffer.append(b);
        }
        
    }
    
}
