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
package org.seasar.mayaa.impl.cycle.scope;

import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractWritableAttributeScope
        extends ParameterAwareImpl implements AttributeScope {

    private static final long serialVersionUID = 3301556156255056131L;

    public boolean isAttributeWritable() {
        return true;
    }

    public Object newAttribute(String name, Class attributeClass) {
        if (attributeClass == null) {
            throw new IllegalArgumentException();
        }
        if (StringUtil.isEmpty(name)) {
            name = attributeClass.getName();
        }
        if (hasAttribute(name)) {
            return getAttribute(name);
        }
        Object model = ObjectUtil.newInstance(attributeClass);
        setAttribute(name, model);
        return model;
    }

}
