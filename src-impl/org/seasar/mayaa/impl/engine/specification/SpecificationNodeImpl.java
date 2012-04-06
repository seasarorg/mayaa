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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.engine.specification.CopyToFilter;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeObject;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.engine.specification.serialize.NodeReferenceResolver;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationNodeImpl extends NamespaceImpl
        implements SpecificationNode, CONST_IMPL {

    private static final long serialVersionUID = 7084397580802643259L;
    private static final Log LOG = LogFactory.getLog(SpecificationNodeImpl.class);
    private static final CopyToFilter FILTER_ALL = new AllCopyToFilter();

    private int _sequenceID = -1;
    private String _id = null;
    private Map/*<qName, NodeAttribute>*/ _attributes;
    private NodeTreeWalkerImpl _delegateNodeTreeWalker;
    // for PrefixAwareName
    private QName _qName;
    private transient String _finalizeLabel;

    public SpecificationNodeImpl(QName qName) {
        _qName = qName;
    }

    protected NodeTreeWalker getNodeTreeWalker() {
        if (_delegateNodeTreeWalker == null) {
            synchronized (this) {
                _delegateNodeTreeWalker = new NodeTreeWalkerImpl();
                _delegateNodeTreeWalker.setOwner(this);
                if (LOG.isTraceEnabled()) {
                    LOG.trace("_delegateNodeTreeWalker created.");
                }
            }
        }
        return _delegateNodeTreeWalker;
    }

    protected String makeReleasedLabel() {
        if (_finalizeLabel == null) {
            try {
                _finalizeLabel = toString();
            } catch(Throwable e) {
                _finalizeLabel = getNodeTreeWalker().getSystemID()
                            + " <" + _qName + ">";
            }
        }
        if (_finalizeLabel != null) {
            return "node " + _finalizeLabel + "@" + hashCode() + " unloaded.";
        }
        return "";
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

// デバッグのときだけ有効にすること。finalize()をオーバーライドするとFinalizerなどから特別扱いされる。
//    protected void finalize() throws Throwable {
//        if (LOG.isTraceEnabled()) {
//            LOG.trace(makeReleasedLabel());
//        }
//        super.finalize();
//    }

    // PrefixAwareName implements

    public QName getQName() {
        return _qName;
    }

    public String getPrefix() {
        URI namespaceURI = getQName().getNamespaceURI();
        for (Iterator it = iteratePrefixMapping(true); it.hasNext();) {
            PrefixMapping mapping = (PrefixMapping) it.next();
            if (namespaceURI.equals(mapping.getNamespaceURI())) {
                return mapping.getPrefix();
            }
        }
        return "";
    }

    // SpecificaitonNode implements ------------------------------------

    public void setSequenceID(int sequenceID) {
        if (sequenceID < 0) {
            throw new IllegalArgumentException("sequenceID");
        }
        _sequenceID = sequenceID;
        _id = _qName.toString() + ":" + getSystemID() +
                ":" + _sequenceID;
    }

    public int getSequenceID() {
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

    public NodeAttribute removeAttribute(QName qName) {
        if (_attributes != null) {
            synchronized (_attributes) {
                return (NodeAttribute) _attributes.remove(qName);
            }
        }
        return null;
    }

    public void clearAttributes() {
        if (_attributes != null) {
            synchronized (_attributes) {
                _attributes.clear();
            }
        }
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
        copy.setParentSpace(SpecificationUtil.getFixedNamespace(getParentSpace()));
        copy.setDefaultNamespaceURI(getDefaultNamespaceURI());
        return copy;
    }

    public SpecificationNode copyTo() {
        return copyTo(FILTER_ALL);
    }

    public void kill() {
        // TODO deprecated のため削除予定
    }

    // NodeTreeWalker implemetns ------------------------------------

    public void setParentNode(NodeTreeWalker parent) {
        getNodeTreeWalker().setParentNode(parent);
    }

    public NodeTreeWalker getParentNode() {
        return getNodeTreeWalker().getParentNode();
    }

    public void addChildNode(NodeTreeWalker childNode) {
        getNodeTreeWalker().addChildNode(childNode);
    }

    public void insertChildNode(int index, NodeTreeWalker childNode) {
        getNodeTreeWalker().insertChildNode(index, childNode);
    }

    public Iterator iterateChildNode() {
        return getNodeTreeWalker().iterateChildNode();
    }

    public boolean removeChildNode(NodeTreeWalker node) {
        return getNodeTreeWalker().removeChildNode(node);
    }

    public NodeTreeWalker getChildNode(int index) {
        return getNodeTreeWalker().getChildNode(index);
    }

    public int getChildNodeSize() {
        return getNodeTreeWalker().getChildNodeSize();
    }

    public void clearChildNodes() {
        if (_delegateNodeTreeWalker != null) {
            _delegateNodeTreeWalker.clearChildNodes();
        }
    }

    // PositionAware implements -------------------------------------

    public void setSystemID(String systemID) {
        getNodeTreeWalker().setSystemID(systemID);
    }

    public String getSystemID() {
        return getNodeTreeWalker().getSystemID();
    }

    public void setLineNumber(int lineNumber) {
        getNodeTreeWalker().setLineNumber(lineNumber);
    }

    public int getLineNumber() {
        return getNodeTreeWalker().getLineNumber();
    }

    public void setOnTemplate(boolean onTemplate) {
        getNodeTreeWalker().setOnTemplate(onTemplate);
    }

    public boolean isOnTemplate() {
        return getNodeTreeWalker().isOnTemplate();
    }

    // NodeReferenceResolverFinder implements ------------------------------------

    public NodeReferenceResolver findNodeResolver() {
        return getNodeTreeWalker().findNodeResolver();
    }

    // for serialize

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
        // namespace
        out.writeUTF(super.toString());
        if (getParentSpace() != null
                && getParentSpace().getClass() == NamespaceImpl.class) {
            out.writeObject(getParentSpace());
        } else {
            out.writeObject(new Serializable() {
                private static final long serialVersionUID = 1L;
            });
        }
    }

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (_delegateNodeTreeWalker != null) {
            _delegateNodeTreeWalker.setOwner(this);
        }
        // namespace
        String currentMappingInfo = in.readUTF();
        NamespaceImpl namespace = deserialize(currentMappingInfo);
        setDefaultNamespaceMapping(namespace.getDefaultNamespaceMapping());
        for (Iterator it = namespace.iteratePrefixMapping(false)
                ; it.hasNext(); ) {
            PrefixMapping mapping = (PrefixMapping) it.next();
            addPrefixMapping(mapping.getPrefix(), mapping.getNamespaceURI());
        }
        // parent namespace
        Object nscheck = in.readObject();
        if (nscheck instanceof NamespaceImpl) {
            setParentSpace((Namespace)nscheck);
        }
        findNodeResolver().nodeLoaded(this);
    }

    private Object readResolve() {
        for (Iterator it = iterateChildNode(); it.hasNext(); ) {
            NodeTreeWalker child = (NodeTreeWalker) it.next();
            child.setParentNode(this);
            if (child instanceof Namespace) {
                ((Namespace)child).setParentSpace(this);
            }
        }
        for (Iterator it =  iterateAttribute(); it.hasNext(); ) {
            NodeAttributeImpl attr = (NodeAttributeImpl) it.next();
            attr.setNode(this);
        }
        return this;
    }

    public String getId() {
    	return _id;
    }

    public boolean equals(Object other) {
        if (other instanceof SpecificationNodeImpl) {
            SpecificationNodeImpl otherNode = (SpecificationNodeImpl) other;
            if (getId() != null) {
                return getId().equals(otherNode.getId());
            }
        }
        return false;
    }

    // support class --------------------------------------------------

    protected static class AllCopyToFilter implements CopyToFilter {

        public boolean accept(NodeObject test) {
            return true;
        }

    }

}
