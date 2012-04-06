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
package org.seasar.mayaa.impl.provider.factory;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.impl.MarshallUtil;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.provider.ServiceProvider;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptEnvirionmentTagHandler
        extends AbstractParameterAwareTagHandler {

    private ProviderTagHandler _parent;
    private ScriptEnvironment _beforeEnv;
    private ScriptEnvironment _currentEnv;

    public ScriptEnvirionmentTagHandler(
            ProviderTagHandler parent, ServiceProvider beforeProvider) {
        super("scriptEnvironment");
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        if (beforeProvider != null) {
            _beforeEnv = beforeProvider.getScriptEnvironment();
        }
        putHandler(new ScopeTagHandler(this));
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        Class environmentClass = XMLUtil.getClassValue(
                attributes, "class", null);
        _currentEnv = (ScriptEnvironment) MarshallUtil.marshall(
                environmentClass, ScriptEnvironment.class, _beforeEnv,
                systemID, lineNumber);
        _parent.getServiceProvider().setScriptEnvironment(_currentEnv);
    }

    protected void end(String body) {
        _currentEnv = null;
    }

    public ScriptEnvironment getScriptEnvironment() {
        if (_currentEnv == null) {
            throw new IllegalStateException();
        }
        return _currentEnv;
    }

    public ParameterAware getParameterAware() {
        return getScriptEnvironment();
    }

}
