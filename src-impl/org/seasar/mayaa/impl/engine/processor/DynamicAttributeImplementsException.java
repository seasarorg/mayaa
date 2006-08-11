/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.engine.processor;

import javax.servlet.jsp.tagext.DynamicAttributes;

import org.seasar.mayaa.impl.MayaaException;

/**
 * @author Hisayoshi Sasaki (Gluegent,Inc.)
 */
public class DynamicAttributeImplementsException extends MayaaException {
	private static final long serialVersionUID = 8350640885237696171L;

    private Class _tagClass;

    public DynamicAttributeImplementsException(Class tag) {
        super();
        _tagClass = tag;
    }

    /**
     * @see org.seasar.mayaa.impl.MayaaException#getMessageParams()
     */
    protected String[] getMessageParams() {
        return new String[] {
                _tagClass.getName(),
                DynamicAttributes.class.getName()};
    }

}
