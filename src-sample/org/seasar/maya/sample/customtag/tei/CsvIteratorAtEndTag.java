package org.seasar.maya.sample.customtag.tei;

import java.util.Arrays;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class CsvIteratorAtEndTag extends BodyTagSupport {
    public CsvIteratorAtEndTag() {
        super();
    }

    public int doAfterBody() throws JspTagException {
        BodyContent context = getBodyContent();
        String body = context.getString();
        context.clearBody();
        try {
            String[] array = body.split(",");

            pageContext.setAttribute(id,
                    new IteratorHolder(Arrays.asList(array).iterator()));
        } catch (Exception e) {
            throw new JspTagException(e.getMessage());
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    public void release() {
    }
}
