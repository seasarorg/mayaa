/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.cycle.mock;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockRequest implements Request {

    private static final long serialVersionUID = 514052742599144710L;
    private Locale _locale;
    private Map _parameters = new HashMap();
    private Map _attributes = new HashMap();
    
    public Object getUnderlyingObject() {
        return null;
    }
    
    public Locale getLocale() {
        if(_locale == null) {
            throw new IllegalStateException();
        }
        return _locale;
    }

    public void setLocale(Locale locale) {
        if(locale == null) {
            throw new IllegalArgumentException();
        }
        _locale = locale;
    }

    public String getParameter(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        return (String)_parameters.get(name);
    }

    public Map getParameterMap() {
        return _parameters;
    }

    public String getPath() {
        return null;
    }

    public String getScopeName() {
        return ServiceCycle.SCOPE_REQUEST;
    }

    public Object getAttribute(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        return _attributes.get(name);
    }

    public void setAttribute(String name, Object attribute) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if(attribute != null) {
            _attributes.put(name, attribute);
        } else {
            _attributes.remove(name);
        }
    }
    
}
