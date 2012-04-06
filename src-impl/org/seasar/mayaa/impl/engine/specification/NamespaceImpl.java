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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.ReferenceCache;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NamespaceImpl implements Namespace {

    private static final long serialVersionUID = -3738362040016319461L;

    private static final Log LOG = LogFactory.getLog(NamespaceImpl.class);

    private static ReferenceCache/*<Namespace>*/ _cache =
        new ReferenceCache(Namespace.class);

    public static Namespace getInstance(Namespace namespace) {
        return getInstance(serialize(namespace));
    }

    public static Namespace getInstance(String serializeKey) {
        synchronized(_cache) {
            // キャッシュされたシングルトンを返す
            for (Iterator it = _cache.iterator(); it.hasNext(); ) {
                NamespaceImpl space = (NamespaceImpl) it.next();
                if (space.getSerializeKey().equals(serializeKey)) {
                    return space;
                }
            }
            Namespace namespace = deserialize(serializeKey);
            if (_cache.contains(namespace) == false) {
                _cache.add(namespace);
            }
            return namespace;
        }
    }

    public static Namespace copyOf(Namespace namespace) {
        if (namespace == null) {
            return namespace;
        }
        Namespace parent = namespace.getParentSpace();
        if (parent != null) {
            String key = serialize(parent);
            parent = getInstance(key);
        }
        NamespaceImpl result = new NamespaceImpl();
        result.setParentSpace(parent);
        for (Iterator it = namespace.iteratePrefixMapping(false); it.hasNext(); ) {
            PrefixMapping mapping = (PrefixMapping) it.next();
            result.addPrefixMapping(mapping.getPrefix(), mapping.getNamespaceURI());
        }
        result.setDefaultNamespaceURI(namespace.getDefaultNamespaceURI());
        if (namespace instanceof NamespaceImpl) {
            result._serializeKey = ((NamespaceImpl)namespace)._serializeKey;
        }
        return result;
    }

    private transient Namespace _parentSpace;
    private transient Set/*<PrefixMapping>*/ _mappings;
    private transient PrefixMapping _defaultNamespaceMapping;
    private String _serializeKey;
    private transient boolean _needDeserialize;

    public void setParentSpace(Namespace parent) {
        /*
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        */
        if (parent != null) {
            if (_parentSpace == parent || parent.equals(_parentSpace)) {
                return;
            }
        }
        _parentSpace = parent;
        _serializeKey = null;
    }

    public Namespace getParentSpace() {
        doDeserialize();
        return _parentSpace;
    }

    public void addPrefixMapping(String prefix, URI namespaceURI) {
        if (prefix == null || StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        synchronized (this) {
            doDeserialize();
            if (_mappings == null) {
                // テンプレートによってxmlnsの記述順が異なったとしても、
                // 同一のものを保証するためにソートする
                _mappings = new TreeSet(_prefixMappingComparator);
            }
            PrefixMapping mapping =
                SpecificationUtil.createPrefixMapping(prefix, namespaceURI);
            if (_mappings.contains(mapping) == false) {
                _mappings.add(mapping);
                _serializeKey = null;
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtil.getMessage(NamespaceImpl.class, 0,
                            mapping.toString()));
                }
            }
        }
    }

    public void setDefaultNamespaceURI(URI namespaceURI) {
        doDeserialize();
        if (namespaceURI == null) {
            if (_defaultNamespaceMapping != null) {
                _defaultNamespaceMapping = null;
                _serializeKey = null;
            }
            return;
        }
        for (Iterator it = iteratePrefixMapping(true); it.hasNext();) {
            PrefixMapping mapping = (PrefixMapping) it.next();
            if (mapping.getNamespaceURI().equals(namespaceURI)) {
                if (mapping.equals(_defaultNamespaceMapping) == false) {
                    _defaultNamespaceMapping = mapping;
                    _serializeKey = null;
                }
                return;
            }
        }
        if (namespaceURI.equals(CONST_IMPL.URI_HTML)) {
            if (SpecificationUtil.HTML_DEFAULT_PREFIX_MAPPING.equals(
                    _defaultNamespaceMapping) == false) {
                _defaultNamespaceMapping =
                    SpecificationUtil.HTML_DEFAULT_PREFIX_MAPPING;
                _serializeKey = null;
            }
            return;
        }
        if (namespaceURI.equals(CONST_IMPL.URI_XHTML)) {
            if (SpecificationUtil.XHTML_DEFAULT_PREFIX_MAPPING.equals(
                    _defaultNamespaceMapping) == false) {
                _defaultNamespaceMapping =
                    SpecificationUtil.XHTML_DEFAULT_PREFIX_MAPPING;
                _serializeKey = null;
            }
            return;
        }

        _defaultNamespaceMapping =
            SpecificationUtil.createPrefixMapping("", namespaceURI);
        _serializeKey = null;
    }

    protected void setDefaultNamespaceMapping(
            PrefixMapping defaultNamespaceMapping) {
        _defaultNamespaceMapping = defaultNamespaceMapping;
    }

    protected PrefixMapping getDefaultNamespaceMapping() {
        doDeserialize();
        return _defaultNamespaceMapping;
    }

    public URI getDefaultNamespaceURI() {
        doDeserialize();
        if (_defaultNamespaceMapping == null) {
            return null;
        }
        return _defaultNamespaceMapping.getNamespaceURI();
    }

    protected PrefixMapping getMapping(
            boolean fromPrefix, String test, boolean all) {
        if (test == null) {
            throw new IllegalArgumentException();
        }
        doDeserialize();
        if (_defaultNamespaceMapping != null) {
            if (fromPrefix) {
                if ("".equals(test) || _defaultNamespaceMapping.getPrefix().equals(test)) {
                    return _defaultNamespaceMapping;
                }
            } else {
                if (_defaultNamespaceMapping.getNamespaceURI().equals(test)) {
                    return _defaultNamespaceMapping;
                }
            }
        }
        for (Iterator it = iteratePrefixMapping(all); it.hasNext();) {
            PrefixMapping mapping = (PrefixMapping) it.next();
            String value = fromPrefix
                    ? mapping.getPrefix() : mapping.getNamespaceURI().getValue();
            if (test.equals(value)) {
                return mapping;
            }
        }
        if (CONST_IMPL.URI_XML.equals(test)) {
            return SpecificationUtil.XML_DEFAULT_PREFIX_MAPPING;
        }
        return null;
    }

    public PrefixMapping getMappingFromPrefix(String prefix, boolean all) {
        if (prefix == null) {
            prefix = "";
        }
        return getMapping(true, prefix, all);
    }

    public PrefixMapping getMappingFromURI(
            URI namespaceURI, boolean all) {
        if (StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        return getMapping(false, namespaceURI.getValue(), all);
    }

    public Iterator iteratePrefixMapping(boolean all) {
        if (all && getParentSpace() != null) {
            return new AllNamespaceIterator(this);
        }
        doDeserialize();
        if (_mappings != null) {
            return _mappings.iterator();
        }
        return NullIterator.getInstance();
    }

    public boolean addedMapping() {
        doDeserialize();
        if (_mappings == null) {
            return false;
        }
        synchronized (_mappings) {
            return _mappings.size() > 0;
        }
    }

    protected String getSerializeKey() {
        doDeserialize();
        serialize(this);
        return _serializeKey;
    }

    protected static String serialize(Namespace instance) {
        if (instance instanceof NamespaceImpl == false) {
            throw new IllegalStateException();
        }
        NamespaceImpl impl = (NamespaceImpl)instance;
        if (impl._serializeKey != null) {
            return impl._serializeKey;
        }

        List spaces = new ArrayList();
        Namespace parent = instance.getParentSpace();
        while (parent != null) {
            spaces.add(parent);
            parent = parent.getParentSpace();
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = spaces.size()-1; i >= 0; i--) {
            Namespace current = (Namespace)spaces.get(i);
            if (current instanceof NamespaceImpl) {
                NamespaceImpl currentImpl = (NamespaceImpl) current;
                buffer.append(currentImpl.namespaceToString());
            } else {
                throw new IllegalStateException();
            }
        }
        buffer.append(impl.namespaceToString());
        String result = buffer.toString();
        impl._serializeKey = result;
        return result;
    }

    protected static NamespaceImpl deserialize(String serializeData) {
        String[] lines = serializeData.split("\n");
        Namespace parent = null;
        NamespaceImpl current = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith("/")) {
                line = line.substring("/".length());
                if (current != null) {
                    synchronized(_cache) {
                        if (_cache.contains(current) == false) {
                            _cache.add(current);
                        }
                    }
                    parent = current;
                }
                current = new NamespaceImpl();

                if (String.valueOf((Object)null).equals(line) == false) {
                    current._defaultNamespaceMapping =
                        SpecificationUtil.createPrefixMapping(line);
                }
                if (parent != null) {
                    current.setParentSpace(parent);
                }
            } else if (line.startsWith("\t") && current != null) {
                line = line.substring("\t".length());
                // パフォーマンス重視のためにaddPrefixMappingを呼ばない
                if (current._mappings == null) {
                    current._mappings = new TreeSet(_prefixMappingComparator);
                }
                current._mappings.add(SpecificationUtil.createPrefixMapping(line));
            }
        }
        return current;
    }

    private String namespaceToString() {
        StringBuffer sb = new StringBuffer();
        sb.append("/").append(_defaultNamespaceMapping).append("\n");
        for (Iterator it = iteratePrefixMapping(false); it.hasNext(); ) {
            sb.append("\t").append(it.next()).append("\n");
        }
        return sb.toString();
    }

    public String toString() {
        return namespaceToString();
    }

    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(NamespaceImpl.class)) {
            NamespaceImpl other = (NamespaceImpl)obj;
            String otherKey = other.getSerializeKey();
            return getSerializeKey().equals(otherKey);
        }
        return false;
    }

    // TODO hashCodeを実装する

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        if (getClass() == NamespaceImpl.class) {
            getSerializeKey();
            out.defaultWriteObject();
        }
    }

    protected void doDeserialize() {
        if (_needDeserialize) {
            _needDeserialize = false;

            NamespaceImpl current = deserialize(getSerializeKey());

            _parentSpace = current._parentSpace;
            _defaultNamespaceMapping = current._defaultNamespaceMapping;
            _mappings = new TreeSet(_prefixMappingComparator);
            for (Iterator it = current.iteratePrefixMapping(false)
                    ; it.hasNext(); ) {
                _mappings.add(it.next());
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        if (getClass() == NamespaceImpl.class) {
            in.defaultReadObject();
            _needDeserialize = true;
        }
    }


    // support class -------------------------------------------------

    protected static class AllNamespaceIterator implements Iterator {

        private Namespace _current;
        private Iterator _it;

        public AllNamespaceIterator(Namespace current) {
            if (current == null) {
                throw new IllegalArgumentException();
            }
            _current = current;
            _it = current.iteratePrefixMapping(false);
        }

        public boolean hasNext() {
            while (_it != null) {
                boolean ret = _it.hasNext();
                if (ret) {
                    return true;
                }
                if (_current != null && (_current = _current.getParentSpace()) != null) {
                    _it = _current.iteratePrefixMapping(false);
                } else {
                    _it = null;
                }
            }
            return false;
        }

        public Object next() {
            if (hasNext()) {
                return _it.next();
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    protected static final Comparator _prefixMappingComparator = new Comparator() {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof PrefixMapping && o2 instanceof PrefixMapping) {
                PrefixMapping mapping1 = (PrefixMapping)o1;
                PrefixMapping mapping2 = (PrefixMapping)o2;
                URI ns1 = mapping1.getNamespaceURI();
                URI ns2 = mapping2.getNamespaceURI();
                int result = 0;
                if (ns1 == null) {
                    if (ns2 != null) {
                        result = -1;
                    }
                } else if (ns2 == null) {
                    result = 1;
                } else {
                    result = ns1.compareTo(ns2);
                }
                if (result == 0) {
                    String prefix1 = mapping1.getPrefix();
                    String prefix2 = mapping2.getPrefix();
                    if (prefix1 == null) {
                        prefix1 = "";
                    }
                    if (prefix2 == null) {
                        prefix2 = "";
                    }
                    result = prefix1.compareTo(prefix2);
                }
                return result;
            }
            throw new IllegalStateException();
        }

        public boolean equals(Object obj) {
            return obj == this;
        }

        public int hashCode() {
            return super.hashCode();
        }

    };

}
