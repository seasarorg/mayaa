/*
 * Copyright 2004-2022 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.builder.parser;

import java.io.CharConversionException;
import java.io.EOFException;
import java.io.IOException;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.ExternalSubsetResolver;
import org.apache.xerces.impl.XMLEntityHandler;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.XMLScanner;
import org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.apache.xerces.impl.io.MalformedByteSequenceException;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.seasar.mayaa.impl.knowledge.HTMLKnowledge;

/**
 * 
 * 外部の文書宣言は解決しない
 * フォーマル公開識別子については妥当性の検証を行わない。
 * HTML文字参照以外は解決しない
 */
public class HTMLScanner extends XMLScanner implements XMLDocumentScanner, XMLEntityHandler {
    /* -------------------------------------- */
    // scanner states

    protected enum ScanState {
        START_OF_MARKUP,    /** Scanner state: start of markup. */
        COMMENT,    /** Scanner state: comment. */
        PI, /** Scanner state: processing instruction. */
        DOCTYPE,        /** Scanner state: DOCTYPE. */
        ROOT_ELEMENT,    /** Scanner state: root element. */
        CONTENT,       /** Scanner state: content. */
        REFERENCE,        /** Scanner state: reference. */
        END_OF_INPUT,    /** Scanner state: end of input. */
        TERMINATED,    /** Scanner state: terminated. */
        CDATA,        /** Scanner state: CDATA section. */
        TEXT_DECL,        /** Scanner state: Text declaration. */
        SCRIPT,        /** Scanner state: content. */
        /* ------ XMLDocumentScannerImpl ------------------------- */
        XML_DECL,    /** Scanner state: XML declaration. */
        PROLOG,    /** Scanner state: prolog. */
        TRAILING_MISC,    /** Scanner state: trailing misc. */
        DTD_INTERNAL_DECLS,    /** Scanner state: DTD internal declarations. */
        DTD_EXTERNAL,    /** Scanner state: open DTD external subset. */
        DTD_EXTERNAL_DECLS,    /** Scanner state: DTD external declarations. */
    };

    /** Debug attribute normalization. */
    // protected static final boolean DEBUG_ATTR_NORMALIZATION = true;

    // feature identifiers

    /** Feature identifier: namespaces. */
    protected static final String NAMESPACES = Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;

    /** Feature identifier: notify built-in refereces. */
    protected static final String NOTIFY_BUILTIN_REFS = Constants.XERCES_FEATURE_PREFIX
            + Constants.NOTIFY_BUILTIN_REFS_FEATURE;

    // property identifiers

    /** Property identifier: entity resolver. */
    protected static final String ENTITY_RESOLVER = Constants.XERCES_PROPERTY_PREFIX
            + Constants.ENTITY_RESOLVER_PROPERTY;

    /** Property identifier: DTD scanner. */
    protected static final String DTD_SCANNER = Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_SCANNER_PROPERTY;

    /** property identifier: ValidationManager */
    protected static final String VALIDATION_MANAGER = Constants.XERCES_PROPERTY_PREFIX
            + Constants.VALIDATION_MANAGER_PROPERTY;

    /** property identifier: NamespaceContext */
    protected static final String NAMESPACE_CONTEXT = Constants.XERCES_PROPERTY_PREFIX
            + Constants.NAMESPACE_CONTEXT_PROPERTY;

    // recognized features and properties

    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES = {
            NAMESPACES,
            VALIDATION,
            NOTIFY_BUILTIN_REFS,
            NOTIFY_CHAR_REFS,
            /** from XMLDocumentScannerImpl */
    };

    /** Feature defaults. */
    private static final Boolean[] FEATURE_DEFAULTS = {
            null,
            null,
            Boolean.FALSE,
            Boolean.FALSE,
            Boolean.TRUE,
            Boolean.FALSE,
    };

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {
            SYMBOL_TABLE,
            ERROR_REPORTER,
            ENTITY_MANAGER,
            ENTITY_RESOLVER,
            DTD_SCANNER, /** from XMLDocumentScannerImpl */
            VALIDATION_MANAGER, /** from XMLDocumentScannerImpl */
            NAMESPACE_CONTEXT, /** from XMLDocumentScannerImpl */
    };

    /** Property defaults. */
    private static final Object[] PROPERTY_DEFAULTS = {
            null,
            null,
            null,
            null,
            null,
            null,
            null,
    };

    // debugging

    /** Debug scanner state. */
    private static final boolean DEBUG_SCANNER_STATE = false;

    /** Debug dispatcher. */
    private static final boolean DEBUG_DISPATCHER = false;

    /** Debug content dispatcher scanning. */
    protected static final boolean DEBUG_CONTENT_SCANNING = false;

    //
    // Data
    //

    // protected data

    /** 欠落している終了タグを追加する */
    boolean fAddMissingEndElement = false;

    /** headタグを検出済み */
    boolean fSeenHead = false;
    /** bodyタグを検出済み */
    boolean fSeenBody = false;

    /** Document handler. */
    protected XMLDocumentHandler fDocumentHandler;

    /** Entity stack. */
    protected int[] fEntityStack = new int[4];

    /** Markup depth. */
    protected int fMarkupDepth;

    /** Scanner state. */
    protected ScanState fScannerState;

    /** SubScanner state: inside scanContent method. */
    protected boolean fInScanContent = false;

    /** has external dtd */
    protected boolean fHasExternalDTD;

    /** Standalone. */
    protected boolean fStandalone;

    /** True if [Entity Declared] is a VC; false if it is a WFC. */
    protected boolean fIsEntityDeclaredVC;

    /** External subset resolver. **/
    protected ExternalSubsetResolver fExternalSubsetResolver;

    // element information

    /** Current element. */
    protected QName fCurrentElement;

    /** Element stack. */
    protected final ElementStack fElementStack = new ElementStack();

    // other info

    /**
     * Document system identifier.
     * REVISIT: So what's this used for? - NG
     * protected String fDocumentSystemId;
     ******/

    // features

    /** Notify built-in references. */
    protected boolean fNotifyBuiltInRefs = false;

        /* ------ XMLDocumentScannerImpl ------------------------- */
    // properties
    /** DTD scanner. */
    protected XMLDTDScanner fDTDScanner;

    // protected data

    // other info

    /** Doctype name. */
    protected String fDoctypeName;

    /** Doctype declaration public identifier. */
    protected String fDoctypePublicId;

    /** Doctype declaration system identifier. */
    protected String fDoctypeSystemId;

    /** Namespace support. */
    protected NamespaceContext fNamespaceContext = new NamespaceSupport();

    // features

    // state

    // dispatchers

    /** Active dispatcher. */
    protected Dispatcher fDispatcher;

    /** Content dispatcher. */
    protected final Dispatcher fContentDispatcher = createContentDispatcher();

    /** XML declaration dispatcher. */
    protected final Dispatcher fXMLDeclDispatcher = new XMLDeclDispatcher();

    /** Prolog dispatcher. */
    protected final Dispatcher fPrologDispatcher = new PrologDispatcher();

    /** DTD dispatcher. */
    protected final Dispatcher fDTDDispatcher = new DTDDispatcher();

    /** Trailing miscellaneous section dispatcher. */
    protected final Dispatcher fTrailingMiscDispatcher = new TrailingMiscDispatcher();
    
    
    // temporary variables

    /** Element QName. */
    protected final QName fElementQName = new QName();

    /** Attribute QName. */
    protected final QName fAttributeQName = new QName();

    /** Element attributes. */
    protected final XMLAttributesImpl fAttributes = new XMLAttributesImpl();

    /** String. */
    protected final XMLString fTempString = new XMLString();

    /** String. */
    protected final XMLString fTempString2 = new XMLString();

    // temporary variables

    /** String. */
    private final XMLString fString = new XMLString();

    /** External subset source. */
    private XMLInputSource fExternalSubsetSource = null;

    /** A DTD Description. */
    private final XMLDTDDescription fDTDDescription = new XMLDTDDescription(null, null, null, null, null);

    /** Array of 3 strings. */
    private final String[] fStrings = new String[3];

    /** String buffer. */
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();

    /** String buffer. */
    private final XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();

    /** Another QName. */
    private final QName fQName = new QName();

    /** Single character array. */
    private final char[] fSingleChar = new char[1];

    /**
     * Saw spaces after element name or between attributes.
     * 
     * This is reserved for the case where scanning of a start element spans
     * several methods, as is the case when scanning the start of a root element
     * where a DTD external subset may be read after scanning the element name.
     */
    private boolean fSawSpace;

    /** Reusable Augmentations. */
    private Augmentations fTempAugmentations = null;

    //
    // XMLDocumentScanner methods
    //

    /**
     * Scans a document.
     *
     * @param complete True if the scanner should scan the document
     *                 completely, pushing all events to the registered
     *                 document handler. A value of false indicates that
     *                 that the scanner should only scan the next portion
     *                 of the document and return. A scanner instance is
     *                 permitted to completely scan a document if it does
     *                 not support this "pull" scanning model.
     *
     * @return True if there is more to scan, false otherwise.
     */
    @Override
    public boolean scanDocument(boolean complete)
            throws IOException, XNIException {

        // reset entity scanner
        fEntityScanner = fEntityManager.getEntityScanner();
        
        // keep dispatching "events"
        fEntityManager.setEntityHandler(this);
        do {
            if (!fDispatcher.dispatch(complete)) {
                return false;
            }
        } while (complete);

        // return success
        return true;

    } // scanDocument(boolean):boolean

    //
    // XMLComponent methods
    //

    @Override
    public String[] getRecognizedFeatures() {
        return (String[]) (RECOGNIZED_FEATURES.clone());
    } // getRecognizedFeatures():String[]

    @Override
    public void setFeature(String featureId, boolean state)
            throws XMLConfigurationException {

        super.setFeature(featureId, state);

        // Xerces properties
        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            final int suffixLength = featureId.length() - Constants.XERCES_FEATURE_PREFIX.length();
            if (suffixLength == Constants.NOTIFY_BUILTIN_REFS_FEATURE.length() &&
                    featureId.endsWith(Constants.NOTIFY_BUILTIN_REFS_FEATURE)) {
                fNotifyBuiltInRefs = state;
            }
        }

    } // setFeature(String,boolean)

    @Override
    public String[] getRecognizedProperties() {
        return (String[]) (RECOGNIZED_PROPERTIES.clone());
    } // getRecognizedProperties():String[]

    @Override
    public void setProperty(String propertyId, Object value)
            throws XMLConfigurationException {

        super.setProperty(propertyId, value);

        // Xerces properties
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.XERCES_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.ENTITY_MANAGER_PROPERTY.length() &&
                    propertyId.endsWith(Constants.ENTITY_MANAGER_PROPERTY)) {
                fEntityManager = (XMLEntityManager) value;
                return;
            }
            if (suffixLength == Constants.ENTITY_RESOLVER_PROPERTY.length() &&
                    propertyId.endsWith(Constants.ENTITY_RESOLVER_PROPERTY)) {
                fExternalSubsetResolver = (value instanceof ExternalSubsetResolver) ? (ExternalSubsetResolver) value
                        : null;
                return;
            }
            if (suffixLength == Constants.DTD_SCANNER_PROPERTY.length() &&
                    propertyId.endsWith(Constants.DTD_SCANNER_PROPERTY)) {
                fDTDScanner = (XMLDTDScanner) value;
            }
            if (suffixLength == Constants.NAMESPACE_CONTEXT_PROPERTY.length() &&
                    propertyId.endsWith(Constants.NAMESPACE_CONTEXT_PROPERTY)) {
                if (value != null) {
                    fNamespaceContext = (NamespaceContext) value;
                }
            }

            return;
        }

    } // setProperty(String,Object)

    @Override
    public Boolean getFeatureDefault(String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; i++) {
            if (RECOGNIZED_FEATURES[i].equals(featureId)) {
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } // getFeatureDefault(String):Boolean

    @Override
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; i++) {
            if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } // getPropertyDefault(String):Object


    protected boolean isValidNameChar(int value) {
        return (XMLChar.isName(value) || value == '@' || value == '[' || value == ']');
    } // isValidNameChar(int): boolean

    protected boolean isValidNameStartChar(int value) {
        return (XMLChar.isNameStart(value) || value == '@');
    } // isValidNameStartChar(int): boolean

    //
    // XMLDocumentSource methods
    //

    /**
     * setDocumentHandler
     * 
     * @param documentHandler
     */
    public void setDocumentHandler(XMLDocumentHandler documentHandler) {
        fDocumentHandler = documentHandler;
    } // setDocumentHandler(XMLDocumentHandler)

    /** Returns the document handler */
    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler;
    }

    // scanning methods

    /**
     * Scans an XML or text declaration.
     * <p>
     * 
     * <pre>
     * [23] XMLDecl ::= '&lt;?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
     * [24] VersionInfo ::= S 'version' Eq (' VersionNum ' | " VersionNum ")
     * [80] EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' |  "'" EncName "'" )
     * [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
     * [32] SDDecl ::= S 'standalone' Eq (("'" ('yes' | 'no') "'")
     *                 | ('"' ('yes' | 'no') '"'))
     *
     * [77] TextDecl ::= '&lt;?xml' VersionInfo? EncodingDecl S? '?>'
     * </pre>
     *
     * @param scanningTextDecl True if a text declaration is to
     *                         be scanned instead of an XML
     *                         declaration.
     */
    protected void scanXMLDeclOrTextDecl(boolean scanningTextDecl)
            throws IOException, XNIException {

        // scan decl
        super.scanXMLDeclOrTextDecl(scanningTextDecl, fStrings);
        fMarkupDepth--;

        // pseudo-attribute values
        String version = fStrings[0];
        String encoding = fStrings[1];
        String standalone = fStrings[2];

        // set standalone
        fStandalone = standalone != null && standalone.equals("yes");
        fEntityManager.setStandalone(fStandalone);

        // set version on reader
        fEntityScanner.setXMLVersion(version);

        // call handler
        if (fDocumentHandler != null) {
            if (scanningTextDecl) {
                fDocumentHandler.textDecl(version, encoding, null);
            } else {
                fDocumentHandler.xmlDecl(version, encoding, standalone, null);
            }
        }

        // set encoding on reader
        if (encoding != null) {
            fEntityScanner.setEncoding(encoding);
        }

    } // scanXMLDeclOrTextDecl(boolean)

    /**
     * Scans a processing data. This is needed to handle the situation
     * where a document starts with a processing instruction whose
     * target name <em>starts with</em> "xml". (e.g. xmlfoo)
     *
     * @param target The PI target
     * @param data   The string to fill in with the data
     */
    protected void scanPIData(String target, XMLString data)
            throws IOException, XNIException {

        super.scanPIData(target, data);
        fMarkupDepth--;

        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.processingInstruction(target, data, null);
        }

    } // scanPIData(String)

    /**
     * Scans a comment.
     * <p>
     * 
     * <pre>
     * [15] Comment ::= '&lt;!--' ((Char - '-') | ('-' (Char - '-')))* '-->'
     * </pre>
     * <p>
     * <strong>Note:</strong> Called after scanning past '&lt;!--'
     */
    protected void scanComment() throws IOException, XNIException {

        //----
        // text
        // REVISIT: handle invalid character, eof
        fStringBuffer.clear();
        while (fEntityScanner.scanData("--", fStringBuffer)) {
            int c = fEntityScanner.peekChar();
            if (c != -1) {
                if (XMLChar.isHighSurrogate(c)) {
                    scanSurrogates(fStringBuffer);
                }
                else if (isInvalidLiteral(c)) {
                    reportFatalError("InvalidCharInComment",
                                     new Object[] { Integer.toHexString(c) }); 
                    fEntityScanner.scanChar();
                }
            } 
        }
        if (!fEntityScanner.skipChar('>')) {
            reportFatalError("DashDashInComment", null);
        }
        //----
        fMarkupDepth--;

        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.comment(fStringBuffer, null);
        }

    } // scanComment()

    /**
     * &lt;/script&gt; までスキャンしてスクリプト内容をcharacterイベントとして発火させ、その後、endElementイベントを発火する。
     * @throws IOException
     * @throws XNIException
     */
    protected void scanScript() throws IOException, XNIException {
        fStringBuffer.clear();
        while (fEntityScanner.scanData("</script>", fStringBuffer)) {
            int c = fEntityScanner.peekChar();
            if (c != -1) {
                if (XMLChar.isHighSurrogate(c)) {
                    scanSurrogates(fStringBuffer);
                }
                else if (isInvalidLiteral(c)) {
                    reportFatalError("InvalidCharInComment",
                                     new Object[] { Integer.toHexString(c) }); 
                    fEntityScanner.scanChar();
                }
            }
        }
        if (fDocumentHandler != null) {
            fDocumentHandler.characters(fStringBuffer, null);

            fElementStack.popElement(fElementQName);
            fMarkupDepth--;
            fDocumentHandler.endElement(fElementQName, null);
        }
    }

    /**
     * Scans a start element. This method will handle the binding of
     * namespace information and notifying the handler of the start
     * of the element.
     * <p>
     * 
     * <pre>
     * [44] EmptyElemTag ::= '&lt;' Name (S Attribute)* S? '/>'
     * [40] STag ::= '&lt;' Name (S Attribute)* S? '>'
     * </pre>
     * <p>
     * <strong>Note:</strong> This method assumes that the leading
     * '&lt;' character has been consumed.
     * <p>
     * <strong>Note:</strong> This method uses the fElementQName and
     * fAttributes variables. The contents of these variables will be
     * destroyed. The caller should copy important information out of
     * these variables before calling this method.
     *
     * @return True if element is empty. (i.e. It matches
     *         production [44].
     */

    /**
     * Scans the name of an element in a start or empty tag.
     * 
     * @see #scanStartElement()
     */
    protected void scanStartElementName()
            throws IOException, XNIException {
        // name
        String name = fEntityScanner.scanName();
        fElementQName.setValues(null, name, name, null);
        // Must skip spaces here because the DTD scanner
        // would consume them at the end of the external subset.
        fSawSpace = fEntityScanner.skipSpaces();
    } // scanStartElementName()

    /**
     * Scans the remainder of a start or empty tag after the element name.
     * 
     * @see #scanStartElement
     * @return True if element is empty.
     */
    protected boolean scanStartElementAfterName()
            throws IOException, XNIException {
        String rawname = fElementQName.rawname;

        // push element stack
        fCurrentElement = fElementStack.pushElement(fElementQName);

        // attributes
        boolean empty = false;
        fAttributes.removeAllAttributes();
        do {

            // end tag?
            int c = fEntityScanner.peekChar();
            if (c == '>') {
                fEntityScanner.scanChar();
                break;
            } else if (c == '/') {
                fEntityScanner.scanChar();
                if (!fEntityScanner.skipChar('>')) {
                    reportFatalError("ElementUnterminated",
                            new Object[] { rawname });
                }
                empty = true;
                break;
            } else if (!isValidNameStartChar(c) || !fSawSpace) {
                // Second chance. Check if this character is a high
                // surrogate of a valid name start character.
                if (!isValidNameStartHighSurrogate(c) || !fSawSpace) {
                    reportFatalError("ElementUnterminated",
                            new Object[] { rawname });
                }
            }

            // attributes
            scanAttribute(fAttributes);

            // spaces
            fSawSpace = fEntityScanner.skipSpaces();

        } while (true);

        // call handler
        if (fDocumentHandler != null) {
            if (empty) {

                // decrease the markup depth..
                fMarkupDepth--;
                // check that this element was opened in the same entity
                if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
                    reportFatalError("ElementEntityMismatch",
                            new Object[] { fCurrentElement.rawname });
                }

                fDocumentHandler.emptyElement(fElementQName, fAttributes, null);

                // pop the element off the stack..
                fElementStack.popElement(fElementQName);
            } else {
                fDocumentHandler.startElement(fElementQName, fAttributes, null);
            }
        }

        if (DEBUG_CONTENT_SCANNING)
            System.out.println("<<< scanStartElementAfterName(): " + empty);
        return empty;
    } // scanStartElementAfterName()

    /**
     * Scans an attribute.
     * <p>
     * 
     * <pre>
     * [41] Attribute ::= Name Eq AttValue
     * </pre>
     * <p>
     * <strong>Note:</strong> This method assumes that the next
     * character on the stream is the first character of the attribute
     * name.
     * <p>
     * <strong>Note:</strong> This method uses the fAttributeQName and
     * fQName variables. The contents of these variables will be
     * destroyed.
     *
     * @param attributes The attributes list for the scanned attribute.
     */
    /**
     * Scan attributes in start element.
     * consume
     */
    protected void scanAttribute(XMLAttributes attributes) throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING)
            System.out.println(">>> scanAttribute()");

        // name
        int c = fEntityScanner.peekChar();
        if (isValidNameStartChar(c)) {
            fStringBuffer.clear();
            do {
                c = fEntityScanner.scanChar();
                fStringBuffer.append((char) c);
            } while (isValidNameChar(fEntityScanner.peekChar()));
            String name = fStringBuffer.toString();
            fAttributeQName.setValues(null, name, name, null);
        }

        // content
        int oldLen = attributes.getLength();
        int attrIndex = attributes.addAttribute(fAttributeQName, XMLSymbols.fCDATASymbol, null);

        // WFC: Unique Att Spec
        if (oldLen == attributes.getLength()) {
            reportFatalError("AttributeNotUnique", new Object[] { fCurrentElement.rawname, fAttributeQName.rawname });
        }

        // equals
        fEntityScanner.skipSpaces();
        if (fEntityScanner.skipChar('=')) {
            fEntityScanner.skipSpaces();

            // Scan attribute value and return true if the un-normalized and normalized
            // value are the same
            boolean isSameNormalizedAttr = scanAttributeValue(fTempString, fTempString2,
                    fAttributeQName.rawname, fIsEntityDeclaredVC, fCurrentElement.rawname);

            attributes.setValue(attrIndex, fTempString.toString());
            // If the non-normalized and normalized value are the same, avoid creating a new
            // string.
            if (!isSameNormalizedAttr) {
                attributes.setNonNormalizedValue(attrIndex, fTempString2.toString());
            }
            attributes.setSpecified(attrIndex, true);

            if (DEBUG_CONTENT_SCANNING)
                System.out.println("<<< scanAttribute()");
        } else {
            // if a value is omitted, it is treated as empty string.
            attributes.setValue(attrIndex, "");
            attributes.setSpecified(attrIndex, true);
        }
    }

    @Override
    protected boolean scanAttributeValue(XMLString value,
            XMLString nonNormalizedValue,
            String atName,
            boolean checkEntities, String eleName)
            throws IOException, XNIException {

        // quote
        int quote = fEntityScanner.peekChar();
        if (quote != '\'' && quote != '"') {
            // 引用符から始まらない場合は半角空白(0x20)まで読み取る。
            quote = ' ';
        } else {
            quote = fEntityScanner.scanChar();
        }

        int entityDepth = fEntityDepth;

        int c = fEntityScanner.scanLiteral(quote, value);
        if (DEBUG_ATTR_NORMALIZATION) {
            System.out.println("** scanLiteral -> \"" + value.toString() + "\"");
        }

        int fromIndex = 0;
        if (c == quote && (fromIndex = isUnchangedByNormalization(value)) == -1) {
            /** Both the non-normalized and normalized attribute values are equal. **/
            nonNormalizedValue.setValues(value);
            int cquote = fEntityScanner.scanChar();
            if (cquote != quote) {
                reportFatalError("CloseQuoteExpected", new Object[] { eleName, atName });
            }
            return true;
        }
        XMLStringBuffer fStringBuffer = new XMLStringBuffer();
        XMLStringBuffer fStringBuffer2 = new XMLStringBuffer(value);
        XMLStringBuffer fStringBuffer3 = new XMLStringBuffer();
        normalizeWhitespace(value, fromIndex);
        if (DEBUG_ATTR_NORMALIZATION) {
            System.out.println("** normalizeWhitespace -> \"" + value.toString() + "\"");
        }
        if (quote == ' ' && (c == '\r' || c == '\n')) {
            quote = c;
        }
        if (c != quote) {
            fScanningAttribute = true;
            fStringBuffer.clear();
            do {
                fStringBuffer.append(value);
                if (DEBUG_ATTR_NORMALIZATION) {
                    System.out.println("** value2: \"" + fStringBuffer.toString() + "\"");
                }
                if (c == '&') {
                    fEntityScanner.scanChar();
                    fStringBuffer.append((char) c);
                    if (entityDepth == fEntityDepth) {
                        fStringBuffer2.append((char) c);
                    }
                    if (DEBUG_ATTR_NORMALIZATION) {
                        System.out.println("** valueF: \""
                                + fStringBuffer.toString() + "\"");
                    }
                } else if (c == '<') {
                    reportFatalError("LessthanInAttValue",
                            new Object[] { eleName, atName });
                    fEntityScanner.scanChar();
                    if (entityDepth == fEntityDepth) {
                        fStringBuffer2.append((char) c);
                    }
                } else if (c == '%' || c == ']') {
                    fEntityScanner.scanChar();
                    fStringBuffer.append((char) c);
                    if (entityDepth == fEntityDepth) {
                        fStringBuffer2.append((char) c);
                    }
                    if (DEBUG_ATTR_NORMALIZATION) {
                        System.out.println("** valueF: \""
                                + fStringBuffer.toString() + "\"");
                    }
                } else if (c == '\n' || c == '\r') {
                    fEntityScanner.scanChar();
                    fStringBuffer.append(' ');
                    if (entityDepth == fEntityDepth) {
                        fStringBuffer2.append('\n');
                    }
                } else if (c != -1 && XMLChar.isHighSurrogate(c)) {
                    fStringBuffer3.clear();
                    if (scanSurrogates(fStringBuffer3)) {
                        fStringBuffer.append(fStringBuffer3);
                        if (entityDepth == fEntityDepth) {
                            fStringBuffer2.append(fStringBuffer3);
                        }
                        if (DEBUG_ATTR_NORMALIZATION) {
                            System.out.println("** valueI: \""
                                    + fStringBuffer.toString()
                                    + "\"");
                        }
                    }
                } else if (c != -1 && isInvalidLiteral(c)) {
                    reportFatalError("InvalidCharInAttValue",
                            new Object[] { eleName, atName, Integer.toString(c, 16) });
                    fEntityScanner.scanChar();
                    if (entityDepth == fEntityDepth) {
                        fStringBuffer2.append((char) c);
                    }
                }
                c = fEntityScanner.scanLiteral(quote, value);
                if (entityDepth == fEntityDepth) {
                    fStringBuffer2.append(value);
                }
                normalizeWhitespace(value);
            } while (c != quote || entityDepth != fEntityDepth);
            fStringBuffer.append(value);
            if (DEBUG_ATTR_NORMALIZATION) {
                System.out.println("** valueN: \"" + fStringBuffer.toString() + "\"");
            }
            value.setValues(fStringBuffer);
            fScanningAttribute = false;
        }
        nonNormalizedValue.setValues(fStringBuffer2);

        // quote
        int cquote = fEntityScanner.scanChar();
        if (cquote != quote) {
            reportFatalError("CloseQuoteExpected", new Object[] { eleName, atName });
        }
        return nonNormalizedValue.equals(value.ch, value.offset, value.length);

    } // scanAttributeValue()

    protected boolean scanStartElement() throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING)
            System.out.println(">>> scanStartElement()");

        // name
        String name = fEntityScanner.scanName();
        fElementQName.setValues(null, name, name, null);
        String rawname = fElementQName.rawname;

        // push element stack
        fCurrentElement = fElementStack.pushElement(fElementQName);

        // attributes
        boolean empty = false;
        boolean ignoreThis = false;
        fAttributes.removeAllAttributes();
        do {
            // spaces
            fEntityScanner.skipSpaces();

            // end tag?
            int c = fEntityScanner.peekChar();
            if (c == '>') {
                fEntityScanner.scanChar();
                // -- Mayaa Specific Code START
                if ("head".equalsIgnoreCase(fElementQName.rawname)) {
                    if (fSeenHead) {
                        ignoreThis = true;
                    }
                    fSeenHead = true;
                } else if ("body".equalsIgnoreCase(fElementQName.rawname)) {
                    if (fSeenBody) {
                        ignoreThis = true;
                    }
                    fSeenBody = true;
                }
                if (HTMLKnowledge.isVoidElementLocalPart(rawname)) {
                    empty = true;
                }
                // -- Mayaa Specific Code END
                break;
            } else if (c == '/') {
                fEntityScanner.scanChar();
                if (!fEntityScanner.skipChar('>')) {
                    reportFatalError("ElementUnterminated",
                            new Object[] { rawname });
                }
                empty = true;
                break;
            } else if (!isValidNameStartChar(c)) {
                // Second chance. Check if this character is a high
                // surrogate of a valid name start character.
                if (!isValidNameStartHighSurrogate(c)) {
                    reportFatalError("ElementUnterminated",
                            new Object[] { rawname });
                }
            }

            // attributes
            scanAttribute(fAttributes);

        } while (true);

        // -- Mayaa では 1.2以前でのnekohtmlの動作の互換性を保つためにheadおよびbodyは一度のみ発火する。
        if (ignoreThis) {
            // この開始タグを無視する場合は階層を上げる
            fElementStack.popElement(fElementQName);
            fMarkupDepth--;
        } else {
            // call handler
            if (fDocumentHandler != null) {
                if (empty) {

                    // decrease the markup depth..
                    fMarkupDepth--;
                    // check that this element was opened in the same entity
                    if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
                        reportFatalError("ElementEntityMismatch", new Object[] { fCurrentElement.rawname });
                    }

                    fDocumentHandler.emptyElement(fElementQName, fAttributes, null);

                    // pop the element off the stack..
                    fElementStack.popElement(fElementQName);
                } else {
                    fDocumentHandler.startElement(fElementQName, fAttributes, null);
                }
            }
        }

        if (DEBUG_CONTENT_SCANNING)
            System.out.println("<<< scanStartElement(): " + empty);
        return empty;
    }

    /**
     * Scans element content.
     *
     * @return Returns the next character on the stream.
     */
    protected int scanContent() throws IOException, XNIException {

        XMLString content = fTempString;
        int c = fEntityScanner.scanContent(content);
        if (c == '\r') {
            // happens when there is the character reference &#13;
            fEntityScanner.scanChar();
            fStringBuffer.clear();
            fStringBuffer.append(fTempString);
            fStringBuffer.append((char) c);
            content = fStringBuffer;
            c = -1;
        }
        if (fDocumentHandler != null && content.length > 0) {
            fDocumentHandler.characters(content, null);
        }

        if (c == ']' && fTempString.length == 0) {
            fStringBuffer.clear();
            fStringBuffer.append((char) fEntityScanner.scanChar());
            // remember where we are in case we get an endEntity before we
            // could flush the buffer out - this happens when we're parsing an
            // entity which ends with a ]
            fInScanContent = true;
            //
            // We work on a single character basis to handle cases such as:
            // ']]]>' which we might otherwise miss.
            //
            if (fEntityScanner.skipChar(']')) {
                fStringBuffer.append(']');
                while (fEntityScanner.skipChar(']')) {
                    fStringBuffer.append(']');
                }
                if (fEntityScanner.skipChar('>')) {
                    reportFatalError("CDEndInContent", null);
                }
            }
            if (fDocumentHandler != null && fStringBuffer.length != 0) {
                fDocumentHandler.characters(fStringBuffer, null);
            }
            fInScanContent = false;
            c = -1;
        }
        return c;

    } // scanContent():int

    /**
     * Scans a CDATA section.
     * <p>
     * <strong>Note:</strong> This method uses the fTempString and
     * fStringBuffer variables.
     *
     * @param complete True if the CDATA section is to be scanned
     *                 completely.
     *
     * @return True if CDATA is completely scanned.
     */
    protected boolean scanCDATASection(boolean complete)
            throws IOException, XNIException {

        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.startCDATA(null);
        }

        while (true) {
            fStringBuffer.clear();
            if (!fEntityScanner.scanData("]]", fStringBuffer)) {
                if (fDocumentHandler != null && fStringBuffer.length > 0) {
                    fDocumentHandler.characters(fStringBuffer, null);
                }
                int brackets = 0;
                while (fEntityScanner.skipChar(']')) {
                    brackets++;
                }
                if (fDocumentHandler != null && brackets > 0) {
                    fStringBuffer.clear();
                    if (brackets > XMLEntityManager.DEFAULT_BUFFER_SIZE) {
                        // Handle large sequences of ']'
                        int chunks = brackets / XMLEntityManager.DEFAULT_BUFFER_SIZE;
                        int remainder = brackets % XMLEntityManager.DEFAULT_BUFFER_SIZE;
                        for (int i = 0; i < XMLEntityManager.DEFAULT_BUFFER_SIZE; i++) {
                            fStringBuffer.append(']');
                        }
                        for (int i = 0; i < chunks; i++) {
                            fDocumentHandler.characters(fStringBuffer, null);
                        }
                        if (remainder != 0) {
                            fStringBuffer.length = remainder;
                            fDocumentHandler.characters(fStringBuffer, null);
                        }
                    } else {
                        for (int i = 0; i < brackets; i++) {
                            fStringBuffer.append(']');
                        }
                        fDocumentHandler.characters(fStringBuffer, null);
                    }
                }
                if (fEntityScanner.skipChar('>')) {
                    break;
                }
                if (fDocumentHandler != null) {
                    fStringBuffer.clear();
                    fStringBuffer.append("]]");
                    fDocumentHandler.characters(fStringBuffer, null);
                }
            } else {
                if (fDocumentHandler != null) {
                    fDocumentHandler.characters(fStringBuffer, null);
                }
                int c = fEntityScanner.peekChar();
                if (c != -1 && isInvalidLiteral(c)) {
                    if (XMLChar.isHighSurrogate(c)) {
                        fStringBuffer.clear();
                        scanSurrogates(fStringBuffer);
                        if (fDocumentHandler != null) {
                            fDocumentHandler.characters(fStringBuffer, null);
                        }
                    } else {
                        reportFatalError("InvalidCharInCDSect",
                                new Object[] { Integer.toString(c, 16) });
                        fEntityScanner.scanChar();
                    }
                }
            }
        }
        fMarkupDepth--;

        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.endCDATA(null);
        }

        return true;

    } // scanCDATASection(boolean):boolean

    /**
     * Scans an end element.
     * <p>
     * 
     * <pre>
     * [42] ETag ::= '&lt;/' Name S? '>'
     * </pre>
     * <p>
     * <strong>Note:</strong> This method uses the fElementQName variable.
     * The contents of this variable will be destroyed. The caller should
     * copy the needed information out of this variable before calling
     * this method.
     *
     * @return The element depth.
     */
    protected int scanEndElement() throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING)
            System.out.println(">>> scanEndElement()");

        fElementStack.popElement(fElementQName);

        // Take advantage of the fact that next string _should_ be
        // "fElementQName.rawName",
        // In scanners most of the time is consumed on checks done for XML characters,
        // we can
        // optimize on it and avoid the checks done for endElement,
        // we will also avoid symbol table lookup - neeraj.bajaj@sun.com

        // this should work both for namespace processing true or false...

        // REVISIT: if the string is not the same as expected.. we need to do better
        // error handling..
        // We can skip this for now... In any case if the string doesn't match --
        // document is not well formed.
        if (!fEntityScanner.skipString(fElementQName.rawname)) {
            if (fAddMissingEndElement) {
                // call handler
                if (fDocumentHandler != null) {
                    fDocumentHandler.endElement(fElementQName, null);
                }
            } else {
                // Ignore missing end element.
                // reportFatalError("ETagRequired", new Object[]{fElementQName.rawname});
            }
            fMarkupDepth--;
            String name = fEntityScanner.scanName();
            fElementQName.setValues(null, name, name, null);
        }

        // end
        fEntityScanner.skipSpaces();
        if (!fEntityScanner.skipChar('>')) {
            reportFatalError("ETagUnterminated",
                    new Object[] { fElementQName.rawname });
        }
        fMarkupDepth--;

        // we have increased the depth for two markup "<" characters
        fMarkupDepth--;

        // check that this element was opened in the same entity
        if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
            reportFatalError("ElementEntityMismatch",
                    new Object[] { fCurrentElement.rawname });
        }

        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.endElement(fElementQName, null);
        }

        return fMarkupDepth;

    } // scanEndElement():int

    /**
     * Scans a character reference.
     * <p>
     * 
     * <pre>
     * [66] CharRef ::= '&amp;#' [0-9]+ ';' | '&amp;#x' [0-9a-fA-F]+ ';'
     * </pre>
     */
    protected void scanCharReference()
            throws IOException, XNIException {

        fStringBuffer2.clear();
        int ch = scanCharReferenceValue(fStringBuffer2, null);
        fMarkupDepth--;
        if (ch != -1) {
            // call handler
            if (fDocumentHandler != null) {
                if (fNotifyCharRefs) {
                    fDocumentHandler.startGeneralEntity(fCharRefLiteral, null, null, null);
                }
                Augmentations augs = null;
                if (fValidation && ch <= 0x20) {
                    if (fTempAugmentations != null) {
                        fTempAugmentations.removeAllItems();
                    } else {
                        fTempAugmentations = new AugmentationsImpl();
                    }
                    augs = fTempAugmentations;
                    augs.putItem(Constants.CHAR_REF_PROBABLE_WS, Boolean.TRUE);
                }
                fDocumentHandler.characters(fStringBuffer2, augs);
                if (fNotifyCharRefs) {
                    fDocumentHandler.endGeneralEntity(fCharRefLiteral, null);
                }
            }
        }

    } // scanCharReference()

    /**
     * Scans an entity reference.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws XNIException Thrown if handler throws exception upon
     *                      notification.
     */
    protected void scanEntityReference() throws IOException, XNIException {

        // name
        String name = fEntityScanner.scanName();
        if (name == null) {
            reportFatalError("NameRequiredInReference", null);
            return;
        }

        // end
        if (!fEntityScanner.skipChar(';')) {
            reportFatalError("SemicolonRequiredInReference", new Object[] { name });
        }
        fMarkupDepth--;

        // handle built-in entities
        if (name == fAmpSymbol) {
            handleCharacter('&', fAmpSymbol);
        } else if (name == fLtSymbol) {
            handleCharacter('<', fLtSymbol);
        } else if (name == fGtSymbol) {
            handleCharacter('>', fGtSymbol);
        } else if (name == fQuotSymbol) {
            handleCharacter('"', fQuotSymbol);
        } else if (name == fAposSymbol) {
            handleCharacter('\'', fAposSymbol);
        } else {
            if (fNotifyBuiltInRefs) {
                fDocumentHandler.startGeneralEntity(name, null, null, null);
                fDocumentHandler.endGeneralEntity(name, null);
            }
        }
    } // scanEntityReference()

    // utility methods

    /**
     * Calls document handler with a single character resulting from
     * built-in entity resolution.
     *
     * @param c
     * @param entity built-in name
     */
    private void handleCharacter(char c, String entity) throws XNIException {
        if (fDocumentHandler != null) {
            if (fNotifyBuiltInRefs) {
                fDocumentHandler.startGeneralEntity(entity, null, null, null);
            }

            fSingleChar[0] = c;
            fTempString.setValues(fSingleChar, 0, 1);
            fDocumentHandler.characters(fTempString, null);

            if (fNotifyBuiltInRefs) {
                fDocumentHandler.endGeneralEntity(entity, null);
            }
        }
    } // handleCharacter(char)

    /**
     * Handles the end element. This method will make sure that
     * the end element name matches the current element and notify
     * the handler about the end of the element and the end of any
     * relevent prefix mappings.
     * <p>
     * <strong>Note:</strong> This method uses the fQName variable.
     * The contents of this variable will be destroyed.
     *
     * @param element The element.
     *
     * @return The element depth.
     *
     * @throws XNIException Thrown if the handler throws a SAX exception
     *                      upon notification.
     *
     */
    // REVISIT: need to remove this method. It's not called anymore, because
    // the handling is done when the end tag is scanned. - SG
    protected int handleEndElement(QName element, boolean isEmpty)
            throws XNIException {

        fMarkupDepth--;
        // check that this element was opened in the same entity
        if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
            reportFatalError("ElementEntityMismatch",
                    new Object[] { fCurrentElement.rawname });
        }
        // make sure the elements match
        QName startElement = fQName;
        fElementStack.popElement(startElement);
        if (element.rawname != startElement.rawname) {
            reportFatalError("ETagRequired",
                    new Object[] { startElement.rawname });
        }

        // call handler
        if (fDocumentHandler != null && !isEmpty) {
            fDocumentHandler.endElement(element, null);
        }

        return fMarkupDepth;

    } // callEndElement(QName,boolean):int

    // helper methods

    /**
     * Sets the scanner state.
     *
     * @param state The new scanner state.
     */
    protected final void setScannerState(ScanState state) {

        fScannerState = state;
        if (DEBUG_SCANNER_STATE) {
            System.out.print("### setScannerState: ");
            System.out.print(getScannerStateName(state));
            System.out.println();
        }

    } // setScannerState(int)

    /**
     * Sets the dispatcher.
     *
     * @param dispatcher The new dispatcher.
     */
    protected final void setDispatcher(Dispatcher dispatcher) {
        fDispatcher = dispatcher;
        if (DEBUG_DISPATCHER) {
            System.out.print("%%% setDispatcher: ");
            System.out.print(getDispatcherName(dispatcher));
            System.out.println();
        }
    }

    //
    // Private methods
    //

    /** Returns the dispatcher name. */
    public String getDispatcherName(Dispatcher dispatcher) {

        if (DEBUG_DISPATCHER) {
            if (dispatcher != null) {
                String name = dispatcher.getClass().getName();
                int index = name.lastIndexOf('.');
                if (index != -1) {
                    name = name.substring(index + 1);
                    index = name.lastIndexOf('$');
                    if (index != -1) {
                        name = name.substring(index + 1);
                    }
                }
                return name;
            }
        }
        return "null";

    } // getDispatcherName():String

    //
    // Classes
    //

    //
    // XMLDocumentScanner methods
    //

    /**
     * Sets the input source.
     *
     * @param inputSource The input source.
     *
     * @throws IOException Thrown on i/o error.
     */
    @Override
    public void setInputSource(XMLInputSource inputSource) throws IOException {
        fEntityManager.setEntityHandler(this);
        fEntityManager.startDocumentEntity(inputSource);
        // fDocumentSystemId = fEntityManager.expandSystemId(inputSource.getSystemId());
    } // setInputSource(XMLInputSource)

    //
    // XMLComponent methods
    //
    boolean isSeenDoctypeDecl() {
        return fDoctypeName != null && !fDoctypeName.isEmpty();
    }

    /**
     * Resets the component. The component can query the component manager
     * about any features and properties that affect the operation of the
     * component.
     *
     * @param componentManager The component manager.
     */
    @Override
    public void reset(XMLComponentManager componentManager)
            throws XMLConfigurationException {

        super.reset(componentManager);
        // other settings
        // fDocumentSystemId = null;

        // initialize vars
        fMarkupDepth = 0;
        fCurrentElement = null;
        fElementStack.clear();
        fHasExternalDTD = false;
        fStandalone = false;
        fIsEntityDeclaredVC = false;
        fInScanContent = false;
        fSeenHead = false;
        fSeenBody = false;

        // other settings
        fDoctypeName = null;
        fDoctypePublicId = null;
        fDoctypeSystemId = null;
        fExternalSubsetSource = null;

        // setup dispatcher
        setScannerState(ScanState.CONTENT);
        setDispatcher(fContentDispatcher);

        if (fParserSettings) {
            // parser settings have changed. reset them.

            // xerces features
            try {
                fNotifyBuiltInRefs = componentManager.getFeature(NOTIFY_BUILTIN_REFS);
            } catch (XMLConfigurationException e) {
                fNotifyBuiltInRefs = false;
            }

            // xerces properties
            try {
                Object resolver = componentManager.getProperty(ENTITY_RESOLVER);
                fExternalSubsetResolver = (resolver instanceof ExternalSubsetResolver)
                        ? (ExternalSubsetResolver) resolver
                        : null;
            } catch (XMLConfigurationException e) {
                fExternalSubsetResolver = null;
            }
        }

        if (!fParserSettings) {
            // parser settings have not been changed
            fNamespaceContext.reset();
            // setup dispatcher
            setScannerState(ScanState.XML_DECL);
            setDispatcher(fXMLDeclDispatcher);
            return;
        }

        // xerces features
        try {
            fNamespaceContext = (NamespaceContext) componentManager.getProperty(NAMESPACE_CONTEXT);
        } catch (XMLConfigurationException e) {
        }
        if (fNamespaceContext == null) {
            fNamespaceContext = new NamespaceSupport();
        }
        fNamespaceContext.reset();

        fSymbolTable = (SymbolTable) componentManager.getProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY);

        // setup dispatcher
        setScannerState(ScanState.XML_DECL);
        setDispatcher(fXMLDeclDispatcher);

    } // reset(XMLComponentManager)

    //
    // XMLEntityHandler methods
    //

        /**
     * Scans External ID and return the public and system IDs.
     *
     * @param identifiers An array of size 2 to return the system id,
     *                    and public id (in that order).
     * @param optionalSystemId Specifies whether the system id is optional.
     *
     * <strong>Note:</strong> This method uses fString and fStringBuffer,
     * anything in them at the time of calling is lost.
     */
    @Override
    protected void scanExternalID(String[] identifiers,
                                  boolean optionalSystemId)
        throws IOException, XNIException {

        String systemId = null;
        String publicId = null;
        if (fEntityScanner.skipString("PUBLIC")) {
            if (!fEntityScanner.skipSpaces()) {
                reportFatalError("SpaceRequiredAfterPUBLIC", null);
            }
            scanPubidLiteral(fString);
            publicId = fString.toString();

            if (!fEntityScanner.skipSpaces() && !optionalSystemId) {
                reportFatalError("SpaceRequiredBetweenPublicAndSystem", null);
            }
        }

        if (publicId != null || fEntityScanner.skipString("SYSTEM")) {
            if (publicId == null && !fEntityScanner.skipSpaces()) {
                reportFatalError("SpaceRequiredAfterSYSTEM", null);
            }
            int quote = fEntityScanner.peekChar();
            if (quote != '\'' && quote != '"') {
                if (publicId != null && optionalSystemId) {
                    // looks like we don't have any system id
                    // simply return the public id
                    identifiers[0] = null;
                    identifiers[1] = publicId;
                    return;
                }
                reportFatalError("QuoteRequiredInSystemID", null);
            }
            fEntityScanner.scanChar();
            XMLString ident = fString;
            if (fEntityScanner.scanLiteral(quote, ident) != quote) {
                fStringBuffer.clear();
                do {
                    fStringBuffer.append(ident);
                    int c = fEntityScanner.peekChar();
                    if (XMLChar.isMarkup(c) || c == ']') {
                        fStringBuffer.append((char)fEntityScanner.scanChar());
                    }
                    else if (XMLChar.isHighSurrogate(c)) {
                        scanSurrogates(fStringBuffer);
                    }
                    else if (isInvalidLiteral(c)) {
                        reportFatalError("InvalidCharInSystemID",
                                new Object[] { Integer.toHexString(c) }); 
                        fEntityScanner.scanChar();
                    }
                } while (fEntityScanner.scanLiteral(quote, ident) != quote);
                fStringBuffer.append(ident);
                ident = fStringBuffer;
            }
            systemId = ident.toString();
            if (!fEntityScanner.skipChar(quote)) {
                reportFatalError("SystemIDUnterminated", null);
            }
        }

        // store result in array
        identifiers[0] = systemId;
        identifiers[1] = publicId;
    }

    /**
     * This method notifies of the start of an entity. The DTD has the
     * pseudo-name of "[dtd]" parameter entity names start with '%'; and
     * general entities are just specified by their name.
     *
     * @param name       The name of the entity.
     * @param identifier The resource identifier.
     * @param encoding   The auto-detected IANA encoding name of the entity
     *                   stream. This value will be null in those situations
     *                   where the entity encoding is not auto-detected (e.g.
     *                   internal entities or a document entity that is
     *                   parsed from a java.io.Reader).
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void startEntity(String name,
            XMLResourceIdentifier identifier,
            String encoding, Augmentations augs) throws XNIException {

        // keep track of this entity before fEntityDepth is increased
        if (fEntityDepth == fEntityStack.length) {
            int[] entityarray = new int[fEntityStack.length * 2];
            System.arraycopy(fEntityStack, 0, entityarray, 0, fEntityStack.length);
            fEntityStack = entityarray;
        }
        fEntityStack[fEntityDepth] = fMarkupDepth;

        super.startEntity(name, identifier, encoding, augs);

        // WFC: entity declared in external subset in standalone doc
        if (fStandalone && fEntityManager.isEntityDeclInExternalSubset(name)) {
            reportFatalError("MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE",
                    new Object[] { name });
        }

        // call handler
        if (fDocumentHandler != null && !fScanningAttribute) {
            if (!name.equals("[xml]")) {
                fDocumentHandler.startGeneralEntity(name, identifier, encoding, augs);
            }
        }

        // prepare to look for a TextDecl if external general entity
        if (!name.equals("[xml]") && fEntityScanner.isExternal()) {
            setScannerState(ScanState.TEXT_DECL);
        }

        // call handler
        if (fDocumentHandler != null && name.equals("[xml]")) {
            fDocumentHandler.startDocument(fEntityScanner, encoding, fNamespaceContext, null);
        }

    } // startEntity(String,identifier,String)

    /**
     * This method notifies the end of an entity. The DTD has the pseudo-name
     * of "[dtd]" parameter entity names start with '%'; and general entities
     * are just specified by their name.
     *
     * @param name The name of the entity.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    @Override
    public void endEntity(String name, Augmentations augs) throws XNIException {

        // flush possible pending output buffer - see scanContent
        if (fInScanContent && fStringBuffer.length != 0
                && fDocumentHandler != null) {
            fDocumentHandler.characters(fStringBuffer, null);
            fStringBuffer.length = 0; // make sure we know it's been flushed
        }

        super.endEntity(name, augs);

        // make sure markup is properly balanced
        if (fMarkupDepth != fEntityStack[fEntityDepth]) {
            reportFatalError("MarkupEntityMismatch", null);
        }

        // call handler
        if (fDocumentHandler != null && !fScanningAttribute) {
            if (!name.equals("[xml]")) {
                fDocumentHandler.endGeneralEntity(name, augs);
            }
        }

        // call handler
        if (fDocumentHandler != null && name.equals("[xml]")) {
            fDocumentHandler.endDocument(null);
        }

    } // endEntity(String)

    //
    // Protected methods
    //

    // dispatcher factory methods

    /** Creates a content dispatcher. */
    protected Dispatcher createContentDispatcher() {
        // return new FragmentContentDispatcher();
        return new ContentDispatcher();
    } // createContentDispatcher():Dispatcher

    // scanning methods

    /** Scans a doctype declaration.
     *  @return true:Internal DTD記述が存在する場合
     */
    protected boolean scanDoctypeDecl() throws IOException, XNIException {

        // spaces
        if (!fEntityScanner.skipSpaces()) {
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ROOT_ELEMENT_TYPE_IN_DOCTYPEDECL",
                    null);
        }

        // root element name
        fDoctypeName = fEntityScanner.scanName();
        if (fDoctypeName == null) {
            reportFatalError("MSG_ROOT_ELEMENT_TYPE_REQUIRED", null);
        }

        // external id
        if (fEntityScanner.skipSpaces()) {
            scanExternalID(fStrings, true);
            fDoctypeSystemId = fStrings[0];
            fDoctypePublicId = fStrings[1];
            fEntityScanner.skipSpaces();
        }

        fHasExternalDTD = fDoctypeSystemId != null;

        // Attempt to locate an external subset with an external subset resolver.
        if (!fHasExternalDTD && fExternalSubsetResolver != null) {
            fDTDDescription.setValues(null, null, fEntityManager.getCurrentResourceIdentifier().getExpandedSystemId(),
                    null);
            fDTDDescription.setRootName(fDoctypeName);
            fExternalSubsetSource = fExternalSubsetResolver.getExternalSubset(fDTDDescription);
            fHasExternalDTD = fExternalSubsetSource != null;
        }

        // call handler
        if (fDocumentHandler != null) {
            if (fExternalSubsetSource == null) {
                fDocumentHandler.doctypeDecl(fDoctypeName, fDoctypePublicId, fDoctypeSystemId, null);
            } else {
                fDocumentHandler.doctypeDecl(fDoctypeName,
                    fExternalSubsetSource.getPublicId(),
                    fExternalSubsetSource.getSystemId(),
                    null);
            }
        }

        // is there an internal subset?
        boolean internalSubset = true;
        if (!fEntityScanner.skipChar('[')) {
            internalSubset = false;
            fEntityScanner.skipSpaces();
            if (!fEntityScanner.skipChar('>')) {
                reportFatalError("DoctypedeclUnterminated", new Object[] { fDoctypeName });
            }
            fMarkupDepth--;
        }

        return internalSubset;

    } // scanDoctypeDecl():boolean

    protected void skipSpaces() throws IOException {
        fStringBuffer.clear();

        do {
            int c = fEntityScanner.peekChar();
            if (c == '<') {
                XMLString contents = fStringBuffer;
                if (fDocumentHandler != null) {
                    fDocumentHandler.characters(contents, null);
                }
                return;
            } else if (c == -1) {
                return;
            } else {
                c = fEntityScanner.scanChar();
                fStringBuffer.append((char) c);
            }
        } while (true);
    }

    //
    // Private methods
    //

    /** Returns the scanner state name. */
    protected String getScannerStateName(ScanState state) {

        /** from XMLDocumentScanner */
        switch (state) {
            case XML_DECL:
                return "ScanState.XML_DECL";
            case PROLOG:
                return "ScanState.PROLOG";
            case TRAILING_MISC:
                return "ScanState.TRAILING_MISC";
            case DTD_INTERNAL_DECLS:
                return "ScanState.DTD_INTERNAL_DECLS";
            case DTD_EXTERNAL:
                return "ScanState.DTD_EXTERNAL";
            case DTD_EXTERNAL_DECLS:
                return "ScanState.DTD_EXTERNAL_DECLS";
            /** from XMLFragmentScanner */
            case DOCTYPE:
                return "ScanState.DOCTYPE";
            case ROOT_ELEMENT:
                return "ScanState.ROOT_ELEMENT";
            case START_OF_MARKUP:
                return "ScanState.START_OF_MARKUP";
            case COMMENT:
                return "ScanState.COMMENT";
            case PI:
                return "ScanState.PI";
            case CONTENT:
                return "ScanState.CONTENT";
            case REFERENCE:
                return "ScanState.REFERENCE";
            case END_OF_INPUT:
                return "ScanState.END_OF_INPUT";
            case TERMINATED:
                return "ScanState.TERMINATED";
            case CDATA:
                return "ScanState.CDATA";
            case TEXT_DECL:
                return "ScanState.TEXT_DECL";
            case SCRIPT:
                return "ScanState.SCRIPT";
        }

        return "??? (" + state + ')';

    } // getScannerStateName(int):String

    //
    // Classes
    //
    /**
     * This interface defines an XML "event" dispatching model. Classes
     * that implement this interface are responsible for scanning parts
     * of the XML document and dispatching callbacks.
     *
     * @author Glenn Marcy, IBM
     */
    protected interface Dispatcher {

        //
        // Dispatcher methods
        //

        /**
         * Dispatch an XML "event".
         *
         * @param complete True if this dispatcher is intended to scan
         *                 and dispatch as much as possible.
         *
         * @return True if there is more to dispatch either from this
         *         or a another dispatcher.
         *
         * @throws IOException  Thrown on i/o error.
         * @throws XNIException Thrown on parse error.
         */
        public boolean dispatch(boolean complete)
                throws IOException, XNIException;

    } // interface Dispatcher

    /**
     * Dispatcher to handle content scanning.
     *
     * @author Andy Clark, IBM
     * @author Eric Ye, IBM
     */
    protected class FragmentContentDispatcher
            implements Dispatcher {

        public boolean dispatch(boolean complete)
                throws IOException, XNIException {
            try {
                boolean again;
                do {
                    again = false;
                    switch (fScannerState) {
                        case CONTENT: {
                            if (fEntityScanner.skipChar('<')) {
                                setScannerState(ScanState.START_OF_MARKUP);
                                again = true;
                            } else if (fEntityScanner.skipChar('&')) {
                                setScannerState(ScanState.REFERENCE);
                                again = true;
                            } else {
                                do {
                                    int c = scanContent();
                                    if (c == '<') {
                                        fEntityScanner.scanChar();
                                        setScannerState(ScanState.START_OF_MARKUP);
                                        break;
                                    } else if (c == '&') {
                                        fEntityScanner.scanChar();
                                        setScannerState(ScanState.REFERENCE);
                                        break;
                                    } else if (c != -1 && isInvalidLiteral(c)) {
                                        if (XMLChar.isHighSurrogate(c)) {
                                            // special case: surrogates
                                            fStringBuffer.clear();
                                            if (scanSurrogates(fStringBuffer)) {
                                                // call handler
                                                if (fDocumentHandler != null) {
                                                    fDocumentHandler.characters(fStringBuffer, null);
                                                }
                                            }
                                        } else {
                                            reportFatalError("InvalidCharInContent",
                                                    new Object[] {
                                                            Integer.toString(c, 16) });
                                            fEntityScanner.scanChar();
                                        }
                                    }
                                } while (complete);
                            }
                            break;
                        }
                        case START_OF_MARKUP: {
                            fMarkupDepth++;
                            if (fEntityScanner.skipChar('/')) {
                                if (scanEndElement() == 0) {
                                    if (elementDepthIsZeroHook()) {
                                        return true;
                                    }
                                }
                                setScannerState(ScanState.CONTENT);
                            } else if (isValidNameStartChar(fEntityScanner.peekChar())) {
                                scanStartElement();
                                if ("script".equalsIgnoreCase(fElementQName.localpart)) {
                                    setScannerState(ScanState.SCRIPT);
                                } else {
                                    setScannerState(ScanState.CONTENT);
                                }
                            } else if (fEntityScanner.skipChar('!')) {
                                if (fEntityScanner.skipChar('-')) {
                                    if (!fEntityScanner.skipChar('-')) {
                                        reportFatalError("InvalidCommentStart", null);
                                    }
                                    setScannerState(ScanState.COMMENT);
                                    again = true;
                                } else if (fEntityScanner.skipString("[CDATA[")) {
                                    setScannerState(ScanState.CDATA);
                                    again = true;
                                } else if (!scanForDoctypeHook()) {
                                    reportFatalError("MarkupNotRecognizedInContent", null);
                                }
                            } else if (fEntityScanner.skipChar('?')) {
                                setScannerState(ScanState.PI);
                                again = true;
                            } else if (isValidNameStartHighSurrogate(fEntityScanner.peekChar())) {
                                scanStartElement();
                                setScannerState(ScanState.CONTENT);
                            } else {
                                reportFatalError("MarkupNotRecognizedInContent", null);
                                setScannerState(ScanState.CONTENT);
                            }
                            break;
                        }
                        case SCRIPT: {
                            scanScript();
                            setScannerState(ScanState.CONTENT);
                            break;
                        }
                        case COMMENT: {
                            scanComment();
                            setScannerState(ScanState.CONTENT);
                            break;
                        }
                        case PI: {
                            scanPI();
                            setScannerState(ScanState.CONTENT);
                            break;
                        }
                        case CDATA: {
                            scanCDATASection(complete);
                            setScannerState(ScanState.CONTENT);
                            break;
                        }
                        case REFERENCE: {
                            fMarkupDepth++;
                            // NOTE: We need to set the state beforehand
                            // because the XMLEntityHandler#startEntity
                            // callback could set the state to
                            // ScanState.TEXT_DECL and we don't want
                            // to override that scanner state.
                            setScannerState(ScanState.CONTENT);
                            if (fEntityScanner.skipChar('#')) {
                                scanCharReference();
                            } else {
                                scanEntityReference();
                            }
                            break;
                        }
                        case TEXT_DECL: {
                            // scan text decl
                            if (fEntityScanner.skipString("<?xml")) {
                                fMarkupDepth++;
                                // NOTE: special case where entity starts with a PI
                                // whose name starts with "xml" (e.g. "xmlfoo")
                                if (isValidNameChar(fEntityScanner.peekChar())) {
                                    fStringBuffer.clear();
                                    fStringBuffer.append("xml");
                                    while (isValidNameChar(fEntityScanner.peekChar())) {
                                        fStringBuffer.append((char) fEntityScanner.scanChar());
                                    }
                                    String target = fSymbolTable.addSymbol(fStringBuffer.ch, fStringBuffer.offset,
                                            fStringBuffer.length);
                                    scanPIData(target, fTempString);
                                }

                                // standard text declaration
                                else {
                                    scanXMLDeclOrTextDecl(true);
                                }
                            }
                            // now that we've straightened out the readers, we can read in chunks:
                            fEntityManager.getCurrentEntity().mayReadChunks = true;
                            setScannerState(ScanState.CONTENT);
                            break;
                        }
                        case ROOT_ELEMENT: {
                            if (scanRootElementHook()) {
                                return true;
                            }
                            setScannerState(ScanState.CONTENT);
                            break;
                        }
                        case DOCTYPE: {
                            reportFatalError("DoctypeIllegalInContent",
                                    null);
                            setScannerState(ScanState.CONTENT);
                        }
                        default:
                            break;
                    }
                } while (complete || again);
            }
            // encoding errors
            catch (MalformedByteSequenceException e) {
                fErrorReporter.reportError(e.getDomain(), e.getKey(),
                        e.getArguments(), XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            } catch (CharConversionException e) {
                fErrorReporter.reportError(
                        XMLMessageFormatter.XML_DOMAIN,
                        "CharConversionFailure",
                        null,
                        XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            // premature end of file
            catch (EOFException e) {
                endOfFileHook(e);
                return false;
            }

            return true;

        } // dispatch(boolean):boolean

        //
        // Protected methods
        //

        // hooks

        // NOTE: These hook methods are added so that the full document
        // scanner can share the majority of code with this class.

        /**
         * Scan for DOCTYPE hook. This method is a hook for subclasses
         * to add code to handle scanning for a the "DOCTYPE" string
         * after the string "&lt;!" has been scanned.
         * 
         * @return True if the "DOCTYPE" was scanned; false if "DOCTYPE"
         *         was not scanned.
         */
        protected boolean scanForDoctypeHook()
                throws IOException, XNIException {
            return false;
        } // scanForDoctypeHook():boolean

        /**
         * Element depth iz zero. This methos is a hook for subclasses
         * to add code to handle when the element depth hits zero. When
         * scanning a document fragment, an element depth of zero is
         * normal. However, when scanning a full XML document, the
         * scanner must handle the trailing miscellanous section of
         * the document after the end of the document's root element.
         *
         * @return True if the caller should stop and return true which
         *         allows the scanner to switch to a new scanning
         *         dispatcher. A return value of false indicates that
         *         the content dispatcher should continue as normal.
         */
        protected boolean elementDepthIsZeroHook()
                throws IOException, XNIException {
            return false;
        } // elementDepthIsZeroHook():boolean

        /**
         * Scan for root element hook. This method is a hook for
         * subclasses to add code that handles scanning for the root
         * element. When scanning a document fragment, there is no
         * "root" element. However, when scanning a full XML document,
         * the scanner must handle the root element specially.
         *
         * @return True if the caller should stop and return true which
         *         allows the scanner to switch to a new scanning
         *         dispatcher. A return value of false indicates that
         *         the content dispatcher should continue as normal.
         */
        protected boolean scanRootElementHook()
                throws IOException, XNIException {
            return false;
        } // scanRootElementHook():boolean

        /**
         * End of file hook. This method is a hook for subclasses to
         * add code that handles the end of file. The end of file in
         * a document fragment is OK if the markup depth is zero.
         * However, when scanning a full XML document, an end of file
         * is always premature.
         */
        protected void endOfFileHook(EOFException e)
                throws IOException, XNIException {

            // NOTE: An end of file is only only an error if we were
            // in the middle of scanning some markup. -Ac
            if (fMarkupDepth != 0) {
                reportFatalError("PrematureEOF", null);
            }

        } // endOfFileHook()

    } // class FragmentContentDispatcher

    /**
     * Dispatcher to handle content scanning.
     *
     * @author Andy Clark, IBM
     * @author Eric Ye, IBM
     */
    protected class ContentDispatcher
            extends FragmentContentDispatcher {

        //
        // Protected methods
        //

        // hooks

        // NOTE: These hook methods are added so that the full document
        // scanner can share the majority of code with this class.

        /**
         * Scan for DOCTYPE hook. This method is a hook for subclasses
         * to add code to handle scanning for a the "DOCTYPE" string
         * after the string "&lt;!" has been scanned.
         *
         * @return True if the "DOCTYPE" was scanned; false if "DOCTYPE"
         *         was not scanned.
         */
        protected boolean scanForDoctypeHook()
                throws IOException, XNIException {

            if (fEntityScanner.skipString("DOCTYPE")) {
                setScannerState(ScanState.DOCTYPE);
                return true;
            }
            return false;

        } // scanForDoctypeHook():boolean

        /**
         * Element depth iz zero. This methos is a hook for subclasses
         * to add code to handle when the element depth hits zero. When
         * scanning a document fragment, an element depth of zero is
         * normal. However, when scanning a full XML document, the
         * scanner must handle the trailing miscellanous section of
         * the document after the end of the document's root element.
         *
         * @return True if the caller should stop and return true which
         *         allows the scanner to switch to a new scanning
         *         dispatcher. A return value of false indicates that
         *         the content dispatcher should continue as normal.
         */
        protected boolean elementDepthIsZeroHook()
                throws IOException, XNIException {

            setScannerState(ScanState.TRAILING_MISC);
            setDispatcher(fTrailingMiscDispatcher);
            return true;

        } // elementDepthIsZeroHook():boolean

        /**
         * Scan for root element hook. This method is a hook for
         * subclasses to add code that handles scanning for the root
         * element. When scanning a document fragment, there is no
         * "root" element. However, when scanning a full XML document,
         * the scanner must handle the root element specially.
         *
         * @return True if the caller should stop and return true which
         *         allows the scanner to switch to a new scanning
         *         dispatcher. A return value of false indicates that
         *         the content dispatcher should continue as normal.
         */
        protected boolean scanRootElementHook()
                throws IOException, XNIException {

            if (fExternalSubsetResolver != null && !isSeenDoctypeDecl() && fValidation) {
                scanStartElementName();
                resolveExternalSubsetAndRead();
                if (scanStartElementAfterName()) {
                    setScannerState(ScanState.TRAILING_MISC);
                    setDispatcher(fTrailingMiscDispatcher);
                    return true;
                }
            } else if (scanStartElement()) {
                setScannerState(ScanState.TRAILING_MISC);
                setDispatcher(fTrailingMiscDispatcher);
                return true;
            }
            return false;

        } // scanRootElementHook():boolean

        /**
         * End of file hook. This method is a hook for subclasses to
         * add code that handles the end of file. The end of file in
         * a document fragment is OK if the markup depth is zero.
         * However, when scanning a full XML document, an end of file
         * is always premature.
         */
        protected void endOfFileHook(EOFException e)
                throws IOException, XNIException {

            reportFatalError("PrematureEOF", null);
            // in case continue-after-fatal-error set, should not do this...
            // throw e;

        } // endOfFileHook()

        /**
         * <p>
         * Attempt to locate an external subset for a document that does not otherwise
         * have one. If an external subset is located, then it is scanned.
         * </p>
         */
        protected void resolveExternalSubsetAndRead()
                throws IOException, XNIException {

            fDTDDescription.setValues(null, null, fEntityManager.getCurrentResourceIdentifier().getExpandedSystemId(),
                    null);
            fDTDDescription.setRootName(fElementQName.rawname);
            XMLInputSource src = fExternalSubsetResolver.getExternalSubset(fDTDDescription);

            if (src != null) {
                fDoctypeName = fElementQName.rawname;
                fDoctypePublicId = src.getPublicId();
                fDoctypeSystemId = src.getSystemId();
                // call document handler
                if (fDocumentHandler != null) {
                    // This inserts a doctypeDecl event into the stream though no
                    // DOCTYPE existed in the instance document.
                    fDocumentHandler.doctypeDecl(fDoctypeName, fDoctypePublicId, fDoctypeSystemId, null);
                }
                try {
                    // This sends startDTD and endDTD calls down the pipeline.
                    fDTDScanner.setInputSource(null);
                } finally {
                    fEntityManager.setEntityHandler(HTMLScanner.this);
                }
            }
        } // resolveExternalSubsetAndRead()

    } // class ContentDispatcher

    /**
     * Dispatcher to handle XMLDecl scanning.
     *
     * @author Andy Clark, IBM
     */
    protected final class XMLDeclDispatcher
            implements Dispatcher {

        public boolean dispatch(boolean complete)
                throws IOException, XNIException {

            // next dispatcher is prolog regardless of whether there
            // is an XMLDecl in this document
            setScannerState(ScanState.PROLOG);
            setDispatcher(fPrologDispatcher);

            // scan XMLDecl
            try {
                if (fEntityScanner.skipString("<?xml")) {
                    fMarkupDepth++;
                    // NOTE: special case where document starts with a PI
                    // whose name starts with "xml" (e.g. "xmlfoo")
                    if (XMLChar.isName(fEntityScanner.peekChar())) {
                        fStringBuffer.clear();
                        fStringBuffer.append("xml");
                        while (XMLChar.isName(fEntityScanner.peekChar())) {
                            fStringBuffer.append((char) fEntityScanner.scanChar());
                        }
                        String target = fSymbolTable.addSymbol(fStringBuffer.ch, fStringBuffer.offset,
                                fStringBuffer.length);
                        scanPIData(target, fString);
                    }

                    // standard XML declaration
                    else {
                        scanXMLDeclOrTextDecl(false);
                    }
                }
                fEntityManager.getCurrentEntity().mayReadChunks = true;

                // if no XMLDecl, then scan piece of prolog
                return true;
            }
            // encoding errors
            catch (MalformedByteSequenceException e) {
                fErrorReporter.reportError(e.getDomain(), e.getKey(),
                        e.getArguments(), XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            } catch (CharConversionException e) {
                fErrorReporter.reportError(
                        XMLMessageFormatter.XML_DOMAIN,
                        "CharConversionFailure",
                        null,
                        XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            // premature end of file
            catch (EOFException e) {
                reportFatalError("PrematureEOF", null);
                return false;
                // throw e;
            }

        } // dispatch(boolean):boolean

    } // class XMLDeclDispatcher

    /**
     * Dispatcher to handle prolog scanning.
     *
     * @author Andy Clark, IBM
     */
    protected final class PrologDispatcher
            implements Dispatcher {

        public boolean dispatch(boolean complete)
                throws IOException, XNIException {

            try {
                boolean again;
                do {
                    again = false;
                    switch (fScannerState) {
                        case PROLOG: {
                            skipSpaces();
                            if (fEntityScanner.skipChar('<')) {
                                setScannerState(ScanState.START_OF_MARKUP);
                                again = true;
                            } else if (fEntityScanner.skipChar('&')) {
                                setScannerState(ScanState.REFERENCE);
                                again = true;
                            } else {
                                setScannerState(ScanState.CONTENT);
                                again = true;
                            }
                            break;
                        }
                        case START_OF_MARKUP: {
                            fMarkupDepth++;
                            if (fEntityScanner.skipChar('!')) {
                                if (fEntityScanner.skipChar('-')) {
                                    if (!fEntityScanner.skipChar('-')) {
                                        reportFatalError("InvalidCommentStart",
                                                null);
                                    }
                                    setScannerState(ScanState.COMMENT);
                                    again = true;
                                } else if (fEntityScanner.skipString("DOCTYPE")) {
                                    setScannerState(ScanState.DOCTYPE);
                                    again = true;
                                } else {
                                    reportFatalError("MarkupNotRecognizedInProlog",
                                            null);
                                }
                            } else if (isValidNameStartChar(fEntityScanner.peekChar())) {
                                setScannerState(ScanState.ROOT_ELEMENT);
                                setDispatcher(fContentDispatcher);
                                return true;
                            } else if (fEntityScanner.skipChar('?')) {
                                setScannerState(ScanState.PI);
                                again = true;
                            } else if (isValidNameStartHighSurrogate(fEntityScanner.peekChar())) {
                                setScannerState(ScanState.ROOT_ELEMENT);
                                setDispatcher(fContentDispatcher);
                                return true;
                            } else {
                                reportFatalError("MarkupNotRecognizedInProlog",
                                        null);
                            }
                            break;
                        }
                        case COMMENT: {
                            scanComment();
                            setScannerState(ScanState.PROLOG);
                            break;
                        }
                        case PI: {
                            scanPI();
                            setScannerState(ScanState.PROLOG);
                            break;
                        }
                        case DOCTYPE: {
                            if (isSeenDoctypeDecl()) {
                                reportFatalError("AlreadySeenDoctype", null);
                            }

                            // scanDoctypeDecl() sends XNI doctypeDecl event that
                            // in SAX is converted to startDTD() event.
                            if (scanDoctypeDecl()) {
                                setScannerState(ScanState.DTD_INTERNAL_DECLS);
                                setDispatcher(fDTDDispatcher);
                                return true;
                            }

                            // handle external subset
                            if (fDoctypeSystemId != null) {
                                fIsEntityDeclaredVC = !fStandalone;
                            } else if (fExternalSubsetSource != null) {
                                fIsEntityDeclaredVC = !fStandalone;
                            }

                            // Send endDTD() call if:
                            // a) systemId is null or if an external subset resolver could not locate an
                            // external subset.
                            // b) "load-external-dtd" and validation are false
                            // c) DTD grammar is cached

                            // in XNI this results in 3 events: doctypeDecl, startDTD, endDTD
                            // in SAX this results in 2 events: startDTD, endDTD
                            fDTDScanner.setInputSource(null);
                            setScannerState(ScanState.PROLOG);
                            break;
                        }
                        case CONTENT: {
                            reportFatalError("ContentIllegalInProlog", null);
                            fEntityScanner.scanChar();
                        }
                        case REFERENCE: {
                            reportFatalError("ReferenceIllegalInProlog", null);
                        }
                        default:
                            break;
                    }
                } while (complete || again);

                if (complete) {
                    if (fEntityScanner.scanChar() != '<') {
                        reportFatalError("RootElementRequired", null);
                    }
                    setScannerState(ScanState.ROOT_ELEMENT);
                    setDispatcher(fContentDispatcher);
                }
            }
            // encoding errors
            catch (MalformedByteSequenceException e) {
                fErrorReporter.reportError(e.getDomain(), e.getKey(),
                        e.getArguments(), XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            } catch (CharConversionException e) {
                fErrorReporter.reportError(
                        XMLMessageFormatter.XML_DOMAIN,
                        "CharConversionFailure",
                        null,
                        XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            // premature end of file
            catch (EOFException e) {
                reportFatalError("PrematureEOF", null);
                return false;
                // throw e;
            }

            return true;

        } // dispatch(boolean):boolean

    } // class PrologDispatcher

    /**
     * Dispatcher to handle the internal and external DTD subsets.
     *
     * @author Andy Clark, IBM
     */
    protected final class DTDDispatcher
            implements Dispatcher {

        public boolean dispatch(boolean complete)
                throws IOException, XNIException {
            fEntityManager.setEntityHandler(null);
            try {
                boolean again;
                do {
                    again = false;
                    switch (fScannerState) {
                        case DTD_INTERNAL_DECLS: {
                            // REVISIT: Should there be a feature for
                            // the "complete" parameter?
                            boolean completeDTD = true;
                            boolean readExternalSubset = false;
                            boolean moreToScan = fDTDScanner.scanDTDInternalSubset(completeDTD, fStandalone,
                                    fHasExternalDTD && readExternalSubset);
                            if (!moreToScan) {
                                // end doctype declaration
                                if (!fEntityScanner.skipChar(']')) {
                                    reportFatalError("EXPECTED_SQUARE_BRACKET_TO_CLOSE_INTERNAL_SUBSET",
                                            null);
                                }
                                fEntityScanner.skipSpaces();
                                if (!fEntityScanner.skipChar('>')) {
                                    reportFatalError("DoctypedeclUnterminated", new Object[] { fDoctypeName });
                                }
                                fMarkupDepth--;

                                // scan external subset next
                                if (fDoctypeSystemId != null) {
                                    fIsEntityDeclaredVC = !fStandalone;
                                    if (readExternalSubset) {
                                        setScannerState(ScanState.DTD_EXTERNAL);
                                        break;
                                    }
                                } else if (fExternalSubsetSource != null) {
                                    fIsEntityDeclaredVC = !fStandalone;
                                    if (readExternalSubset) {
                                        // This handles the case of a DOCTYPE that only had an internal subset.
                                        fDTDScanner.setInputSource(fExternalSubsetSource);
                                        fExternalSubsetSource = null;
                                        setScannerState(ScanState.DTD_EXTERNAL_DECLS);
                                        break;
                                    }
                                }
                                // This document only has an internal subset. If it contains parameter entity
                                // references and standalone="no" then [Entity Declared] is a validity
                                // constraint.
                                else {
                                    fIsEntityDeclaredVC = /* fEntityManager.hasPEReferences() && */!fStandalone;
                                }

                                // break out of this dispatcher.
                                setScannerState(ScanState.PROLOG);
                                setDispatcher(fPrologDispatcher);
                                fEntityManager.setEntityHandler(HTMLScanner.this);
                                return true;
                            }
                            break;
                        }
                        case DTD_EXTERNAL: {
                            fDTDDescription.setValues(fDoctypePublicId, fDoctypeSystemId, null, null);
                            fDTDDescription.setRootName(fDoctypeName);
                            XMLInputSource xmlInputSource = fEntityManager.resolveEntity(fDTDDescription);
                            fDTDScanner.setInputSource(xmlInputSource);
                            setScannerState(ScanState.DTD_EXTERNAL_DECLS);
                            again = true;
                            break;
                        }
                        case DTD_EXTERNAL_DECLS: {
                            // REVISIT: Should there be a feature for
                            // the "complete" parameter?
                            boolean completeDTD = true;
                            boolean moreToScan = fDTDScanner.scanDTDExternalSubset(completeDTD);
                            if (!moreToScan) {
                                setScannerState(ScanState.PROLOG);
                                setDispatcher(fPrologDispatcher);
                                fEntityManager.setEntityHandler(HTMLScanner.this);
                                return true;
                            }
                            break;
                        }
                        default: {
                            throw new XNIException("DTDDispatcher#dispatch: scanner state=" + fScannerState + " ("
                                    + getScannerStateName(fScannerState) + ')');
                        }
                    }
                } while (complete || again);
            }
            // encoding errors
            catch (MalformedByteSequenceException e) {
                fErrorReporter.reportError(e.getDomain(), e.getKey(),
                        e.getArguments(), XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            } catch (CharConversionException e) {
                fErrorReporter.reportError(
                        XMLMessageFormatter.XML_DOMAIN,
                        "CharConversionFailure",
                        null,
                        XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            // premature end of file
            catch (EOFException e) {
                reportFatalError("PrematureEOF", null);
                return false;
                // throw e;
            }

            // cleanup
            finally {
                fEntityManager.setEntityHandler(HTMLScanner.this);
            }

            return true;

        } // dispatch(boolean):boolean

    } // class DTDDispatcher

    /**
     * Dispatcher to handle trailing miscellaneous section scanning.
     *
     * @author Mitsutaka WATANABE
     */
    protected final class TrailingMiscDispatcher
            implements Dispatcher {

        //
        // Dispatcher methods
        //

        /**
         * Dispatch an XML "event".
         *
         * @param complete True if this dispatcher is intended to scan
         *                 and dispatch as much as possible.
         *
         * @return True if there is more to dispatch either from this
         *         or a another dispatcher.
         *
         * @throws IOException  Thrown on i/o error.
         * @throws XNIException Thrown on parse error.
         */
        public boolean dispatch(boolean complete)
                throws IOException, XNIException {

            try {
                boolean again;
                do {
                    again = false;
                    switch (fScannerState) {
                        case TRAILING_MISC: {
                            fEntityScanner.skipSpaces();
                            if (fEntityScanner.skipChar('<')) {
                                setScannerState(ScanState.START_OF_MARKUP);
                                again = true;
                            } else {
                                setScannerState(ScanState.CONTENT);
                                again = true;
                            }
                            break;
                        }
                        case START_OF_MARKUP: {
                            fMarkupDepth++;
                            if (fEntityScanner.skipChar('?')) {
                                setScannerState(ScanState.PI);
                                again = true;
                            } else if (fEntityScanner.skipChar('!')) {
                                setScannerState(ScanState.COMMENT);
                                again = true;
                            } else if (fEntityScanner.skipChar('/')) {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                        null);
                                again = true;
                            } else if (isValidNameStartChar(fEntityScanner.peekChar())) {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                        null);
                                scanStartElement();
                                setScannerState(ScanState.CONTENT);
                            } else if (isValidNameStartHighSurrogate(fEntityScanner.peekChar())) {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                        null);
                                scanStartElement();
                                setScannerState(ScanState.CONTENT);
                            } else {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                        null);
                            }
                            break;
                        }
                        case PI: {
                            scanPI();
                            setScannerState(ScanState.TRAILING_MISC);
                            break;
                        }
                        case COMMENT: {
                            if (!fEntityScanner.skipString("--")) {
                                reportFatalError("InvalidCommentStart", null);
                            }
                            scanComment();
                            setScannerState(ScanState.TRAILING_MISC);
                            break;
                        }
                        case CONTENT: {
                            int ch = fEntityScanner.peekChar();
                            if (ch == -1) {
                                setScannerState(ScanState.TERMINATED);
                                return false;
                            }
                            reportFatalError("ContentIllegalInTrailingMisc",
                                    null);
                            fEntityScanner.scanChar();
                            setScannerState(ScanState.TRAILING_MISC);
                            break;
                        }
                        case REFERENCE: {
                            reportFatalError("ReferenceIllegalInTrailingMisc",
                                    null);
                            setScannerState(ScanState.TRAILING_MISC);
                            break;
                        }
                        case TERMINATED: {
                            return false;
                        }
                        default:
                            break;
                    }
                } while (complete || again);
            }
            // encoding errors
            catch (MalformedByteSequenceException e) {
                fErrorReporter.reportError(e.getDomain(), e.getKey(),
                        e.getArguments(), XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            } catch (CharConversionException e) {
                fErrorReporter.reportError(
                        XMLMessageFormatter.XML_DOMAIN,
                        "CharConversionFailure",
                        null,
                        XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            } catch (EOFException e) {
                // NOTE: This is the only place we're allowed to reach
                // the real end of the document stream. Unless the
                // end of file was reached prematurely.
                if (fMarkupDepth != 0) {
                    reportFatalError("PrematureEOF", null);
                    return false;
                    // throw e;
                }

                setScannerState(ScanState.TERMINATED);
                return false;
            }

            return true;

        } // dispatch(boolean):boolean

    } // class TrailingMiscDispatcher

}


/**
 * Element stack. This stack operates without synchronization, error
 * checking, and it re-uses objects instead of throwing popped items
 * away.
 *
 * @author Andy Clark, IBM
 */
class ElementStack {

    //
    // Data
    //

    /** The stack data. */
    protected QName[] fElements;

    /** The size of the stack. */
    protected int fSize;

    //
    // Constructors
    //

    /** Default constructor. */
    public ElementStack() {
        fElements = new QName[10];
        for (int i = 0; i < fElements.length; i++) {
            fElements[i] = new QName();
        }
    } // <init>()

    //
    // Public methods
    //

    /**
     * Pushes an element on the stack.
     * <p>
     * <strong>Note:</strong> The QName values are copied into the
     * stack. In other words, the caller does <em>not</em> orphan
     * the element to the stack. Also, the QName object returned
     * is <em>not</em> orphaned to the caller. It should be
     * considered read-only.
     *
     * @param element The element to push onto the stack.
     *
     * @return Returns the actual QName object that stores the
     */
    public QName pushElement(QName element) {
        if (fSize == fElements.length) {
            QName[] array = new QName[fElements.length * 2];
            System.arraycopy(fElements, 0, array, 0, fSize);
            fElements = array;
            for (int i = fSize; i < fElements.length; i++) {
                fElements[i] = new QName();
            }
        }
        fElements[fSize].setValues(element);
        return fElements[fSize++];
    } // pushElement(QName):QName

    /**
     * Pops an element off of the stack by setting the values of
     * the specified QName.
     * <p>
     * <strong>Note:</strong> The object returned is <em>not</em>
     * orphaned to the caller. Therefore, the caller should consider
     * the object to be read-only.
     */
    public void popElement(QName element) {
        element.setValues(fElements[--fSize]);
    } // popElement(QName)

    /** Clears the stack without throwing away existing QName objects. */
    public void clear() {
        fSize = 0;
    } // clear()

} // class ElementStack
