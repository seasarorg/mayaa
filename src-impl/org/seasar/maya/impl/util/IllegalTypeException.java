/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.util;

import org.seasar.maya.impl.MayaException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class IllegalTypeException extends MayaException {

	private static final long serialVersionUID = -5744177696584258746L;
	private Class _expectedType;
    private Class _value;
    
    public IllegalTypeException(Class expectedType, Class value) {
        _expectedType = expectedType;
        _value = value;
    }
    
    public Class getExpectedType() {
        return _expectedType;
    }
    
    public Class getValue() {
        return _value;
    }
    
    protected Object[] getMessageParams() {
        return new Object[] { _expectedType, _value };
    }
    
}
