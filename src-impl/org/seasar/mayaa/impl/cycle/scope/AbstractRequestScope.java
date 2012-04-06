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
package org.seasar.mayaa.impl.cycle.scope;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractRequestScope
        extends AbstractWritableAttributeScope
        implements RequestScope, CONST_IMPL {

    private static final long serialVersionUID = 951877000090019212L;

    private String _pageName;
    private String _requestedSuffix;
    private String _extension;
    private String _mimeType;

    public void parsePath(String path) {
        String suffixSeparator = EngineUtil.getEngineSetting(
                SUFFIX_SEPARATOR, "$");
        String[] parsed = StringUtil.parsePath(path, suffixSeparator);
        if (parsed[0] != null && parsed[0].length() > 0 && parsed[0].charAt(0) != '/') {
            _pageName = "/" + parsed[0];
        } else {
            _pageName = parsed[0];
        }
        _requestedSuffix = parsed[1];
        _extension = parsed[2];
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        ApplicationScope application = cycle.getApplicationScope();
        _mimeType = application.getMimeType(path);
    }

    public void setForwardPath(String relativeUrlPath) {
        if (StringUtil.isEmpty(relativeUrlPath)) {
            throw new IllegalArgumentException();
        }
        parsePath(relativeUrlPath);
    }

    public String getScopeName() {
        return ServiceCycle.SCOPE_REQUEST;
    }

    public String getPageName() {
        if (_pageName == null) {
            parsePath(getRequestedPath());
        }
        return _pageName;
    }

    public String getRequestedSuffix() {
        if (_requestedSuffix == null) {
            parsePath(getRequestedPath());
        }
        return _requestedSuffix;
    }

    public String getExtension() {
        if (_extension == null) {
            parsePath(getRequestedPath());
        }
        return _extension;
    }

    public String getMimeType() {
        if (_mimeType == null) {
            parsePath(getRequestedPath());
        }
        return _mimeType;
    }

}
