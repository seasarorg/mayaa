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
package org.seasar.mayaa.impl.builder.library;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.library.LibraryDefinition;
import org.seasar.mayaa.builder.library.ProcessorDefinition;
import org.seasar.mayaa.builder.library.TemplateAttributeReader;
import org.seasar.mayaa.builder.library.converter.PropertyConverter;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.engine.processor.ProcessorPropertyLiteral;
import org.seasar.mayaa.impl.engine.specification.NodeAttributeImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author Hisayoshi Sasaki (Gluegent, Inc.)
 */
public class TLDPropertyDefinition extends PropertyDefinitionImpl {

    private static final long serialVersionUID = -2999228650173085084L;
    private static final Log LOG =
        LogFactory.getLog(TLDPropertyDefinition.class);

    private boolean _rtexprvalue;

    protected PropertyConverter getConverterForProcessorProperty() {
        LibraryDefinition library = getPropertySet().getLibraryDefinition();
        PropertyConverter converter =
            library.getPropertyConverter(ProcessorProperty.class);
        if (converter == null) {
            throw new IllegalStateException();
        }
        return converter;
    }

    protected NodeAttribute getTemplateAttribute(
            SpecificationNode original, SpecificationNode injected) {
        TemplateAttributeReader reader = ProviderUtil.getTemplateAttributeReader();
        String value = reader.getValue(injected.getQName(), getName(), original);
        if (value != null) {
            return new NodeAttributeImpl(getQName(injected), value);
        }
        return null;
    }

    public Object createProcessorProperty(ProcessorDefinition processorDef,
            TemplateProcessor processor,
            SpecificationNode original, SpecificationNode injected) {
        if (injected == null) {
            throw new IllegalArgumentException();
        }
        Class propertyClass = getPropertyClass(processorDef, processor);
        if (propertyClass == null) {
            // real property not found on the tag.
            String processorName = processorDef.getName();
            if (LOG.isWarnEnabled()) {
                String msg = StringUtil.getMessage(TLDPropertyDefinition.class,
                        0, processorName, getName());
                LOG.warn(msg);
            }
            return null;
        }
        QName qName = getQName(injected);
        NodeAttribute attribute = injected.getAttribute(qName);
        if (attribute == null) {
            attribute = getTemplateAttribute(original, injected);
        }
        if (attribute != null) {
            if (isRtexprvalue()) {
                // 動的値を許可
                PropertyConverter converter = getConverterForProcessorProperty();
                return converter.convert(attribute, attribute.getValue(), propertyClass);
            }

            // 動的値は不許可
            PrefixAwareName prefixAwareName =
                SpecificationUtil.createPrefixAwareName(
                        attribute.getQName(), attribute.getPrefix());
            return new ProcessorPropertyLiteral(prefixAwareName, attribute.getValue());
        } else if (isRequired()) {
            String processorName = processorDef.getName();
            throw new NoRequiredPropertyException(processorName, qName);
        }
        return null;
    }

    public boolean isRtexprvalue() {
        return _rtexprvalue;
    }

    public void setRtexprvalue(boolean rtexprvalue) {
        this._rtexprvalue = rtexprvalue;
    }
}
