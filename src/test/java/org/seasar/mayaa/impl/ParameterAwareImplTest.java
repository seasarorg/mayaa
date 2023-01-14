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
package org.seasar.mayaa.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class ParameterAwareImplTest {

    @Test
    public void testGetParameter() {
        ParameterAwareImpl paramAware = new ParameterAwareImpl();
        paramAware.setParameter("test", "testValue");
        assertEquals("testValue", paramAware.getParameter("test"));
    }

    @Test
    public void testIterateParameterNames() {
        ParameterAwareImpl paramAware = new ParameterAwareImpl();
        paramAware.setParameter("test1", "testValue1");
        paramAware.setParameter("test2", "testValue2");
        paramAware.setParameter("test3", "testValue3");
        Iterator<String> it = paramAware.iterateParameterNames();
        Set<String> set = new HashSet<>();
        set.add(it.next());
        set.add(it.next());
        set.add(it.next());
        assertTrue(set.contains("test1"));
        assertTrue(set.contains("test2"));
        assertTrue(set.contains("test3"));
    }

    @Test
    public void testGetSystemID() {
        ParameterAwareImpl paramAware = new ParameterAwareImpl();
        paramAware.setSystemID("/testID");
        assertEquals("/testID", paramAware.getSystemID());
    }

    @Test
    public void testGetLineNumber() {
        ParameterAwareImpl paramAware = new ParameterAwareImpl();
        paramAware.setLineNumber(5);
        assertEquals(5, paramAware.getLineNumber());
    }

}
