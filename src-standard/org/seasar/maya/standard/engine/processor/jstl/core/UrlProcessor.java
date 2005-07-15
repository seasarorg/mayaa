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

import java.util.HashMap;
import java.util.Map;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;
import org.seasar.maya.standard.engine.processor.ChildParamReciever;

/**
 * @author maruo_syunsuke
 */
public class UrlProcessor extends TemplateProcessorSupport
        implements ChildParamReciever{
    
    private static final long serialVersionUID = 6419793385551077813L;

    private String _value ;
    private String _var ;
    private String _scope ;
    private Map _childParam ;
    
    public void setVar(String var) {
        _var = var;
    }
    
    public void setScope(String scope) {
    	_scope = scope;
    }
    
    public int doEndProcess(ServiceCycle cycle) {
        String unEncodeString = _value + "?" + getParamString(); 
        String encodedString  = cycle.encodeURL(unEncodeString);
        outPutEncodedString(cycle, encodedString);
        return super.doEndProcess(cycle);
    }
    
    private void outPutEncodedString(ServiceCycle cycle, String encodedString) {
        if( StringUtil.isEmpty( _var ) ){
            cycle.getResponse().write(encodedString);
        } else {
            AttributeValue attributeValue = AttributeValueFactory.create(_var, _scope);
            attributeValue.setValue(cycle, encodedString);
        }
    }
    
    private String getParamString() {
        java.util.Iterator it = _childParam.keySet().iterator();
        String paramString = "";
        while (it.hasNext()) {
            String key = (String) it.next();
            paramString += key + "=" + _childParam.get(key) + "&" ;
        }
        return paramString.substring(0, paramString.length() - 1) ;
    }
    
    public void addChildParam(String name, String value) {
        if(_childParam == null) {
            _childParam = new HashMap();
        }
        _childParam.put(name, value);
    }

}