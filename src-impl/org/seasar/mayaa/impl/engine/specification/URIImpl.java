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

import org.seasar.mayaa.engine.specification.URI;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public final class URIImpl implements URI {
    private static final long serialVersionUID = 7985133276316017754L;

    private static final Cache<String, URIImpl> _cache = Caffeine.newBuilder()
        .softValues()
        .build();

    public static URI NULL_NS_URI = new URIImpl("");

    public static URI getInstance(String uri) {
        if (uri == null || uri.isEmpty()) {
            return NULL_NS_URI;
        }
        URIImpl result = _cache.get(uri, k -> new URIImpl(uri));
        return result;
    }

    private String _value;

    private URIImpl() {
        // for serialize
    }

    private URIImpl(String uri) {
        _value = uri;
    }

    public String getValue() {
        return _value;
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
        return _value.hashCode();
    }

    // serializable
    private Object readResolve() {
        return getInstance(_value);
    }

    public int compareTo(URI o) {
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