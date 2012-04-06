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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.library.LibraryDefinition;
import org.seasar.mayaa.builder.library.PropertyDefinition;
import org.seasar.mayaa.builder.library.PropertySet;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertySetImpl extends ParameterAwareImpl
        implements PropertySet {

    private static final long serialVersionUID = 6254708300179494624L;
    private static final Log LOG =
        LogFactory.getLog(PropertySetImpl.class);

    private LibraryDefinition _library;
    private String _name;
    private List _properties;
    private Set _propertyNames;
    private int _lineNumber;

    public void setLineNumber(int lineNumber) {
        if (lineNumber < 0) {
            throw new IllegalArgumentException();
        }
        _lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return _lineNumber;
    }

    public void setLibraryDefinition(LibraryDefinition library) {
        if (library == null) {
            throw new IllegalArgumentException();
        }
        _library = library;
    }

    public LibraryDefinition getLibraryDefinition() {
        if (_library == null) {
            throw new IllegalStateException();
        }
        return _library;
    }

    public void setName(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        _name = name;
    }

    public String getName() {
        if (StringUtil.isEmpty(_name)) {
            throw new IllegalStateException();
        }
        return _name;
    }

    public void addPropertyDefinitiion(PropertyDefinition property) {
        if (property == null) {
            throw new IllegalArgumentException();
        }
        if (_properties == null) {
            _propertyNames = new HashSet();
            _properties = new ArrayList();
        }
        String propName = property.getName();
        if (_propertyNames.add(propName)) {
            _properties.add(property);
        } else {
            if (LOG.isWarnEnabled()) {
                String msg = StringUtil.getMessage(PropertySetImpl.class,
                        0, getName(), propName);
                LOG.warn(msg);
            }
        }
    }

    protected boolean contain(URI namespaceURI, NodeAttribute attr) {
        if (StringUtil.isEmpty(namespaceURI) || attr == null) {
            throw new IllegalArgumentException();
        }
        if (_propertyNames == null) {
            return false;
        }
        URI attrNS = attr.getQName().getNamespaceURI();
        String attrName = attr.getQName().getLocalName();
        return _propertyNames.contains(attrName)
                && namespaceURI.equals(attrNS);
    }

    public Iterator iteratePropertyDefinition() {
        if (_properties == null) {
            return NullIterator.getInstance();
        }
        return _properties.iterator();
    }

}
