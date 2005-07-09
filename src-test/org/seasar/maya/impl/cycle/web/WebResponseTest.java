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
package org.seasar.maya.impl.cycle.web;

import java.io.IOException;
import java.io.PrintWriter;

import org.seasar.maya.impl.cycle.servlet.MockHttpServletResponse;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebResponseTest extends TestCase {

    private MockHttpServletResponse _httpServletResponse;
    private WebResponse _response;
    
    public WebResponseTest(String name) {
        super(name);
    }

    protected void setUp() {
        _httpServletResponse = new MockHttpServletResponse();
        _response =  new WebResponse();
        _response.setHttpServletResponse(_httpServletResponse);
    }
    
    public void testWriteToUnderlyingObject() {
        _response.writeToUnderlyingObject("<html></html>".getBytes());
        assertEquals("<html></html>", new String(_httpServletResponse.getBuffer()));
    }

    public void testGetOutputStream() throws IOException {
        PrintWriter writer = new PrintWriter(_response.getUnderlyingOutputStream());
        writer.write("<html></html>");
        writer.flush();
        assertEquals("<html></html>", new String(_httpServletResponse.getBuffer()));
    }
 
    public void testFlush() {
        _response.write("<html></html>");
        assertEquals("", new String(_httpServletResponse.getBuffer()));
        _response.flush();
        assertEquals("<html></html>", new String(_httpServletResponse.getBuffer()));
    }
    
}
