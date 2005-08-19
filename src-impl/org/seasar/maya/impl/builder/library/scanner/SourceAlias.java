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
package org.seasar.maya.impl.builder.library.scanner;

import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SourceAlias {

    public static String ALIAS = SourceAlias.class + ".ALIAS";
    
    private String _alias;
    private String _systemID;
    
    public SourceAlias(String alias, String systemID) {
        if(StringUtil.isEmpty(alias) || StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        _alias = alias;
        _systemID = systemID;
    }
    
    public String getAlias() {
        return _alias;
    }
    
    public String getSystemID() {
        return _systemID;
    }
    
}
