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

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NativeJavaList extends NativeJavaObject {

    private static final long serialVersionUID = -1963435161453782180L;
    private List _list;
    
    public NativeJavaList(Scriptable scope, List list) {
        super(scope, list, List.class);
        if(list == null) {
            throw new IllegalArgumentException();
        }
        _list = list;
    }
    
    public boolean has(int index, Scriptable start) {
        return 0 <= index && index < _list.size();
    }

    public Object get(int index, Scriptable start) {
        if(0 <= index && index < _list.size()) {
            return _list.get(index);
        }
        return Undefined.instance;
    }

    public void put(int index, Scriptable start, Object value) {
        if(index == _list.size()) {
            _list.add(value);
        } else if(0 < index) {
            for(int i = _list.size(); i <= index; i++) {
                _list.add(Undefined.instance);
            }
            _list.set(index, value);
        }
    }    

    public Object[] getIds() {
        int listSize = _list.size();
        Object[] ids = super.getIds();
        Object[] ret = new Object[listSize + ids.length];
        for(int i = 0; i < listSize; i++) {
            ret[i] = new Integer(i);
        }
        for(int i = 0; i < ids.length; i++) {
            ret[i + listSize] = ids[i];
        }
        return ret;
    }

    public String getClassName() {
        return "javaList";
    }

}
