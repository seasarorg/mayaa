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
import java.util.List;

import org.seasar.maya.cycle.CycleWriter;
import org.seasar.maya.engine.processor.ChildEvaluationProcessor;
import org.seasar.maya.engine.processor.InformalPropertyAcceptable;
import org.seasar.maya.engine.processor.ProcessorProperty;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractAttributableProcessor
        extends TemplateProcessorSupport
		implements ChildEvaluationProcessor, InformalPropertyAcceptable {

    private boolean _childEvaluation;
    private List _attributes;
    private ThreadLocal _processtimeInfo = new ThreadLocal();

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
    public void addProcesstimeProperty(ProcessorProperty prop) {
        if(prop == null) {
            throw new IllegalArgumentException();
        }
        List list = getProcesstimeProperties();
        if(list.contains(prop) == false) {
            list.add(prop);
        }
    }
    
    public List getProcesstimeProperties() {
        ProcesstimeInfo info = getProcesstimeInfo();
        if(info._processtimeProperties == null) {
            info._processtimeProperties = new ArrayList();
        }
        return info._processtimeProperties;
    }
    
    protected abstract ProcessStatus writeStartElement();
    
    protected abstract void writeEndElement();
    
    public ProcessStatus doStartProcess() {
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
        info._body = body;
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
            if(info._body != null) {
            	try {
                    info._body.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        writeEndElement();
        return EVAL_PAGE;
    }

    //helper class, methods ----------------------------------------
    
    protected ProcesstimeInfo getProcesstimeInfo() {
        ProcesstimeInfo info = (ProcesstimeInfo)_processtimeInfo.get();
        if(info == null) {
            info = new ProcesstimeInfo();
            _processtimeInfo.set(info);
        }
        return info;
    }
    
    protected class ProcesstimeInfo {
        
        private CycleWriter _body; 
        private List _processtimeProperties;
        
    }
    
}
