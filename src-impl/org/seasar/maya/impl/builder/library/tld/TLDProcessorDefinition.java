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
package org.seasar.maya.impl.builder.library.tld;

import java.util.Iterator;

import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.builder.library.PropertyDefinition;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.builder.library.ProcessorDefinitionImpl;
import org.seasar.maya.impl.engine.processor.JspCustomTagProcessor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TLDProcessorDefinition extends ProcessorDefinitionImpl {

    private Class _tagClass;

    public void setProcessorClass(Class processorClass) {
        if(processorClass == null || 
                Tag.class.isAssignableFrom(processorClass) == false) {
            throw new IllegalArgumentException();
        }
        _tagClass = processorClass;
    }

    public Class getProcessorClass() {
        if(_tagClass == null) {
            throw new IllegalStateException();
        }
        return _tagClass;
    }

    protected TemplateProcessor newInstance(SpecificationNode injected) {
        JspCustomTagProcessor processor = new JspCustomTagProcessor();
        processor.setTagClass(getProcessorClass());
        return processor;
    }
    
    protected void settingProperties(
            SpecificationNode injected, TemplateProcessor processor) {
        for(Iterator it = iteratePropertyDefinition(); it.hasNext(); ) {
            PropertyDefinition property = (PropertyDefinition)it.next();
            Object prop = property.createProcessorProperty(injected);
            if(prop != null) {
    	        JspCustomTagProcessor jsp =
                    (JspCustomTagProcessor)processor;
    	        jsp.addProcessorProperty((ProcessorProperty)prop);
            }
        }
    }
    
}
