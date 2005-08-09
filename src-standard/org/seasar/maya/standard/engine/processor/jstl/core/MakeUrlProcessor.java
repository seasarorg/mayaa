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
import org.seasar.maya.impl.util.CycleUtil;

/**
 * @author maruo_syunsuke
 */
public abstract class MakeUrlProcessor extends HasParamsProcessor{
    
    private static final long serialVersionUID = -6741423544407439357L;

    protected abstract String getBaseURL();

	protected String getEncodedUrlString() {
		String unEncodeString = getBaseURL() + getQueryString();
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        String encodedString  = cycle.getResponse().encodeURL(unEncodeString);
		return encodedString;
	}
    
    private String getQueryString() {
    	Map childParamMap = getChildParamMap() ;
        Iterator it = childParamMap.entrySet().iterator();
        String paramString = "?";
        while (it.hasNext()) {
        	Map.Entry entry = (Map.Entry) it.next();
            paramString += entry.getKey() + "=" + entry.getValue() + "&" ;
        }
        return paramString.substring(0,paramString.length()-1) ;
    }
}