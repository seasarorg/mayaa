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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationNodeImplTest {

    private SpecificationNodeImpl _node;

    @Before
    public void setUp() {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();

        _node = new SpecificationNodeImpl(SpecificationUtil.createQName("NODE_TEST"));
        _node.addAttribute(SpecificationUtil.createQName("ATTR_TEST1"), "VALUE1");
        _node.addAttribute(SpecificationUtil.createQName("ATTR_TEST2"), "VALUE2");
        _node.addAttribute(SpecificationUtil.createQName("ATTR_TEST2"), "VALUE2");
    }

    @After
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testGetAttribute() {
        NodeAttribute attr = _node.getAttribute(SpecificationUtil.createQName("ATTR_TEST1"));
        assertNotNull(attr);
        assertEquals("VALUE1", attr.getValue());
    }

    @Test
    public void testIterateAttributes() {
        Iterator<?> it = _node.iterateAttribute();
        assertNotNull(it.next());
        assertNotNull(it.next());
        assertFalse(it.hasNext());
    }

}
