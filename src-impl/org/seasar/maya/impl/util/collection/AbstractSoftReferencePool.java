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
package org.seasar.maya.impl.util.collection;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractSoftReferencePool implements Serializable {

    private List _pool = new ArrayList();

    protected abstract Object createObject();
    
    protected abstract boolean validateObject(Object obj);
    
    protected synchronized Object borrowObject() {
        Object obj = null;
        while(obj == null) {
            if(_pool.isEmpty()) {
                obj = createObject();
            } else {
                SoftReference ref = (SoftReference)_pool.remove(_pool.size() - 1);
                obj = ref.get();
            }
        }
        return obj;
    }
    
    protected void returnObject(Object obj) {
        boolean success = validateObject(obj);
        synchronized(this) {
            if(success) {
                _pool.add(new SoftReference(obj));
            }
            notifyAll();
        }
    }
    
}
