/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.provider;

import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.builder.PathAdjuster;
import org.seasar.mayaa.builder.SpecificationBuilder;
import org.seasar.mayaa.builder.TemplateBuilder;
import org.seasar.mayaa.builder.library.LibraryManager;
import org.seasar.mayaa.builder.library.TemplateAttributeReader;
import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.specification.ParentSpecificationResolver;
import org.seasar.mayaa.provider.ProviderFactory;
import org.seasar.mayaa.provider.ServiceProvider;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProviderUtil {

    private ProviderUtil() {
        // no instantiate.
    }

    private static ServiceProvider getServiceProvider() {
        ProviderFactory factory =
            (ProviderFactory) FactoryFactory.getFactory(ProviderFactory.class);
        return factory.getServiceProvider();
    }

    public static boolean isInitialized() {
        ProviderFactory factory =
            (ProviderFactory) FactoryFactory.getFactory(ProviderFactory.class);
        return factory != null && factory.isServiceProviderInitialized();
    }

    public static LibraryManager getLibraryManager() {
        return getServiceProvider().getLibraryManager();
    }

    public static ScriptEnvironment getScriptEnvironment() {
        return getServiceProvider().getScriptEnvironment();
    }

    public static SpecificationBuilder getSpecificationBuilder() {
        return getServiceProvider().getSpecificationBuilder();
    }

    public static TemplateBuilder getTemplateBuilder() {
        return getServiceProvider().getTemplateBuilder();
    }

    public static PathAdjuster getPathAdjuster() {
        return getServiceProvider().getPathAdjuster();
    }

    public static Engine getEngine() {
        return getServiceProvider().getEngine();
    }

    public static TemplateAttributeReader getTemplateAttributeReader() {
        return getServiceProvider().getTemplateAttributeReader();
    }

    public static ParentSpecificationResolver getParentSpecificationResolver() {
        return getServiceProvider().getParentSpecificationResolver();
    }

}
