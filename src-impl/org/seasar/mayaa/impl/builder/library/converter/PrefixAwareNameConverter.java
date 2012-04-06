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
package org.seasar.mayaa.impl.builder.library.converter;

import java.io.Serializable;

import org.seasar.mayaa.builder.library.converter.PropertyConverter;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.builder.BuilderUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class PrefixAwareNameConverter
        extends ParameterAwareImpl implements PropertyConverter {

    private static final long serialVersionUID = 7617633992522264452L;

    public Class getPropetyClass() {
        return PrefixAwareName.class;
    }

    public Serializable convert(
            NodeAttribute attribute, String value, Class expectedClass) {
        if (attribute == null || value == null) {
            throw new IllegalArgumentException();
        }
        return BuilderUtil.parseName(attribute.getNode(), value);
    }

}
