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
package org.seasar.mayaa.impl.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyberneko.html.HTMLEntities;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.CharsetConverter;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.specification.NamespaceImpl;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.engine.specification.URIImpl;
import org.seasar.mayaa.impl.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TemplateNodeHandler extends SpecificationNodeHandler implements EntityResolver2, LexicalHandler {

    private static final Log LOG = LogFactory.getLog(TemplateNodeHandler.class);

    // TODO doctype宣言後の改行をテンプレート通りにしたあと削除
    private boolean _afterDocType;// workaround for doctype
    private int _inEntity;
    private boolean inDTD = false;
    private boolean _outputTemplateWhitespace = true;
    private boolean _isSSIIncludeReplacementEnabled = false;

    public TemplateNodeHandler(Template specification) {
        super(specification);
    }

    protected Template getTemplate() {
        return (Template) getSpecification();
    }

    public void setOutputTemplateWhitespace(boolean outputTemplateWhitespace) {
        _outputTemplateWhitespace = outputTemplateWhitespace;
    }

    public void setSSIIncludeReplacementEnabled(boolean isSSIIncludeReplacementEnabled) {
        _isSSIIncludeReplacementEnabled = isSSIIncludeReplacementEnabled;
    }

    /**
     * SSI includeの記述をm:insertへ置き換える処理が有効か。
     * @return 有効なら{@code true}、そうでなければ{@code false}を返す。
     */
    protected boolean isSSIIncludeReplacementEnabled() {
        return _isSSIIncludeReplacementEnabled;
    }

    /**
     * テンプレート由来のノードオブジェクトを生成する。
     */
    @Override
    protected SpecificationNode createChildNode(
            QName qName, String systemID, int lineNumber, int sequenceID, String prefix) {
        return SpecificationUtil.createSpecificationNode(
                qName, systemID, lineNumber, true, sequenceID, prefix);
    }

    protected boolean isRemoveWhitespace() {
        return _outputTemplateWhitespace == false;
    }

    /**
     * metaタグのhttp-equiv="content-type" の場合、contentのcharsetを変換して
     * 返すようにする。
     * それ以外の場合はなにもしない。
     *
     * @param namespaceURI 名前空間URI
     * @param localName 要素名
     * @param qName QName
     * @param attributes 属性リスト
     * @return 変換された属性リストまたは変換されなかった場合は引数に指定した属性リスト
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

        // TODO doctype宣言後の改行をテンプレート通りにする
        // workaround NekoHTMLParserがdoctype宣言後に"\n"のみを含めてしまう
        // また、xercesそのままの場合は改行文字が来ない
        if (_afterDocType) {
            _afterDocType = false;// workaround for doctype
            int length = _charactersBuffer.length();
            if (length > 0) {
                int firstCharIndex;
                for (firstCharIndex = 0; firstCharIndex < length; firstCharIndex++) {
                    char currentChar = _charactersBuffer.charAt(firstCharIndex);
                    if (currentChar != ' ' && currentChar != '\t') {
                        break;
                    }
                }
                if (_charactersBuffer.charAt(firstCharIndex) == '\n') {
                    _charactersBuffer.insert(firstCharIndex, '\r');
                } else if (_charactersBuffer.charAt(firstCharIndex) != '\r') {
                    _charactersBuffer.insert(firstCharIndex, "\r\n");
                }
            } else {
                _charactersBuffer.insert(0, "\r\n");
            }
        }// workaround
        super.startElement(namespaceURI, localName, qName, wrapedAttributes);
    }

    protected String removeIgnorableWhitespace(String characters) {
        StringBuilder buffer = new StringBuilder(characters.length());
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

    @Override
    public void characters(char[] buffer, int start, int length) {
        if (_inEntity == 0) {
            appendCharactersBuffer(buffer, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] buffer, int start, int length) {
        appendCharactersBuffer(buffer, start, length);
    }

    @Override
    public void comment(char[] buffer, int start, int length) {
        // パーサがDTD定義をコメントに埋め込んでくるためDTD処理中であれば除外する。
        if (inDTD) {
            return;
        }
        addCharactersNode();
        String comment = new String(buffer, start, length);

        if (isSSIIncludeReplacementEnabled()) {
            String trimmed = comment.trim();
            if (trimmed.startsWith(INCLUDE_PREFIX)) {
                Matcher match = INCLUDE_PATTERN.matcher(trimmed);
                if (match.find()) {
                    LOG.debug("replace SSI to m:insert. " + trimmed);
                    includeToInsert(match.group(2));
                    return;
                }
            }
        }

        SpecificationNode node = addNode(QM_COMMENT);
        node.setDefaultNamespaceURI(node.getParentSpace().getDefaultNamespaceURI());
        node.addAttribute(QM_TEXT, comment);
    }

    @Override
    public void startCDATA() {
        addCharactersNode();
        SpecificationNode node = addNode(QM_CDATA);

        node.setParentNode(getCurrentNode());
        setCurrentNode(node);
        enterCData();
    }

    @Override
    public void endCDATA() {
        addCharactersNode();
        setCurrentNode(getCurrentNode().getParentNode());
        leaveCData();
    }

    /**
     * &lt;!--#include virtual="/common/include.html?foo=bar&amp;amp;bar=baz#fragment" --&gt;
     */
    private static final String INCLUDE_PREFIX = "#include";
    private static final Pattern INCLUDE_PATTERN =
        Pattern.compile("#include\\s+(virtual|file)\\s*=\\s*\"([^\"]+)\"\\s*");
    private static final Pattern AMPERSAND_PATTERN = Pattern.compile("(&amp;|&)");
    protected static final String AUTO_INSERT_NAMESPACE = "autoinsertnamespace";

    /**
     * SSI includeの記述を m:insert に置き換える。virtual, fileの両方に対応。
     * ただし、本来fileでは上位階層を指定できないが、ここでは制限を意識しない。
     * <p>
     * &lt;!--#include virtual="/common/include.html?foo=bar&amp;amp;bar=baz#fragment" --&gt;
     * </p>
     * @param virtualValue virtual属性の値
     * @throws NullPointerException virtualValueがnullの場合に発生する。
     */
    protected void includeToInsert(String virtualValue) {
        if (virtualValue == null) {
            throw new IllegalArgumentException("virtualValue must not null.");
        }
        String[] parsed = StringUtil.parseURIQuery(virtualValue, EngineUtil.getEngineSetting(CONST_IMPL.SUFFIX_SEPARATOR, "$"));

        includeToInsert(parsed[0], parsed[1], parsed[2]);
    }

    protected void includeToInsert(String componentPath, String query, String componentName) {
        URI namespace = URI_MAYAA;
        NodeTreeWalker current = getCurrentNode();
        if (current instanceof SpecificationNode) {
            namespace = ((SpecificationNode) current).getDefaultNamespaceURI();
        }
        SpecificationNode newNode = addNode(QNameImpl.getInstance(namespace, "span"));
        newNode.setDefaultNamespaceURI(newNode.getParentSpace().getDefaultNamespaceURI());

        String mayaaPrefix = "mmmmmmmmmmmm";// 重複しないようにまず使われないものを。
        newNode.addPrefixMapping(mayaaPrefix, URI_MAYAA);
        newNode.addAttribute(QM_INJECT, mayaaPrefix + ":insert");
        newNode.addAttribute(QNameImpl.getInstance("path"), componentPath);
        if (StringUtil.hasValue(componentName)) {
            newNode.addAttribute(QNameImpl.getInstance("name"), componentName);
        }
        if (StringUtil.hasValue(query)) {
            URI parameterURI = URIImpl.getInstance(AUTO_INSERT_NAMESPACE);// 重複しないようにまず使われないものを。
            String[] parameters = AMPERSAND_PATTERN.split(query);
            for (int i = 0; i < parameters.length; i++) {
                String name;
                String value;
                int eq = parameters[i].indexOf('=');
                if (eq > 0) {
                    name = parameters[i].substring(0, eq).trim();
                    value = parameters[i].substring(eq + 1).trim();
                } else {
                    name = parameters[i].trim();
                    value = "";
                }
                newNode.addAttribute(QNameImpl.getInstance(parameterURI, name), value);
            }
        }
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
         * @param index The attribute index (zero-based).
         * @return The XML 1.0 qualified name, or the empty string if none is available, or null if the index is out of range.
         * @see org.xml.sax.Attributes#getQName(int)
         */
        public String getQName(int index) {
            return _original.getQName(index);
        }

        /**
         * @param index The attribute index (zero-based).
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

    //- implementation of DTD Catalog.
    // Java9 より標準の javax.xml.catalog.CatalogReader が提供されるので最低バージョンが上がるタイミングで標準半に置き換えたい。
    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return null;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return this.resolveEntity(null, publicId, null, systemId);
    }

    @Override
    /**
     * xhtmlのパースの際にリモートのDTD定義を取得しに行こうとするためJAR内に格納したものを返す。
     */
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
            throws SAXException, IOException {

        String path = null;
        switch (systemId) {
            case "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd":
            case "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd":
            case "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd":
                path = "/dtd/" + systemId.substring("http://www.w3.org/".length());
                break;
            case "xhtml-lat1.ent":
            case "xhtml-symbol.ent":
            case "xhtml-special.ent":
                path = "/dtd/" + systemId;
                break;
            default:
                break;
        }
        if (path != null) {
            InputStream is = getClass().getResourceAsStream(path);
            return new InputSource(is);
        }
        return null;
    }

    //- LexicalHander
    @Override
    public void startEntity(String name) {
        if (inDTD) {
            return;
        }
        if (HTMLEntities.get(name) != -1) {
            String entityRef = "&" + name + ";";
            appendCharactersBuffer(entityRef);    
        }
        ++_inEntity;
    }

    @Override
    public void endEntity(String name) {
        if (inDTD) {
            return;
        }
        --_inEntity;
    }

    @Override
    public void startDTD(String name, String publicID, String systemID) {
        inDTD = true;
        addCharactersNode();
        SpecificationNode node = addNode(QM_DOCTYPE);
        node.addAttribute(QM_NAME, name);
        if (StringUtil.hasValue(publicID)) {
            node.addAttribute(QM_PUBLIC_ID, publicID);
        }
        if (StringUtil.hasValue(systemID)) {
            node.addAttribute(QM_SYSTEM_ID, systemID);
        }

        // TODO doctype宣言後の改行をテンプレート通りにしたあと削除
        _afterDocType = true;// workaround for doctype
    }

    @Override
    public void endDTD() {
        inDTD = false;
    }

    @Override
    Namespace getTopLevelNamespace() {
        Specification spec = getSpecification();
        PrefixMapping defaultMapping = BuilderUtil.getPrefixMapping(spec.getSystemID());
        URI defaultURI = defaultMapping.getNamespaceURI();

        Namespace _topLevelNamespace = new NamespaceImpl();
        _topLevelNamespace.setDefaultNamespaceURI(defaultURI);
        return _topLevelNamespace;
    }

}
