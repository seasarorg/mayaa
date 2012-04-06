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

import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NodeAttributeImpl implements NodeAttribute {

    private static final long serialVersionUID = -1384526104972846069L;

    private transient SpecificationNode _node;
    private QName _qName;
    private String _value;
    private String _prefix; // 定義時のもの

    public NodeAttributeImpl(QName qName, String value) {
        this(qName, value, null);
    }

    public NodeAttributeImpl(QName qName, String value, String prefix) {
        if (qName == null || value == null) {
            throw new IllegalArgumentException();
        }
        _qName = qName;
        _value = value;
        _prefix = prefix;
    }

    public void setNode(SpecificationNode node) {
        if (node == null) {
            throw new IllegalArgumentException();
        }
        _node = node;
    }

    public SpecificationNode getNode() {
        return _node;
    }

    public String getValue() {
        return _value;
    }

    public String toString() {
        return getQName().toString() + "=\"" + getValue() + "\"";
    }

    // PrefixAwareName implements ----------------------------------------

    public QName getQName() {
        return _qName;
    }

    public String getPrefix() {
        if (_prefix == null) {
            URI namespaceURI = getQName().getNamespaceURI();
            PrefixMapping mapping = getMappingFromURI(namespaceURI, true);
            if (mapping != null) {
                if (StringUtil.equals(namespaceURI,
                        mapping.getNamespaceURI()) == false) {
                    return mapping.getPrefix();
                }
            }
            return "";
        }
        return _prefix;
    }

    // Namespace implemetns ----------------------------------------

    public void setParentSpace(Namespace parent) {
        throw new UnsupportedOperationException();
    }

    public Namespace getParentSpace() {
        if (getNode() != null) {
            return getNode().getParentSpace();
        }
        return null;
    }

    public void addPrefixMapping(String prefix, String namespaceURI) {
        throw new UnsupportedOperationException();
    }

    public PrefixMapping getMappingFromPrefix(String prefix, boolean all) {
        if (getNode() != null) {
            return getNode().getMappingFromPrefix(prefix, all);
        }
        return null;
    }

    public PrefixMapping getMappingFromURI(
            URI namespaceURI, boolean all) {
        if (getNode() != null) {
            return getNode().getMappingFromURI(namespaceURI, all);
        }
        return null;
    }

    public Iterator iteratePrefixMapping(boolean all) {
        if (getNode() != null) {
            return getNode().iteratePrefixMapping(all);
        }
        return NullIterator.getInstance();
    }

    public boolean addedMapping() {
        return false;
    }

    public URI getDefaultNamespaceURI() {
        if (getNode() != null) {
            return getNode().getDefaultNamespaceURI();
        }
        return null;
    }

    public void setDefaultNamespaceURI(URI namespaceURI) {
        if (getNode() != null) {
            getNode().setDefaultNamespaceURI(namespaceURI);
        }
    }
}
