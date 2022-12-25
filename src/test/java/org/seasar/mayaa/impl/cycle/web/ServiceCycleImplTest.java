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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.cycle.CycleFactory;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceCycleImplTest {

    private ServiceCycle _cycle;

    @BeforeEach
    public void setUp() {
        ManualProviderFactory.setUp(this);
        CycleFactory factory = FactoryFactory.getCycleFactory();
        _cycle = factory.getServiceCycle();
    }

    @Test
    public void testGetApplication() {
        assertNotNull(_cycle.getApplicationScope());
    }

    @Test
    public void testGetRequest() {
        assertNotNull(_cycle.getRequestScope());
    }

    @Test
    public void testGetResponse() {
        assertNotNull(_cycle.getResponse());
    }

}
