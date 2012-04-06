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
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.engine.processor.ProcessorPropertyImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class ProcessorPropertyConverter
        extends ParameterAwareImpl implements PropertyConverter {

    private static final long serialVersionUID = -6093527316881781459L;

    public Class getPropetyClass() {
        return ProcessorProperty.class;
    }

    public Serializable convert(
            NodeAttribute attribute, String value, Class expectedClass) {
        if (attribute == null || expectedClass == null) {
            throw new IllegalArgumentException();
        }
        // メモリ消費軽減と速度性能アップのためにキャッシュ利用
        PrefixAwareName prefixAwareName =
            SpecificationUtil.createPrefixAwareName(
                    attribute.getQName(), attribute.getPrefix());
        return new ProcessorPropertyImpl(prefixAwareName, value, expectedClass);
    }

}
