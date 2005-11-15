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
package org.seasar.mayaa.impl.engine.specification;

import java.util.Iterator;

import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PrefixAwareNameImpl extends NamespaceImpl
        implements PrefixAwareName {

    private QName _qName;
    
    public PrefixAwareNameImpl(QName qName) {
        if(qName == null) {
	        throw new IllegalArgumentException();
	    }
        _qName = qName;
    }

    public QName getQName() {
        return _qName;
    }

    public String getPrefix() {
        String namespaceURI = getQName().getNamespaceURI();
	    for(Iterator it = iteratePrefixMapping(true); it.hasNext(); ) {
	        PrefixMapping mapping = (PrefixMapping)it.next();
	        if(namespaceURI.equals(mapping.getNamespaceURI())) {
	            return mapping.getPrefix();
	        }
	    }
	    return "";
	}

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        String prefix = getPrefix();
        if(StringUtil.hasValue(prefix)) {
            buffer.append(prefix).append(":");
        }
        buffer.append(getQName().getLocalName());
        return buffer.toString();
    }

    public boolean equals(Object test) {
        return getQName().equals(test);
    }
    
}
