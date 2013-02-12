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
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PrefixAwareNameImpl implements PrefixAwareName, Serializable {
    private static final long serialVersionUID = -8898891078217203404L;

    private static Map _cache =
            Collections.synchronizedMap(new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT, true));

    public static PrefixAwareName getInstance(QName qName, String prefix) {
        String key = forPrefixAwareNameString(qName, prefix);
        PrefixAwareName result;
        synchronized (_cache) {
            result = (PrefixAwareName)_cache.get(key);
            if (result == null) {
                result = new PrefixAwareNameImpl(qName, prefix);
                _cache.put(key, result);
            }
        }
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
        StringBuffer buffer = new StringBuffer();
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
