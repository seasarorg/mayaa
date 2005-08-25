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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.seasar.maya.impl.cycle.ServiceCycleImpl;
import org.seasar.maya.impl.cycle.web.WebApplication;
import org.seasar.maya.impl.cycle.web.WebRequest;
import org.seasar.maya.impl.cycle.web.WebResponse;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebServiceProvider implements ServiceProvider, CONST_IMPL {
    
    private Object _context;
    private Application _application;
    private Engine _engine;
    private ScriptCompiler _scriptCompiler;
    private SpecificationBuilder _specificationBuilder;
    private TemplateBuilder _templateBuilder;
    private Class _pageSourceClass;
    private Map _pageParams;
	private ThreadLocal _currentServiceCycle = new ThreadLocal();
	
    public WebServiceProvider(Object context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        _context = context;
    }
    
    public Application getApplication() {
        if(_application == null) {
            if(_context instanceof ServletContext == false) {
                throw new IllegalStateException();
            }
            ServletContext servletContext = (ServletContext)_context;
            WebApplication application = new WebApplication();
            application.setServletContext(servletContext);
            _application = application;
        }
        return _application;
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

    public void putPageSourceParameter(String name, String value) {
        if(StringUtil.isEmpty(name) || value == null) {
            throw new IllegalArgumentException();
        }
        if(_pageParams == null) {
            _pageParams = new HashMap();
        }
        _pageParams.put(name, value);
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
        if(_pageParams != null) {
            for(Iterator it = _pageParams.keySet().iterator(); it.hasNext(); ) {
                String key = (String)it.next();
                String value = (String)_pageParams.get(key);
                source.setParameter(key, value);
            }
        }
        return source;
    }

    private Request createRequest(HttpServletRequest request) {
    	if(request == null) {
    		throw new IllegalArgumentException();
    	}
    	WebRequest webRequest = new WebRequest();
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
    
	public void initialize(Object request, Object response) {
		if(request == null || response == null ||
                request instanceof HttpServletRequest == false ||
                response instanceof HttpServletResponse == false) {
			throw new IllegalArgumentException();
		}
    	ServiceCycleImpl cycle = (ServiceCycleImpl)_currentServiceCycle.get();
    	if(cycle == null) {
    		cycle = new ServiceCycleImpl(getApplication());
            SpecificationUtil.setEngine(cycle, getEngine());
    		_currentServiceCycle.set(cycle);
    	}
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;
		cycle.setRequest(createRequest(httpRequest));
        cycle.setResponse(createResponse(httpResponse));
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

