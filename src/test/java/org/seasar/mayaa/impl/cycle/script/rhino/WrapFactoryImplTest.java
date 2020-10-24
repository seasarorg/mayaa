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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.seasar.mayaa.impl.cycle.web.RequestScopeImpl;
import org.seasar.mayaa.impl.cycle.web.ServiceCycleImpl;

/**
 * @author Koji Suga (Gluegent Inc.)
 */
public class WrapFactoryImplTest {

    private Context _cx;
    private Scriptable _scope;
    private WrapFactoryImpl _factory;

    @Before
    public void setUp() {
        _cx = Context.enter();
        _cx.setWrapFactory(new WrapFactoryImpl());
        _scope = _cx.initStandardObjects();
        _factory = new WrapFactoryImpl();
    }

    @After
    public void tearDown() {
        Context.exit();
    }

    /**
     * Test method for
     * {@link org.seasar.mayaa.impl.cycle.script.rhino.WrapFactoryImpl#wrapAsJavaObject(org.mozilla.javascript.Context, org.mozilla.javascript.Scriptable, java.lang.Object, java.lang.Class)}.
     */
    @Test
    public void testWrapAsJavaObjectContextScriptableObjectClass() {
        assertTrue(_factory.wrapAsJavaObject(_cx, _scope, new HashMap<Object,Object>(), null) instanceof NativeMap);
        assertTrue(_factory.wrapAsJavaObject(_cx, _scope, new ArrayList<Object>(), null) instanceof NativeList);
        assertTrue(
                _factory.wrapAsJavaObject(_cx, _scope, new RequestScopeImpl(), null) instanceof NativeAttributeScope);
        assertFalse(_factory.wrapAsJavaObject(_cx, _scope, _scope, null) instanceof NativeAttributeScope);
        assertTrue(_factory.wrapAsJavaObject(_cx, _scope, new ServiceCycleImpl(), null) instanceof NativeServiceCycle);
        DynaBean dynaBean = new BasicDynaBean(new BasicDynaClass());
        assertTrue(_factory.wrapAsJavaObject(_cx, _scope, dynaBean, null) instanceof NativeDynaBean);
    }

}
