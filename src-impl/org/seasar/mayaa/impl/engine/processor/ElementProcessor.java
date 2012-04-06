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
package org.seasar.mayaa.impl.engine.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.cyberneko.html.HTMLElements;
import org.seasar.mayaa.builder.SequenceIDGenerator;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.builder.BuilderUtil;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.DefaultCycleLocalInstantiator;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ElementProcessor extends AbstractAttributableProcessor
        implements CONST_IMPL {

    private static final long serialVersionUID = -1041576023468766303L;
    private static final String SUFFIX_DUPLICATED = "_d";
    private static final Set XHTML_EMPTY_ELEMENTS;
    private static final String RENDERED_NS_STACK_KEY =
            ElementProcessor.class.getName() + "#renderedNSStack";
    private static final String CURRENT_NS_KEY =
            ElementProcessor.class.getName() + "#currentNS";
    static {
        XHTML_EMPTY_ELEMENTS = new HashSet();
        XHTML_EMPTY_ELEMENTS.add("base");
        XHTML_EMPTY_ELEMENTS.add("meta");
        XHTML_EMPTY_ELEMENTS.add("link");
        XHTML_EMPTY_ELEMENTS.add("hr");
        XHTML_EMPTY_ELEMENTS.add("br");
        XHTML_EMPTY_ELEMENTS.add("param");
        XHTML_EMPTY_ELEMENTS.add("img");
        XHTML_EMPTY_ELEMENTS.add("area");
        XHTML_EMPTY_ELEMENTS.add("input");
        XHTML_EMPTY_ELEMENTS.add("col");
        // transitional
        XHTML_EMPTY_ELEMENTS.add("basefont");
        XHTML_EMPTY_ELEMENTS.add("isindex");
        // nonstandard
        XHTML_EMPTY_ELEMENTS.add("frame");
        XHTML_EMPTY_ELEMENTS.add("wbr");
        XHTML_EMPTY_ELEMENTS.add("bgsound");
        XHTML_EMPTY_ELEMENTS.add("nextid");
        XHTML_EMPTY_ELEMENTS.add("sound");
        XHTML_EMPTY_ELEMENTS.add("spacer");

        CycleUtil.registVariableFactory(RENDERED_NS_STACK_KEY,
                new DefaultCycleLocalInstantiator() {
            public Object create(Object[] params) {
                return new Stack();
            }});
        CycleUtil.registVariableFactory(CURRENT_NS_KEY,
                new DefaultCycleLocalInstantiator() {
            public Object create(Object[] params) {
                SpecificationNode originalNode = (SpecificationNode) params[0];
                Namespace currentNS = SpecificationUtil.createNamespace();
                currentNS.setParentSpace(originalNode.getParentSpace());
                for (Iterator it = originalNode.iteratePrefixMapping(false); it.hasNext();) {
                    PrefixMapping prefixMapping = (PrefixMapping) it.next();
                    currentNS.addPrefixMapping(prefixMapping.getPrefix(), prefixMapping.getNamespaceURI());
                }
                currentNS.setDefaultNamespaceURI(originalNode.getDefaultNamespaceURI());
                return currentNS;
            }});
    }

    private PrefixAwareName _name;
    private boolean _duplicated;

    protected void clearCurrentNS() {
        CycleUtil.clearGlobalVariable(CURRENT_NS_KEY);
    }

    protected Namespace getCurrentNS() {
        return (Namespace) CycleUtil.getGlobalVariable(CURRENT_NS_KEY,
                new Object[] { getOriginalNode() });
    }

    public void notifyBeginRender(Page topLevelPage) {
        CycleUtil.clearGlobalVariable(RENDERED_NS_STACK_KEY);
    }

    protected Stack getRenderedNS() {
        return (Stack) CycleUtil.getGlobalVariable(RENDERED_NS_STACK_KEY, null);
    }

    protected void addRendered(PrefixMapping mapping) {
        ((List) getRenderedNS().peek()).add(mapping);
    }

    protected String alreadyRenderedPrefix(URI namespaceURI) {
        Iterator iteratorRendered = getRenderedNS().iterator();
        while (iteratorRendered.hasNext()) {
            Iterator mappings = ((List) iteratorRendered.next()).iterator();
            while (mappings.hasNext()) {
                PrefixMapping mapping = (PrefixMapping) mappings.next();
                if (mapping.getNamespaceURI().equals(namespaceURI)) {
                    return mapping.getPrefix();
                }
            }
        }
        return null;
    }

    // MLD property
    public void setDuplicated(boolean duplicated) {
        _duplicated = duplicated;
    }

    // exported property
    public boolean isDuplicated() {
        return _duplicated;
    }

    // MLD property
    public void setName(PrefixAwareName name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        _name = name;
    }

    protected PrefixAwareName getName() {
        if (_name == null) {
            throw new IllegalStateException();
        }
        return _name;
    }

    public String getUniqueID() {
        String uniqueID = super.getUniqueID();
        if (isDuplicated()) {
            uniqueID = uniqueID + SUFFIX_DUPLICATED;
        }
        return uniqueID;
    }

    public Class getExpectedClass() {
        return String.class;
    }

    protected void resolvePrefix(PrefixAwareName name, Namespace currentNS) {
        if (name == null) {
            throw new IllegalArgumentException();
        }

        URI namespaceURI = name.getQName().getNamespaceURI();
        PrefixMapping mapping =
            currentNS.getMappingFromURI(namespaceURI, true);
        if (mapping != null) {
            return;
        }

        Namespace namespace = getInjectedNode().getParentSpace();
        if (namespace != null) {
            mapping = namespace.getMappingFromURI(namespaceURI, true);
            if (mapping != null) {
                currentNS.addPrefixMapping(
                        mapping.getPrefix(), mapping.getNamespaceURI());
                return;
            }
        }
        currentNS.addPrefixMapping(name.getPrefix(), namespaceURI);
    }

    protected void resolvePrefixAll() {
        Namespace currentNS = getCurrentNS();
        if (CycleUtil.isDraftWriting() == false) {
            currentNS = SpecificationUtil.copyNamespace(currentNS);
        }
        resolvePrefix(getName(), currentNS);
        for (Iterator it = iterateProcesstimeProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            resolvePrefix(prop.getName(), currentNS);
        }
        for (Iterator it = iterateInformalProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            resolvePrefix(prop.getName(), currentNS);
        }
    }

    protected String getResolvedPrefix(PrefixAwareName name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (URI_MAYAA.equals(name.getQName().getNamespaceURI())) {
            return "";
        }
        String prefix = alreadyRenderedPrefix(
                name.getQName().getNamespaceURI());
        if (prefix != null) {
            return prefix;
        }
        Namespace currentNS = getCurrentNS();
        URI namespaceURI = name.getQName().getNamespaceURI();
        PrefixMapping mapping =
            currentNS.getMappingFromURI(namespaceURI, true);
        if (mapping != null) {
            return mapping.getPrefix();
        }
        // new inject prefixMapping
        currentNS.addPrefixMapping("", namespaceURI);
        return "";
    }

    protected boolean appendPrefixMappingString(
            StringBuffer buffer, PrefixMapping mapping) {
        String pre = mapping.getPrefix();
        URI uri = mapping.getNamespaceURI();
        if (URI_MAYAA.equals(uri)) {
            return false;
        }
        if (URI_XML.equals(uri) && pre.equals("xml")) {
            return false;
        }
        if (StringUtil.hasValue(pre)) {
            buffer.append(" xmlns:").append(pre);
        } else {
            buffer.append(" xmlns");
        }
        buffer.append("=\"").append(uri).append('"');
        addRendered(mapping);
        return true;
    }

    protected void appendPrefixMappingStrings(
            StringBuffer buffer, Namespace namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException();
        }
        for (Iterator it = namespace.iteratePrefixMapping(false);
                it.hasNext();) {
            appendPrefixMappingString(buffer, (PrefixMapping) it.next());
        }
    }

    protected void appendAttributeString(
            StringBuffer buffer, PrefixAwareName propName, Object value) {
        QName qName = propName.getQName();
        if (URI_MAYAA.equals(qName.getNamespaceURI())) {
            return;
        }
        if (getInjectedNode().getQName().equals(QM_DUPLECATED)) {
            if (getChildProcessorSize() > 0
                    && getChildProcessor(0) instanceof JspProcessor) {
                JspProcessor processor = (JspProcessor)getChildProcessor(0);
                URI injectNS = processor.getInjectedNode().getQName().getNamespaceURI();
                if (injectNS == qName.getNamespaceURI()) {
                    return;
                }
            }
        }

        String attrPrefix = propName.getPrefix();
        if (StringUtil.hasValue(attrPrefix)) {
            attrPrefix = getResolvedPrefix(propName);
            if (StringUtil.hasValue(attrPrefix)) {
                attrPrefix = attrPrefix + ":";
            }
        }
        StringBuffer temp = new StringBuffer(32);
        temp.append(" ");
        temp.append(attrPrefix);
        temp.append(qName.getLocalName());
        if (value != null) {
            temp.append("=\"");
            if (value instanceof CompiledScript) {
                CompiledScript script = (CompiledScript) value;
                if (CycleUtil.isDraftWriting()) {
                    temp.append(script.getScriptText());
                } else {
                    Object result = script.execute(null);
                    if (result == null) {
                        return;
                    }
                    temp.append(result);
                }
            } else {
                temp.append(value.toString());
            }
            temp.append("\"");
        }
        buffer.append(temp.toString());
    }

    protected boolean needsCloseElement(QName qName) {
        if (isXHTML(qName)
                && XHTML_EMPTY_ELEMENTS.contains(qName.getLocalName())) {
            return false;
        } else if (isHTML(qName)) {
            HTMLElements.Element element =
                HTMLElements.getElement(qName.getLocalName());
            return element.isEmpty() == false;
        }
        return getChildProcessorSize() > 0;
    }

    protected void write(String value) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.getResponse().write(value);
    }

    /**
     * 要素名をbufferに書き出します。
     *
     * @param buffer 書き出す対象
     */
    protected void writeElementName(StringBuffer buffer) {
        QName qName = getName().getQName();
        String prefix;
        prefix = getResolvedPrefix(getName());
        if (StringUtil.hasValue(prefix)) {
            buffer.append(prefix).append(":");
        }
        buffer.append(qName.getLocalName());
    }

    /**
     * "<"と要素名をbufferに書き出します。
     *
     * @param buffer 書き出す対象
     */
    protected void writePart1(StringBuffer buffer) {
        buffer.append("<");
        writeElementName(buffer);
    }

    /**
     * 動的なattributeをbufferに書き出します。
     *
     * @param buffer 書き出す対象
     */
    protected void writePart2(StringBuffer buffer) {
        for (Iterator it = iterateProcesstimeProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            appendAttributeString(buffer, prop.getName(), prop.getValue());
        }
        for (Iterator it = iterateInformalProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            if (hasProcesstimeProperty(prop) == false
                    && prop.getValue().isLiteral() == false) {
                appendAttributeString(buffer, prop.getName(), prop.getValue());
            }
        }
    }

    /**
     * 静的なattributeと">"をbufferに書き出します。
     *
     * @param buffer 書き出す対象
     */
    protected void writePart3(StringBuffer buffer) {
        for (Iterator it = iterateInformalProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            if (hasProcesstimeProperty(prop) == false
                    && prop.getValue().isLiteral()) {
                appendAttributeString(buffer, prop.getName(), prop.getValue());
            }
        }
        QName qName = getName().getQName();

        if (isXHTML(qName)
                && XHTML_EMPTY_ELEMENTS.contains(qName.getLocalName())) {
            buffer.append(" />");
        } else if (isHTML(qName) || getChildProcessorSize() > 0) {
            buffer.append(">");
        } else {
            buffer.append("></");
            writeElementName(buffer);
            buffer.append(">");
        }
    }

    /**
     * 静的な閉じタグをbufferに書き出します。
     *
     * @param buffer 書き出す対象
     */
    protected void writePart4(StringBuffer buffer) {
        QName qName = getName().getQName();
        if (needsCloseElement(qName)) {
            buffer.append("</");
            writeElementName(buffer);
            buffer.append(">");
        }
    }

    protected ProcessStatus writeStartElement() {
        if (getName() == null) {
            throw new IllegalStateException();
        }
        StringBuffer buffer = new StringBuffer(128);
        writePart1(buffer);
        appendPrefixMappingStrings(buffer, getCurrentNS());
        writePart2(buffer);
        writePart3(buffer);
        write(buffer.toString());
        return ProcessStatus.EVAL_BODY_INCLUDE;
    }

    protected void writeBody(String body) {
        write(body);
    }

    protected void writeEndElement() {
        if (getName() == null) {
            throw new IllegalStateException();
        }
        StringBuffer buffer = new StringBuffer(16);
        writePart4(buffer);
        write(buffer.toString());
    }

    public ProcessStatus processStart(Page topLevelPage) {
        if (topLevelPage != null) {
            topLevelPage.registBeginRenderNotifier(this);
        }
        renderInit();
        return super.processStart(topLevelPage);
    }

    public ProcessStatus processEnd() {
        ProcessStatus result = super.processEnd();
        renderExit();
        return result;
    }

    protected void renderInit() {
        clearCurrentNS();
        getRenderedNS().push(new ArrayList());
        resolvePrefixAll();
    }

    protected void renderExit() {
        getRenderedNS().pop();
    }

    public ProcessorTreeWalker[] divide(SequenceIDGenerator sequenceIDGenerator) {
        pushProcesstimeInfo();
        renderInit();
        try {
            if (getInjectedNode().getQName().equals(
                    QM_TEMPLATE_ELEMENT) == false) {
                return new ProcessorTreeWalker[] { this };
            }
            StringBuffer xmlnsDefs = new StringBuffer();
            appendPrefixMappingStrings(xmlnsDefs, getCurrentNS());
            if (xmlnsDefs.length() > 0) {
                // ネームスペース宣言がコンポーネントに引き継がれなくなるので動的なものとする。
                return new ProcessorTreeWalker[] { this };
            }

            List list = new ArrayList();
            StringBuffer buffer = new StringBuffer();
            writePart1(buffer);
            if (buffer.toString().length() > 0) {
                LiteralCharactersProcessor part1 =
                        new LiteralCharactersProcessor(buffer.toString());
                BuilderUtil.characterProcessorCopy(
                        this, part1, sequenceIDGenerator);
                list.add(part1);
            }

            for (Iterator it = iterateProcesstimeProperties(); it.hasNext();) {
                ProcessorProperty prop = (ProcessorProperty) it.next();
                buffer = new StringBuffer();
                appendAttributeString(buffer, prop.getName(), prop.getValue());
                CharactersProcessor part2 =
                        new CharactersProcessor(prop, buffer.toString());
                BuilderUtil.characterProcessorCopy(this, part2, sequenceIDGenerator);
                list.add(part2);
            }
            for (Iterator it = iterateInformalProperties(); it.hasNext();) {
                ProcessorProperty prop = (ProcessorProperty) it.next();
                if (hasProcesstimeProperty(prop) == false
                        && prop.getValue().isLiteral() == false) {
                    buffer = new StringBuffer();
                    appendAttributeString(buffer, prop.getName(), prop.getValue());
                    CharactersProcessor part2 =
                            new CharactersProcessor(prop, buffer.toString());
                    BuilderUtil.characterProcessorCopy(this, part2, sequenceIDGenerator);
                    list.add(part2);
                }
            }

            buffer = new StringBuffer();
            writePart3(buffer);
            if (buffer.toString().length() > 0) {
                LiteralCharactersProcessor part3 =
                        new LiteralCharactersProcessor(buffer.toString());
                BuilderUtil.characterProcessorCopy(this, part3, sequenceIDGenerator);
                list.add(part3);
            }
            int size = getChildProcessorSize();
            for (int i = 0; i < size; i++) {
                list.add(getChildProcessor(i));
            }
            buffer = new StringBuffer();
            writePart4(buffer);
            if (buffer.toString().length() > 0) {
                LiteralCharactersProcessor part4 =
                        new LiteralCharactersProcessor(buffer.toString());
                BuilderUtil.characterProcessorCopy(
                        this, part4, sequenceIDGenerator);
                list.add(part4);
            }
            //optimizeNodes();
            clearChildProcessors();
            return (ProcessorTreeWalker[]) list.toArray(
                    new ProcessorTreeWalker[list.size()]);
        } finally {
            renderExit();
            popProcesstimeInfo();
        }
    }

    protected void optimizeNodes() {
        SpecificationNode originalNode = getOriginalNode();
        NodeTreeWalker originalParent = originalNode.getParentNode();
        for (Iterator it = originalParent.iterateChildNode(); it.hasNext(); ) {
            if (it.next() == originalNode) {
                it.remove();
                break;
            }
        }
        for (Iterator it = getOriginalNode().iterateChildNode(); it.hasNext(); ) {
            NodeTreeWalker child = (NodeTreeWalker)it.next();
            child.setParentNode(originalParent);
            originalParent.addChildNode(child);
        }
        getOriginalNode().clearChildNodes();
        getOriginalNode().clearAttributes();
    }

}
