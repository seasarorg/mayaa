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
package org.seasar.maya.impl.cycle;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.scope.ApplicationScope;
import org.seasar.maya.cycle.scope.AttributeScope;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.engine.specification.NodeTreeWalker;
import org.seasar.maya.impl.cycle.scope.ScopeNotFoundException;
import org.seasar.maya.impl.cycle.script.ScriptUtil;
import org.seasar.maya.impl.source.ApplicationSourceDescriptor;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractServiceCycle implements ServiceCycle {

    private AttributeScope _page;
    private NodeTreeWalker _originalNode;
    private NodeTreeWalker _injectedNode;
    private ProcessorTreeWalker _processor;
    
    public ApplicationScope getApplication() {
        return ProviderFactory.getServiceProvider().getApplication();
    }
    
    public void load(String systemID, String encoding) {
        if(StringUtil.isEmpty(systemID)) {
            throw new ScriptFileNotFoundException("");
        }
        String sid = systemID;
        if (sid.startsWith("/WEB-INF/")) {
            sid = sid.substring(9);
        }
        ApplicationSourceDescriptor appSource = new ApplicationSourceDescriptor();
        if (sid.startsWith("/") == false) {
            appSource.setRoot(ApplicationSourceDescriptor.WEB_INF);
        }
        appSource.setSystemID(sid);
        SourceDescriptor source = null;
        if(appSource.exists()) {
            source = appSource;
        } else {
            ServiceProvider provider = ProviderFactory.getServiceProvider();
            source = provider.getPageSourceDescriptor(sid);
            if(source.exists() == false) {
                source = null;
            }
        }
        if(source == null) {
            throw new ScriptFileNotFoundException(systemID);
        }
        ScriptEnvironment env = ScriptUtil.getScriptEnvironment();
        CompiledScript script = env.compile(source, encoding);
        script.execute();
    }

    public Iterator iterateAttributeScope() {
        Iterator it = ScriptUtil.getScriptEnvironment().iterateAttributeScope();
        return new ScopeIterator(it);
    }

    public boolean hasAttributeScope(String scopeName) {
        if(StringUtil.isEmpty(scopeName)) {
            scopeName = ServiceCycle.SCOPE_PAGE;
        }
        for(Iterator it = CycleUtil.getServiceCycle().iterateAttributeScope(); it.hasNext(); ) {
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
        for(Iterator it = CycleUtil.getServiceCycle().iterateAttributeScope(); it.hasNext(); ) {
            AttributeScope scope = (AttributeScope)it.next();
            if(scope.getScopeName().equals(scopeName)) {
                return scope;
            }
        }
        throw new ScopeNotFoundException(scopeName);
    }

    public void setPage(AttributeScope page) {
        _page = page;
    }
    
    public AttributeScope getPage() {
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
    
    // support class ---------------------------------------------------
    
    private class ScopeIterator implements Iterator {

        private Iterator _it;
        private String _current;
        
        public ScopeIterator(Iterator it) {
            if(it == null) {
                throw new IllegalArgumentException();
            }
            _it = it;
        }
        
        public boolean hasNext() {
            return SCOPE_APPLICATION.equals(_current) == false; 
        }

        public Object next() {
            AttributeScope scope = null;
            if(_current == null) {
                if(_page != null) {
                    scope = _page;
                    _current = SCOPE_PAGE;
                } else if(_it.hasNext()) {
                    scope = (AttributeScope)_it.next();
                    _current = scope.getScopeName();
                }
            } else if(SCOPE_REQUEST.equals(_current)) {
                scope = getSession();
                _current = SCOPE_SESSION;
            } else if(SCOPE_SESSION.equals(_current)) {
                scope = getApplication();
                _current = SCOPE_APPLICATION;
            } else if(SCOPE_APPLICATION.equals(_current)) {
                throw new NoSuchElementException();
            } else {
                if(_it.hasNext()) {
                    scope = (AttributeScope)_it.next();
                    _current = scope.getScopeName();
                } else {
                    scope = getRequest();
                    _current = SCOPE_REQUEST;
                }
            }
            return scope;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
