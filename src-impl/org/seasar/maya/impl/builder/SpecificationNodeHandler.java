/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.specification.Namespace;
import org.seasar.maya.engine.specification.PrefixMapping;
import org.seasar.maya.engine.specification.NodeTreeWalker;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.parser.AdditionalHandler;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.engine.specification.QNameImpl;
import org.seasar.maya.impl.engine.specification.SpecificationNodeImpl;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationNodeHandler
        implements EntityResolver, DTDHandler, ContentHandler, 
        ErrorHandler, LexicalHandler, AdditionalHandler, CONST_IMPL {
    
    private static final Log LOG = 
        LogFactory.getLog(SpecificationNodeHandler.class);

    protected static final QName QM_DATA = 
        new QNameImpl("data");
    protected static final QName QM_PUBLIC_ID = 
        new QNameImpl("publicID");
    protected static final QName QM_SYSTEM_ID = 
        new QNameImpl("systemID");
    protected static final QName QM_TARGET = 
        new QNameImpl("target");

    private Specification _specification;
    private NodeTreeWalker _current;
    private Locator _locator;
    private Namespace _namespace;
    private StringBuffer _charactersBuffer;
    private boolean _outputWhitespace = true;
    private int _inEntity;
    
    public SpecificationNodeHandler(Specification specification) {
        if(specification == null) {
            throw new IllegalArgumentException();
        }
        _specification = specification;
    }

    public void setOutputWhitespace(boolean outputWhitespace) {
        _outputWhitespace = outputWhitespace;
    }
    
    public void setDocumentLocator(Locator locator) {
        _locator = locator;
    }

    protected void initNamespace() {
        _namespace = SpecificationUtil.createNamespace();
        _namespace.addPrefixMapping("", URI_HTML);
    }

    protected void pushNamespace() {
        Namespace parentSpace = _namespace;
        _namespace = SpecificationUtil.createNamespace();
        _namespace.setParentSpace(parentSpace);
    }
    
    protected void popNamespace() {
        _namespace = _namespace.getParentSpace();
        if(_namespace == null) {
            throw new IllegalStateException();
        }
    }
    
    public void startDocument() {
        _charactersBuffer = new StringBuffer(128);
        _current = _specification;
        initNamespace();
        pushNamespace();
    }

    public void startPrefixMapping(String prefix, String uri) {
        _namespace.addPrefixMapping(prefix, uri);
    }
    
    public void endPrefixMapping(String prefix) {
        PrefixMapping mapping = _namespace.getMappingFromPrefix(prefix, false);
        if(mapping == null) {
            throw new IllegalStateException();
        }
    }
    
    protected SpecificationNode addNode(QName qName) {
		SpecificationNodeImpl child = new SpecificationNodeImpl(
				qName, _locator.getSystemId(), _locator.getLineNumber());
        child.setParentSpace(_namespace);
	    _current.addChildNode(child);
		return child;
    }
    
    protected void addCharactersNode() {
    	if(_charactersBuffer.length() > 0) {
    		SpecificationNode node = addNode(QM_CHARACTERS);
    		node.addAttribute(QM_TEXT, _charactersBuffer.toString());
            _charactersBuffer = new StringBuffer(128);
    	}
    }
    
    protected void saveToCycle(NodeTreeWalker originalNode) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(originalNode);
    }
    
    protected boolean checkAttribute(String qName, String value) {
    	// workaround for XML parser(NekoHTML?)'s bug.
    	if(StringUtil.isEmpty(qName)) {
    		throw new IllegalArgumentException();
    	}
    	String prefix;
    	if("xmlns".equals(qName)) {
            prefix = "";
    	} else if(qName.startsWith("xmlns:")) {
    		prefix = qName.substring(6);
    	} else {
    		return true;
    	}
    	if(_namespace.getMappingFromPrefix(prefix, false) == null) {
    		startPrefixMapping(prefix, value);
    	}
        if(LOG.isWarnEnabled()) {
        	LOG.warn(StringUtil.getMessage(SpecificationNodeHandler.class, 
        			0, new String[] { prefix, value }));
        }
        return false;
    }
    
    public void startElement(String namespaceURI, 
            String localName, String qName, Attributes attributes) {
        addCharactersNode();
        QNameable parsedName = 
            BuilderUtil.parseName(_namespace, qName);
        QName nodeQName = parsedName.getQName();
        String nodeURI = nodeQName.getNamespaceURI();
        SpecificationNode node = addNode(nodeQName);
        Namespace elementNS = SpecificationUtil.createNamespace();
        elementNS.setParentSpace(_namespace);
        elementNS.addPrefixMapping("", nodeURI);
        for(int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            String attrValue = attributes.getValue(i);
            if(checkAttribute(attrName, attrValue)) {
	            QNameable parsedAttrName = 
	                BuilderUtil.parseName(elementNS, attrName);
	            QName attrQName = parsedAttrName.getQName();
	            node.addAttribute(attrQName, attrValue);
            }
        }
        _current = node;
        saveToCycle(_current);
        pushNamespace();
    }

    public void endElement(String namespaceURI, 
            String localName, String qName) {
        popNamespace();
        addCharactersNode();
        _current = _current.getParentNode();
        saveToCycle(_current);
    }

    public void endDocument() {
        saveToCycle(_specification);
    }

    public void characters(char[] buffer, int start, int length) {
    	if(_inEntity == 0) {
	    	if(_outputWhitespace) {
	            _charactersBuffer.append(buffer, start, length);
	    	} else {
	    	    String characters = new String(buffer, start, length).trim();
		        if(characters.length() > 0) {
		            _charactersBuffer.append(characters);
		        }
	    	}
    	}
    }

    public void ignorableWhitespace(char[] buffer, int start, int length) {
    	if(_outputWhitespace) {
    		_charactersBuffer.append(buffer, start, length);
    	}
    }
    
    public void xmlDecl(String version, String encoding, String standalone) {
        addCharactersNode();
        SpecificationNode node = addNode(QM_PI);
        node.addAttribute(QM_TARGET, "xml");
        StringBuffer buffer = new StringBuffer();
        if(StringUtil.hasValue(version)) {
            buffer.append("version=\"").append(version).append("\" ");
        }
        if(StringUtil.hasValue(encoding)) {
            buffer.append("encoding=\"").append(encoding).append("\" ");
        }
        if(StringUtil.hasValue(standalone)) {
            buffer.append("standalone=\"").append(standalone).append("\" ");
        }
        if(buffer.length() > 0) {
            node.addAttribute(QM_DATA, buffer.toString());
        }
    }

    public void processingInstruction(String target, String data) {
        addCharactersNode();
		SpecificationNode node = addNode(QM_PI);
		node.addAttribute(QM_TARGET, target);
		if(StringUtil.hasValue(data)) {
			node.addAttribute(QM_DATA, data);
		}
    }

    public void skippedEntity(String name) {
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        return null;
    }

    public void startEntity(String name) {
        String entityRef = "&" + name + ";";
        _charactersBuffer.append(entityRef);
        ++_inEntity;
    }

    public void endEntity(String name) {
        --_inEntity;
    }
    
    public void comment(char[] buffer, int start, int length) {
        addCharactersNode();
        String comment = new String(buffer, start, length);
        SpecificationNode node = addNode(QM_COMMENT);
        node.addAttribute(QM_TEXT, comment);
    }

    public void notationDecl(String name, String publicId, String systemId) {
    }

    public void unparsedEntityDecl(
            String name, String publicId, String systemId, String notationName) {
    }

    public void startDTD(String name, String publicID, String systemID) {
        addCharactersNode();
        SpecificationNode node = addNode(QM_DOCTYPE);
		node.addAttribute(QM_NAME, name);
		if(StringUtil.hasValue(publicID)) {
			node.addAttribute(QM_PUBLIC_ID, publicID);
		}
		if(StringUtil.hasValue(systemID)) {
			node.addAttribute(QM_SYSTEM_ID, systemID);
		}
		_charactersBuffer.append("\r\n");
    }

    public void endDTD() {
    }

    public void startCDATA() {
        addCharactersNode();
        SpecificationNode node = addNode(QM_CDATA);
        _current = node; 
    }

    public void endCDATA() {
        addCharactersNode();
        _current = _current.getParentNode();
    }

    public void warning(SAXParseException e) {
        if(LOG.isWarnEnabled()) {
            LOG.warn(e);
        }
    }

    public void fatalError(SAXParseException e) {
        if(LOG.isFatalEnabled()) {
            LOG.fatal(e);
        }
        throw new RuntimeException(e);
    }
    
    public void error(SAXParseException e) {
        if(LOG.isErrorEnabled()) {
            LOG.error(e);
        }
        throw new RuntimeException(e);
    }

}
