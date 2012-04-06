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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.seasar.mayaa.builder.library.TemplateAttributeReader;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TemplateAttributeReaderImpl
        extends ParameterAwareImpl
        implements TemplateAttributeReader {

    private static final long serialVersionUID = -7340519771743083660L;

    private Set _ignoreAttributes;
    private Map _aliasAttributes;
    private boolean _enabled;

    public TemplateAttributeReaderImpl() {
        _ignoreAttributes = new LinkedHashSet();
        _aliasAttributes = new LinkedHashMap();
        _enabled = false;
    }

    public void addIgnoreAttribute(String qName, String attribute) {
        if (isQName(qName) == false || StringUtil.isEmpty(attribute)) {
            throw new IllegalArgumentException();
        }
        synchronized (_ignoreAttributes) {
            _ignoreAttributes.add(toKey(qName, attribute));
        }
    }

    public void addAliasAttribute(
            String qName, String attribute, String templateAttribute) {
        if (isQName(qName) == false || StringUtil.isEmpty(attribute)
                || isQNameOrLocalName(templateAttribute) == false) {
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
                for (Iterator it = _aliasAttributes.keySet().iterator(); it.hasNext(); ) {
                    AttributeKey aliasKey = (AttributeKey) it.next();
                    if (aliasKey.match(key)) {
                        QName attributeQName = getQName(original,
                                (String) _aliasAttributes.get(aliasKey));
                        NodeAttribute attribute =
                            original.getAttribute(attributeQName);
                        if (attribute != null) {
                            return StringUtil.resolveEntity(attribute.getValue());
                        }
                    }
                }
                NodeAttribute attribute =
                    original.getAttribute(getQName(original, attributeName));
                if (attribute != null) {
                    return StringUtil.resolveEntity(attribute.getValue());
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

    private QName getQName(SpecificationNode original, String attribute) {
        if(attribute.startsWith("{")) {
            String[] split = attribute.split("[\\{\\}]");
            return SpecificationUtil.createQName(
                    SpecificationUtil.createURI(split[1]), split[2]);
        }
        return SpecificationUtil.createQName(
                original.getQName().getNamespaceURI(), attribute);
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

    private static final Pattern QNAME_PATTERN =
        Pattern.compile("^\\{[^\\{\\}]+\\}[^\\{\\}]+$");

    private boolean isQName(String test) {
        return StringUtil.hasValue(test) && QNAME_PATTERN.matcher(test).matches();
    }

    private boolean isQNameOrLocalName(String test) {
        return StringUtil.hasValue(test)
                && (QNAME_PATTERN.matcher(test).matches()
                        || (test.indexOf('{') == -1 && test.indexOf('}') == -1));
    }

    private static class AttributeKey {
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
