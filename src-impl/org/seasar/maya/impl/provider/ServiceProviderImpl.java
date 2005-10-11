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

import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.builder.TemplateBuilder;
import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.provider.ServiceProvider;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceProviderImpl extends ParameterAwareImpl
		implements ServiceProvider, CONST_IMPL {
    
	private static final long serialVersionUID = -8659297907641816962L;

	private Engine _engine;
    private ScriptEnvironment _scriptEnvironment;
    private LibraryManager _libraryManager; 
    private SpecificationBuilder _specificationBuilder;
    private TemplateBuilder _templateBuilder;
	
    public void setEngine(Engine engine) {
        if(engine == null) {
            throw new IllegalArgumentException();
        }
        _engine = engine;
    }
    
    public Engine getEngine() {
    	if(_engine == null) {
    	    throw new IllegalStateException();
    	}
        return _engine;
    }
    
    public void setScriptEnvironment(ScriptEnvironment scriptEnvironment) {
        if(scriptEnvironment == null) {
            throw new IllegalArgumentException();
        }
        _scriptEnvironment = scriptEnvironment;
    }
    
    public ScriptEnvironment getScriptEnvironment() {
        if(_scriptEnvironment == null) {
            throw new IllegalStateException();
        }
        return _scriptEnvironment;
    }

    public void setLibraryManager(LibraryManager libraryManager) {
        if(libraryManager == null) {
            throw new IllegalArgumentException();
        }
        _libraryManager = libraryManager;
    }
    
    public LibraryManager getLibraryManager() {
        if(_libraryManager == null) {
            throw new IllegalStateException();
        }
        return _libraryManager;
    }
    
    public void setSpecificationBuilder(
    		SpecificationBuilder specificationBuilder) {
        if(specificationBuilder == null) {
            throw new IllegalArgumentException();
        }
        _specificationBuilder = specificationBuilder;
    }
    
    public SpecificationBuilder getSpecificationBuilder() {
    	if(_specificationBuilder == null) {
            throw new IllegalStateException();
    	}
        return _specificationBuilder;
    }
    
    public void setTemplateBuilder(TemplateBuilder templateBuilder) {
        if(templateBuilder == null) {
            throw new IllegalArgumentException();
        }
        _templateBuilder = templateBuilder;
    }
    
    public TemplateBuilder getTemplateBuilder() {
    	if(_templateBuilder == null) {
    	    throw new IllegalStateException();
    	}
        return _templateBuilder;
    }

}

