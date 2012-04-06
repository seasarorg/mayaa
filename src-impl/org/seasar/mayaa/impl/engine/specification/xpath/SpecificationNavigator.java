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
package org.seasar.mayaa.impl.engine.specification.xpath;

import java.util.Iterator;

import org.jaxen.DefaultNavigator;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.Navigator;
import org.jaxen.XPath;
import org.jaxen.util.SingleObjectIterator;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.AbstractScanningIterator;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationNavigator extends DefaultNavigator
        implements NamedAccessNavigator, CONST_IMPL {

    private static final long serialVersionUID = -8845415744894196361L;

    private static final Navigator INSTANCE = new SpecificationNavigator();

    public static Navigator getInstance() {
        return INSTANCE;
    }

    protected SpecificationNavigator() {
        // singleton
    }

    protected URI getNamespaceURI(Namespace namespace, String prefix) {
        if (namespace == null) {
            throw new IllegalArgumentException();
        }
        if (prefix == null) {
            prefix = "";
        }
        PrefixMapping mapping = namespace.getMappingFromPrefix(prefix, true);
        if (mapping != null) {
            return mapping.getNamespaceURI();
        }
        return null;
    }

    public Iterator getParentAxisIterator(Object obj) {
        Object parent = null;
        if (obj instanceof NodeAttribute) {
            parent = ((NodeAttribute) obj).getNode();
        } else if (obj instanceof NodeTreeWalker) {
            parent = ((NodeTreeWalker) obj).getParentNode();
        }
        if (parent != null) {
            return new SingleObjectIterator(parent);
        }
        return NullIterator.getInstance();
    }

    public Iterator getNamespaceAxisIterator(Object obj) {
        if (obj instanceof Namespace) {
            Namespace namespace = (Namespace) obj;
            return namespace.iteratePrefixMapping(true);
        }
        return NullIterator.getInstance();
    }

    public Iterator getAttributeAxisIterator(Object obj) {
        if (obj instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode) obj;
            return node.iterateAttribute();
        }
        return NullIterator.getInstance();
    }

    public Iterator getAttributeAxisIterator(
            Object obj, String localName, String namespacePrefix,
            String namespaceURI) {
        if (obj instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode) obj;
            if (StringUtil.isEmpty(namespaceURI)) {
                URI uri = getNamespaceURI(node, namespacePrefix);
                if (uri == null) {
                    return NullIterator.getInstance();
                }
                namespaceURI = uri.getValue();
            }
            QName qName = SpecificationUtil.createQName(
                    SpecificationUtil.createURI(namespaceURI), localName);
            return new QNameFilteredIterator(qName, node.iterateAttribute());
        }
        return NullIterator.getInstance();
    }

    public Iterator getChildAxisIterator(Object obj) {
        if (obj instanceof NodeTreeWalker) {
            NodeTreeWalker node = (NodeTreeWalker) obj;
            return node.iterateChildNode();
        }
        return NullIterator.getInstance();
    }

    public Iterator getChildAxisIterator(
            Object obj, String localName, String namespacePrefix,
            String namespaceURI) {
        if (obj instanceof NodeTreeWalker) {
            NodeTreeWalker node = (NodeTreeWalker) obj;
            if (StringUtil.isEmpty(namespaceURI)) {
                if (node instanceof Namespace) {
                    namespaceURI = getNamespaceURI(
                            (Namespace) node, namespacePrefix).getValue();
                } else {
                    namespaceURI = URI_MAYAA.getValue();
                }
            }
            QName qName = SpecificationUtil.createQName(
                    SpecificationUtil.createURI(namespaceURI), localName);
            return new QNameFilteredIterator(qName, node.iterateChildNode());
        }
        return NullIterator.getInstance();
    }

    public Object getDocumentNode(Object obj) {
        if (obj instanceof NodeTreeWalker) {
            for (NodeTreeWalker current = (NodeTreeWalker) obj;
                    current != null; current = current.getParentNode()) {
                if (current instanceof Specification) {
                    return current;
                }
            }
        }
        return null;
    }

    public String translateNamespacePrefixToUri(String prefix, Object obj) {
        Namespace namaspace = null;
        if (obj instanceof Namespace) {
            namaspace = (Namespace) obj;
        }
        if (namaspace != null) {
            return getNamespaceURI(namaspace, prefix).getValue();
        }
        return null;
    }

    public String getAttributeName(Object obj) {
        if (obj instanceof NodeAttribute) {
            NodeAttribute attr = (NodeAttribute) obj;
            return attr.getQName().getLocalName();
        }
        return null;
    }

    public String getAttributeNamespaceUri(Object obj) {
        if (obj instanceof NodeAttribute) {
            NodeAttribute attr = (NodeAttribute) obj;
            return attr.getQName().getNamespaceURI().getValue();
        }
        return null;
    }

    public String getAttributeQName(Object obj) {
        if (obj instanceof NodeAttribute) {
            NodeAttribute attr = (NodeAttribute) obj;
            String prefix = attr.getPrefix();
            if (StringUtil.hasValue(prefix)) {
                return prefix + ":" + getAttributeName(obj);
            }
            return getAttributeName(obj);
        }
        return null;
    }

    public String getAttributeStringValue(Object obj) {
        if (obj instanceof NodeAttribute) {
            NodeAttribute attr = (NodeAttribute) obj;
            return attr.getValue();
        }
        return null;
    }

    public String getCommentStringValue(Object obj) {
        if (isComment(obj)) {
            SpecificationNode node = (SpecificationNode) obj;
            return SpecificationUtil.getAttributeValue(node, QM_TEXT);
        }
        return null;
    }

    public String getElementName(Object obj) {
        if (obj instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode) obj;
            return node.getQName().getLocalName();
        }
        return null;
    }

    public String getElementNamespaceUri(Object obj) {
        if (obj instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode) obj;
            return node.getQName().getNamespaceURI().getValue();
        }
        return null;
    }

    public String getElementQName(Object obj) {
        if (obj instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode) obj;
            String prefix = node.getPrefix();
            if (StringUtil.hasValue(prefix)) {
                return prefix + ":" + getElementName(obj);
            }
            return getElementName(obj);
        }
        return null;
    }

    public String getElementStringValue(Object obj) {
        if (obj instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode) obj;
            StringBuffer buffer = new StringBuffer();
            for (Iterator it = node.iterateChildNode(); it.hasNext();) {
                SpecificationNode child = (SpecificationNode) it.next();
                if (isText(child)) {
                    String value = getTextStringValue(child);
                    if (value != null) {
                        buffer.append(value.trim());
                    }
                }
            }
            return buffer.toString();
        }
        return null;
    }

    public String getNamespacePrefix(Object obj) {
        if (obj instanceof PrefixMapping) {
            PrefixMapping mapping = (PrefixMapping) obj;
            return mapping.getPrefix();
        }
        return null;
    }

    public String getNamespaceStringValue(Object obj) {
        if (obj instanceof PrefixMapping) {
            PrefixMapping mapping = (PrefixMapping) obj;
            return mapping.getNamespaceURI().getValue();
        }
        return null;
    }

    public String getTextStringValue(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        } else if (isText(obj)) {
            SpecificationNode node = (SpecificationNode) obj;
            String text = SpecificationUtil.getAttributeValue(node, QM_TEXT);
            if (text != null) {
                return text;
            }
        }
        return "";
    }

    public boolean isAttribute(Object obj) {
        return obj instanceof NodeAttribute;
    }

    public boolean isComment(Object obj) {
        if (obj instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode) obj;
            return QM_COMMENT.equals(node.getQName());
        }
        return false;
    }

    public boolean isDocument(Object obj) {
        return obj instanceof Specification;
    }

    public boolean isElement(Object obj) {
        return obj instanceof SpecificationNode
                && isProcessingInstruction(obj) == false
                && isText(obj) == false && isDocType(obj) == false;
    }

    public boolean isNamespace(Object obj) {
        return obj instanceof PrefixMapping;
    }

    public boolean isProcessingInstruction(Object obj) {
        if (obj instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode) obj;
            return QM_PI.equals(node.getQName());
        }
        return false;
    }

    public boolean isText(Object obj) {
        if (obj instanceof String) {
            return true;
        } else if (obj instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode) obj;
            return QM_CHARACTERS.equals(node.getQName())
                    || QM_CDATA.equals(node.getQName());
        }
        return false;
    }

    public XPath parseXPath(String xpath) {
        return SpecificationXPath.createXPath(xpath, null);
    }

    public boolean isDocType(Object obj) {
        if (obj instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode) obj;
            return QM_DOCTYPE.equals(node.getQName());
        }
        return false;
    }

    // support class ------------------------------------------------

    protected static class QNameFilteredIterator extends AbstractScanningIterator {

        private QName _qName;

        public QNameFilteredIterator(QName qName, Iterator iterator) {
            super(iterator);
            if (qName == null) {
                throw new IllegalArgumentException();
            }
            _qName = qName;
        }

        protected boolean filter(Object test) {
            if (test == null || (test instanceof PrefixAwareName == false)) {
                return false;
            }
            PrefixAwareName prefixAwareName = (PrefixAwareName) test;
            return _qName.equals(prefixAwareName.getQName());
        }

    }

}
