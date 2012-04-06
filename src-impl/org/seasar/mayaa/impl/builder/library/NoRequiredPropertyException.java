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

import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.MayaaException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NoRequiredPropertyException extends MayaaException {

    private static final long serialVersionUID = -8388715165180536210L;

    private final URI _namespaceURI;
    private final String _processorName;
    private final String _propertyName;

    public NoRequiredPropertyException(String processorName, QName qName) {
        _processorName = processorName;
        if (qName != null) {
            _namespaceURI = qName.getNamespaceURI();
            _propertyName = qName.getLocalName();
        } else {
            _namespaceURI = null;
            _propertyName = null;
        }
    }

    public URI getNamespaceURI() {
        return _namespaceURI;
    }

    public String getProcessorName() {
        return _processorName;
    }

    public String getPropertyName() {
        return _propertyName;
    }

    protected String[] getMessageParams() {
        return new String[] {
                String.valueOf(_namespaceURI), _processorName, _propertyName };
    }

}
