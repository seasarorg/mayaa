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
package org.seasar.maya.impl.engine.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.seasar.maya.cycle.CycleWriter;
import org.seasar.maya.cycle.ServiceCycle;
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
    public void addProcesstimeProperty(ServiceCycle cycle, ProcessorProperty prop) {
        if(cycle == null || prop == null) {
            throw new IllegalArgumentException();
        }
        List list = getProcesstimeProperties(cycle);
        if(list.contains(prop) == false) {
            list.add(prop);
        }
    }
    
    public List getProcesstimeProperties(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        ProcesstimeInfo info = getProcesstimeInfo(cycle);
        if(info._processtimeProperties == null) {
            info._processtimeProperties = new ArrayList();
        }
        return info._processtimeProperties;
    }
    
    protected abstract ProcessStatus writeStartElement(ServiceCycle cycle);
    
    protected abstract void writeEndElement(ServiceCycle cycle);
    
    public ProcessStatus doStartProcess(ServiceCycle cycle) {
        if(_childEvaluation) {
            return EVAL_BODY_BUFFERED;
        }
        return writeStartElement(cycle);
    }
    
    public ProcessStatus doEndProcess(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        ProcesstimeInfo info = getProcesstimeInfo(cycle);
        if(_childEvaluation) {
            writeStartElement(cycle);
            if(info._body != null) {
            	try {
                    info._body.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        writeEndElement(cycle);
        return EVAL_PAGE;
    }
    
    public boolean isIteration(ServiceCycle cycle) {
        return false;
    }
    
    public boolean isChildEvaluation(ServiceCycle cycle) {
        return _childEvaluation;
    }
    
    public void setBodyContent(ServiceCycle cycle, CycleWriter body) {
        if (cycle == null || body == null) {
            throw new IllegalArgumentException();
        }
        ProcesstimeInfo info = getProcesstimeInfo(cycle);
        info._body = body;
    }

    public void doInitChildProcess(ServiceCycle cycle) {
    }
    
    public ProcessStatus doAfterChildProcess(ServiceCycle cycle) {
        return SKIP_BODY;
    }

    //helper class, methods ----------------------------------------
    
    private String getRuntimeKey() {
        return hashCode() + "@" + AbstractAttributableProcessor.class + ".RuntimeInfo";
    }
    
    protected ProcesstimeInfo getProcesstimeInfo(ServiceCycle cycle) {
        String key = getRuntimeKey();
        ProcesstimeInfo info = (ProcesstimeInfo)cycle.getAttribute(key);
        if(info == null) {
            info = new ProcesstimeInfo();
            cycle.setAttribute(key, info);
        }
        return info;
    }
    
    protected void removeProcesstimeInfo(ServiceCycle cycle) {
        cycle.setAttribute(getRuntimeKey(), null);
    }
    
    protected class ProcesstimeInfo {
        
        private CycleWriter _body; 
        private List _processtimeProperties;
        
    }
    
}
