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

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NamespaceImpl implements Namespace {

    private static final Log LOG = LogFactory.getLog(NamespaceImpl.class);

    private Namespace _parentSpace;
    private Set _mappings;
    private PrefixMapping _defaultNamespaceMapping;

    public void setParentSpace(Namespace parent) {
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        _parentSpace = parent;
    }

    public Namespace getParentSpace() {
        return _parentSpace;
    }

    protected PrefixMapping createPrefixMapping(
            String prefix, String namespaceURI) {
        return PrefixMappingImpl.getInstance(prefix, namespaceURI);
    }

    private static Comparator _prefixNamespaceOrder = new Comparator() {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof PrefixMapping && o2 instanceof PrefixMapping) {
                String ns1 = ((PrefixMapping)o1).getNamespaceURI();
                String ns2 = ((PrefixMapping)o2).getNamespaceURI();
                return ns1.compareTo(ns2);
            }
            return 0;
        }

        public boolean equals(Object obj) {
            return obj == this;
        }

    };

    public void addPrefixMapping(String prefix, String namespaceURI) {
        if (prefix == null || StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        synchronized (this) {
            if (_mappings == null) {
                _mappings = new TreeSet(_prefixNamespaceOrder);
            }
            PrefixMapping mapping =
                createPrefixMapping(prefix, namespaceURI);
            if (_mappings.contains(mapping) == false) {
                _mappings.add(mapping);
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtil.getMessage(NamespaceImpl.class, 0,
                            mapping.toString()));
                }
            }
        }
    }

    public void setDefaultNamespaceURI(String namespaceURI) {
        if (namespaceURI == null) {
            _defaultNamespaceMapping = null;
            return;
        }
        for (Iterator it = iteratePrefixMapping(true); it.hasNext();) {
            PrefixMapping mapping = (PrefixMapping) it.next();
            if (mapping.getNamespaceURI().equals(namespaceURI)) {
                _defaultNamespaceMapping = mapping;
                return;
            }
        }
        if (namespaceURI.equals(CONST_IMPL.URI_HTML)) {
            _defaultNamespaceMapping =
                SpecificationUtil.HTML_DEFAULT_PREFIX_MAPPING;
            return;
        }
        throw new IllegalStateException("unregisted namespace");
    }

    public String getDefaultNamespaceURI() {
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
                    ? mapping.getPrefix() : mapping.getNamespaceURI();
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
            String namespaceURI, boolean all) {
        if (StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        return getMapping(false, namespaceURI, all);
    }

    public Iterator iteratePrefixMapping(boolean all) {
        if (all && getParentSpace() != null) {
            return new AllNamespaceIterator(this);
        }
        if (_mappings != null) {
            return _mappings.iterator();
        }
        return NullIterator.getInstance();
    }

    public boolean addedMapping() {
        if (_mappings == null) {
            return false;
        }
        synchronized (_mappings) {
            return _mappings.size() > 0;
        }
    }

    // support class -------------------------------------------------

    protected class AllNamespaceIterator implements Iterator {

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
                _current = _current.getParentSpace();
                if (_current != null) {
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

}
