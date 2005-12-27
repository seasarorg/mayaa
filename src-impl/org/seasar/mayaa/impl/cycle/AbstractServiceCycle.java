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
package org.seasar.mayaa.impl.cycle;

import java.util.Iterator;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.cycle.scope.ScopeNotFoundException;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.source.ApplicationSourceDescriptor;
import org.seasar.mayaa.impl.source.SourceUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractServiceCycle
		extends ParameterAwareImpl implements ServiceCycle {

    private Object _context;
    private AttributeScope _page;
    private NodeTreeWalker _originalNode;
    private NodeTreeWalker _injectedNode;
    private ProcessorTreeWalker _processor;
    private Throwable _t;

    public void load(String systemID) {
        load(systemID, "UTF-8");
    }

    public void load(String systemID, String encoding) {
        if(StringUtil.isEmpty(systemID)) {
            throw new ScriptFileNotFoundException("");
        }
        String sid = systemID;
        if (sid.startsWith("/WEB-INF/")) {
            sid = sid.substring(9);
        } else if (sid.startsWith("/") == false) {
            String pageName = EngineUtil.getPageName();
            sid = StringUtil.adjustRelativeName(pageName, sid);
        }
        ApplicationSourceDescriptor appSource =
            new ApplicationSourceDescriptor();
        if (sid.startsWith("/") == false) {
            appSource.setRoot(ApplicationSourceDescriptor.WEB_INF);
        }
        appSource.setSystemID(sid);
        SourceDescriptor source = null;
        if(appSource.exists()) {
            source = appSource;
        } else {
            source = SourceUtil.getSourceDescriptor(sid);
            if(source.exists() == false) {
                source = null;
            }
        }
        if(source == null) {
            throw new ScriptFileNotFoundException(systemID);
        }
        ScriptEnvironment env = ProviderUtil.getScriptEnvironment();
        CompiledScript script = env.compile(source, encoding);
        script.execute(null);
    }

    public Iterator iterateAttributeScope() {
        Iterator it = ProviderUtil.getScriptEnvironment().iterateAttributeScope();
        return new ScopeIterator(this, it);
    }

    public boolean hasAttributeScope(String scopeName) {
        if(StringUtil.isEmpty(scopeName)) {
            scopeName = ServiceCycle.SCOPE_PAGE;
        }
        for(Iterator it = CycleUtil.getServiceCycle().iterateAttributeScope();
                it.hasNext(); ) {
            AttributeScope scope = (AttributeScope)it.next();
            if(scope.getScopeName().equals(scopeName)) {
                return true;
            }
        }
        return false;
    }
    
    public AttributeScope getAttributeScope(String scopeName) {
        if(StringUtil.isEmpty(scopeName)) {
            scopeName = ServiceCycle.SCOPE_PAGE;
        }
        for(Iterator it = CycleUtil.getServiceCycle().iterateAttributeScope();
                it.hasNext(); ) {
            AttributeScope scope = (AttributeScope)it.next();
            if(scope.getScopeName().equals(scopeName)) {
                return scope;
            }
        }
        throw new ScopeNotFoundException(scopeName);
    }

    public void setPageScope(AttributeScope page) {
        _page = page;
    }
    
    public AttributeScope getPageScope() {
        return _page;
    }

    public void setOriginalNode(NodeTreeWalker originalNode) {
        _originalNode = originalNode;
    }    

    public NodeTreeWalker getOriginalNode() {
        return _originalNode;
    }
    
	public void setInjectedNode(NodeTreeWalker injectedNode) {
		_injectedNode = injectedNode;
	}
    
    public NodeTreeWalker getInjectedNode() {
		return _injectedNode;
	}

    public void setProcessor(ProcessorTreeWalker processor) {
        _processor = processor;
    }

    public ProcessorTreeWalker getProcessor() {
        return _processor;
    }

    public void setHandledError(Throwable t) {
        _t = t;
    }

    public Throwable getHandledError() {
        return _t;
    }

    // ContextAware implements -------------------------------------
    
	public void setUnderlyingContext(Object context) {
		if(context == null) {
			throw new IllegalArgumentException();
		}
		_context = context;
	}
    
    public Object getUnderlyingContext() {
    	if(_context == null) {
    		throw new IllegalStateException();
    	}
		return _context;
	}

}
