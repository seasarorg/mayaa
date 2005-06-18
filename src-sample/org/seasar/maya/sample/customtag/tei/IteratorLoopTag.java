package org.seasar.maya.sample.customtag.tei;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class IteratorLoopTag extends BodyTagSupport {
    IteratorHolder _iterator;

    public IteratorLoopTag() {
        super();
    }

    public int doStartTag() throws JspTagException {
        _iterator = (IteratorHolder) pageContext.getAttribute(id);
        try {
            if (_iterator.hasNext()) {
                return EVAL_BODY_AGAIN;
            }
            return SKIP_BODY;
        } catch (Exception e) {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }
    }

    public int doAfterBody() throws JspTagException {
        BodyContent body = getBodyContent();
        try {
            body.writeOut(getPreviousOut());
        } catch (IOException e) {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }
        body.clearBody();

        if (_iterator.hasNext()) {
            return EVAL_BODY_AGAIN;
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    public void release() {
        _iterator = null;
    }
}
