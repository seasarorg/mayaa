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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.seasar.mayaa.UnifiedFactory;
import org.seasar.mayaa.cycle.CycleFactory;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.impl.cycle.CycleFactoryImpl;
import org.seasar.mayaa.impl.source.ApplicationSourceDescriptor;
import org.seasar.mayaa.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.mayaa.source.SourceDescriptor;
import org.springframework.mock.web.MockServletContext;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class FactoryFactoryImplTest {

    @Test
    public void testCheckInterface() {
        FactoryFactoryImpl factory = new FactoryFactoryImpl();
        assertTrue(factory.checkInterface(CycleFactory.class));
        assertFalse(factory.checkInterface(CycleFactoryImpl.class));
        assertFalse(factory.checkInterface(null));
    }

    @Test
    public void testGetBootstrapApplication() {
        FactoryFactoryImpl factory = new FactoryFactoryImpl();
        ApplicationScope application = factory.getApplicationScope(new MockServletContext((String)null));
        assertNotNull(application);
    }

    @Test
    public void testGetBootstrapSource() {
        FactoryFactoryImpl factory = new FactoryFactoryImpl();
        SourceDescriptor source = factory.getBootstrapSource(ApplicationSourceDescriptor.WEB_INF, "/testID",
                new MockServletContext((String)null));
        assertNotNull(source);
        assertEquals("/testID", source.getSystemID());
    }

    @Test
    public void testMarshallFactory() {
        FactoryFactoryImpl factory = new FactoryFactoryImpl();
        CycleFactoryImpl before = new CycleFactoryImpl();
        before.setServiceClass(ServiceCycle.class);
        ClassLoaderSourceDescriptor source = new ClassLoaderSourceDescriptor();
        source.setSystemID("/test.factory");
        source.setNeighborClass(FactoryFactoryImpl.class);
        UnifiedFactory current = factory.marshallFactory(CycleFactory.class, new MockServletContext((String)null),
                source, before);
        assertEquals(ServiceCycle.class, current.getServiceClass());
    }

}
