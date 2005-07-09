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
package org.seasar.maya.impl.cycle.local;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LocalRequest implements Request {

    private static final long serialVersionUID = 514052742599144710L;
    private Locale _locale;
    private Map _parameters = new HashMap();
    private Map _attributes = new HashMap();
    private String _path;
    
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
    
    public Iterator iterateParameterNames() {
        return _parameters.keySet().iterator();
    }
    
    public Map getParameterMap() {
        return _parameters;
    }

    public String[] getParameterValues(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        return (String[])_parameters.get(name);
    }

    public String getParameter(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        String values[] = getParameterValues(name);
        if(values != null && values.length > 0) {
            return values[0];
        }
        return null; 
    }

    public void addParameter(String name, String value) {
        if(StringUtil.isEmpty(name) || value == null) {
            throw new IllegalArgumentException();
        }
        String[] values = (String[])_parameters.get(name);
        if(values == null) {
            _parameters.put(name, new String[] { value });
        } else {
            int len = values.length;
            String[] newValues = new String[len + 1];
            System.arraycopy(values, 0, newValues, 0, len);
            newValues[len] = value;
            _parameters.put(name, newValues);
        }
    }
    
    public String getPath() {
        if(_path == null) {
            throw new IllegalStateException();
        }
        return _path;
    }

    public void setPath(String path) {
        if(path == null) {
            throw new IllegalArgumentException();
        }
        _path = StringUtil.preparePath(path);
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
