/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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

import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.standard.engine.processor.ChildParamReciever;

/**
 * @author maruo_syunsuke
 */
public class RedirectProcessor extends TemplateProcessorSupport 
        implements ChildParamReciever{
    
    private static final long serialVersionUID = -6741423544407439357L;

    private String _url ;
    private Map    _childParam ;
    
    public ProcessStatus doStartProcess(ServiceCycle cycle) {
        return EVAL_BODY_INCLUDE;
    }
    
    public ProcessStatus doEndProcess(ServiceCycle cycle) {
        String paramString    = getParamString();
        String unEncodeString = _url + "?" + paramString;
        String encodedString  = cycle.getResponse().encodeURL(unEncodeString);
        cycle.redirect(encodedString);
        return SKIP_BODY;
    }
    
    private String getParamString(){
        Iterator it = _childParam.keySet().iterator();
        String paramString = "";
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