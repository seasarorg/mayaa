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
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class ScopeTestTag extends TagSupport {

    private static final long serialVersionUID = -272720933733898885L;

    private String _atbegin;
    private String _nested;
    private String _atend;

    public int doStartTag() {
        pageContext.setAttribute(_atbegin, "at_begin", PageContext.PAGE_SCOPE);
        pageContext.setAttribute(_nested, "nested", PageContext.PAGE_SCOPE);
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() {
        pageContext.setAttribute(_atend, "at_end", PageContext.PAGE_SCOPE);
        pageContext.removeAttribute(_nested, PageContext.PAGE_SCOPE);
        return EVAL_PAGE;
    }

    public void setAtbegin(String atbegin) {
        _atbegin = atbegin;
    }

    public void setNested(String nested) {
        _nested = nested;
    }

    public void setAtend(String atend) {
        _atend = atend;
    }

    protected void prepareIndex(StringBuilder handlers, String name)
            throws JspException {
    }

}
