/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class IteratorEnumeration implements Enumeration {

    private Iterator _iterator;
    
    public IteratorEnumeration(Iterator iterator) {
        if(iterator == null) {
            throw new IllegalArgumentException();
        }
        _iterator = iterator;
    }
    
    public boolean hasMoreElements() {
        return _iterator.hasNext();
    }
    
    public Object nextElement() {
        return _iterator.next();
    }
    
}
