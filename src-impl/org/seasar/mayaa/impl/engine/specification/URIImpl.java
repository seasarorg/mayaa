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
package org.seasar.mayaa.impl.engine.specification;

import java.util.Iterator;

import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.util.ReferenceCache;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class URIImpl implements URI {
    private static final long serialVersionUID = 7985133276316017754L;

    private static ReferenceCache _cache = new ReferenceCache(URIImpl.class);

    public static URIImpl getInstance(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        // undeploy時に_cacheが消されたあとアクセスされる場合がある
        if (_cache == null) {
            return null;
        }
        for (Iterator it = _cache.iterator(); it.hasNext(); ) {
            URIImpl namespaceURI = (URIImpl)it.next();
            if (namespaceURI.equals(uri)) {
                return namespaceURI;
            }
        }
        return new URIImpl(uri);
    }

    private String _value;
    private int _hashCode;

    private URIImpl() {
        // for serialize
    }

    private URIImpl(String uri) {
        setValue(uri);
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        if (_value != null) {
            _cache.remove(this);
        }
        _value = uri;
        if (_cache.contains(this) == false) {
            _cache.add(this);
        }
        _hashCode = (getClass().getName() + "|" + _value).hashCode();
    }

    public String toString() {
        return _value;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof URIImpl) {
            obj = ((URIImpl) obj)._value;
        }
        if (obj instanceof String) {
            return obj.equals(_value);
        }
        return false;
    }

    public int hashCode() {
        return _hashCode;
    }

    private Object readResolve() {
        return getInstance(_value);
    }

    public int compareTo(Object o) {
        if (o instanceof URI) {
            URI other = (URI)o;
            if (getValue() == null) {
                if (other.getValue() == null) {
                    return 0;
                }
                return -1;
            }
            return getValue().compareTo(other.getValue());
        }
        return 0;
    }

}