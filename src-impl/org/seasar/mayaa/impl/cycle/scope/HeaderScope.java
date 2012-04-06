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

import java.util.Iterator;

import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.impl.cycle.CycleUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class HeaderScope extends AbstractReadOnlyAttributeScope {

    private static final long serialVersionUID = 5356393611509751716L;

    // AttributeScope implements -------------------------------------

    public String getScopeName() {
        return "header";
    }

    public Iterator iterateAttributeNames() {
        AttributeScope values = CycleUtil.getRequestScope().getHeaderValues();
        return values.iterateAttributeNames();
    }

    public boolean hasAttribute(String name) {
        AttributeScope values = CycleUtil.getRequestScope().getHeaderValues();
        return values.hasAttribute(name);
    }

    public Object getAttribute(String name) {
        if (hasAttribute(name)) {
            AttributeScope values = CycleUtil.getRequestScope().getHeaderValues();
            String[] headers = (String[]) values.getAttribute(name);
            if (headers.length == 0) {
                return "";
            }
            return headers[0];
        }
        return null;
    }

}
