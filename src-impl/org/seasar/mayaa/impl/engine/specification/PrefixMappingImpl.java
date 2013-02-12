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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PrefixMappingImpl implements PrefixMapping, Serializable {
    private static final long serialVersionUID = -7627574345551562433L;

    private static volatile Map _cache =
            Collections.synchronizedMap(new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT, true));

    public static PrefixMapping getInstance(String prefix, URI namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException();
        }
        // undeploy時に_cacheが消されたあとアクセスされる場合がある
        if (_cache == null) {
            return null;
        }

        String key = forPrefixMappingString(prefix, namespaceURI);

        PrefixMapping result = (PrefixMapping)_cache.get(key);
        if (result == null) {
            result = new PrefixMappingImpl(prefix, namespaceURI);
            _cache.put(key, result);
        }
        return result;
    }


    private String _prefix;
    private URI _namespaceURI;

    private PrefixMappingImpl(String prefix, URI namespaceURI) {
        if (StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        if (prefix == null) {
            prefix = "";
        }
        _prefix = prefix;
        _namespaceURI = namespaceURI;
    }

    public String getPrefix() {
        return _prefix;
    }

    public URI getNamespaceURI() {
        return _namespaceURI;
    }

    public static String forPrefixMappingString(
            String prefix, URI namespaceURI) {
        String uri = namespaceURI.toString();
        StringBuffer buffer = new StringBuffer(uri.length() + 10);
        if (StringUtil.hasValue(prefix)) {
            buffer.append(prefix);
        }
        buffer.append(':').append(uri);
        return buffer.toString();
    }

    public static PrefixMapping revertStringToMapping(String prefixMappingString) {
        if (prefixMappingString == null) {
            return null;
        }
        int index = prefixMappingString.indexOf(':');
        String prefix;
        String namespaceURI;
        if (index >= 0) {
            prefix = prefixMappingString.substring(0, index);
            namespaceURI = prefixMappingString.substring(index+1);
        } else if (String.valueOf((Object)null).equals(prefixMappingString)) {
            return null;
        } else {
            prefix = "";
            namespaceURI = prefixMappingString;
        }
        return getInstance(prefix, SpecificationUtil.createURI(namespaceURI));
    }

    public String toString() {
        return forPrefixMappingString(getPrefix(), getNamespaceURI());
    }

    public boolean equals(Object test) {
        if (test == null || (test instanceof PrefixMapping) == false) {
            return false;
        }
        PrefixMappingImpl ns = (PrefixMappingImpl) test;
        return getPrefix().equals(ns.getPrefix())
                && getNamespaceURI().equals(ns.getNamespaceURI());
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public static int keptSize() {
        return _cache.size();
    }
    private Object readResolve() {
        return getInstance(_prefix, _namespaceURI);
    }

}
