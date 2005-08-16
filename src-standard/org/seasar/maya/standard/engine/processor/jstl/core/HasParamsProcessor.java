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

import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.standard.engine.processor.ChildParamReciever;
import org.seasar.maya.standard.engine.processor.ObjectAttributeUtil;

/**
 * @author maruo_syunsuke
 */
public abstract class HasParamsProcessor extends TemplateProcessorSupport 
        implements ChildParamReciever{
    
    private static final long serialVersionUID = -6741423544407439357L;
    private static final String CHILD_PARAMS = "childParams";
    
    public ProcessStatus doStartProcess() {
    	initChildParam();
        return EVAL_BODY_INCLUDE;
    }

    public void addChildParam(String name, String value) {
    	Map childParamMap = getChildParamMap();
    	childParamMap.put(name,value);
    }

	protected Map getChildParamMap() {
		return (Map)ObjectAttributeUtil.getAttribute(this, CHILD_PARAMS);
	}
	
	private void initChildParam() {
		ObjectAttributeUtil.setAttribute(this, CHILD_PARAMS, new HashMap());
	}
}