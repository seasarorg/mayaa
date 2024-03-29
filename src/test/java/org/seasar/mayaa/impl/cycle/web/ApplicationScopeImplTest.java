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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.test.util.ManualProviderFactory;
import org.springframework.mock.web.MockServletContext;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ApplicationScopeImplTest {

    private ApplicationScope _application;

    @BeforeEach
    public void setUp() {
        ManualProviderFactory.setUp(this);
        _application = new ApplicationScopeImpl();
        _application.setUnderlyingContext(new MockServletContext((String) null));
    }

    @AfterEach
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testGetScopeName() {
        assertEquals("application", _application.getScopeName());
    }

    @Test
    public void testGetAttribute() {
        _application.setAttribute("test", "test attr");
        assertEquals("test attr", _application.getAttribute("test"));
        _application.setAttribute("test", null);
        assertNull(_application.getAttribute("test"));
    }

}
