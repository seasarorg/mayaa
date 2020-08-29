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
package org.seasar.mayaa.impl.engine.specification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.engine.specification.PrefixMapping;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NamespaceImplTest {

    private NamespaceImpl _namespace;

    @Before
    public void setUp() {
        _namespace = new NamespaceImpl();
        _namespace.addPrefixMapping("PREFIX_TEST1", SpecificationUtil.createURI("URI_TEST1"));
        _namespace.addPrefixMapping("PREFIX_TEST2", SpecificationUtil.createURI("URI_TEST2"));
        _namespace.addPrefixMapping("PREFIX_TEST3", SpecificationUtil.createURI("URI_TEST2"));
    }

    @Test
    public void testGetNamespace() {
        PrefixMapping mapping = _namespace.getMappingFromPrefix("PREFIX_TEST1", false);
        assertNotNull(mapping);
        assertEquals("PREFIX_TEST1", mapping.getPrefix());
        assertEquals("URI_TEST1", mapping.getNamespaceURI().toString());
    }

    @Test
    public void testIterateNamespace() {
        Iterator<?> it = _namespace.iteratePrefixMapping(false);
        assertNotNull(it.next());
        assertNotNull(it.next());
        assertNotNull(it.next());
        assertFalse(it.hasNext());
    }

}
