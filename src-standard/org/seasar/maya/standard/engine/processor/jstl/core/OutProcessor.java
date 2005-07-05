/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.standard.engine.processor.jstl.core;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.cyberneko.html.HTMLEntities;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.standard.engine.processor.AbstractBodyTextProcessor;

/**
 * @author maruo_syunsuke
 */
public class OutProcessor extends AbstractBodyTextProcessor {

    private ProcessorProperty _value ;
    private ProcessorProperty _defaultValue ;
    private ProcessorProperty _escapeXml;

    protected int process(PageContext context,String bodyString) {
        Writer out = context.getOut();
        String outputString = getOutputString(context,bodyString);
        try {
            out.write(escapeXml(context, outputString));
            return Tag.EVAL_PAGE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getOutputString(PageContext context, String bodyString) {
        if( _value != null ){
            return _value.getValue(context).toString() ;
        }
        if( _defaultValue != null ){
            return _defaultValue.getLiteral();
        }
        if( bodyString != null ){
            return bodyString ;
        }
        return "" ;
    }

    private String escapeXml(PageContext context, Object obj) {
        String plainString = String.valueOf(obj);
        if(isEscape(context)) {
            return buildEscapeString(plainString);
        }
        return plainString;
    }

    private String buildEscapeString(String srcString) {
        String buffer = "" ;
        for(int i = 0; i < srcString.length(); i++) {
            char partCharOfSrcString = srcString.charAt(i);
            buffer += convertCharToEscapedString(partCharOfSrcString);
        }
        return buffer;
    }
    private String convertCharToEscapedString(char srcChar){
        String name = HTMLEntities.get(srcChar);
        if(name != null) {
            return "&" + name + ";";
        }
        return String.valueOf(srcChar);
    }
    private boolean isEscape(PageContext context) {
        return getBoolean(context, _escapeXml);
    }
    private boolean getBoolean(PageContext context, ProcessorProperty value) {
        if(value == null) {
            return false;
        }
        Object obj = value.getValue(context);
        return ObjectUtil.booleanValue(obj, false);
    }

    public void setValue(ProcessorProperty value) {
        _value = value ;
    }
    public void setDefault(ProcessorProperty defaultValue) {
        _defaultValue = defaultValue ;
    }
    public void setEscapeXml(ProcessorProperty escapeXml) {
        _escapeXml = escapeXml;
    }
}
