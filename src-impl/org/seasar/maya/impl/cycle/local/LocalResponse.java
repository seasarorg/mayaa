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
package org.seasar.maya.impl.cycle.local;

import java.io.IOException;
import java.io.OutputStream;

import org.seasar.maya.impl.cycle.AbstractResponse;
import org.seasar.maya.impl.cycle.CycleWriter;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LocalResponse extends AbstractResponse {

    private static final long serialVersionUID = -8914260934763222285L;

    private CycleWriter _cycleWriter = new CycleWriter();

    public byte[] getResult() {
        return _cycleWriter.getBuffer();
    }
    
    public Object getUnderlyingObject() {
        return _cycleWriter;
    }
    
    protected void setMimeTypeToUnderlyingObject(String mimeType) {
    }
    
    protected void writeToUnderlyingObject(String text) {
        try {
            _cycleWriter.write(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OutputStream getUnderlyingOutputStream() {
        return _cycleWriter.getOutputStream();
    }
    
}
