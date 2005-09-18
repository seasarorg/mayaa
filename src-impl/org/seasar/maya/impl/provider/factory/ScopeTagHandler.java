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
package org.seasar.maya.impl.provider.factory;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.impl.util.XUtil;
import org.seasar.maya.provider.Parameterizable;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScopeTagHandler extends AbstractParameterizableTagHandler {
    
    private ScriptEnvirionmentTagHandler _parent;
    private AttributeScope _scope;
    
    public ScopeTagHandler(ScriptEnvirionmentTagHandler parent) {
        super("scope");
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }
    
    public void start(Attributes attributes) {
        _scope = (AttributeScope)XUtil.getObjectValue(
                attributes, "class", null, AttributeScope.class);
        _parent.getScriptEnvironment().addAttributeScope(_scope);
    }
    
    public void end(String body) {
        _scope = null;
    }
    
    public Parameterizable getParameterizable() {
        if(_scope == null) {
            throw new IllegalStateException();
        }
        return _scope;
    }
    
}
