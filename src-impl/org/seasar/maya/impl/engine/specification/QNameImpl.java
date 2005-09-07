/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.engine.specification;

import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class QNameImpl implements QName, CONST_IMPL {

    private String _namespaceURI;
    private String _localName;
    
    public QNameImpl(String localName) {
        this(URI_MAYA, localName);
    }
    
    public QNameImpl(String namespaceURI, String localName) {
        if(StringUtil.isEmpty(namespaceURI) || StringUtil.isEmpty(localName)) {
            throw new IllegalArgumentException();
        }
        _namespaceURI = namespaceURI;
        _localName = localName;
    }
    
    public String getNamespaceURI() {
        return _namespaceURI;
    }
    
    public String getLocalName() {
        return _localName;
    }
    
    public String toString() {
        return "{" + getNamespaceURI() + "}" + getLocalName();
    }
    
    public boolean equals(Object test) {
        if(test instanceof QNameImpl) {
            QNameImpl qName = (QNameImpl)test;
            return _namespaceURI.equals(qName.getNamespaceURI()) &&
            	_localName.equalsIgnoreCase(qName.getLocalName());
        }
        return false;
    }
    
    public int hashCode() {
        return toString().hashCode();
    }

}
