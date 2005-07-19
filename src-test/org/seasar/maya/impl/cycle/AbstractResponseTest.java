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

import junit.framework.TestCase;

import org.seasar.maya.impl.cycle.servlet.MockHttpServletResponse;
import org.seasar.maya.impl.cycle.web.WebResponse;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AbstractResponseTest extends TestCase {

    private AbstractResponse _response;
    
    public AbstractResponseTest(String name) {
        super(name);
    }
    
    public void setUp() {
        WebResponse response = new WebResponse();
        response.setHttpServletResponse(new MockHttpServletResponse());
        _response = response;
    }
    
    public void testSetMimeType() {
        _response.setMimeType("text/html; charset=Shift_JIS");
        assertEquals("Shift_JIS", _response.getEncoding());
    }
    
    public void testWrite() {
        _response.write("<html></html>");
        assertEquals("<html></html>", _response.getString());
    }
    
    public void testClearBuffer() {
        _response.write("<html></html>");
        _response.clearBuffer();
        assertEquals("", _response.getString());
    }
    
    public void testPushWriter1() {
        _response.write("<html>");
        _response.pushWriter();
        _response.write("<body></body>");
        _response.flush();
        _response.popWriter();
        _response.write("</html>");
        assertEquals("<html><body></body></html>", _response.getString());
    }

    public void testPushWriter2() {
        _response.write("<html>");
        _response.pushWriter();
        _response.write("<body></body>");
        _response.popWriter();
        _response.write("</html>");
        assertEquals("<html></html>", _response.getString());
    }
    
}
