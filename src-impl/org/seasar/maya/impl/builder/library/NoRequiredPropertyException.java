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
package org.seasar.maya.impl.builder.library;

import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.impl.MayaException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NoRequiredPropertyException extends MayaException {

	private static final long serialVersionUID = -8388715165180536210L;

    private QName _qName; 
    
    public NoRequiredPropertyException(QName qName) {
        _qName = qName;
    }
    
    public QName getQName() {
        return _qName;
    }
 
    protected String[] getMessageParams() {
        return new String[] { _qName.getNamespaceURI(), _qName.getLocalName() };
    }
    
}
