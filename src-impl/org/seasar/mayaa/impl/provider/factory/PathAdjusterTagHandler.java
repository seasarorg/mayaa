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
import org.seasar.mayaa.builder.PathAdjuster;
import org.seasar.mayaa.impl.MarshallUtil;
import org.seasar.mayaa.impl.builder.PathAdjusterImpl;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.provider.ServiceProvider;
import org.xml.sax.Attributes;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class PathAdjusterTagHandler extends AbstractParameterAwareTagHandler {

    private static Class<PathAdjuster> INTERFACE_CLASS = PathAdjuster.class;
    private static Class<? extends PathAdjuster> DEFAULT_IMPL_CLASS = PathAdjusterImpl.class;

    private ProviderTagHandler _parent;
    private PathAdjuster _beforeAdjuster;
    private PathAdjuster _currentAdjuster;

    public PathAdjusterTagHandler(
            ProviderTagHandler parent, ServiceProvider beforeProvider) {
        super("pathAdjuster");
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        if (beforeProvider != null) {
            _beforeAdjuster = beforeProvider.getPathAdjuster();
        }
        _parent = parent;
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        Class<?> adjusterClass = XMLUtil.getClassValue(attributes, "class", DEFAULT_IMPL_CLASS);
        _currentAdjuster = MarshallUtil.marshall(
                adjusterClass, INTERFACE_CLASS, _beforeAdjuster,
                systemID, lineNumber);
        _parent.getServiceProvider().setPathAdjuster(_currentAdjuster);
    }

    protected void end(String body) {
        if (_currentAdjuster == null) {
            throw new IllegalStateException();
        }
        _currentAdjuster = null;
    }

    public PathAdjuster getPathAdjuster() {
        if (_currentAdjuster == null) {
            throw new IllegalStateException();
        }
        return _currentAdjuster;
    }

    public ParameterAware getParameterAware() {
        return getPathAdjuster();
    }

}
