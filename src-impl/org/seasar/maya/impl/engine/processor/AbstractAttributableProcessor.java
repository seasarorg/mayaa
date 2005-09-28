/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.maya.impl.engine.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.cycle.CycleWriter;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.processor.ChildEvaluationProcessor;
import org.seasar.maya.engine.processor.InformalPropertyAcceptable;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractAttributableProcessor
        extends TemplateProcessorSupport
		implements ChildEvaluationProcessor, InformalPropertyAcceptable {

    private boolean _childEvaluation;
    private List _attributes;
    private ThreadLocal _processtimeInfo = new ThreadLocal();

    protected void clearProcesstimeInfo() {
        _processtimeInfo.set(null);
    }
    
    protected ProcesstimeInfo getProcesstimeInfo() {
        ProcesstimeInfo info = (ProcesstimeInfo)_processtimeInfo.get();
        if(info == null) {
            info = new ProcesstimeInfo();
            _processtimeInfo.set(info);
        }
        return info;
    }
    
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
    
    public Iterator iterateInformalProperties() {
        if(_attributes == null) {
            return NullIterator.getInstance();
        }
        return _attributes.iterator();
    }
    
    // processtime method
    public void addProcesstimeProperty(ProcessorProperty prop) {
        if(prop == null) {
            throw new IllegalArgumentException();
        }
        ProcesstimeInfo info = getProcesstimeInfo();
       	info.addProcesstimeProperty(prop);
    }
    
    public boolean hasProcesstimeProperty(ProcessorProperty prop) {
        if(prop == null) {
            throw new IllegalArgumentException();
        }
        ProcesstimeInfo info = getProcesstimeInfo();
        return info.hasProcesstimeProperty(prop);
    }
    
    public Iterator iterateProcesstimeProperties() {
        ProcesstimeInfo info = getProcesstimeInfo();
        return info.iterateProcesstimeProperties();
    }
    
    protected abstract ProcessStatus writeStartElement();
    
    protected abstract void writeEndElement();
    
    public ProcessStatus doStartProcess(Page topLevelPage) {
        clearProcesstimeInfo();
        if(_childEvaluation) {
            return EVAL_BODY_BUFFERED;
        }
        return writeStartElement();
    }
    
    public boolean isChildEvaluation() {
        return _childEvaluation;
    }
    
    public void setBodyContent(CycleWriter body) {
        if (body == null) {
            throw new IllegalArgumentException();
        }
        ProcesstimeInfo info = getProcesstimeInfo();
        info.setBody(body);
    }

    public void doInitChildProcess() {
    }
    
    public boolean isIteration() {
        return false;
    }
    
    public ProcessStatus doAfterChildProcess() {
        return SKIP_BODY;
    }
    
    public ProcessStatus doEndProcess() {
        ProcesstimeInfo info = getProcesstimeInfo();
        if(_childEvaluation) {
            writeStartElement();
            CycleWriter body = info.getBody();
            if(body != null) {
            	try {
                    body.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        writeEndElement();
        return EVAL_PAGE;
    }

    //helper class, methods ----------------------------------------
    
    protected class ProcesstimeInfo {
        
        private CycleWriter _body; 
        private List _processtimeProperties;
        
        public void setBody(CycleWriter body) {
            if(body == null) {
                throw new IllegalArgumentException();
            }
            _body = body;
        }
        
        public CycleWriter getBody() {
            return _body;
        }
        
        public boolean hasProcesstimeProperty(ProcessorProperty property) {
            if(property == null) {
                throw new IllegalArgumentException();
            }
            if(_processtimeProperties == null) {
                return false;
            }
            return _processtimeProperties.contains(property);
        }
        
        public void addProcesstimeProperty(ProcessorProperty property) {
            if(property == null) {
                throw new IllegalArgumentException();
            }
            if(_processtimeProperties == null) {
                _processtimeProperties = new ArrayList();
            }
            if(_processtimeProperties.contains(property) == false) {
                _processtimeProperties.add(property);
            }
        }
        
        public Iterator iterateProcesstimeProperties() {
            if(_processtimeProperties == null) {
                return NullIterator.getInstance();
            }
            return _processtimeProperties.iterator();
        }
        
    }
    
}
