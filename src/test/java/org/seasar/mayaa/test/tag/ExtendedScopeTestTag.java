/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.test.tag;

import jakarta.servlet.jsp.JspException;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class ExtendedScopeTestTag extends ScopeTestTag {

    private static final long serialVersionUID = -272720933733898886L;

    private String _extended;

    public void setExtended(String extended) {
        _extended = extended;
    }

    public String getExtended() {
        return _extended;
    }

    protected void prepareIndex(StringBuilder handlers, String name)
            throws JspException {
        super.prepareIndex(handlers, name);
    }

}
