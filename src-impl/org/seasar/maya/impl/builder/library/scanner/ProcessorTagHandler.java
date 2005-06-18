/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.builder.library.scanner;

import org.seasar.maya.impl.builder.library.LibraryDefinitionImpl;
import org.seasar.maya.impl.builder.library.ProcessorDefinitionImpl;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProcessorTagHandler extends TagHandler {

    private LibraryTagHandler _parent;
    
    private ProcessorDefinitionImpl _processorDefinition;
    
    public ProcessorTagHandler(LibraryTagHandler parent) {
        _parent = parent;
        putHandler("property", new PropertyTagHandler(this));
    }
    
    protected void start(Attributes attributes) {
        String name = attributes.getValue("name");
        String className = attributes.getValue("class");
        if(StringUtil.isEmpty(name) || StringUtil.isEmpty(className)) {
            // TODO ÉçÉOèoóÕÅB
            invalidate();
        } else {
	        _processorDefinition = new ProcessorDefinitionImpl();
	        _processorDefinition.setName(name);
	        _processorDefinition.setClassName(className);
	        LibraryDefinitionImpl library = _parent.getLibraryDefinition();
	        _processorDefinition.setLibraryDefinition(library);
	        library.addProcessorDefinition(_processorDefinition);
        }
    }
    
    protected void end(String body) {
        _processorDefinition = null;
    }

    public ProcessorDefinitionImpl getProcessorDefinition() {
        if(_processorDefinition == null) {
            throw new IllegalStateException();
        }
        return _processorDefinition;
    }
    
}
