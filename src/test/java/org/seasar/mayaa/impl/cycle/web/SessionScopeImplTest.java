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
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.test.util.ManualProviderFactory;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SessionScopeImplTest {

    private MockHttpServletRequest _httpServletRequest;
    private SessionScopeImpl _session;

    @Before
    public void setUp() {
        ManualProviderFactory.setUp(this);
        _httpServletRequest = ManualProviderFactory.HTTP_SERVLET_REQUEST;
        _session = new SessionScopeImpl();
        _session.setUnderlyingContext(_httpServletRequest);
    }

    @After
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testGetScopeName() {
        assertEquals("session", _session.getScopeName());
    }

    @Test
    public void testGetAttribute() {
        _session.setAttribute("test", "test attr");
        assertEquals("test attr", _session.getAttribute("test"));
        _session.setAttribute("test", null);
        assertNull(_session.getAttribute("test"));
    }

}
