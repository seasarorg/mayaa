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
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.standard.engine.processor.ChildParamReciever;

/**
 * @author maruo_syunsuke
 */
public class RedirectProcessor extends TemplateProcessorSupport implements ChildParamReciever{
    private String _url ;
//    private String _context ;
    private Map    _childParam ;
    
    public int doStartProcess(PageContext context) {
        return Tag.EVAL_BODY_INCLUDE;
    }
    
    public int doEndProcess(PageContext context) {
        HttpServletResponse httpServletResponse 
        		= (HttpServletResponse)context.getResponse();

        String sessionID      = context.getSession().getId();
        String paramString    = getParamString();
        String unEncodeString = _url + ";jsessionid="+ sessionID + "?" + paramString;
        String encodedString  = httpServletResponse.encodeURL(unEncodeString);

        try {
            httpServletResponse.sendRedirect(encodedString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return super.doEndProcess(context);
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