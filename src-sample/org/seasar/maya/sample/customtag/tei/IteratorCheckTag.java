package org.seasar.maya.sample.customtag.tei;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

public class IteratorCheckTag extends TagSupport {
    IteratorHolder _iterator;

    public IteratorCheckTag() {
        super();
    }

    public int doEndTag() throws JspTagException {
        _iterator = (IteratorHolder) pageContext.getAttribute(id);
        try {
            if (_iterator == null) {
                pageContext.getOut().write("null");
            } else if (_iterator.hasNext()) {
                pageContext.getOut().write("available");
            } else {
                pageContext.getOut().write("not available");
            }
        } catch (IOException e) {
            throw new JspTagException(e.getMessage());
        }
        return EVAL_PAGE;
    }

    public void release() {
        _iterator = null;
    }
}
