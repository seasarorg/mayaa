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
package org.seasar.maya.impl.engine.processor;

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.impl.util.ScriptUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProcessorPropertyImpl implements ProcessorProperty {

    private QNameable _name;
    private CompiledScript _compiled;
    
    public ProcessorPropertyImpl(
            QNameable name, String value, Class expectedType) {
        if(name == null || expectedType == null) {
            throw new IllegalArgumentException();
        }
        _name = name;
        _compiled = ScriptUtil.compile(value, expectedType);
    }
    
    public QNameable getName() {
        return _name;
    }
    
    public CompiledScript getValue() {
        return _compiled;
    }
    
}
