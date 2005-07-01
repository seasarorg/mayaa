package org.seasar.maya.standard.engine.processor.jstl.fmt;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;
import org.seasar.maya.standard.engine.processor.jstl.ProcessorPropertyString;

/**
 * @author maruo_syunsuke
 */
public class RequestEncodingProcessor extends AbstractBodyProcessor {

    public void setValue(String var) {
        if(StringUtil.isEmpty(var)) {
            throw new IllegalArgumentException();
        }
        super.setValue(new ProcessorPropertyString( var ));
    }
    
    public void setValue(ProcessorProperty value) {
        super.setValue(value);
    }
    
    public int process(PageContext context, Object obj) {
        HttpServletRequest httpServletRequest = (HttpServletRequest)context.getRequest();
        try {
            httpServletRequest.setCharacterEncoding(obj.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return Tag.SKIP_BODY;
    }

}