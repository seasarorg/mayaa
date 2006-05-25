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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
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
    private int _charactersStartLineNumber;
    private boolean _outputMayaaWhitespace = false;
    private int _inEntity;
    private int _sequenceID;
    private Map _internalNamespacePrefixMap; 
    private boolean _inCData;

    public SpecificationNodeHandler(Specification specification) {
        if (specification == null) {
            throw new IllegalArgumentException();
        }
        _specification = specification;
    }

    public void setOutputMayaaWhitespace(boolean outputMayaaWhitespace) {
        _outputMayaaWhitespace = outputMayaaWhitespace;
    }

    public void setDocumentLocator(Locator locator) {
        _locator = locator;
    }

    protected void initNamespace() {
        _namespace = SpecificationUtil.createNamespace();
        getCurrentInternalNamespacePrefixMap().put("", URI_HTML);
        getCurrentInternalNamespacePrefixMap().put("xml", URI_XML);
    }

    protected void pushNamespace(Namespace newNamespace) {
        _namespace = newNamespace;
    }

    protected void popNamespace() {
        _namespace = _namespace.getParentSpace();
        if (_namespace == null) {
            throw new IllegalStateException(getClass().getName());
        }
    }

    private static final int DEFAULT_BUFFER_SIZE = 128;

    protected void initCharactersBuffer() {
        _charactersBuffer = new StringBuffer(DEFAULT_BUFFER_SIZE);
        _charactersStartLineNumber = _locator.getLineNumber();
    }

    protected void appendCharactersBuffer(String str) {
        if (_charactersStartLineNumber == -1) {
            _charactersStartLineNumber = _locator.getLineNumber();
        }
        _charactersBuffer.append(str);
    }

    protected void appendCharactersBuffer(char str[], int offset, int len) {
        if (_charactersStartLineNumber == -1) {
            _charactersStartLineNumber = _locator.getLineNumber();
        }
        _charactersBuffer.append(str, offset, len);
    }

    public void startDocument() {
        _sequenceID = 1;
        initCharactersBuffer();
        _charactersStartLineNumber = -1;
        _current = _specification;
        _internalNamespacePrefixMap = new HashMap();
        initNamespace();
    }

    protected Map getCurrentInternalNamespacePrefixMap() {
        Map result = (Map)_internalNamespacePrefixMap.get(_current);
        if (result == null) {
            result = new HashMap();
            _internalNamespacePrefixMap.put(_current, result);
        }
        return result;
    }

    public void startPrefixMapping(String prefix, String uri) {
        getCurrentInternalNamespacePrefixMap().put(prefix, uri);
    }

    public void endPrefixMapping(String prefix) {
        getCurrentInternalNamespacePrefixMap().remove(prefix);
    }

    protected SpecificationNode addNode(QName qName) {
        int lineNumber = _locator.getLineNumber();
        return addNode(qName, lineNumber);
    }

    protected SpecificationNode createChildNode(
            QName qName, String systemID, int lineNumber, int sequenceID) {
        return SpecificationUtil.createSpecificationNode(
                qName, systemID, lineNumber, false, sequenceID);
    }

    protected SpecificationNode addNode(QName qName, int lineNumber) {
        String systemID = StringUtil.removeFileProtocol(_locator.getSystemId());
        SpecificationNode child = createChildNode(
                qName, systemID, lineNumber, _sequenceID);
        _sequenceID += 1;
        child.setParentSpace(SpecificationUtil.toFinalNamespace(_namespace));
        _current.addChildNode(child);
        return child;
    }

    protected boolean isRemoveWhitespace() {
        return _outputMayaaWhitespace == false
                    && _inCData == false;
    }

    protected void addCharactersNode() {
        if (_charactersBuffer.length() > 0) {
            String characters = _charactersBuffer.toString();
            if (isRemoveWhitespace()) {
                characters = removeIgnorableWhitespace(characters);
            }
            if (characters.length() > 0) {
                SpecificationNode node =
                    addNode(QM_CHARACTERS, _charactersStartLineNumber);
                node.addAttribute(QM_TEXT, characters);
            }
            initCharactersBuffer();
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
                if (i < line.length - 1
                        && ((i + 1 < line.length - 1)
                                || (line[i + 1].trim().length() > 0))) {
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
        return ("xmlns".equals(qName) == false
                && qName.startsWith("xmlns:") == false);
    }

    public void startElement(String namespaceURI,
            String localName, String qName, Attributes attributes) {
        addCharactersNode();
        
        Namespace elementNS = SpecificationUtil.createNamespace();
        Iterator it = getCurrentInternalNamespacePrefixMap().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            elementNS.addPrefixMapping((String)entry.getKey(), (String)entry.getValue());
        }
        elementNS.setParentSpace(SpecificationUtil.toFinalNamespace(_namespace));

        PrefixAwareName parsedName =
            BuilderUtil.parseName(elementNS, qName);
        QName nodeQName = parsedName.getQName();
        String nodeURI = nodeQName.getNamespaceURI();
        elementNS.setDefaultNamespaceURI(nodeURI);

        elementNS = SpecificationUtil.toFinalNamespace(elementNS);

        SpecificationNode node = addNode(nodeQName);
        it = getCurrentInternalNamespacePrefixMap().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            node.addPrefixMapping((String)entry.getKey(), (String)entry.getValue());
        }
        node.setDefaultNamespaceURI(nodeURI);

        for (int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            String attrValue = attributes.getValue(i);
            if (checkAttribute(attrName, attrValue)) {
                PrefixAwareName parsedAttrName =
                    BuilderUtil.parseName(elementNS, attrName);
                QName attrQName = parsedAttrName.getQName();
                node.addAttribute(attrQName, attrName, attrValue);
            }
        }
        _current = node;
        saveToCycle(_current);
        pushNamespace(elementNS);
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
        _internalNamespacePrefixMap.clear();
        _internalNamespacePrefixMap = null;
    }

    public void characters(char[] buffer, int start, int length) {
        if (_inEntity == 0) {
            appendCharactersBuffer(buffer, start, length);
        }
    }

    public void ignorableWhitespace(char[] buffer, int start, int length) {
//これは要素内のスペースなので保存する必要は無い        
//        appendCharactersBuffer(buffer, start, length);
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

    protected void processEntity(String name) {
        appendCharactersBuffer(StringUtil.resolveEntity('&' + name + ';'));
    }

    public void startEntity(String name) {
        processEntity(name);
        ++_inEntity;
    }

    public void endEntity(String name) {
        --_inEntity;
    }

    public void comment(char[] buffer, int start, int length) {
        // do nothing.
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
        appendCharactersBuffer("\r\n");
    }

    public void endDTD() {
        // do nothing.
    }

    public void startCDATA() {
        addCharactersNode();
        SpecificationNode node = addNode(QM_CDATA);
        _current = node;
        _inCData = true;
    }

    public void endCDATA() {
        addCharactersNode();
        _current = _current.getParentNode();
        _inCData = false;
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
