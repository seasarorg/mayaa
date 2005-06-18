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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.ChildEvaluationProcessor;
import org.seasar.maya.engine.processor.InformalPropertyAcceptable;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractAttributableProcessor extends TemplateProcessorSupport
		implements ChildEvaluationProcessor, InformalPropertyAcceptable {

    private boolean _childEvaluation;
    private List _attributes;

    // MLD property
    public void setChildEvaluation(boolean childEvaluation) {
        _childEvaluation = childEvaluation;
    }

    // MLD method
    public void addInformalProperty(ProcessorProperty attr) {
        if(_attributes == null) {
            _attributes = new ArrayList();
        }
        _attributes.add(attr);
    }
    
    public List getInformalProperties() {
        if(_attributes == null) {
            _attributes = new ArrayList();
        }
        return _attributes;
    }
    
    // processtime method
    public void addProcesstimeProperty(PageContext context, ProcessorProperty prop) {
        if(context == null || prop == null) {
            throw new IllegalArgumentException();
        }
        List list = getProcesstimeProperties(context);
        if(list.contains(prop) == false) {
            list.add(prop);
        }
    }
    
    public List getProcesstimeProperties(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        ProcesstimeInfo info = getProcesstimeInfo(context);
        if(info._processtimeProperties == null) {
            info._processtimeProperties = new ArrayList();
        }
        return info._processtimeProperties;
    }
    
    protected abstract void writeStartElement(PageContext context);
    
    protected abstract void writeEndElement(PageContext context);
    
    public int doStartProcess(PageContext context) {
        if(_childEvaluation) {
            return BodyTag.EVAL_BODY_BUFFERED;
        }
        writeStartElement(context);
        return Tag.EVAL_BODY_INCLUDE;
    }
    
    public int doEndProcess(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        Writer out = context.getOut();
        ProcesstimeInfo info = getProcesstimeInfo(context);
        if(_childEvaluation) {
            writeStartElement(context);
            if(info._bodyContent != null) {
                try {
	            	out.write(info._bodyContent.getString());
	            } catch(IOException e) {
	            	throw new RuntimeException(e);
	            }
            }
        }
        writeEndElement(context);
        return Tag.EVAL_PAGE;
    }
    
    public boolean isIteration(PageContext context) {
        return false;
    }
    
    public boolean isChildEvaluation(PageContext context) {
        return _childEvaluation;
    }
    
    public void setBodyContent(PageContext context, BodyContent bodyContent) {
        if (context == null || bodyContent == null) {
            throw new IllegalArgumentException();
        }
        ProcesstimeInfo info = getProcesstimeInfo(context);
        info._bodyContent = bodyContent;
    }

    public void doInitChildProcess(PageContext context) {
    }
    
    public int doAfterChildProcess(PageContext context) {
        return Tag.SKIP_BODY;
    }

    //helper class, methods ----------------------------------------
    
    private String getRuntimeKey() {
        return hashCode() + "@" + AbstractAttributableProcessor.class + ".RuntimeInfo";
    }
    
    protected ProcesstimeInfo getProcesstimeInfo(PageContext context) {
        String key = getRuntimeKey();
        ProcesstimeInfo info = (ProcesstimeInfo)context.getAttribute(key);
        if(info == null) {
            info = new ProcesstimeInfo();
            context.setAttribute(key, info);
        }
        return info;
    }
    
    protected void removeProcesstimeInfo(PageContext context) {
        context.removeAttribute(getRuntimeKey());
    }
    
    protected class ProcesstimeInfo {
        
        private BodyContent _bodyContent; 
        private List _processtimeProperties;
        
    }
    
}
