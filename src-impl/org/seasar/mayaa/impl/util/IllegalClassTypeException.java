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
package org.seasar.mayaa.impl.util;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class IllegalClassTypeException extends AbstractMessagedException {

    private static final long serialVersionUID = -5744177696584258746L;
    private Class _expectedClass;
    private Class _value;

    public IllegalClassTypeException(Class expectedClass, Class value) {
        _expectedClass = expectedClass;
        _value = value;
    }

    public Class getExpectedClass() {
        return _expectedClass;
    }

    public Class getValue() {
        return _value;
    }

    protected String[] getParamValues() {
        return new String[] {
                _expectedClass.getName(),
                _value.getName()
                };
    }

}
