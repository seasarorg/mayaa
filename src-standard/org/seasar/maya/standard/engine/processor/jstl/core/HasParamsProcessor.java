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
import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.standard.engine.processor.ChildParamReciever;
import org.seasar.maya.standard.engine.processor.ObjectAttributeUtil;

/**
 * @author maruo_syunsuke
 */
public abstract class HasParamsProcessor extends TemplateProcessorSupport 
        implements ChildParamReciever{
    
    private static final long   serialVersionUID = -6741423544407439357L;
    private static final String CHILD_PARAMS     = "childParams" ;
    
    public ProcessStatus doStartProcess(ServiceCycle cycle) {
    	initChildParam(cycle);
        return EVAL_BODY_INCLUDE;
    }

    protected abstract String getBaseURL();

	protected String getEncodedUrlString(ServiceCycle cycle) {
		String unEncodeString = getBaseURL() + getQueryString(cycle);
        String encodedString  = cycle.getResponse().encodeURL(unEncodeString);
		return encodedString;
	}
    
    private String getQueryString(ServiceCycle cycle){
    	Map childParamMap = getChildParamMap(cycle) ;
        Iterator it = childParamMap.entrySet().iterator();
        String paramString = "?";
        while (it.hasNext()) {
        	Map.Entry entry = (Map.Entry) it.next();
            paramString += entry.getKey() + "=" + entry.getValue() + "&" ;
        }
        return paramString.substring(0,paramString.length()-1) ;
    }
    
    public void addChildParam(ServiceCycle cycle, String name, String value) {
    	Map childParamMap = getChildParamMap(cycle);
    	childParamMap.put(name,value);
    }

	private Map getChildParamMap(ServiceCycle cycle) {
		return (Map)ObjectAttributeUtil.getAttribute(cycle,this,CHILD_PARAMS);
	}
	
	private void initChildParam(ServiceCycle cycle) {
		ObjectAttributeUtil.setAttribute(cycle,this,CHILD_PARAMS,new HashMap());
	}
}