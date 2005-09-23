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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;

import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.builder.TemplateBuilder;
import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.scope.ApplicationScope;
import org.seasar.maya.cycle.scope.RequestScope;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.web.ApplicationScopeImpl;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceProviderImpl implements ServiceProvider, CONST_IMPL {
    
    private Object _context;
    private ApplicationScope _application;
    private Class _serviceCycleClass;
    private Map _serviceCycleParams;
    private Engine _engine;
    private ScriptEnvironment _scriptEnvironment;
    private LibraryManager _libraryManager; 
    private SpecificationBuilder _specificationBuilder;
    private TemplateBuilder _templateBuilder;
    private Class _pageSourceClass;
    private Map _pageSourceParams;
	private ThreadLocal _currentServiceCycle = new ThreadLocal();
	
    public ServiceProviderImpl(Object context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        _context = context;
    }
    
    public ApplicationScope getApplication() {
        if(_application == null) {
            if(_context instanceof ServletContext == false) {
                throw new IllegalStateException();
            }
            ServletContext servletContext = (ServletContext)_context;
            _application = new ApplicationScopeImpl();
            _application.setUnderlyingObject(servletContext);
        }
        return _application;
    }

    public void setServiceCycleClass(Class serviceCycleClass) {
        if(serviceCycleClass == null) {
            throw new IllegalArgumentException();
        }
        _serviceCycleClass = serviceCycleClass;
    }

    public void setServiceCycleParameter(String name, String value) {
        if(StringUtil.isEmpty(name) || value == null) {
            throw new IllegalArgumentException();
        }
        if(_serviceCycleParams == null) {
            _serviceCycleParams = new HashMap();
        }
        _serviceCycleParams.put(name, value);
    }

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
    
    public void setSpecificationBuilder(SpecificationBuilder specificationBuilder) {
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
    
    public void setPageSourceClass(Class pageSourceClass) {
        if(pageSourceClass == null) {
            throw new IllegalArgumentException();
        }
        _pageSourceClass = pageSourceClass;
    }

    public void setPageSourceParameter(String name, String value) {
        if(StringUtil.isEmpty(name) || value == null) {
            throw new IllegalArgumentException();
        }
        if(_pageSourceParams == null) {
            _pageSourceParams = new HashMap();
        }
        _pageSourceParams.put(name, value);
    }
    
    public SourceDescriptor getPageSourceDescriptor(String systemID) {
        if(StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        if(_pageSourceClass == null) {
            throw new IllegalStateException();
        }
        SourceDescriptor source = 
            (SourceDescriptor)ObjectUtil.newInstance(_pageSourceClass);
        source.setSystemID(systemID);
        if(_pageSourceParams != null) {
            for(Iterator it = _pageSourceParams.keySet().iterator(); it.hasNext(); ) {
                String key = (String)it.next();
                String value = (String)_pageSourceParams.get(key);
                source.setParameter(key, value);
            }
        }
        return source;
    }

	public void initialize(Object requestContext, Object responseContext) {
		if(requestContext == null || responseContext == null) {
			throw new IllegalArgumentException();
		}
		ServiceCycle cycle = (ServiceCycle)_currentServiceCycle.get();
    	if(cycle == null) {
            if(_serviceCycleClass == null) {
                throw new IllegalStateException();
            }
    		cycle = (ServiceCycle)ObjectUtil.newInstance(_serviceCycleClass);
            if(_serviceCycleParams != null) {
                for(Iterator it = _serviceCycleParams.keySet().iterator(); it.hasNext(); ) {
                    String key = (String)it.next();
                    String value = (String)_serviceCycleParams.get(key);
                    cycle.setParameter(key, value);
                }
            }
    		_currentServiceCycle.set(cycle);
    	}
		RequestScope request = cycle.getRequest();
        request.setUnderlyingObject(requestContext);
        Response response = cycle.getResponse();
        response.setUnderlyingObject(responseContext);
	}

	public ServiceCycle getServiceCycle() {
		ServiceCycle cycle = (ServiceCycle)_currentServiceCycle.get();
		if(cycle == null) {
			throw new IllegalStateException();
		}
		return cycle;
    }

}

