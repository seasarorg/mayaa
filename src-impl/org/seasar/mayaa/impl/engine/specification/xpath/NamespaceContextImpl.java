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
package org.seasar.mayaa.impl.engine.specification.xpath;

import org.jaxen.NamespaceContext;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.PrefixMapping;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NamespaceContextImpl implements NamespaceContext {

    private Namespace _namespace;

    public NamespaceContextImpl(Namespace namespaceable) {
        if (namespaceable == null) {
            throw new IllegalArgumentException();
        }
        _namespace = namespaceable;
    }

    public String translateNamespacePrefixToUri(String prefix) {
        PrefixMapping mapping = _namespace.getMappingFromPrefix(prefix, true);
        if (mapping != null) {
            return mapping.getNamespaceURI().getValue();
        }
        return null;
    }

}
