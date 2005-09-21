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
package org.seasar.maya.impl.engine.specification;

import org.seasar.maya.engine.specification.Namespace;
import org.seasar.maya.engine.specification.PrefixMapping;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PrefixMappingImpl implements PrefixMapping {

    private Namespace _namespace;
    private String _prefix;
    private String _namespaceURI;
    
    public PrefixMappingImpl(String prefix, String namespaceURI) {
        if(StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        if(prefix == null) {
            prefix = ""; 
        }
        _prefix = prefix;
        _namespaceURI = namespaceURI;
    }

    public void setNamespace(Namespace namespace) {
        if(namespace == null) {
            throw new IllegalArgumentException();
        }
        _namespace = namespace;
    }

    public Namespace getNamespace() {
        if(_namespace == null) {
            throw new IllegalStateException();
        }
        return _namespace;
    }

    public String getPrefix() {
        return _prefix;
    }
    
    public String getNamespaceURI() {
        return _namespaceURI;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("xmlns");
        if(StringUtil.hasValue(_prefix)) {
            buffer.append(":").append(_prefix);
        }
        buffer.append("=").append(_namespaceURI);
        return buffer.toString();
    }
    
    public boolean equals(Object test) {
        if(test == null || (test instanceof PrefixMappingImpl) == false) {
            return false;
        }
        PrefixMappingImpl ns = (PrefixMappingImpl)test;
        return _prefix.equals(ns.getPrefix()) && 
        		_namespaceURI.equals(ns.getNamespaceURI());
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
    
}
