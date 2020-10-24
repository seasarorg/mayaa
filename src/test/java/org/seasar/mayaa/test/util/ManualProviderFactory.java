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
package org.seasar.mayaa.test.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;

import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.UnifiedFactory;
import org.seasar.mayaa.cycle.CycleFactory;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.impl.FactoryFactoryImpl;
import org.seasar.mayaa.impl.builder.SpecificationBuilderImpl;
import org.seasar.mayaa.impl.builder.TemplateBuilderImpl;
import org.seasar.mayaa.impl.cycle.CycleFactoryImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.script.rhino.ScriptEnvironmentImpl;
import org.seasar.mayaa.impl.cycle.web.ApplicationScopeImpl;
import org.seasar.mayaa.impl.cycle.web.ServiceCycleImpl;
import org.seasar.mayaa.impl.engine.EngineImpl;
import org.seasar.mayaa.impl.provider.ProviderFactoryImpl;
import org.seasar.mayaa.impl.provider.ServiceProviderImpl;
import org.seasar.mayaa.impl.source.PageSourceDescriptor;
import org.seasar.mayaa.impl.source.PageSourceFactoryImpl;
import org.seasar.mayaa.provider.ProviderFactory;
import org.seasar.mayaa.provider.ServiceProvider;
import org.seasar.mayaa.source.PageSourceFactory;
import org.seasar.mayaa.source.SourceDescriptor;
import org.seasar.mayaa.test.mock.MockHttpServletRequest;
import org.seasar.mayaa.test.mock.MockHttpServletResponse;
import org.seasar.mayaa.test.mock.MockServletContext;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ManualProviderFactory extends FactoryFactory {

    private static final long serialVersionUID = -8568174561545251202L;

    public static ManualProviderFactory MOCK_INSTANCE;
    public static MockServletContext SERVLET_CONTEXT;
    public static MockHttpServletRequest HTTP_SERVLET_REQUEST;
    public static MockHttpServletResponse HTTP_SERVLET_RESPONSE;

    public static ServiceProviderImpl PROVIDER;
    public static EngineImpl ENGINE;
    public static ScriptEnvironmentImpl SCRIPT_ENVIRONMENT;
    public static SpecificationBuilderImpl SPECIFICATION_BUILDER;
    public static TemplateBuilderImpl TEMPLATE_BUILDER;

    protected CycleFactory _factory;
    private SourceDescriptor _bootstrapSource;

    public ManualProviderFactory(Object targetTestCase) {
        SERVLET_CONTEXT = new MockServletContext(targetTestCase.getClass());
        setContext(SERVLET_CONTEXT);

        PROVIDER = new ServiceProviderImpl();
        ENGINE = new EngineImpl();
        PROVIDER.setEngine(ENGINE);
        SCRIPT_ENVIRONMENT = new ScriptEnvironmentImpl();
        SCRIPT_ENVIRONMENT.setParameter("wrapFactory",
                "org.seasar.mayaa.impl.cycle.script.rhino.WrapFactoryImpl");
        PROVIDER.setScriptEnvironment(SCRIPT_ENVIRONMENT);
        SPECIFICATION_BUILDER = new SpecificationBuilderImpl();
        PROVIDER.setSpecificationBuilder(SPECIFICATION_BUILDER);
        TEMPLATE_BUILDER = new TemplateBuilderImpl();
        PROVIDER.setTemplateBuilder(TEMPLATE_BUILDER);

        HTTP_SERVLET_REQUEST =
            new MockHttpServletRequest(SERVLET_CONTEXT);
        HTTP_SERVLET_RESPONSE = new MockHttpServletResponse();
    }

    public static void setUp(Object test) {
        MOCK_INSTANCE = new ManualProviderFactory(test);
        setInstance(MOCK_INSTANCE);
    }

    public static void tearDown() {
        SERVLET_CONTEXT = null;
        HTTP_SERVLET_REQUEST = null;
        HTTP_SERVLET_RESPONSE = null;
        PROVIDER = null;
        ENGINE = null;
        SCRIPT_ENVIRONMENT = null;
        SPECIFICATION_BUILDER = null;
        TEMPLATE_BUILDER = null;

        release();
        try {
            Field singletonField = CycleUtil.class.getDeclaredField("_singleton");
            singletonField.setAccessible(true);
            Object singleton = singletonField.get(null);
            Field factoryField = CycleUtil.class.getDeclaredField("_factory");
            factoryField.setAccessible(true);
            factoryField.set(singleton, null);

            FactoryFactory.setInstance(new FactoryFactoryImpl());
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setBootstrapSource(SourceDescriptor bootstrapSource) {
        _bootstrapSource = bootstrapSource;
    }

    protected UnifiedFactory getFactory(
            Class<?> interfaceClass, Object context) {
        if(interfaceClass == null || context == null) {
            throw new IllegalArgumentException();
        }

        if(CycleFactory.class.isAssignableFrom(interfaceClass)) {
            if (_factory == null) {
                _factory = new CycleFactoryImpl();
                _factory.setServiceClass(ServiceCycleImpl.class);
                _factory.setUnderlyingContext(SERVLET_CONTEXT);
                _factory.initialize(HTTP_SERVLET_REQUEST, HTTP_SERVLET_RESPONSE);
            }
            return _factory;
        }
        if(ProviderFactory.class.isAssignableFrom(interfaceClass)) {
            return new ProviderFactoryImpl() {
                private static final long serialVersionUID =
                    -7327918968313668847L;
                public ServiceProvider getServiceProvider() {
                    return PROVIDER;
                }
                public boolean isServiceProviderInitialized() {
                    return true;
                }
            };
        }
        if(PageSourceFactory.class.isAssignableFrom(interfaceClass)) {
            return getPageSourceFactory(context);
        }
        throw new IllegalArgumentException();
    }

    protected PageSourceFactory getPageSourceFactory(Object context) {
        PageSourceFactoryImpl factory = new PageSourceFactoryImpl();
        factory.setServiceClass(PageSourceDescriptor.class);
        return factory;   
    }

    protected ApplicationScope getApplicationScope(Object context) {
        ApplicationScopeImpl scope = new ApplicationScopeImpl();
        scope.setUnderlyingContext(SERVLET_CONTEXT);
        return scope;
    }

    protected SourceDescriptor getBootstrapSource(
            String root, String systemID, Object context) {
        return _bootstrapSource;
    }

    public void setParameter(String name, String value) {
        // do nothing.
    }

    public String getParameter(String name) {
        return null;
    }

    public Iterator<String> iterateParameterNames() {
        return Collections.emptyIterator();
    }

}
