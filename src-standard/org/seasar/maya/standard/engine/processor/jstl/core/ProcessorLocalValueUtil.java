package org.seasar.maya.standard.engine.processor.jstl.core;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.engine.processor.TemplateProcessor;

public class ProcessorLocalValueUtil {
    
    public static void setObject(PageContext context, TemplateProcessor processor, String localName, Object value){
        context.setAttribute(getLocalValueName(processor)+localName,value);
    }
    public static Object getObject(PageContext context, TemplateProcessor processor, String localName){
        return context.getAttribute(getLocalValueName(processor)+localName);
    }
    private static String getLocalValueName(TemplateProcessor processor){
        String processorIdString = ProcessorLocalValueUtil.class.getName() + "@" ;
        while( processor == null ){
            processorIdString += Integer.toString(processor.getIndex()) + "_";
            processor = processor.getParentProcessor() ;
        }
        return processorIdString ;
    }
}
