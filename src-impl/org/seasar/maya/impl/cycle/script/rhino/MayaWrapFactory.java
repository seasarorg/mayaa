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
package org.seasar.maya.impl.cycle.script.rhino;

import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.seasar.maya.cycle.AttributeScope;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MayaWrapFactory extends WrapFactory {

    public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
            Object javaObject, Class staticType) {
        if(javaObject instanceof Map) {
            return new NativeJavaMap(scope, (Map)javaObject);
        } else if(javaObject instanceof List) {
            return new NativeJavaList(scope, (List)javaObject);
        } else if(javaObject instanceof AttributeScope &&
        		javaObject instanceof Scriptable == false) {
        	AttributeScope attrs = (AttributeScope)javaObject;
        	return new NativeJavaAttributeScope(scope, attrs);
        }
        return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
    }
    
}
