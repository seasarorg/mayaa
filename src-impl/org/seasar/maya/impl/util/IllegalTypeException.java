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
    
    protected String[] getMessageParams() {
        return new String[] { _expectedType.getName(), _value.getName() };
    }
    
}
