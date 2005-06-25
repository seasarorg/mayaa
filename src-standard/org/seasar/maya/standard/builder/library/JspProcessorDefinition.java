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
package org.seasar.maya.standard.builder.library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;

import org.seasar.maya.builder.library.PropertyDefinition;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.builder.library.ProcessorDefinitionImpl;
import org.seasar.maya.standard.engine.processor.jsp.JspCustomTagProcessor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspProcessorDefinition extends ProcessorDefinitionImpl {

    private Class _tagClass;
    private List _tagVariableInfo;
    private TagExtraInfo _tei;

    public void setClassName(String className) {
        throw new UnsupportedOperationException();
    }
    
    public String getClassName() {
        return JspCustomTagProcessor.class.getName();
    }
    
    public void setTagClass(Class tagClass) {
        if(tagClass == null || Tag.class.isAssignableFrom(tagClass) == false) {
            throw new IllegalArgumentException();
        }
        _tagClass = tagClass;
    }
    
    public Class getTagClass() {
        return _tagClass;
    }
    
    public void setTEI(TagExtraInfo tei) {
        // TODO TagExtraInfo#setTagInfo(TagInfo)にて、TagInfoをセットする必要があるか？
        _tei = tei;
    }
    
    public TagExtraInfo getTEI() {
        return _tei;
    }

    public void addTagVariableInfo(TagVariableInfo tagVariableInfo) {
        if(tagVariableInfo == null) {
            throw new IllegalArgumentException();
        }
        if(_tagVariableInfo == null) {
            _tagVariableInfo = new ArrayList();
        }
        _tagVariableInfo.add(tagVariableInfo);
    }
    
    public Iterator iterateTagVariableInfo() {
        return _tagVariableInfo.iterator();
    }
    
    protected TemplateProcessor newInstance(Template template, SpecificationNode injected) {
        if(_tagClass == null) {
            throw new IllegalStateException();
        }
        JspCustomTagProcessor processor = new JspCustomTagProcessor();
        processor.setTagClass(_tagClass);
        processor.setTEI(_tei);
        processor.setTagVariableInfo(_tagVariableInfo);
        return processor;
    }
    
    protected void settingProperties(
            SpecificationNode injected, TemplateProcessor processor) {
        for(Iterator it = iteratePropertyDefinition(); it.hasNext(); ) {
            PropertyDefinition property = (PropertyDefinition)it.next();
            Object prop = property.getProcessorProperty(injected, processor);
            if(prop != null) {
    	        JspCustomTagProcessor jspProcessor = (JspCustomTagProcessor)processor;
    	        jspProcessor.addProcessorProperty((ProcessorProperty)prop);
            }
        }
    }

    
}
