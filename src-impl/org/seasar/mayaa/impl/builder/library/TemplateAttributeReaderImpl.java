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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
public class TemplateAttributeReaderImpl extends ParameterAwareImpl implements TemplateAttributeReader {

    private Set _ignoreAttributes;
    private Map _aliasAttributes;
    private boolean _enabled;

    public TemplateAttributeReaderImpl() {
        _ignoreAttributes = new LinkedHashSet();
        _aliasAttributes = new LinkedHashMap();
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
            throw new IllegalArgumentException();
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
            AttributeKey key = qNameToKey(qName, attributeName);
            if (isTarget(key)) {
                String templateAttribute = getTemplateAttribute(key, attributeName);
                NodeAttribute attribute =
                    original.getAttribute(getQName(original, templateAttribute));
                if (attribute != null) {
                    return attribute.getValue();
                }
            }
        }
        return null;
    }

    private boolean isTarget(AttributeKey key) {
        for (Iterator it = _ignoreAttributes.iterator(); it.hasNext(); ) {
            AttributeKey ignoreKey = (AttributeKey) it.next();
            if (ignoreKey.match(key)) {
                return false;
            }
        }
        return true;
    }

    private String getTemplateAttribute(AttributeKey key, String attributeName) {
        for (Iterator it = _aliasAttributes.keySet().iterator(); it.hasNext(); ) {
            AttributeKey aliasKey = (AttributeKey) it.next();
            if (aliasKey.match(key)) {
                return (String) _aliasAttributes.get(aliasKey);
            }
        }
        return attributeName;
    }

    private QName getQName(SpecificationNode original, String attribute) {
        return new QNameImpl(original.getQName().getNamespaceURI(), attribute);
    }

    private AttributeKey toKey(String qName, String attribute) {
        if (StringUtil.isEmpty(qName) || StringUtil.isEmpty(attribute)) {
            throw new IllegalArgumentException();
        }
        return new AttributeKey(qName, attribute);
    }

    private AttributeKey qNameToKey(QName qName, String attribute) {
        if (qName == null || StringUtil.isEmpty(attribute)) {
            throw new IllegalArgumentException();
        }
        return new AttributeKey(qName.toString(), attribute);
    }

    private class AttributeKey {
        private String _tagName;
        private String _attributeName;

        protected AttributeKey(String tagName, String attributeName) {
            _tagName = tagName;
            _attributeName = attributeName;
        }

        protected boolean match(AttributeKey other) {
            return matchWildCard(_tagName, other._tagName)
                && matchWildCard(_attributeName, other._attributeName);
        }

        private boolean matchWildCard(String pattern, String test) {
            if (pattern.charAt(pattern.length() - 1) == '*') {
                return test.startsWith(pattern.substring(0, pattern.length() - 1));
            }
            return test.equals(pattern);
        }
    }

}
