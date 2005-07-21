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

import javax.servlet.jsp.JspFactory;

import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.impl.builder.library.LibraryDefinitionImpl;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspLibraryDefinition extends LibraryDefinitionImpl {

    private static String VERSION_JSP ;
    static {
        JspFactory factory = JspFactory.getDefaultFactory();
        if(factory != null) {
            VERSION_JSP = factory.getEngineInfo().getSpecificationVersion();
        } else {
            VERSION_JSP = "1.1";
        }
    }
    
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

    public ProcessorDefinition getProcessorDefinition(String localName) {
        if(_requiredVersion != null && VERSION_JSP.compareTo(_requiredVersion) < 0) {
            return null;
        }
        return super.getProcessorDefinition(localName);
    }
    
}
