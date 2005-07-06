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

import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockResponseTest extends TestCase {

    public MockResponseTest(String name) {
        super(name);
    }
    
    public void testGetMimeType() {
        MockResponse response = new MockResponse();
        response.setMimeTypeToUnderlyingObject("text/html");
        assertEquals("text/html", response.getMimeType());
    }
    
    public void testWriteToUnderlyingObject() {
        MockResponse response = new MockResponse();
        response.writeToUnderlyingObject("<html></html>", "Shift_JIS");
        assertEquals("Shift_JIS", response.getEncoding());
        assertEquals("<html></html>", response.getBufferedText());
    }

    public void testGetOutputStream() throws IOException {
        MockResponse response = new MockResponse();
        PrintWriter writer = new PrintWriter(response.getOutputStream());
        writer.write("<html></html>");
        assertEquals("<html></html>", response.getBufferedText());
    }
    
}
