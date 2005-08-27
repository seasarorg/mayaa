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
package org.seasar.maya.impl.builder.library;

import java.util.Iterator;

import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.builder.library.PropertyDefinition;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.engine.processor.JspCustomTagProcessor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspProcessorDefinition extends ProcessorDefinitionImpl {

    private Class _tagClass;

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
    
    protected TemplateProcessor newInstance(Template template, SpecificationNode injected) {
        if(_tagClass == null) {
            throw new IllegalStateException();
        }
        JspCustomTagProcessor processor = new JspCustomTagProcessor();
        processor.setTagClass(_tagClass);
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
