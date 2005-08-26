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
package org.seasar.maya.impl.cycle.script.resolver;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.script.resolver.ScriptResolver;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.impl.cycle.implicit.ParamMap;
import org.seasar.maya.impl.cycle.script.PropertyNotWritableException;
import org.seasar.maya.impl.engine.processor.ComponentPageProcessor;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ComponentBindingScriptResolver implements ScriptResolver {
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(name);
    }

    public Object getVariable(String name) {
		if ("binding".equals(name)) {
			Template template = SpecificationUtil.getTemplate();
			TemplateProcessor parent = template.getParentProcessor();
			if (parent == null) {
				return new ParamMap();
			} else if (parent instanceof ComponentPageProcessor) {
				ComponentPageProcessor processor = 
					(ComponentPageProcessor) parent;
				return new ComponentBindingMap(processor);
			} else {
				throw new IllegalStateException();
			}
		}
		return AttributeScope.UNDEFINED;
	}

    public boolean setVariable(String name, Object value) {
    	if ("binding".equals(name)) {
    		throw new PropertyNotWritableException(name);
    	}
        return false;
    }

}