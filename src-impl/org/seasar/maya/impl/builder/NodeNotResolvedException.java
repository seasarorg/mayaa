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
package org.seasar.maya.impl.builder;

import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.impl.MayaException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class NodeNotResolvedException extends MayaException {

    private String _namespaceURI;
    private String _localName;
    private String _prefix;
    
    protected NodeNotResolvedException(QNameable qNameable) {
        _namespaceURI = qNameable.getQName().getNamespaceURI();
        _localName = qNameable.getQName().getLocalName();
        _prefix = qNameable.getPrefix();
    }
    
    public String getNamespaceURI() {
        return _namespaceURI;
    }
    
    public String getLocalName() {
        return _localName;
    }
    
    public String getPrefix() {
        return _prefix;
    }
    
    protected String[] getMessageParams() {
        return new String[] { _namespaceURI, _localName, _prefix };
    }
    
}
