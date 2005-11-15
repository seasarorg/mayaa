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
package org.seasar.mayaa.impl.builder.library.converter;

import org.seasar.mayaa.builder.library.converter.PropertyConverter;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.engine.processor.ProcessorPropertyImpl;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class ProcessorPropertyConverter
		extends ParameterAwareImpl implements PropertyConverter {

	public Class getPropetyClass() {
		return ProcessorProperty.class;
	}

	public Object convert(
			NodeAttribute attribute, String value, Class expectedClass) {
        if(attribute == null || expectedClass == null) {
            throw new IllegalArgumentException();
        }
        return new ProcessorPropertyImpl(
                attribute, value, expectedClass);
	}
	
}
