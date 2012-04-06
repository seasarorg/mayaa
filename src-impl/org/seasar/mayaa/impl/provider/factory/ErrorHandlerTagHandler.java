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
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.error.ErrorHandler;
import org.seasar.mayaa.impl.MarshallUtil;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ErrorHandlerTagHandler
        extends AbstractParameterAwareTagHandler {

    private EngineTagHandler _parent;
    private ErrorHandler _beforeHandler;
    private ErrorHandler _currentHandler;

    public ErrorHandlerTagHandler(
            EngineTagHandler parent, Engine beforeEngine) {
        super("errorHandler");
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        if (beforeEngine != null) {
            _beforeHandler = beforeEngine.getErrorHandler();
        }
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        Class handlerClass = XMLUtil.getClassValue(
                attributes, "class", null);
        _currentHandler = (ErrorHandler) MarshallUtil.marshall(
                handlerClass, ErrorHandler.class, _beforeHandler,
                systemID, lineNumber);
        _parent.getEngine().setErrorHandler(_currentHandler);
    }

    protected void end(String body) {
        _currentHandler = null;
    }

    public ParameterAware getParameterAware() {
        if (_currentHandler == null) {
            throw new IllegalStateException();
        }
        return _currentHandler;
    }

}
