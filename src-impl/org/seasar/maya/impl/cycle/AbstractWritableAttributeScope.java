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

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractWritableAttributeScope
        implements AttributeScope {

    public boolean isAttributeWritable() {
        return true;
    }

    public Object newAttribute(String name, Class attributeType) {
        if(attributeType == null) {
            throw new IllegalArgumentException();
        }
        if(StringUtil.isEmpty(name)) {
            name = attributeType.getName();
        }
        if(hasAttribute(name)) {
            return getAttribute(name); 
        }
        Object model = ObjectUtil.newInstance(attributeType);
        setAttribute(name, model);
        return model;
    }

}
