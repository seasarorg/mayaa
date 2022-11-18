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
package org.seasar.mayaa.impl.cycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.cycle.ServiceCycle;

/**
 *
 * @version $Revision$ $Date$
 * @author suga
 */
public class StandardScopeTest {

    private StandardScope _standardScope;

    @BeforeEach
    public void setUp() {
        _standardScope = new StandardScope();
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.cycle.StandardScope.addScope(String)'
     */
    @Test
    public void testAddScope() {
        String newScope = "myscope";
        assertFalse(_standardScope.contains(newScope));

        _standardScope.addScope(newScope);
        assertTrue(_standardScope.contains(newScope));

        assertEquals(5, _standardScope.size());
        assertEquals(ServiceCycle.SCOPE_PAGE, _standardScope.get(0));
        assertEquals(newScope, _standardScope.get(1));
        assertEquals(ServiceCycle.SCOPE_REQUEST, _standardScope.get(2));
        assertEquals(ServiceCycle.SCOPE_SESSION, _standardScope.get(3));
        assertEquals(ServiceCycle.SCOPE_APPLICATION, _standardScope.get(4));
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.cycle.StandardScope.contains(String)'
     */
    @Test
    public void testContains() {
        assertTrue(_standardScope.contains(ServiceCycle.SCOPE_PAGE));
        assertTrue(_standardScope.contains(ServiceCycle.SCOPE_REQUEST));
        assertTrue(_standardScope.contains(ServiceCycle.SCOPE_SESSION));
        assertTrue(_standardScope.contains(ServiceCycle.SCOPE_APPLICATION));
    }

}
