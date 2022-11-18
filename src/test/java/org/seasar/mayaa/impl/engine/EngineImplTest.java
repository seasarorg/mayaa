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
package org.seasar.mayaa.impl.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class EngineImplTest {

    @BeforeEach
    public void setUp() {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
    }

    @AfterEach
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testIsPageRequested() {
        EngineImpl engine = new EngineImpl();
        engine.setParameter(CONST_IMPL.NOT_TEMPLATE_PATH_PATTERN, ".*");
        engine.setParameter(CONST_IMPL.TEMPLATE_PATH_PATTERN, ".*\\.(html|xml)");
        engine.setParameter(CONST_IMPL.TEMPLATE_PATH_PATTERN, ".*\\.(xhtml|mayaa)");
        engine.setParameter(CONST_IMPL.NOT_TEMPLATE_PATH_PATTERN, "/docs/.*");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/test.xml");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertTrue(engine.isPageRequested(), "1");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/test.html");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertTrue(engine.isPageRequested(), "2");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/test.xhtml");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertTrue(engine.isPageRequested(), "3");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/test.mayaa");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertTrue(engine.isPageRequested(), "4");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/test.txt");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertFalse(engine.isPageRequested(), "5");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/docs/test.html");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertFalse(engine.isPageRequested(), "6");
    }

    @Test
    public void testIsPageRequested2() {
        EngineImpl engine = new EngineImpl();
        engine.setParameter(CONST_IMPL.NOT_TEMPLATE_PATH_PATTERN, ".*");
        engine.setParameter(CONST_IMPL.TEMPLATE_PATH_PATTERN, ".*\\.(html|xml|xhtml|mayaa)");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/test.xml");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertTrue(engine.isPageRequested(), "1");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/test.txt");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertFalse(engine.isPageRequested(), "2");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/docs/test.html");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertTrue(engine.isPageRequested(), "3");
    }

    @Test
    public void testIsPageRequested3() {
        EngineImpl engine = new EngineImpl();
        engine.setParameter(CONST_IMPL.TEMPLATE_PATH_PATTERN, ".*");
        engine.setParameter(CONST_IMPL.NOT_TEMPLATE_PATH_PATTERN, "/docs/.*");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/test.xml");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertTrue(engine.isPageRequested(), "1");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/test.txt");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertTrue(engine.isPageRequested(), "2");

        ManualProviderFactory.HTTP_SERVLET_REQUEST.setPathInfo("/docs/test.html");
        CycleUtil.initialize(ManualProviderFactory.HTTP_SERVLET_REQUEST, ManualProviderFactory.HTTP_SERVLET_RESPONSE);
        assertFalse(engine.isPageRequested(), "3");
    }

}
