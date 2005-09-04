/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.util;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptUtil implements CONST_IMPL {

	private ScriptUtil() {
	}
    
    public static ScriptEnvironment getScriptEnvironment() {
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        return provider.getScriptEnvironment();
    }
    
    public static void initScope() {
        getScriptEnvironment().initScope();
    }
    
    public static void startScope(Object model) {
        getScriptEnvironment().startScope(model);
    }
    
    public static void endScope() {
        getScriptEnvironment().endScope();
    }
    
    public static CompiledScript compile(String text, Class expectedType) {
        if(expectedType == null) {
        	throw new IllegalArgumentException();
        }
        if(StringUtil.hasValue(text)) {
	        ScriptEnvironment environment = getScriptEnvironment();
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            SpecificationNode node = cycle.getInjectedNode();
            CompiledScript compiled = environment.compile(
                    text, node.getSystemID(), node.getLineNumber());
            compiled.setExpectedType(expectedType);
            return compiled;
        }
        return null;
    }
    
    public static Object execute(Object obj) {
        if (obj instanceof CompiledScript) {
            CompiledScript script = (CompiledScript)obj;
            return script.execute();
        }
        return obj;
    }
    
    public static  void execEvent(Specification specification, QName eventName) {
        if(specification == null || eventName == null) {
            throw new IllegalArgumentException();
        }
        SpecificationNode maya = SpecificationUtil.getMayaNode(specification);
        if(maya != null) {
	        NodeAttribute attr = maya.getAttribute(eventName);
	        if(attr != null) {
	        	String text = attr.getValue();
	        	if(StringUtil.hasValue(text)) {
		            Object obj = ScriptUtil.compile(text, Void.class);
		            ScriptUtil.execute(obj);
	        	}
	        }
        }
    }
    
    public static Object convertFromScriptObject(Object scriptObject) {
        ScriptEnvironment env = getScriptEnvironment();
        return env.convertFromScriptObject(scriptObject);
    }

}
