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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.URI;

/**
 * @author Taro Kato (Gluegent, Inc.)
 * @deprecated
 */
public class FinalNamespace extends NamespaceImpl implements Namespace {
    private static final long serialVersionUID = -3137745935798561250L;

    private static Map _namespaces =
        new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT, true);

    private static volatile long _instanceTotal;
    static volatile long _finalLastKey = 1;

    public static Namespace getFinalInstance(Namespace namespace) {
        if (namespace == null) {
            return null;
        }
        if (namespace instanceof FinalNamespace) {
            return namespace;
        }
        String key = forNamespaceString(namespace);

        FinalNamespace result;
        synchronized(_namespaces) {
            result = (FinalNamespace) _namespaces.get(key);
            if (result == null) {
                result = new FinalNamespace(namespace);
                _instanceTotal++;
                _namespaces.put(key, result);
            }
        }
        return result;
    }

    public static String forNamespaceString(Namespace namespace) {
        return forNamespaceString(namespace, true);
    }

    private static String forNamespaceString(Namespace namespace, boolean allPrefixes) {
        if (namespace == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("parent={");
        Namespace parent = namespace.getParentSpace();
        if (parent != null) {
            if (parent instanceof FinalNamespace) {
                sb.append(((FinalNamespace)parent).getFinalKey());
            } else {
                throw new IllegalStateException("Set parents to IFinalNamespace previously");
            }
        } else {
            sb.append("null");
        }
        sb.append("},current={");
        sb.append(forNamespaceString0(namespace, allPrefixes));
        sb.append("}");
        sb.append(",defaultURI=");
        sb.append(namespace.getDefaultNamespaceURI());
        return sb.toString();
    }

    private static String forNamespaceString0(Namespace namespace, boolean allPrefixes) {
        StringBuffer sb = new StringBuffer();
        sb.append("prefixes={");
        sb.append(forNamespaceString1(namespace.iteratePrefixMapping(false)));
        sb.append("}");
        if (allPrefixes) {
            sb.append(",allprefixes={");
            sb.append(forNamespaceString1(namespace.iteratePrefixMapping(true)));
            sb.append("}");
        }
        return sb.toString();
    }

    private static String forNamespaceString1(Iterator prefixMappingIterator) {
        StringBuffer sb = new StringBuffer();
        Set sortSet = new TreeSet();
        while (prefixMappingIterator.hasNext()) {
            PrefixMapping prefixMapping = (PrefixMapping)prefixMappingIterator.next();
            sortSet.add(prefixMapping.toString());
        }
        Iterator it = sortSet.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private transient boolean _protect = true;
    private long _key;

    private FinalNamespace(Namespace copyFrom) {
        _key = _finalLastKey++;
        _protect = false;
        try {
            Namespace parentNamespace = getFinalInstance(copyFrom.getParentSpace());
            if (parentNamespace != null) {
                setParentSpace(parentNamespace);
            }
            Iterator it = copyFrom.iteratePrefixMapping(false);
            while (it.hasNext()) {
                PrefixMapping prefixMapping = (PrefixMapping)it.next();
                addPrefixMapping(prefixMapping.getPrefix(), prefixMapping.getNamespaceURI());
            }
            setDefaultNamespaceURI(copyFrom.getDefaultNamespaceURI());
        } finally {
            _protect = true;
        }
    }

    public void addPrefixMapping(String prefix, URI namespaceURI) {
        if (_protect)
            throw new UnsupportedOperationException();
        super.addPrefixMapping(prefix, namespaceURI);
    }

    public void setParentSpace(Namespace parent) {
        if (_protect || parent instanceof FinalNamespace == false)
            throw new UnsupportedOperationException();
        super.setParentSpace(parent);
    }

    public long getFinalKey() {
        return _key;
    }

    public static int keptSize() {
        return _namespaces.size();
    }
}
