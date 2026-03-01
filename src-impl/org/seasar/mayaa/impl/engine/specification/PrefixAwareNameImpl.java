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

import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.management.CacheControllerRegistry;
import org.seasar.mayaa.impl.util.StringUtil;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PrefixAwareNameImpl implements PrefixAwareName {
    private static final long serialVersionUID = -8898891078217203404L;

    private static final Cache<String, PrefixAwareName> _cache = Caffeine.newBuilder()
        .softValues()
        .build();
    
    static {
        CacheControllerRegistry.registerCacheController("PrefixAwareName", _cache);
    }

    public static PrefixAwareName getInstance(QName qName, String prefix) {
        String key = forPrefixAwareNameString(qName, prefix);
        PrefixAwareName result = _cache.get(key, k -> new PrefixAwareNameImpl(qName, prefix) );
        return result;
    }

    private QName _qName;
    private String _prefix;

    private PrefixAwareNameImpl() {
        // for serialize
    }

    private PrefixAwareNameImpl(QName qName, String prefix) {
        if (qName == null || prefix == null) {
            throw new IllegalArgumentException();
        }
        _qName = qName;
        _prefix = prefix;
    }

    public QName getQName() {
        return _qName;
    }

    public String getPrefix() {
        return _prefix;
    }

    public String toString() {
        return forPrefixAwareNameString(getQName(), getPrefix());
    }

    public boolean equals(Object test) {
        return getQName().equals(test);
    }

    public int hashCode() {
        return getQName().hashCode();
    }

    public static String forPrefixAwareNameString(QName qName, String prefix) {
        StringBuilder buffer = new StringBuilder();
        if (StringUtil.hasValue(prefix)) {
            buffer.append(prefix).append(":");
        }
        buffer.append(qName.toString());
        return buffer.toString();
    }

    private Object readResolve() {
        return getInstance(_qName, _prefix);
    }

}
