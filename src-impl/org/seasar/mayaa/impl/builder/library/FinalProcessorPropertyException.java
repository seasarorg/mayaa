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
package org.seasar.maya.impl.builder.library;

import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.impl.MayaException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class FinalProcessorPropertyException extends MayaException {

    private static final long serialVersionUID = -330877631840274409L;

    String _namespaceURI;
    String _processorName;
    String _propertyName;
    
    public FinalProcessorPropertyException(
            String processorName, QName qName) {
        _processorName = processorName;
        if(qName != null) {
            _namespaceURI = qName.getNamespaceURI();
            _propertyName = qName.getLocalName();
        }
    }
   
    public String getNamespaceURI() {
        return _namespaceURI;
    }
    
    public String getProcessorName() {
        return _processorName;
    }
    
    public String getPropertyName() {
        return _propertyName;
    }
 
    protected String[] getMessageParams() {
        return new String[] { _namespaceURI, _processorName, _propertyName };
    }
    
}
