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
package org.seasar.mayaa.impl.engine.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cyberneko.html.HTMLElements;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ElementProcessor extends AbstractAttributableProcessor
        implements CONST_IMPL {

    private static final long serialVersionUID = 923306412062075314L;
    private static final String SUFFIX_DUPLICATED = "_d";
    private static final Set XHTML_EMPTY_ELEMENTS;
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
    }

    private PrefixAwareName _name;
    private boolean _duplicated;
    private ThreadLocal _currentNS = new ThreadLocal();

    protected void clearCurrentNS() {
        _currentNS.set(null);
    }

    protected Namespace getCurrentNS() {
        Namespace currentNS = (Namespace) _currentNS.get();
        if (currentNS == null) {
            currentNS = SpecificationUtil.createNamespace();
            currentNS.setParentSpace(getOriginalNode().getParentSpace());
            _currentNS.set(currentNS);
        }
        return currentNS;
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

    protected void resolvePrefix(PrefixAwareName name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        Namespace currentNS = getCurrentNS();
        String namespaceURI = name.getQName().getNamespaceURI();
        PrefixMapping mapping =
            currentNS.getMappingFromURI(namespaceURI, true);
        if (mapping != null) {
            return;
        }
        Namespace namespace = getInjectedNode().getParentSpace();
        mapping = namespace.getMappingFromURI(namespaceURI, true);
        if (mapping != null) {
            currentNS.addPrefixMapping(
                    mapping.getPrefix(), mapping.getNamespaceURI());
            return;
        }
        currentNS.addPrefixMapping(name.getPrefix(), namespaceURI);
    }

    protected void resolvePrefixAll() {
        resolvePrefix(getName());
        for (Iterator it = iterateProcesstimeProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            resolvePrefix(prop.getName());
        }
        for (Iterator it = iterateInformalProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            resolvePrefix(prop.getName());
        }
    }

    protected String getResolvedPrefix(PrefixAwareName name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        Namespace currentNS = getCurrentNS();
        String namespaceURI = name.getQName().getNamespaceURI();
        PrefixMapping mapping =
            currentNS.getMappingFromURI(namespaceURI, true);
        if (mapping != null) {
            return mapping.getPrefix();
        }
        throw new IllegalStateException();
    }

    protected void appendPrefixMappingString(
            StringBuffer buffer, Namespace namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException();
        }
        for (Iterator it = namespace.iteratePrefixMapping(false);
                it.hasNext();) {
            PrefixMapping mapping = (PrefixMapping) it.next();
            String pre = mapping.getPrefix();
            String uri = mapping.getNamespaceURI();
            if (uri.startsWith(URI_MAYAA)) {
                continue;
            }
            if (StringUtil.hasValue(pre)) {
                buffer.append(" xmlns:").append(pre);
            } else {
                buffer.append(" xmlns");
            }
            buffer.append("=\"").append(uri).append("\"");
        }
    }

    protected void appendAttributeString(
            StringBuffer buffer, PrefixAwareName propName, Object value) {
        QName qName = propName.getQName();
        if (URI_MAYAA.equals(qName.getNamespaceURI())) {
            return;
        }
        buffer.append(" ");
        String attrPrefix = getResolvedPrefix(propName);
        if (StringUtil.hasValue(attrPrefix)) {
            buffer.append(attrPrefix).append(":");
        }
        buffer.append(qName.getLocalName());
        buffer.append("=\"");
        if (value instanceof CompiledScript) {
            CompiledScript script = (CompiledScript) value;
            if (CycleUtil.isDraftWriting()) {
                buffer.append(script.getScriptText());
            } else {
                buffer.append(script.execute(null));
            }
        } else {
            buffer.append(value.toString());
        }
        buffer.append("\"");
    }

    protected boolean needsCloseElement(QName qName) {
        if (isHTML(qName)) {
            HTMLElements.Element element =
                HTMLElements.getElement(qName.getLocalName());
            return element.isEmpty() == false;
        }
        return getChildProcessorSize() > 0;
    }

    protected PrefixAwareName getIDName() {
        String namespaceURI = getName().getQName().getNamespaceURI();
        PrefixAwareName name = SpecificationUtil.createPrefixAwareName(
                SpecificationUtil.createQName(namespaceURI, "id"));
        name.setParentSpace(getName().getParentSpace());
        return name;
    }

    protected void write(String value) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.getResponse().write(value);
    }

    protected void writeElementName(StringBuffer buffer) {
        String prefix = getResolvedPrefix(getName());
        if (StringUtil.hasValue(prefix)) {
            buffer.append(prefix).append(":");
        }
        QName qName = getName().getQName();
        buffer.append(qName.getLocalName());
    }

    protected void writePart1(StringBuffer buffer) {   // 静的 <xxx
        buffer.append("<");
        writeElementName(buffer);
        if (isHTML(getName().getQName()) == false) {
            appendPrefixMappingString(buffer, getCurrentNS());
            appendPrefixMappingString(buffer, getName().getParentSpace());
        }
    }

    protected void writePart2(StringBuffer buffer) {   // 動的 attribute
        for (Iterator it = iterateProcesstimeProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            internalWritePart2(buffer, prop);
        }
    }

    protected void internalWritePart2(StringBuffer buffer, ProcessorProperty prop) {
        PrefixAwareName propName = prop.getName();
        QName propQName = propName.getQName();
        String propURI = propQName.getNamespaceURI();
        String propLocalName = propQName.getLocalName();
        if (isDuplicated()) {
            // TODO Feb 24, 2006 8:40:30 AM id を消すか？
            if (getName().getQName().getNamespaceURI().equals(propURI)
                    && "id".equals(propLocalName)) {
                return;
            }
        }
        appendAttributeString(buffer, propName, prop.getValue());
    }

    protected void writePart3(StringBuffer buffer) {   // 静的 attributeと、>
        for (Iterator it = iterateInformalProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            if (hasProcesstimeProperty(prop) == false) {
                appendAttributeString(buffer, prop.getName(), prop.getValue());
            }
        }
        QName qName = getName().getQName();
        if (isHTML(qName) || getChildProcessorSize() > 0) {
            buffer.append(">");
        } else {
            if (isXHTML(qName)
                    && XHTML_EMPTY_ELEMENTS.contains(qName.getLocalName())) {
                buffer.append("/>");
            } else {
                buffer.append("></");
                writeElementName(buffer);
                buffer.append(">");
            }
        }
    }

    protected void writePart4(StringBuffer buffer) {   // 静的 </xxx>
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
        resolvePrefixAll();
        StringBuffer buffer = new StringBuffer();
        writePart1(buffer);
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
        StringBuffer buffer = new StringBuffer();
        writePart4(buffer);
        write(buffer.toString());
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        clearCurrentNS();
        return super.doStartProcess(topLevelPage);
    }

    public ProcessorTreeWalker[] divide() {
        resolvePrefixAll();

        if (getProcessorDefinition().getName().equals(
                QM_TEMPLATE_ELEMENT.getLocalName()) == false) {
            return new ProcessorTreeWalker[] { this };
        }

        for (Iterator it = iterateInformalProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            if (prop.getValue().isLiteral() == false) {
                return new ProcessorTreeWalker[] { this };
            }
        }

        List list = new ArrayList();
        StringBuffer buffer = new StringBuffer();
        writePart1(buffer);
        if (buffer.toString().length() > 0) {
            LiteralCharactersProcessor part1 =
                    new LiteralCharactersProcessor(this, buffer.toString());
            list.add(part1);
        }

        for (Iterator it = iterateProcesstimeProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            buffer = new StringBuffer();
            internalWritePart2(buffer, prop);
            CharactersProcessor part2 =
                    new CharactersProcessor(this, prop, buffer.toString());
            list.add(part2);
        }

        buffer = new StringBuffer();
        writePart3(buffer);
        if (buffer.toString().length() > 0) {
            LiteralCharactersProcessor part3 =
                    new LiteralCharactersProcessor(this, buffer.toString());
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
                    new LiteralCharactersProcessor(this, buffer.toString());
            list.add(part4);
        }

        return (ProcessorTreeWalker[]) list.toArray(new ProcessorTreeWalker[list.size()]);
    }

}
