/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.provider;

import org.seasar.maya.FactoryFactory;
import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.builder.TemplateBuilder;
import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.provider.ProviderFactory;
import org.seasar.maya.provider.ServiceProvider;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProviderUtil {
    
    public ProviderUtil() {
        // no instantiate.
    }
    
    private static ServiceProvider getServiceProvider() {
        ProviderFactory factory = FactoryFactory.getProviderFactory();
        return factory.getServiceProvider();
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
    
    public static Engine getEngine() {
        return getServiceProvider().getEngine();
    }
    
}
