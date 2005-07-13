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
package org.seasar.maya.impl.cycle.implicit;

import java.util.Iterator;

import org.seasar.maya.cycle.Request;
import org.seasar.maya.impl.util.collection.AbstractAttributeMap;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class HeaderValuesMap extends AbstractAttributeMap {
    
    private final Request _request;

    public HeaderValuesMap(Request request) {
        if(request == null) {
            throw new IllegalArgumentException();
        }
        _request = request;
    }

    protected Object getAttribute(String key) {
        return _request.getHeaderValues(key);
    }

    protected void setAttribute(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    protected Iterator getAttributeNames() {
        return _request.iterateHeaderNames();
    }
    
}