/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.engine.specification;

import java.util.Iterator;

import org.jaxen.DefaultNavigator;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.Navigator;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.util.SingleObjectIterator;
import org.seasar.maya.engine.specification.Namespaceable;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.AbstractScanningIterator;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationNavigator extends DefaultNavigator
		implements NamedAccessNavigator, CONST_IMPL {

	private static final long serialVersionUID = -8845415744894196361L;
	private static final Navigator _instance = new SpecificationNavigator();

    public static Navigator getInstance() {
        return _instance;
    }
    
    private SpecificationNavigator() {
    }

	private String getNamespaceURI(Namespaceable namespaceable, String prefix) {
	    if(namespaceable == null) {
	        throw new IllegalArgumentException();
	    }
	    if(StringUtil.isEmpty(prefix)) {
	        return URI_HTML;
	    }
	    for(Iterator it = namespaceable.iterateNamespace(); it.hasNext(); ) {
	        NodeNamespace ns = (NodeNamespace)it.next();
	        if(prefix.equals(ns.getPrefix())) {
	            return ns.getNamespaceURI();
	        }
	    }
	    return null;
	}
    
    public Iterator getParentAxisIterator(Object obj) {
        Object parent = null;
        if(obj instanceof NodeNamespace) {
            parent =((NodeNamespace)obj).getNamespaceable();
        } else if(obj instanceof NodeAttribute) {
            parent = ((NodeAttribute)obj).getNode();
        } else if(obj instanceof SpecificationNode) {
            parent = ((SpecificationNode)obj).getParentNode();
        }
        if(parent != null) {
            return new SingleObjectIterator(parent);
        }
        return NullIterator.getInstance();
    }
    
    public Iterator getNamespaceAxisIterator(Object obj) {
        if(obj instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode)obj;
            return node.iterateNamespace();
        }
        return NullIterator.getInstance();
    }

    public Iterator getAttributeAxisIterator(Object obj) {
        SpecificationNode node = (SpecificationNode)obj;
        return node.iterateAttribute();
    }

    public Iterator getAttributeAxisIterator(Object obj,
            String localName, String namespacePrefix, String namespaceURI) {
        SpecificationNode node = (SpecificationNode)obj;
        if(StringUtil.isEmpty(namespaceURI)) {
            namespaceURI = getNamespaceURI(node, namespacePrefix);
        }
        QName qName = new QName(namespaceURI, localName);
        return new QNameFilteredIterator(qName, node.iterateAttribute());
    }

    public Iterator getChildAxisIterator(Object obj) {
        SpecificationNode node = (SpecificationNode)obj;
        return node.iterateChildNode();
    }
    
    public Iterator getChildAxisIterator(Object obj, 
            String localName, String namespacePrefix, String namespaceURI) {
        SpecificationNode node = (SpecificationNode)obj;
        if(StringUtil.isEmpty(namespaceURI)) {
            namespaceURI = getNamespaceURI(node, namespacePrefix);
        }
        QName qName = new QName(namespaceURI, localName);
        return new QNameFilteredIterator(qName, node.iterateChildNode());
    }
    
    public Object getDocumentNode(Object obj) {
        for(SpecificationNode current = (SpecificationNode)obj;
        		current != null; current = current.getParentNode()) {
		    if(current instanceof Specification) {
		        return current;
		    }
		}
        return null;
    }

    public String translateNamespacePrefixToUri(String prefix, Object obj) {
    	Namespaceable namaspaceable = null;
        if(obj instanceof NodeNamespace) {
            namaspaceable = ((NodeNamespace)obj).getNamespaceable();
        } else if(obj instanceof NodeAttribute) {
            namaspaceable = ((NodeAttribute)obj).getNode();
        } else if(obj instanceof SpecificationNode) {
            namaspaceable = (SpecificationNode)obj;
        }
        if(namaspaceable != null) {
            return getNamespaceURI(namaspaceable, prefix);
        }
        return "";
    }
    
	public String getAttributeName(Object obj) {
		NodeAttribute attr = (NodeAttribute)obj;
		return attr.getQName().getLocalName();
	}

	public String getAttributeNamespaceUri(Object obj) {
		NodeAttribute attr = (NodeAttribute)obj;
		return attr.getQName().getNamespaceURI();
	}
	
	public String getAttributeQName(Object obj) {
		NodeAttribute attr = (NodeAttribute)obj;
		String prefix = attr.getPrefix();
		if(StringUtil.hasValue(prefix)) {
		    return prefix + ":" + getAttributeName(obj);
		}
		return getAttributeName(obj);
	}
	
	public String getAttributeStringValue(Object obj) {
		NodeAttribute attr = (NodeAttribute)obj;
		String text = attr.getValue();
		if(text != null) {
		    return text;
		}
		return "";
	}
	
	public String getCommentStringValue(Object obj) {
	    if(isComment(obj)) {
	        SpecificationNode node = (SpecificationNode)obj;
	        String text = SpecificationUtil.getAttributeValue(node, QM_TEXT);
	        if(text != null) {
	            return text;
	        }
	    }
		return "";
	}
	
	public String getElementName(Object obj) {
		SpecificationNode node = (SpecificationNode)obj;
		return node.getQName().getLocalName();
	}
	
	public String getElementNamespaceUri(Object obj) {
		SpecificationNode node = (SpecificationNode)obj;
		return node.getQName().getNamespaceURI();
	}
	
	public String getElementQName(Object obj) {
		SpecificationNode node = (SpecificationNode)obj;
		String prefix = node.getPrefix();
		if(StringUtil.hasValue(prefix)) {
		    return prefix + ":" + getElementName(obj);
		}
		return getElementName(obj);
	}
	
	public String getElementStringValue(Object obj) {
		SpecificationNode node = (SpecificationNode)obj;
		StringBuffer buffer = new StringBuffer();
		for(Iterator it = node.iterateChildNode(); it.hasNext(); ) {
		    SpecificationNode child = (SpecificationNode)it.next();
		    if(isText(child)) {
		        String value = getTextStringValue(child);
		        if(value != null) {
		            buffer.append(value.trim());
		        }
		    }
		}
		return buffer.toString();
	}
	
	public String getNamespacePrefix(Object obj) {
	    NodeNamespace ns = (NodeNamespace)obj;
		return ns.getPrefix();
	}
	
	public String getNamespaceStringValue(Object obj) {
	    NodeNamespace ns = (NodeNamespace)obj;
		return ns.getNamespaceURI();
	}
	
	public String getTextStringValue(Object obj) {
	    if(isText(obj)) {
	        SpecificationNode node = (SpecificationNode)obj;
	        String text = SpecificationUtil.getAttributeValue(node, QM_TEXT);
	        if(text != null) {
	            return text;
	        }
	    }
		return "";
	}
	
	public boolean isAttribute(Object obj) {
		return obj instanceof NodeAttribute;
	}
	
	public boolean isComment(Object obj) {
	    if(obj instanceof SpecificationNode) {
			SpecificationNode node = (SpecificationNode)obj;
			return QM_COMMENT.equals(node.getQName());
	    }
	    return false;
	}
	
	public boolean isDocument(Object obj) {
		return obj instanceof Specification;
	}
	
	public boolean isElement(Object obj) {
		return obj instanceof SpecificationNode &&
			obj instanceof Specification == false &&
			isProcessingInstruction(obj) == false &&
			isText(obj) == false && 
			isDocType(obj) == false;
	}
	
	public boolean isNamespace(Object obj) {
		return obj instanceof NodeNamespace;
	}
	
	public boolean isProcessingInstruction(Object obj) {
	    if(obj instanceof SpecificationNode) {
			SpecificationNode node = (SpecificationNode)obj;
			return QM_PROCESSING_INSTRUCTION.equals(node.getQName());
	    }
	    return false;
	}
	
	public boolean isText(Object obj) {
		if(obj instanceof SpecificationNode) {
		    SpecificationNode node = (SpecificationNode)obj;
		    return QM_CHARACTERS.equals(node.getQName()) ||
		    	QM_CDATA.equals(node.getQName());
		}
		return false;
	}
	
	public XPath parseXPath(String xpath) throws SAXPathException {
		return SpecificationXPath.createXPath(xpath, null);
	}
	
	public boolean isDocType(Object obj) {
	    if(obj instanceof SpecificationNode) {
			SpecificationNode node = (SpecificationNode)obj;
			return QM_DOCTYPE.equals(node.getQName());
	    }
	    return false;
	}
    
	private class QNameFilteredIterator extends AbstractScanningIterator {

	    private QName _qName;
	    
	    private QNameFilteredIterator(QName qName, Iterator iterator) {
	        super(iterator);
	        if(qName == null) {
	            throw new IllegalArgumentException();
	        }
	        _qName = qName;
	    }

	    protected boolean filter(Object test) {
	        if(test == null || (test instanceof QNameable == false)) {
	            return false;
	        }
	        QNameable qNameable = (QNameable)test;
		    return _qName.equals(qNameable.getQName());
	    }
	    
	}
	
	
}
