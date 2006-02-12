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
package org.seasar.mayaa.impl.builder.library;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.seasar.mayaa.builder.library.TemplateAttributeReader;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TemplateAttributeReaderImpl extends ParameterAwareImpl
        implements TemplateAttributeReader {

    private Set _ignoreAttributes;
    private Map _aliasAttributes;
    private boolean _enabled;

    public TemplateAttributeReaderImpl() {
        _ignoreAttributes = new HashSet();
        _aliasAttributes = new HashMap();
        _enabled = false;
    }

    public void addIgnoreAttribute(String qName, String attribute) {
        if (StringUtil.isEmpty(qName) || StringUtil.isEmpty(attribute)) {
            throw new IllegalArgumentException();
        }
        synchronized (_ignoreAttributes) {
            _ignoreAttributes.add(toKey(qName, attribute));
        }
    }

    public void addAliasAttribute(
            String qName, String attribute, String templateAttribute) {
        if (StringUtil.isEmpty(qName) || StringUtil.isEmpty(attribute)
                || StringUtil.isEmpty(templateAttribute)) {
        }
        synchronized (_aliasAttributes) {
            _aliasAttributes.put(toKey(qName, attribute), templateAttribute);
        }
    }

    public void setParameter(String name, String value) {
        if ("enabled".equals(name)) {
            _enabled = Boolean.valueOf(value).booleanValue();
        }
        super.setParameter(name, value);
    }

    public String getValue(
            QName qName, String attributeName, SpecificationNode original) {
        if (_enabled) {
            String key = qNameToKey(qName, attributeName);
            if (_ignoreAttributes.contains(key) == false) {
                String templateAttribute = (String)_aliasAttributes.get(key);
                if (templateAttribute == null) {
                    templateAttribute = attributeName;
                }
                NodeAttribute attribute = original.getAttribute(
                        getQName(original, templateAttribute));
                if (attribute != null) {
                    return attribute.getValue();
                }
            }
        }
        return null;
    }

    private QName getQName(SpecificationNode original, String attribute) {
        return new QNameImpl(original.getQName().getNamespaceURI(), attribute);
    }

    private String toKey(String qName, String attribute) {
        if (StringUtil.isEmpty(qName) || StringUtil.isEmpty(attribute)) {
            throw new IllegalArgumentException();
        }
        return qName + "/" + attribute;
    }

    private String qNameToKey(QName qName, String attribute) {
        if (qName == null || StringUtil.isEmpty(attribute)) {
            throw new IllegalArgumentException();
        }
        return qName.toString() + "/" + attribute;
    }

}
