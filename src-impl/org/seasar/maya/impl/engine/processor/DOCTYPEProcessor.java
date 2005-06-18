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
package org.seasar.maya.impl.engine.processor;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DOCTYPEProcessor extends TemplateProcessorSupport {
    
    private String _name;
    private String _publicID;
    private String _systemID;

    public void setName(String name) {
        if(StringUtil.isEmpty(name)) {
        	throw new IllegalArgumentException();
        }
        _name = name;
    }
    
    public String getName() {
    	return _name;
    }
    
    public void setPublicID(String publicID) {
    	_publicID = publicID;
    }
    
    public String getPublicID() {
    	return _publicID;
    }
    
    public void setSystemID(String systemID) {
    	_systemID = systemID;
    }
    
    public String getSystemID() {
    	return _systemID;
    }
    
    public int doStartProcess(PageContext context) {
    	if(context == null) {
    		throw new IllegalArgumentException();
    	}
        Writer out = context.getOut();
        try {
	        StringBuffer docTypeDecl = new StringBuffer(128);
	        docTypeDecl.append("<!DOCTYPE ").append(_name);
	        if(StringUtil.hasValue(_publicID)) {
	            docTypeDecl.append(" PUBLIC \"").append(_publicID).append("\"");
	        }
	        if(StringUtil.hasValue(_systemID)) {
	            docTypeDecl.append(" SYSTEM \"").append(_systemID).append("\"");
	        }
	        docTypeDecl.append(">");
            out.write(docTypeDecl.toString());
        } catch(IOException e) {
        	throw new RuntimeException(e);
        }
        return Tag.SKIP_BODY;
    }

}
