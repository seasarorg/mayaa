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
package org.seasar.maya.impl.cycle.script;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptUtil {

    private ScriptUtil() {
    }

    public static ScriptEnvironment getScriptEnvironment() {
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        return provider.getScriptEnvironment();
    }

    public static CompiledScript compile(String text, Class expectedType) {
        if(expectedType == null) {
        	throw new IllegalArgumentException();
        }
        CompiledScript compiled;
        if(StringUtil.hasValue(text)) {
            ScriptEnvironment environment = getScriptEnvironment();
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            SpecificationNode node = cycle.getInjectedNode();
            compiled = environment.compile(
                    text, node.getSystemID(), node.getLineNumber());
        } else {
            compiled = new NullScript();
        }
        compiled.setExpectedType(expectedType);
        return compiled;
    }

    
    
}
