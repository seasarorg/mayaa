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

import java.util.Iterator;

import org.seasar.maya.engine.specification.PrefixMapping;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class QNameableImpl extends NamespaceImpl
        implements QNameable {

    private QName _qName;
    
    public QNameableImpl(QName qName) {
        if(qName == null) {
	        throw new IllegalArgumentException();
	    }
        _qName = qName;
    }

    public QName getQName() {
        return _qName;
    }
    
    public String getPrefix() {
        String namespaceURI = _qName.getNamespaceURI();
	    for(Iterator it = iteratePrefixMapping(true); it.hasNext(); ) {
	        PrefixMapping mapping = (PrefixMapping)it.next();
	        if(namespaceURI.equals(mapping.getNamespaceURI())) {
	            return mapping.getPrefix();
	        }
	    }
	    return "";
	}

}
