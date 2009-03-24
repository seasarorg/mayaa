/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
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

import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.engine.CharsetConverter;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.xml.sax.Attributes;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TemplateNodeHandler extends SpecificationNodeHandler {

    private boolean _outputTemplateWhitespace = true;

    public TemplateNodeHandler(Template specification) {
        super(specification);
    }

    protected Template getTemplate() {
        return (Template) getSpecification();
    }

    public void setOutputTemplateWhitespace(boolean outputTemplateWhitespace) {
        _outputTemplateWhitespace = outputTemplateWhitespace;
    }

    protected void initNamespace() {
        super.initNamespace();
        getCurrentInternalNamespacePrefixMap().remove("");
        getCurrentInternalNamespacePrefixMap().remove("xml");
    }

    protected SpecificationNode createChildNode(
            QName qName, String systemID, int lineNumber, int sequenceID) {
        return SpecificationUtil.createSpecificationNode(
                qName, systemID, lineNumber, true, sequenceID);
    }

    protected boolean isRemoveWhitespace() {
        return _outputTemplateWhitespace == false;
    }

    /**
     * metaタグのhttp-equiv="content-type" の場合、contentのcharsetを変換して
     * 返すようにする。
     * それ以外の場合はなにもしない。
     *
     * @param namespaceURI
     * @param localName
     * @param qName
     * @param attributes
     * @return
     */
    protected Attributes wrapContentTypeConverter(String namespaceURI,
            String localName, String qName, Attributes attributes) {
        if (StringUtil.isEmpty(namespaceURI) || URI_HTML.equals(namespaceURI) ||
                URI_XHTML.equals(namespaceURI)) {
            if ("meta".equalsIgnoreCase(localName)) {
                int contentTypeIndex = getContentTypeIndex(namespaceURI, attributes);
                if (contentTypeIndex != -1) {
                    String contentType = attributes.getValue(contentTypeIndex);
                    contentType = CharsetConverter.convertContentType(contentType);
                    return new ContentTypeConvertAttributes(
                            attributes, contentTypeIndex, contentType);
                }
            }
        }
        return attributes;
    }

    protected int getContentTypeIndex(String namespaceURI, Attributes attributes) {
        int httpEquivIndex =
            getIndexIgnoreCase(namespaceURI, "http-equiv", attributes);
        if (httpEquivIndex != -1 &&
                "content-type".equalsIgnoreCase(attributes.getValue(httpEquivIndex))) {
            return getIndexIgnoreCase(namespaceURI, "content", attributes);
        }
        return -1;
    }

    protected int getIndexIgnoreCase(
            String namespaceURI, String localName, Attributes attributes) {
        for (int i = 0; i < attributes.getLength(); i++) {
            String uri = attributes.getURI(i);
            if ((StringUtil.isEmpty(uri) || namespaceURI.equals(uri)) &&
                    localName.equalsIgnoreCase(attributes.getLocalName(i))) {
                return i;
            }
        }
        return -1;
    }

    public void startElement(String namespaceURI,
            String localName, String qName, Attributes attributes) {
        Attributes wrapedAttributes = wrapContentTypeConverter(
                namespaceURI, localName, qName, attributes);
        super.startElement(namespaceURI, localName, qName, wrapedAttributes);
    }

    protected String removeIgnorableWhitespace(String characters) {
        StringBuffer buffer = new StringBuffer(characters.length());
        String[] line = characters.split("\n");
        for (int i = 0; i < line.length; i++) {
            if (line[i].trim().length() > 0) {
                String token = line[i].replaceAll("^[ \t]+", "");
                token = token.replaceAll("[ \t]+$", "");
                buffer.append(token.replaceAll("[ \t]+", " "));
                if (i < line.length - 1) {
                    buffer.append("\n");
                }
            } else if (i == 0) {
                buffer.append("\n");
            }
        }
        return buffer.toString();
    }

    protected void processEntity(String name) {
        String entityRef = "&" + name + ";";
        appendCharactersBuffer(entityRef);
    }

    public void comment(char[] buffer, int start, int length) {
        addCharactersNode();
        String comment = new String(buffer, start, length);
        SpecificationNode node = addNode(QM_COMMENT);
        node.setDefaultNamespaceURI(node.getParentSpace().getDefaultNamespaceURI());
        node.addAttribute(QM_TEXT, comment);
    }

    public void startCDATA() {
        addCharactersNode();
        SpecificationNode node = addNode(QM_CDATA);

        node.setParentNode(getCurrentNode());
        setCurrentNode(node);
        enterCData();
    }

    public void endCDATA() {
        addCharactersNode();
        setCurrentNode(getCurrentNode().getParentNode());
        leaveCData();
    }

    protected static class ContentTypeConvertAttributes implements Attributes {
        private Attributes _original;
        private int _targetIndex;
        private String _newValue;

        public ContentTypeConvertAttributes(Attributes original,
                int targetIndex, String newValue) {
            _original = original;
            _targetIndex = targetIndex;
            _newValue = newValue;
        }

        /**
         * Look up the index of an attribute by Namespace name.
         * @param uri The Namespace URI, or the empty string if the name has no Namespace URI.
         * @param localName The attribute's local name.
         * @return The index of the attribute, or -1 if it does not appear in the list.
         * @see org.xml.sax.Attributes#getIndex(java.lang.String, java.lang.String)
         */
        public int getIndex(String uri, String localName) {
            return _original.getIndex(uri, localName);
        }

        /**
         * Look up the index of an attribute by XML 1.0 qualified name.
         * @param qName The qualified (prefixed) name.
         * @return The index of the attribute, or -1 if it does not appear in the list.
         * @see org.xml.sax.Attributes#getIndex(java.lang.String)
         */
        public int getIndex(String qName) {
            return _original.getIndex(qName);
        }

        /**
         * Return the number of attributes in the list.
         * Once you know the number of attributes, you can iterate through the list.
         * @return The number of attributes in the list.
         * @see org.xml.sax.Attributes#getLength()
         */
        public int getLength() {
            return _original.getLength();
        }

        /**
         * Look up an attribute's local name by index.
         * @param index The attribute index (zero-based).
         * @return The local name, or the empty string if Namespace processing is not being performed, or null if the index is out of range.
         * @see org.xml.sax.Attributes#getLocalName(int)
         */
        public String getLocalName(int index) {
            return _original.getLocalName(index);
        }

        /**
         * Look up an attribute's XML 1.0 qualified name by index.
         * @param The attribute index (zero-based).
         * @return The XML 1.0 qualified name, or the empty string if none is available, or null if the index is out of range.
         * @see org.xml.sax.Attributes#getQName(int)
         */
        public String getQName(int index) {
            return _original.getQName(index);
        }

        /**
         * @param The attribute index (zero-based).
         * @return The attribute's type as a string, or null if the index is out of range.
         * @see org.xml.sax.Attributes#getType(int)
         */
        public String getType(int index) {
            return _original.getType(index);
        }

        /**
         * @param uri The Namespace URI, or the empty String if the name has no Namespace URI.
         * @param localName The local name of the attribute.
         * @return The attribute type as a string, or null if the attribute is not in the list or if Namespace processing is not being performed.
         * @see org.xml.sax.Attributes#getType(java.lang.String, java.lang.String)
         */
        public String getType(String uri, String localName) {
            return _original.getType(uri, localName);
        }

        /**
         * @param qName The XML 1.0 qualified name.
         * @return The attribute type as a string, or null if the attribute is not in the list or if qualified names are not available.
         * @see org.xml.sax.Attributes#getType(java.lang.String)
         */
        public String getType(String qName) {
            return _original.getType(qName);
        }

        /**
         * @param index The attribute index (zero-based).
         * @return The Namespace URI, or the empty string if none is available, or null if the index is out of range.
         * @see org.xml.sax.Attributes#getURI(int)
         */
        public String getURI(int index) {
            return _original.getURI(index);
        }

        /**
         * @param index The attribute index (zero-based).
         * @return The attribute's value as a string, or null if the index is out of range.
         * @see org.xml.sax.Attributes#getValue(int)
         */
        public String getValue(int index) {
            if (_targetIndex == index) {
                return _newValue;
            }
            return _original.getValue(index);
        }

        /**
         * @param uri The Namespace URI, or the empty String if the name has no Namespace URI.
         * @param localName The local name of the attribute.
         * @return The attribute value as a string, or null if the attribute is not in the list.
         * @see org.xml.sax.Attributes#getValue(java.lang.String, java.lang.String)
         */
        public String getValue(String uri, String localName) {
            if (getIndex(uri, localName) == _targetIndex) {
                return _newValue;
            }
            return _original.getValue(uri, localName);
        }

        /**
         * @param qName The XML 1.0 qualified name.
         * @return The attribute value as a string, or null if the attribute is not in the list or if qualified names are not available.
         * @see org.xml.sax.Attributes#getValue(java.lang.String)
         */
        public String getValue(String qName) {
            if (getIndex(qName) == _targetIndex) {
                return _newValue;
            }
            return _original.getValue(qName);
        }

    }

}
