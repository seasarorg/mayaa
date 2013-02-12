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
package org.seasar.mayaa.impl.cycle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.cycle.scope.ScopeNotFoundException;
import org.seasar.mayaa.impl.cycle.script.rhino.PageAttributeScope;
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
        extends ParameterAwareImpl implements ServiceCycle, CONST_IMPL {

    private static final long serialVersionUID = -4084527796306356704L;

    private static final Map _scriptCache =
            Collections.synchronizedMap(new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT, true));
    private AttributeScope _page;
    private NodeTreeWalker _originalNode;
    private NodeTreeWalker _injectedNode;
    private ProcessorTreeWalker _processor;
    private Throwable _t;

    public void load(String systemID) {
        load(systemID, SCRIPT_DEFAULT_CHARSET);
    }

    public void load(String systemID, String encoding) {
        if (StringUtil.isEmpty(systemID)) {
            throw new ScriptFileNotFoundException(systemID);
        }
        String sid = systemID;
        if (sid.startsWith("/WEB-INF/")) {
            sid = sid.substring(9);
        } else if (StringUtil.isRelativePath(sid)) {
            String sourcePath = EngineUtil.getSourcePath();
            sid = StringUtil.adjustRelativePath(sourcePath, sid);
        }

        CompiledScript script = getScript(sid, encoding);
        if (script == null) {
            throw new ScriptFileNotFoundException(systemID);
        }

        script.execute(null);
    }

    protected CompiledScript getScript(String systemID, String encoding) {
        CompiledScript script = (CompiledScript) _scriptCache.get(systemID);
        if (script != null) {
            return script;
        }

        ApplicationSourceDescriptor appSource =
            new ApplicationSourceDescriptor();
        if (systemID.startsWith("/") == false) {
            appSource.setRoot(ApplicationSourceDescriptor.WEB_INF);
        }
        appSource.setSystemID(systemID);
        SourceDescriptor source;
        if (appSource.exists()) {
            source = appSource;
        } else {
            source = SourceUtil.getSourceDescriptor(systemID);
            if (source.exists() == false) {
                return null;
            }
        }

        ScriptEnvironment env = ProviderUtil.getScriptEnvironment();
        script = env.compile(source, encoding);
        _scriptCache.put(systemID, script);
        return script;
    }

    public Iterator iterateAttributeScope() {
        Iterator it = ProviderUtil.getScriptEnvironment().iterateAttributeScope();
        return new ScopeIterator(this, it);
    }

    private static String ATTRIBUTE_SCOPE_CACHE_KEY = "ATTRIBUTE_SCOPE_CACHE";
    static {
        CycleUtil.registVariableFactory(ATTRIBUTE_SCOPE_CACHE_KEY,
                new DefaultCycleLocalInstantiator() {
                    public Object create(Object[] params) {
                        return new HashMap();
                    }
                });
    }

    private Map getAttributeScopeCacheMap() {
        return (Map) CycleUtil.getGlobalVariable(ATTRIBUTE_SCOPE_CACHE_KEY, null);
    }

    public AttributeScope findAttributeScope(String scopeName) {
        boolean currentPageScope = false;
        if (scopeName == null) {
            currentPageScope = true;
            scopeName = ServiceCycle.SCOPE_PAGE;
        }
        if (currentPageScope == false
                && getAttributeScopeCacheMap().containsKey(scopeName)) {
            return (AttributeScope) getAttributeScopeCacheMap().get(scopeName);
        }
        for (Iterator it = iterateAttributeScope(); it.hasNext();) {
            AttributeScope scope = (AttributeScope) it.next();
            if (scope.getScopeName().equals(scopeName)) {
                if (currentPageScope) {
                    scope =
                        (AttributeScope)scope.getAttribute(
                                PageAttributeScope.KEY_CURRENT);
                    return scope;
                }
                getAttributeScopeCacheMap().put(scopeName, scope);
                return scope;
            }
        }
        getAttributeScopeCacheMap().put(scopeName, null);
        return null;
    }

    public boolean hasAttributeScope(String scopeName) {
        if (StringUtil.isEmpty(scopeName)) {
            //scopeName = ServiceCycle.SCOPE_PAGE;
            scopeName = null;
        }
        return findAttributeScope(scopeName) != null;
    }

    public AttributeScope getAttributeScope(String scopeName) {
        if (StringUtil.isEmpty(scopeName)) {
            //scopeName = ServiceCycle.SCOPE_PAGE;
            scopeName = null;
        }
        AttributeScope result = findAttributeScope(scopeName);
        if (result == null) {
            throw new ScopeNotFoundException(scopeName);
        }
        return result;
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

}
