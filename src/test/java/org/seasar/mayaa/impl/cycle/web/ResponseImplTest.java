/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.cycle.web;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.test.mock.MockHttpServletResponse;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ResponseImplTest {

    private MockHttpServletResponse _httpServletResponse;
    private ResponseImpl _response;

    @Before
    public void setUp() {
        _httpServletResponse = new MockHttpServletResponse();
        _response =  new ResponseImpl();
        _response.setUnderlyingContext(_httpServletResponse);
    }

    @Test
    public void testGetOutputStream() {
        PrintWriter writer = new PrintWriter(_response.getOutputStream());
        writer.write("<html></html>");
        writer.flush();
        assertEquals("<html></html>", new String(_httpServletResponse.getBuffer()));
    }

    @Test
    public void testWrite() {
        _response.write("<html></html>");
        assertEquals("<html></html>", _response.getWriter().getString());
    }

    @Test
    public void testClearBuffer() {
        _response.write("<html></html>");
        _response.clearBuffer();
        assertEquals("", _response.getWriter().getString());
    }

    @Test
    public void testPushWriter1() {
        _response.write("<html>");
        _response.pushWriter();
        _response.write("<body></body>");
        _response.flush();
        _response.popWriter();
        _response.write("</html>");
        assertEquals("<html><body></body></html>", _response.getWriter().getString());
    }

    @Test
    public void testPushWriter2() {
        _response.write("<html>");
        _response.pushWriter();
        _response.write("<body></body>");
        _response.popWriter();
        _response.write("</html>");
        assertEquals("<html></html>", _response.getWriter().getString());
    }

    @Test
    public void testFlush() {
        _response.write("<html></html>");
        assertEquals("", new String(_httpServletResponse.getBuffer()));
        _response.flush();
        assertEquals("<html></html>", new String(_httpServletResponse.getBuffer()));
    }

}
