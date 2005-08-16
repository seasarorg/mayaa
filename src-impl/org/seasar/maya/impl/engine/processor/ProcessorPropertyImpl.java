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
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.impl.util.ScriptUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProcessorPropertyImpl implements ProcessorProperty {

    private QName _qName;
    private String _prefix;
    private Object _value;
    
    public ProcessorPropertyImpl(QName qName, String prefix, Object value) {
        if(qName == null || value == null) {
            throw new IllegalArgumentException();
        }
        _qName = qName;
        _prefix = prefix;
        _value = value;
    }
    
    public QName getQName() {
        return _qName;
    }
    
    public String getPrefix() {
        return _prefix;
    }
    
    public String getLiteral() {
        if(_value instanceof CompiledScript) {
            CompiledScript script = (CompiledScript)_value;
            return script.getText();
        }
        return String.valueOf(_value);
    }
    
    public boolean isDynamic() {
        return _value instanceof CompiledScript &&
        		((CompiledScript)_value).isLiteral() == false;
    }

    public Object getValue() {
        if(_value instanceof CompiledScript) {
            CompiledScript script = (CompiledScript)_value;
            return ScriptUtil.execute(script);
        }
        return _value;
    }
    
    public boolean equals(Object test) {
        if(test == null || (test instanceof ProcessorProperty) == false) {
            return false;
        }
        ProcessorProperty prop = (ProcessorProperty)test;
        return _qName.equals(prop.getQName());
    }
    
    public int hashCode() {
        return _qName.hashCode();
    }

    public String toString() {
        Object obj = getLiteral();
        return obj != null ? obj.toString() : "";
    }
    
}
