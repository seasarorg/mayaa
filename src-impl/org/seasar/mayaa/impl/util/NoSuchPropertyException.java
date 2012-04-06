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
public class NoSuchPropertyException extends AbstractMessagedException {

    private static final long serialVersionUID = -803104025776369368L;

    private Class _beanClass;
    private String _propertyName;

    public NoSuchPropertyException(Class beanClass, String propertyName) {
        _beanClass = beanClass;
        _propertyName = propertyName;
    }

    public Class getBeanClass() {
        return _beanClass;
    }

    public String getPropertyName() {
        return _propertyName;
    }

    protected String[] getParamValues() {
        return new String[] {
                _beanClass.getName(),
                _propertyName
                };
    }

}
