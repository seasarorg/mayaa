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
package org.seasar.mayaa.impl.cycle.script.rhino;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SourceCompiledScriptImplTest {

    @BeforeEach
    public void setUp() {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
        ManualProviderFactory.SCRIPT_ENVIRONMENT.startScope(null);
    }

    @AfterEach
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testExec2() {
        ClassLoaderSourceDescriptor source =
                new ClassLoaderSourceDescriptor();
        source.setSystemID("TestScript.js");
        source.setNeighborClass(SourceCompiledScriptImplTest.class);
        SourceCompiledScriptImpl script = new SourceCompiledScriptImpl(
                source, "iso-8859-1");
        script.setExpectedClass(String.class);
        Object obj = script.execute(null);
        assertTrue(obj instanceof String);
        assertEquals("hi", obj);
    }

}
