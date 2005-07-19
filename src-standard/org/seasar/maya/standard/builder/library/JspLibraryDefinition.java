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
package org.seasar.maya.standard.builder.library;

import org.seasar.maya.impl.builder.library.LibraryDefinitionImpl;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspLibraryDefinition extends LibraryDefinitionImpl {

    private String _requiredVersion;
    
    public void setRequiredVersion(String requiredVersion) {
        if(StringUtil.isEmpty(requiredVersion)) {
            throw new IllegalArgumentException();
        }
        _requiredVersion = requiredVersion;
    }
    
    public String getRequiredVersion() {
        return _requiredVersion;
    }
    
}
