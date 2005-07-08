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

import org.seasar.maya.impl.cycle.mock.MockResponse;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AbstractResponseTest extends TestCase {

    public AbstractResponseTest(String name) {
        super(name);
    }
    
    public void testSetMimeType() {
        AbstractResponse response = new MockResponse();
        response.setMimeType("text/html; charset=Shift_JIS");
        assertEquals("Shift_JIS", response.getEncoding());
    }
    
    public void testWrite() {
        AbstractResponse response = new MockResponse();
        response.write("<html></html>");
        assertEquals("<html></html>", response.getBuffer());
    }
    
    public void testClearBuffer() {
        AbstractResponse response = new MockResponse();
        response.write("<html></html>");
        response.clearBuffer();
        assertEquals("", response.getBuffer());
    }
    
    public void testPushBuffer1() {
        AbstractResponse response = new MockResponse();
        response.write("<html>");
        response.pushBuffer();
        response.write("<body></body>");
        response.flush();
        response.write("</html>");
        assertEquals("<html><body></body></html>", response.getBuffer());
    }

    public void testPushBuffer2() {
        AbstractResponse response = new MockResponse();
        response.write("<html>");
        response.pushBuffer();
        response.write("<body></body>");
        response.popBuffer();
        response.write("</html>");
        assertEquals("<html></html>", response.getBuffer());
    }
    
}
