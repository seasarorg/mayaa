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

import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleWriterImplTest extends TestCase {

    public CycleWriterImplTest(String name) {
        super(name);
    }

    public void testGetBuffer() throws IOException {
        CycleWriterImpl writer = new CycleWriterImpl(null);
        writer.write("test string");
        assertEquals("test string", writer.getString());
    }
    
    public void testClearBuffer() throws IOException {
        CycleWriterImpl writer = new CycleWriterImpl(null);
        writer.write("test string");
        writer.clearBuffer();
        assertEquals("", writer.getString());
    }

    public void testPrepareBuffer() throws IOException {
    	CycleWriterImpl writer = new CycleWriterImpl(null, 2, 4);
    	assertEquals(4, writer.prepareBuffer(3));
    	writer.write("123");
    	assertEquals(4, writer.prepareBuffer(1));
    	writer.write("4");
    	assertEquals(8, writer.prepareBuffer(3));
    }
    
}
