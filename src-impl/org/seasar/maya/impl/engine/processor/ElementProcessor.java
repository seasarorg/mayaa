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
package org.seasar.maya.impl.engine.processor;

import java.util.Iterator;

import org.cyberneko.html.HTMLElements;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.specification.Namespace;
import org.seasar.maya.engine.specification.PrefixMapping;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ElementProcessor extends AbstractAttributableProcessor
		implements CONST_IMPL {

	private static final long serialVersionUID = 923306412062075314L;

	private QNameable _name;
    private boolean _duplicated;
    private ThreadLocal _currentNS = new ThreadLocal();

    // exported property
    public boolean isDuplicated() {
    	return _duplicated;
    }
    
    // MLD property
    public void setDuplicated(boolean duplicated) {
        _duplicated = duplicated;
    }
    
    // MLD property
    public void setName(QNameable name) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        _name = name;
    }
    
    protected Namespace getCurrentNS() {
        Namespace currentNS = (Namespace)_currentNS.get();
        if(currentNS == null) {
            currentNS = SpecificationUtil.createNamespace();
            currentNS.setParentSpace(getOriginalNode().getParentSpace());
            _currentNS.set(currentNS);
        }
        return currentNS;
    }
    
    protected void resolvePrefix(QNameable name) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        Namespace currentNS = getCurrentNS();
        String namespaceURI = name.getQName().getNamespaceURI();
        PrefixMapping mapping = 
            currentNS.getMappingFromURI(namespaceURI, true);
        if(mapping != null) {
            return;
        }
        Namespace namespace = getInjectedNode().getParentSpace();
        mapping = namespace.getMappingFromURI(namespaceURI, true);
        if(mapping != null) {
            currentNS.addPrefixMapping(
                    mapping.getPrefix(), mapping.getNamespaceURI());
            return;
        }
        currentNS.addPrefixMapping(name.getPrefix(), namespaceURI);
    }
    
    protected void resolvePrefixAll() {
        resolvePrefix(_name);
        for(Iterator it = iterateProcesstimeProperties(); it.hasNext(); ) {
            ProcessorProperty prop = (ProcessorProperty)it.next();
            resolvePrefix(prop.getName());
        }
        for(Iterator it = iterateInformalProperties(); it.hasNext(); ) {
            ProcessorProperty prop = (ProcessorProperty)it.next();
            resolvePrefix(prop.getName());
        }
    }

    protected String getResolvedPrefix(QNameable name) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        Namespace currentNS = getCurrentNS();
        String namespaceURI = name.getQName().getNamespaceURI();
        PrefixMapping mapping = 
            currentNS.getMappingFromURI(namespaceURI, true);
        if(mapping != null) {
            return mapping.getPrefix();
        }
        throw new IllegalStateException();
    }
    
    protected void appendPrefixMappingString(
            StringBuffer buffer, Namespace namespace) {
        if(namespace == null) {
            throw new IllegalArgumentException();
        }
        for(Iterator it = namespace.iteratePrefixMapping(false);
                it.hasNext(); ) {
            PrefixMapping mapping = (PrefixMapping)it.next();
            String pre = mapping.getPrefix();
            String uri = mapping.getNamespaceURI();
            if(StringUtil.hasValue(pre)) {
                buffer.append(" xmlns:").append(pre);
            } else {
                buffer.append(" xmlns");
            }
            buffer.append("=\"").append(uri).append("\"");
        }
    }
    
    protected void appendAttributeString(
            StringBuffer buffer, ProcessorProperty prop) {
        QName qName = prop.getName().getQName();
        if(URI_MAYA.equals(qName.getNamespaceURI())) {
            return;
        }
        buffer.append(" ");
        String attrPrefix = getResolvedPrefix(prop.getName());
        if(StringUtil.hasValue(attrPrefix)) {
            buffer.append(attrPrefix).append(":");
        }
        buffer.append(qName.getLocalName());
        buffer.append("=\"").append(prop.getValue().execute(null)).append("\"");
    }
    
    protected boolean isHTML(QName qName) {
        String namespaceURI = qName.getNamespaceURI();
        return URI_HTML.equals(namespaceURI);
    }
    
    protected boolean needsCloseElement(QName qName) {
        if(isHTML(qName)) {
            String localName = qName.getLocalName();
            HTMLElements.Element element = 
                HTMLElements.getElement(localName);
            return element.isEmpty() == false;
        }
        return getChildProcessorSize() > 0;
    }
    
    protected void write(StringBuffer buffer) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.getResponse().write(buffer.toString());
    }
    
    protected ProcessStatus writeStartElement() {
        if(_name == null) {
            throw new IllegalStateException();
        }
        resolvePrefixAll();
        StringBuffer buffer = new StringBuffer("<");
        String prefix = getResolvedPrefix(_name);
        if(StringUtil.hasValue(prefix)) {
            buffer.append(prefix).append(":");
        }
        QName qName = _name.getQName();
        buffer.append(qName.getLocalName());
        if(isHTML(qName) == false) {
            appendPrefixMappingString(buffer, getCurrentNS());
            appendPrefixMappingString(buffer, _name.getParentSpace());
        }
        for(Iterator it = iterateProcesstimeProperties(); it.hasNext(); ) {
            ProcessorProperty prop = (ProcessorProperty)it.next();
            if(_duplicated) {
            	QName propQName = prop.getName().getQName(); 
            	if(QH_ID.equals(propQName) || QX_ID.equals(propQName)) {
            		continue;
            	}
            }
            appendAttributeString(buffer, prop);
        }
        for(Iterator it = iterateInformalProperties(); it.hasNext(); ) {
            ProcessorProperty prop = (ProcessorProperty)it.next();
            if(hasProcesstimeProperty(prop) == false) {
                appendAttributeString(buffer, prop);
            }
        }
        if(isHTML(qName) || getChildProcessorSize() > 0) {
            buffer.append(">");
        } else {
            buffer.append("/>");
        }
        write(buffer);
        return EVAL_BODY_INCLUDE;
    }
    
    protected void writeEndElement() {
        if(_name == null) {
            throw new IllegalStateException();
        }
        QName qName = _name.getQName();
        if(needsCloseElement(qName)) {
            StringBuffer buffer = new StringBuffer("</");
            String prefix = getResolvedPrefix(_name);
            if(StringUtil.hasValue(prefix)) {
                buffer.append(prefix).append(":");
            }
            buffer.append(qName.getLocalName()).append(">");
            write(buffer);
        }
    }
    
}
