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
package org.seasar.mayaa.impl.builder.library;

import org.seasar.mayaa.builder.library.converter.PropertyConverter;
import org.seasar.mayaa.impl.MayaaException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ConverterOperationException extends MayaaException {

    private static final long serialVersionUID = -5007393180957134578L;
    private PropertyConverter _converter;
    private String _value;

    public ConverterOperationException(PropertyConverter converter,
            String value) {
        _converter = converter;
        _value = value;
    }

    public PropertyConverter getPropertyConverter() {
        return _converter;
    }

    public String getValue() {
        return _value;
    }

    protected String[] getMessageParams() {
        String converterName = _converter.getClass().getName();
        return new String[] { converterName, _value };
    }

}
