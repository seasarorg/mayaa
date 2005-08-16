/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.provider;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.builder.TemplateBuilder;
import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.ScriptCompiler;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.web.WebApplication;
import org.seasar.maya.impl.cycle.web.WebRequest;
import org.seasar.maya.impl.cycle.web.WebResponse;
import org.seasar.maya.impl.cycle.web.WebServiceCycle;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SimpleServiceProvider implements ServiceProvider, CONST_IMPL {
    
    private ServletContext _servletContext;
    private Application _application;
    private Engine _engine;
    private SourceFactory _sourceFactory;
    private ScriptCompiler _scriptCompiler;
    private SpecificationBuilder _specificationBuilder;
    private TemplateBuilder _templateBuilder;
	private ThreadLocal _currentServiceCycle;
	
    public SimpleServiceProvider(ServletContext servletContext) {
        if(servletContext == null) {
            throw new IllegalArgumentException();
        }
        _servletContext = servletContext;
        _currentServiceCycle = new ThreadLocal();
    }
    
    public ServletContext getServletContext() {
        return _servletContext;
    }
    
    public Application getApplication() {
        if(_application == null) {
            WebApplication application = new WebApplication();
            application.setServletContext(_servletContext);
            _application = application;
        }
        return _application;
    }
    
    public boolean hasSourceFactory() {
        return _sourceFactory != null;
    }
    
    public void setSourceFactory(SourceFactory sourceFactory) {
        if(sourceFactory == null) {
            throw new IllegalArgumentException();
        }
        _sourceFactory = sourceFactory;
    }
    
    public SourceFactory getSourceFactory() {
        if(_sourceFactory == null) {
            throw new IllegalStateException();
        }
    	return _sourceFactory;
    }
    
    public boolean hasEngine() {
        return _engine != null;
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
    
    public boolean hasScriptCompiler() {
        return _scriptCompiler != null;
    }
    
    public void setScriptCompiler(ScriptCompiler scriptCompiler) {
        if(scriptCompiler == null) {
            throw new IllegalArgumentException();
        }
        _scriptCompiler = scriptCompiler;
    }
    
    public ScriptCompiler getScriptCompiler() {
        if(_scriptCompiler == null) {
            throw new IllegalStateException();
        }
        return _scriptCompiler;
    }
    
    public boolean hasSpecificationBuilder() {
        return _specificationBuilder != null;
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
    
    public boolean hasTemplateBuilder() {
        return _templateBuilder != null;
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

    private Request createRequest(HttpServletRequest request) {
    	if(request == null) {
    		throw new IllegalArgumentException();
    	}
        String suffixSeparator = _engine.getEngineSetting().getSuffixSeparator();
    	WebRequest webRequest = new WebRequest(suffixSeparator);
    	webRequest.setHttpServletRequest(request);
    	return webRequest;
    }
    
    private Response createResponse(HttpServletResponse response) {
    	if(response == null) {
    		throw new IllegalArgumentException();
    	}
    	WebResponse webResponse = new WebResponse();
    	webResponse.setHttpServletResponse(response);
    	return webResponse;
    }
    
	public void initialize(HttpServletRequest request, HttpServletResponse response) {
		if(request == null || response == null) {
			throw new IllegalArgumentException();
		}
    	WebServiceCycle cycle = (WebServiceCycle)_currentServiceCycle.get();
    	if(cycle == null) {
    		cycle = new WebServiceCycle(getApplication());
            SpecificationUtil.setEngine(cycle, getEngine());
    		_currentServiceCycle.set(cycle);
    	}
		cycle.setRequest(createRequest(request));
        cycle.setResponse(createResponse(response));
	}

	public ServiceCycle getServiceCycle() {
		ServiceCycle cycle = (ServiceCycle)_currentServiceCycle.get();
		if(cycle == null) {
			throw new IllegalStateException();
		}
		return cycle;
    }
    
	public Object getModel(Object modelKey, String modelScope) {
        if(modelKey instanceof Class == false) {
            throw new IllegalArgumentException();
        }
        Class modelClass = (Class)modelKey;
        String modelName = modelClass.getName();
        ServiceCycle cycle = getServiceCycle(); 
        AttributeScope attrScope = cycle.getAttributeScope(modelScope);
        Object model = attrScope.getAttribute(modelName); 
        if(model != null) {
            return model;
        }
        model = ObjectUtil.newInstance(modelClass);
        attrScope.setAttribute(modelName, model);
        return model;
	}

}

