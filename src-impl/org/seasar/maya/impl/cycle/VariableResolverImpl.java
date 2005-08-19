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
package org.seasar.maya.impl.cycle;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.VariableResolver;

import org.seasar.maya.cycle.script.ScriptCompiler;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class VariableResolverImpl implements VariableResolver {

    private static VariableResolver _instance = new VariableResolverImpl();

    public static VariableResolver getInstance() {
        return _instance;
    }

    public Object resolveVariable(String pName) throws ELException {
        if(StringUtil.hasValue(pName)) {
            ServiceProvider provider = ProviderFactory.getServiceProvider();
            ScriptCompiler compiler = provider.getScriptCompiler();
            return compiler.getScriptResolver().getVariable(pName);
        }
        return null;
    }
    
}
