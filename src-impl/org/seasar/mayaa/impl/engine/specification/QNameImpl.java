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
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author Taro Kato (Gluegent, Inc.)
 */
public class QNameImpl implements QName, CONST_IMPL, Serializable {
    private static final long serialVersionUID = -102674132611191747L;

    private static volatile Map _cache =
        Collections.synchronizedMap(new ReferenceMap(AbstractReferenceMap.HARD, AbstractReferenceMap.SOFT, true));

    public static QName getInstance(String localName) {
        return getInstance(URI_MAYAA, localName);
    }

    public static QName getInstance(URI namespaceURI, String localName) {
        // undeploy時に_cacheが消されたあとアクセスされる場合がある
        if (_cache == null) {
            return null;
        }

        String key = forQNameString(namespaceURI, localName);

        // 一時的に重複しても問題ないので速度を優先する。（synchronizeを外した）
        QName result = (QName)_cache.get(key);
        if (result == null) {
            result = new QNameImpl(namespaceURI, localName);
            _cache.put(key, result);
        }
        return result;
    }

    private URI _namespaceURI;
    private String _localName;

    private QNameImpl() {
        // for serialize
    }

    private QNameImpl(URI namespaceURI, String localName) {
        if (StringUtil.isEmpty(namespaceURI) || StringUtil.isEmpty(localName)) {
            throw new IllegalArgumentException();
        }
        _namespaceURI = namespaceURI;
        _localName = localName;
    }

    public URI getNamespaceURI() {
        return _namespaceURI;
    }

    public String getLocalName() {
        return _localName;
    }

    /**
     * "{URI}localName"形式の文字列を返します。
     *
     * @param namespaceURI 名前空間のURI
     * @param localName ローカル名
     * @return "{URI}localName"形式の文字列
     */
    private static String forQNameString(URI namespaceURI, String localName) {
        String namespace = namespaceURI.getValue();
        int namespaceLength = namespace.length();
        int localNameLength = localName.length();
        char[] buffer = new char[namespaceLength + localNameLength + 2];
        buffer[0] = '{';
        namespace.getChars(0, namespaceLength, buffer, 1);
        buffer[namespaceLength + 1] = '}';
        localName.getChars(0, localNameLength, buffer, namespaceLength + 2);
        return new String(buffer);
    }

    public String toString() {
        return forQNameString(getNamespaceURI(), getLocalName());
    }

    public boolean equals(Object test) {
        if (test instanceof QName) {
            QName qName = (QName) test;
            URI uri = getNamespaceURI();
            URI otherURI = qName.getNamespaceURI();
            return (uri == otherURI || uri.getValue().equals(otherURI.getValue()))
                && getLocalName().equals(qName.getLocalName());
            // TODO 大文字小文字を区別すべきか否か検討
            //  && getLocalName().equalsIgnoreCase(qName.getLocalName());
        }
        return false;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    private Object readResolve() {
        return getInstance(_namespaceURI, _localName);
    }

}
