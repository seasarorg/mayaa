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
package org.seasar.maya.component.util;

import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ComponentUtil {
    
    private ComponentUtil() {
    }

    public static String[] parsePath(String path) {
        if(StringUtil.isEmpty(path)) {
            throw new IllegalArgumentException();
        }
        String[] pagePath = new String[2];
        int dotPos = path.lastIndexOf(".");
        if(dotPos > 0 && dotPos < path.length() - 1) {
            pagePath[0] = StringUtil.preparePath(path.substring(0, dotPos));
            pagePath[1] = path.substring(dotPos + 1);
        } else {
            pagePath[0] = StringUtil.preparePath(path);
            pagePath[1] = "";
        }
        return pagePath;
    }
    
}
