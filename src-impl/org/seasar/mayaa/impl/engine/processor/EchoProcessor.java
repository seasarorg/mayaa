/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.mayaa.builder.SequenceIDGenerator;
import org.seasar.mayaa.builder.library.LibraryDefinition;
import org.seasar.mayaa.builder.library.ProcessorDefinition;
import org.seasar.mayaa.builder.library.converter.PropertyConverter;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class EchoProcessor extends ElementProcessor
        implements CONST_IMPL {

    private static final long serialVersionUID = 3924111635172574833L;

    private PrefixAwareName _customName;
    public void setOriginalNode(SpecificationNode originalNode) {
        super.setOriginalNode(originalNode);
        setupElement(originalNode);
    }

    protected PropertyConverter getConverterForProcessorProperty() {
        ProcessorDefinition processorDef = getProcessorDefinition();
        LibraryDefinition libraryDef = processorDef.getLibraryDefinition();
        PropertyConverter converter =
            libraryDef.getPropertyConverter(ProcessorProperty.class);
        if (converter == null) {
            throw new IllegalStateException();
        }
        return converter;
    }

    protected void setupElement(SpecificationNode originalNode) {
        if (_customName != null) {
            super.setName(_customName);
        } else {
            // メモリリークを起こす可能性のある参照関係を断つ
            PrefixAwareName prefixAwareName = SpecificationUtil.createPrefixAwareName(
                    originalNode.getQName(), originalNode.getPrefix());
            super.setName(prefixAwareName);
        }
        PropertyConverter converter = getConverterForProcessorProperty();
        for (Iterator it = originalNode.iterateAttribute(); it.hasNext();) {
            NodeAttribute attribute = (NodeAttribute) it.next();
            String value = attribute.getValue();
            Class expectedClass = getExpectedClass();
            Serializable property = converter.convert(attribute, value, expectedClass);
            PrefixAwareName propName = SpecificationUtil.createPrefixAwareName(
                    attribute.getQName(), attribute.getPrefix());
            super.addInformalProperty(propName, property);
        }
    }

    // MLD property of ElementProcessor
    public void setName(PrefixAwareName name) {
        _customName = name;
    }

    // MLD method of AbstractAttributableProcessor
    public void addInformalProperty(PrefixAwareName name, Object attr) {
        // doNothing
    }

    // ProcessorTreeWalker implements --------------------------------
    public Map getVariables() {
        Iterator it = iterateInformalProperties();
        if (it.hasNext()) {
            Map attributeMap = new HashMap();
            while (it.hasNext()) {
                ProcessorProperty prop = (ProcessorProperty) it.next();
                attributeMap.put(
                        prop.getName().getQName().getLocalName(),
                        prop.getValue().execute(null));
            }
            return attributeMap;
        }
        return null;
    }

    public ProcessorTreeWalker[] divide(SequenceIDGenerator sequenceIDGenerator) {
        return new ProcessorTreeWalker[] { this };
    }

}
