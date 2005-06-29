package org.seasar.maya.sample.customtag.tei;

import java.util.Arrays;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

public class CsvIteratorNestedTag extends TagSupport {

	private static final long serialVersionUID = -2581842395447865159L;

	public CsvIteratorNestedTag() {
        super();
    }

    private String value;

    public int doStartTag() throws JspTagException {
        if (value != null) {
            try {
                String[] array = value.split(",");
    
                pageContext.setAttribute(id,
                        new IteratorHolder(Arrays.asList(array).iterator()));
            } catch (Exception e) {
                throw new JspTagException(e.getMessage());
            }
        }
        return EVAL_BODY_INCLUDE;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    public void release() {
    }
}
