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
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;
import org.seasar.maya.standard.engine.processor.ChildParamReciever;

/**
 * @author maruo_syunsuke
 */
public class UrlProcessor extends TemplateProcessorSupport implements ChildParamReciever{
    private String _value ;
    private String _context ;
    private String _var ;
    private int    _scope ;
    private Map    _childParam ;
    
    public void setVar(String var) {
        _var = var;
    }
    
    public void setScope(String scope) {
    	_scope = SpecificationUtil.getScopeFromString(scope);
    }
    
    public int doStartProcess(PageContext context) {
        return Tag.EVAL_BODY_INCLUDE;
    }
    
    public int doEndProcess(PageContext context) {
        HttpServletResponse httpServletResponse = (HttpServletResponse)context.getResponse();

        String sessionID      = context.getSession().getId();
        String unEncodeString = _value + ";jsessionid="+ sessionID + "?" + getParamString(); 
        String encodedString  = httpServletResponse.encodeURL(unEncodeString);

        ServletContext      servletContext      = context.getServletContext();
        if(StringUtil.isEmpty(_context) == false ){
            servletContext = servletContext.getContext(_context);
            
        }
        // TODO context‚Á‚Ä‚È‚ñ‚ÉŽg‚¤‚ñ‚¶‚á‚ë(?_?)
        outPutEncodedString(context, encodedString);
        return super.doEndProcess(context);
    }
    private void outPutEncodedString(PageContext context, String encodedString) {
        if( StringUtil.isEmpty( _var ) ){
	        Writer out = context.getOut();
	        try {
	            out.write(encodedString);
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
        }else{
            AttributeValue attributeValue = AttributeValueFactory.create(_var,_scope);
            attributeValue.setValue(context,encodedString);
        }
    }
    private String getParamString(){
        java.util.Iterator it = _childParam.keySet().iterator();
        String paramString = "" ;
        while (it.hasNext()) {
            String key = (String) it.next();
            paramString += key + "=" + _childParam.get(key) + "&" ;
        }
        return paramString.substring(0,paramString.length()-1) ;
    }
    public void addChildParam(String name, String value) {
        _childParam.put(name,value);
    }
}