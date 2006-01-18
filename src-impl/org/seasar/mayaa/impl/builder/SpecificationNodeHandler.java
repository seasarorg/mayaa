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
package org.seasar.mayaa.impl.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.builder.parser.AdditionalHandler;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;
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
        SpecificationUtil.createQName("data");
    protected static final QName QM_PUBLIC_ID =
        SpecificationUtil.createQName("publicID");
    protected static final QName QM_SYSTEM_ID =
        SpecificationUtil.createQName("systemID");
    protected static final QName QM_TARGET =
        SpecificationUtil.createQName("target");

    private Specification _specification;
    private NodeTreeWalker _current;
    private Locator _locator;
    private Namespace _namespace;
    private StringBuffer _charactersBuffer;
    private boolean _outputTemplateWhitespace = true;
    private boolean _outputMayaaWhitespace = false;
    private boolean _onTemplate;
    private int _inEntity;
    private int _sequenceID;

    public SpecificationNodeHandler(Specification specification) {
        if (specification == null) {
            throw new IllegalArgumentException();
        }
        _specification = specification;
        _onTemplate = specification instanceof Template;
    }

    public void setOutputTemplateWhitespace(boolean outputTemplateWhitespace) {
        _outputTemplateWhitespace = outputTemplateWhitespace;
    }

    public void setOutputMayaaWhitespace(boolean outputMayaaWhitespace) {
        _outputMayaaWhitespace = outputMayaaWhitespace;
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
        if (_namespace == null) {
            throw new IllegalStateException();
        }
    }

    private static final int DEFAULT_BUFFER_SIZE = 128;

    public void startDocument() {
        _sequenceID = 1;
        _charactersBuffer = new StringBuffer(DEFAULT_BUFFER_SIZE);
        _current = _specification;
        initNamespace();
        pushNamespace();
    }

    public void startPrefixMapping(String prefix, String uri) {
        _namespace.addPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) {
        PrefixMapping mapping =
            _namespace.getMappingFromPrefix(prefix, false);
        if (mapping == null) {
            throw new IllegalStateException();
        }
    }

    protected SpecificationNode addNode(QName qName) {
        String systemID = StringUtil.removeFileProtocol(_locator.getSystemId());
        int lineNumber = _locator.getLineNumber();
        SpecificationNode child = SpecificationUtil.createSpecificationNode(
                qName, systemID, lineNumber, _onTemplate, _sequenceID);
        _sequenceID++;
        child.setParentSpace(_namespace);
        _current.addChildNode(child);
        return child;
    }

    protected void addCharactersNode() {
        if (_charactersBuffer.length() > 0) {
            SpecificationNode node = addNode(QM_CHARACTERS);

            String characters = _charactersBuffer.toString();
            if (_onTemplate) {
                if (_outputTemplateWhitespace == false) {
                    characters = removeIgnorableWhitespace(characters);
                }
            } else {
                if (_outputMayaaWhitespace == false) {
                    characters = removeIgnorableWhitespace(characters);
                }
            }
            node.addAttribute(QM_TEXT, characters);
            _charactersBuffer = new StringBuffer(DEFAULT_BUFFER_SIZE);
        }
    }

    private String removeIgnorableWhitespace(String characters) {
        StringBuffer buffer = new StringBuffer(characters.length());
        String[] line = characters.split("\n");
        for (int i = 0; i < line.length; i++) {
            if (line[i].trim().length() > 0) {
                String token = line[i].replaceAll("^[ \t]+", "");
                token = token.replaceAll("[ \t]+$", "");
                buffer.append(token.replaceAll("[ \t]+", " "));
                buffer.append("\n");
            } else {
                if (i == 0 && _onTemplate) {
                    buffer.append("\n");
                }
            }
        }
        return buffer.toString();
    }

    protected void saveToCycle(NodeTreeWalker originalNode) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(originalNode);
    }

    protected boolean checkAttribute(String qName, String value) {
        // workaround for XML parser(NekoHTML?)'s bug.
        if (StringUtil.isEmpty(qName)) {
            throw new IllegalArgumentException();
        }
        String prefix;
        if ("xmlns".equals(qName)) {
            prefix = "";
        } else if (qName.startsWith("xmlns:")) {
            prefix = qName.substring("xmlns:".length());
        } else {
            return true;
        }
        if (_namespace.getMappingFromPrefix(prefix, false) == null) {
            startPrefixMapping(prefix, value);
        }
        if (LOG.isWarnEnabled()) {
            LOG.warn(StringUtil.getMessage(SpecificationNodeHandler.class,
                    0, prefix, value));
        }
        return false;
    }

    public void startElement(String namespaceURI,
            String localName, String qName, Attributes attributes) {
        addCharactersNode();
        PrefixAwareName parsedName =
            BuilderUtil.parseName(_namespace, qName);
        QName nodeQName = parsedName.getQName();
        String nodeURI = nodeQName.getNamespaceURI();
        SpecificationNode node = addNode(nodeQName);
        Namespace elementNS = SpecificationUtil.createNamespace();
        elementNS.setParentSpace(_namespace);
        elementNS.addPrefixMapping("", nodeURI);
        for (int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            String attrValue = attributes.getValue(i);
            if (checkAttribute(attrName, attrValue)) {
                PrefixAwareName parsedAttrName =
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
        if (_inEntity == 0) {
            _charactersBuffer.append(buffer, start, length);
        }
    }

    public void ignorableWhitespace(char[] buffer, int start, int length) {
        _charactersBuffer.append(buffer, start, length);
    }

    public void xmlDecl(String version, String encoding, String standalone) {
        addCharactersNode();
        SpecificationNode node = addNode(QM_PI);
        node.addAttribute(QM_TARGET, "xml");
        StringBuffer buffer = new StringBuffer();
        if (StringUtil.hasValue(version)) {
            buffer.append("version=\"").append(version).append("\" ");
        }
        if (StringUtil.hasValue(encoding)) {
            buffer.append("encoding=\"").append(encoding).append("\" ");
        }
        if (StringUtil.hasValue(standalone)) {
            buffer.append("standalone=\"").append(standalone).append("\" ");
        }
        if (buffer.length() > 0) {
            node.addAttribute(QM_DATA, buffer.toString());
        }
    }

    public void processingInstruction(String target, String data) {
        addCharactersNode();
        SpecificationNode node = addNode(QM_PI);
        node.addAttribute(QM_TARGET, target);
        if (StringUtil.hasValue(data)) {
            node.addAttribute(QM_DATA, data);
        }
    }

    public void skippedEntity(String name) {
        // do nothing.
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
        if (_onTemplate) {
            addCharactersNode();
            String comment = new String(buffer, start, length);
            SpecificationNode node = addNode(QM_COMMENT);
            node.addAttribute(QM_TEXT, comment);
        }
    }

    public void notationDecl(String name, String publicId, String systemId) {
        // do nothing.
    }

    public void unparsedEntityDecl(
            String name, String publicId, String systemId, String notationName) {
        // do nothing.
    }

    public void startDTD(String name, String publicID, String systemID) {
        addCharactersNode();
        SpecificationNode node = addNode(QM_DOCTYPE);
        node.addAttribute(QM_NAME, name);
        if (StringUtil.hasValue(publicID)) {
            node.addAttribute(QM_PUBLIC_ID, publicID);
        }
        if (StringUtil.hasValue(systemID)) {
            node.addAttribute(QM_SYSTEM_ID, systemID);
        }
        _charactersBuffer.append("\r\n");
    }

    public void endDTD() {
        // do nothing.
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
        if (LOG.isWarnEnabled()) {
            LOG.warn(e);
        }
    }

    public void fatalError(SAXParseException e) {
        if (LOG.isFatalEnabled()) {
            LOG.fatal(e);
        }
        throw new RuntimeException(e);
    }

    public void error(SAXParseException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error(e);
        }
        throw new RuntimeException(e);
    }

}
