/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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

import java.util.Map;

import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.WeakValueHashMap;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PrefixMappingImpl implements PrefixMapping {
    private static Map _cache = new WeakValueHashMap();

    public static PrefixMapping getInstance(
            String prefix, String namespaceURI) {

        String key = forPrefixMappingString(prefix, namespaceURI);
        PrefixMapping result;
        synchronized (_cache) {
            result = (PrefixMapping)_cache.get(key);
            if (result == null) {
                result = new PrefixMappingImpl(prefix, namespaceURI);
                _cache.put(key, result);
            }
        }
        return result;

    }


    private String _prefix;
    private String _namespaceURI;

    private PrefixMappingImpl(String prefix, String namespaceURI) {
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

    public String getNamespaceURI() {
        return _namespaceURI;
    }

    public static String forPrefixMappingString(
            String prefix, String namespaceURI) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("xmlns");
        if (StringUtil.hasValue(prefix)) {
            buffer.append(":").append(prefix);
        }
        buffer.append("=").append(namespaceURI);
        return buffer.toString();
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

}
