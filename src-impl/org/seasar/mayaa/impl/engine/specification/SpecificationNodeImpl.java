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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.seasar.mayaa.engine.specification.CopyToFilter;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeObject;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationNodeImpl extends NamespaceImpl
        implements SpecificationNode, CONST_IMPL {

    private static final CopyToFilter FILTER_ALL = new AllCopyToFilter();

    private int _sequenceID = -1;
    private Map _attributes;
    private NodeTreeWalker _parent;
    private List _childNodes;
    private String _systemID;
    private int _lineNumber;
    private boolean _onTemplate;
    private QName _qName;

    public SpecificationNodeImpl(QName qName) {
        _qName = qName;
    }

    public QName getQName() {
        return _qName;
    }

    public String getPrefix() {
        String namespaceURI = getQName().getNamespaceURI();
        for (Iterator it = iteratePrefixMapping(true); it.hasNext();) {
            PrefixMapping mapping = (PrefixMapping) it.next();
            if (namespaceURI.equals(mapping.getNamespaceURI())) {
                return mapping.getPrefix();
            }
        }
        return "";
    }


    public void setSequenceID(int sequenceID) {
        if (sequenceID < 0) {
            throw new IllegalArgumentException("sequenceID");
        }
        _sequenceID = sequenceID;
    }

    public int getSequenceID() {
        if (_sequenceID < 0) {
            throw new IllegalStateException("illegal sequenceID");
        }
        return _sequenceID;
    }

    public void addAttribute(QName qName, String value) {
        addAttribute(qName, null, value);
    }
    
    public void addAttribute(QName qName, String originalName, String value) {
        if (qName == null || value == null) {
            throw new IllegalArgumentException();
        }
        synchronized (this) {
            if (_attributes == null) {
                _attributes = new LinkedHashMap();
            }
        }
        synchronized (_attributes) {
            if (_attributes.containsKey(qName) == false) {
                String prefix = null;
                if (originalName != null) {
                    prefix = StringUtil.parsePrefix(originalName); 
                }
                NodeAttributeImpl attr = new NodeAttributeImpl(
                        qName, value, prefix);
                _attributes.put(qName, attr);
                attr.setNode(this);
            }
        }
    }

    public NodeAttribute getAttribute(QName qName) {
        if (qName == null) {
            throw new IllegalArgumentException();
        }
        if (_attributes == null) {
            return null;
        }
        return (NodeAttribute) _attributes.get(qName);
    }

    public Iterator iterateAttribute() {
        if (_attributes == null) {
            return NullIterator.getInstance();
        }
        return _attributes.values().iterator();
    }

    public SpecificationNode copyTo(CopyToFilter filter) {
        SpecificationNode copy = SpecificationUtil.createSpecificationNode(
                getQName(), getSystemID(), getLineNumber(), isOnTemplate(), getSequenceID());
        for (Iterator it = iterateAttribute(); it.hasNext();) {
            NodeAttribute attr = (NodeAttribute) it.next();
            if (filter.accept(attr)) {
                copy.addAttribute(attr.getQName(), attr.getValue());
            }
        }
        for (Iterator it = iterateChildNode(); it.hasNext();) {
            SpecificationNode node = (SpecificationNode) it.next();
            if (filter.accept(node)) {
                copy.addChildNode(node.copyTo(filter));
            }
        }
        for (Iterator it = iteratePrefixMapping(false); it.hasNext();) {
            PrefixMapping prefixMapping = (PrefixMapping)it.next();
            copy.addPrefixMapping(
                    prefixMapping.getPrefix(), prefixMapping.getNamespaceURI());
        }
        copy.setParentSpace(getParentSpace());
        copy.setDefaultNamespaceURI(getDefaultNamespaceURI());
        return copy;
    }

    public SpecificationNode copyTo() {
        return copyTo(FILTER_ALL);
    }

    public String toString() {
        StringBuffer path = new StringBuffer();
        if (getParentNode() != null
                && getParentNode() instanceof Specification == false) {
            path.append(getParentNode());
        }
        path.append("/");
        path.append(PrefixAwareNameImpl.forPrefixAwareNameString(
                getQName(), getPrefix()));
        return path.toString();
    }

    // TreeNodeWalker implemetns ------------------------------------

    public void setParentNode(NodeTreeWalker parent) {
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }

    public NodeTreeWalker getParentNode() {
        return _parent;
    }

    public void addChildNode(NodeTreeWalker child) {
        if (child == null) {
            throw new IllegalArgumentException();
        }
        synchronized (this) {
            if (_childNodes == null) {
                _childNodes = new ArrayList();
            }
        }
        synchronized (_childNodes) {
            _childNodes.add(child);
            child.setParentNode(this);
        }
    }

    public Iterator iterateChildNode() {
        if (_childNodes == null) {
            return NullIterator.getInstance();
        }
        return _childNodes.iterator();
    }

    // PositionAware implements -------------------------------------

    public void setSystemID(String systemID) {
        if (systemID == null) {
            throw new IllegalArgumentException();
        }
        _systemID = systemID;
    }

    public String getSystemID() {
        return _systemID;
    }

    public void setLineNumber(int lineNumber) {
        _lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return _lineNumber;
    }

    public void setOnTemplate(boolean onTemplate) {
        _onTemplate = onTemplate;
    }

    public boolean isOnTemplate() {
        return _onTemplate;
    }

    // support class --------------------------------------------------

    protected static class AllCopyToFilter implements CopyToFilter {

        public boolean accept(NodeObject test) {
            return true;
        }

    }

}
