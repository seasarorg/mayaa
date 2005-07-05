/*
 * Copyright (c) 2004 the Seasar Project and the Others.
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
package org.seasar.maya.standard.engine.processor.jstl.core;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.beanutils.PropertyUtils;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;

/**
 * @author maruo_syunsuke
 */
public class SetPropertyProcessor extends AbstractBodyProcessor {

    private ProcessorProperty _target; 
    private ProcessorProperty _property; 

    public void setTarget(ProcessorProperty target) {
        if(target == null) {
            throw new IllegalArgumentException();
        }
        _target = target;
    }
    
    public void setProperty(ProcessorProperty property) {
        if(property == null) {
            throw new IllegalArgumentException();
        }
        _property = property;
    }
    
    public void setValue(ProcessorProperty value) {
        super.setValue(value);
    }
    
    public int process(PageContext context, Object obj) {
        preProcess(context);
        try {
            mainProcess(context, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Tag.EVAL_PAGE;
    }

    private void preProcess(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        if(_target == null || _property == null) {
            throw new IllegalStateException();
        }
    }
    private void mainProcess(PageContext context, Object obj) 
    									throws 	IllegalAccessException, 
    											InvocationTargetException, 
    											NoSuchMethodException {
        Object bean = _target.getValue(context);
        if(bean == null) return ;
        
        Object prop = _property.getValue(context);
        if(prop == null) return ;
        
        PropertyUtils.setProperty(bean, prop.toString(), obj);
    }

}