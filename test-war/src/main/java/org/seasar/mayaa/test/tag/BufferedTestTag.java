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

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class BufferedTestTag extends BodyTagSupport {

    private static final long serialVersionUID = 9192585525665925369L;

    public int doStartTag() throws JspException {
        pageContext.setAttribute("foo", "bar", PageContext.PAGE_SCOPE);
        return EVAL_BODY_BUFFERED;
    }

    public int doAfterBody() throws JspException {
        if (bodyContent != null) {
            JspWriter writer = pageContext.getOut();
            if (writer instanceof BodyContent) {
                writer = ((BodyContent) writer).getEnclosingWriter();
            }
            try {
                writer.print(bodyContent.getString().replaceAll("original", "replaced"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            bodyContent.clearBody();
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

}
