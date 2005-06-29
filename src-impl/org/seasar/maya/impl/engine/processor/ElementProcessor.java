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
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.cyberneko.html.HTMLElements;
import org.seasar.maya.engine.processor.InformalPropertyAcceptable;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ElementProcessor extends AbstractAttributableProcessor
		implements InformalPropertyAcceptable, CONST_IMPL {

	private static final long serialVersionUID = 923306412062075314L;

	private QName _qName;
    private String _prefix;
    private boolean _duplicated;

    // exported property
    public boolean isDuplicated() {
    	return _duplicated;
    }
    
    // MLD property
    public void setDuplicated(boolean duplicated) {
        _duplicated = duplicated;
    }
    
    public String getInformalAttrituteURI() {
        if(_qName == null) {
            throw new IllegalStateException();
        }
        return _qName.getNamespaceURI();
    }
    
    // Factory property
    public void setQName(QName qName) {
        if(qName == null) {
            throw new IllegalArgumentException();
        }
        _qName = qName;
    }
    
    // Factory property
    public void setPrefix(String prefix) {
        _prefix = prefix;
    }
    
    private boolean isXHTML(QName qName) {
        String namespaceURI = qName.getNamespaceURI();
        return URI_XHTML.equals(namespaceURI);
    }
    
    private boolean needsCloseElement(QName qName) {
        if(isXHTML(qName)) {
            return true;
        }
        String localName = qName.getLocalName();
        HTMLElements.Element element = HTMLElements.getElement(localName);
        return element.isEmpty() == false;
    }
    
    private void appendAttributeString(
            PageContext context, StringBuffer buffer, ProcessorProperty prop) {
        buffer.append(" ");
        String attrPrefix = prop.getPrefix();
        if(StringUtil.hasValue(attrPrefix)) {
            buffer.append(attrPrefix).append(":");
        }
        buffer.append(prop.getQName().getLocalName());
        buffer.append("=\"").append(prop.getValue(context)).append("\"");
    }
    
    private void write(PageContext context, StringBuffer buffer) {
        Writer out = context.getOut();
        try {
        	out.write(buffer.toString());
        } catch(IOException e) {
        	throw new RuntimeException(e);
        }
    }
    
    protected int writeStartElement(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        if(_qName == null) {
            throw new IllegalStateException();
        }
        StringBuffer buffer = new StringBuffer("<");
        if(StringUtil.hasValue(_prefix)) {
            buffer.append(_prefix).append(":");
        }
        buffer.append(_qName.getLocalName());
        List additionalAttributes = getProcesstimeProperties(context);
        for(Iterator it = additionalAttributes.iterator(); it.hasNext(); ) {
            ProcessorProperty prop = (ProcessorProperty)it.next();
            QName propQName = prop.getQName(); 
            if(_duplicated && (QH_ID.equals(propQName) || QX_ID.equals(propQName))) {
                continue;
            }
            appendAttributeString(context, buffer, prop);
        }
        for(Iterator it = getInformalProperties().iterator(); it.hasNext(); ) {
            ProcessorProperty prop = (ProcessorProperty)it.next();
            if(additionalAttributes.contains(prop) == false) {
                appendAttributeString(context, buffer, prop);
            }
        }
        if(isXHTML(_qName) && getChildProcessorSize() == 0) {
            buffer.append("/>");
        } else {
            buffer.append(">");
        }
        write(context, buffer);
        return Tag.EVAL_BODY_INCLUDE;
    }
    
    protected void writeEndElement(PageContext context) {
        if(needsCloseElement(_qName)) {
	        StringBuffer buffer = new StringBuffer("</");
	        if(StringUtil.hasValue(_prefix)) {
	            buffer.append(_prefix).append(":");
	        }
	        buffer.append(_qName.getLocalName()).append(">");
	        write(context, buffer);
        }
        removeProcesstimeInfo(context);
    }
    
}
