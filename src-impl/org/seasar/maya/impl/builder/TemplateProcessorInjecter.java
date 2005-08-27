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
package org.seasar.maya.impl.builder;

import java.util.Iterator;
import java.util.Stack;

import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.builder.specification.InjectionResolver;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.specification.DefaultInjectionChain;
import org.seasar.maya.impl.engine.processor.AttributeProcessor;
import org.seasar.maya.impl.engine.processor.CharactersProcessor;
import org.seasar.maya.impl.engine.processor.DoBodyProcessor;
import org.seasar.maya.impl.engine.processor.ElementProcessor;
import org.seasar.maya.impl.engine.specification.SpecificationNodeImpl;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateProcessorInjecter implements CONST_IMPL {

    private InjectionResolver _injectionResolver;
    private LibraryManager _libraryManger;

    public TemplateProcessorInjecter(
            InjectionResolver injectionResolver, LibraryManager libraryManager) {
        if(injectionResolver == null || libraryManager == null) {
            throw new IllegalArgumentException();
        }
        _injectionResolver = injectionResolver;
        _libraryManger = libraryManager;
    }
    
    private void saveToCycle(SpecificationNode node) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setCurrentNode(node);
    }
	
	private TemplateProcessor getConnectPoint(TemplateProcessor processor) {
	    if(processor instanceof ElementProcessor &&
	            ((ElementProcessor)processor).isDuplicated()) {
	        // m:rendered="true"のとき、duplicatedなelementを剥き取る。
	        return getConnectPoint(processor.getChildProcessor(0));
	    }
	    for(int i = 0; i < processor.getChildProcessorSize(); i++) {
	        TemplateProcessor child = processor.getChildProcessor(i);
		    if(child instanceof CharactersProcessor) {
                ProcessorProperty prop = ((CharactersProcessor)child).getText(); 
		        if(prop.isStatic()) {
                    String value = (String)prop.getValue();
    		        if(StringUtil.hasValue(value.trim())) {
    			        // .maya上のノードにボディテキストがあるとき
    		            return null;
    		        }
                }
	        } else if(child instanceof AttributeProcessor == false) {
	            // .maya上のノードに改行や空白のm:charactersもしくは、
	            // m:attribute以外のネストした子ノードがあるとき
	            return null;
	        }
		}
	    return processor;
	}
    
    private TemplateProcessor resolveInjectedNode(Template template, Stack stack,  
            SpecificationNode original, SpecificationNode injected) {
        if(injected == null) {
            throw new IllegalArgumentException();
        }
        saveToCycle(injected);
        TemplateProcessor processor = null;
        ProcessorDefinition def = _libraryManger.getProcessorDefinition(injected.getQName());
        if(def != null) {
            processor = def.createTemplateProcessor(template, injected);
            processor.setOriginalNode(original);
            processor.setInjectedNode(injected);
        }
        if(processor == null) {
            throw new NodeNotResolvedException(template, injected);
        }
        TemplateProcessor parent = (TemplateProcessor)stack.peek();
        parent.addChildProcessor(processor);
        Iterator it = injected.iterateChildNode();
        if(it.hasNext() == false) {
            return processor;
        }
        // .mayaにネストしたノードが有る場合の処理開始。
        stack.push(processor);
        TemplateProcessor connectionPoint = null;
        while(it.hasNext()) {
            SpecificationNode childNode = (SpecificationNode)it.next();
            saveToCycle(childNode);
            TemplateProcessor childProcessor = resolveInjectedNode(
                    template, stack, original, childNode);
            if(childProcessor instanceof DoBodyProcessor) {
                if(connectionPoint != null) {
                    throw new TooManyDoBodyException(childNode);
                }
                connectionPoint = childProcessor;
            }
        }
        stack.pop();
        if(connectionPoint != null) {
            return connectionPoint;
        }
        return getConnectPoint(processor);
    }
    
    private void resolveChildren(
            Template template, Stack stack, SpecificationNode original) {
        if(original == null) {
            throw new IllegalArgumentException();
        }
        Iterator it;
        it = original.iterateChildNode();
        while(it.hasNext()) {
            SpecificationNode child = (SpecificationNode)it.next();
            saveToCycle(child);
            if(QM_MAYA.equals(child.getQName())) {
                continue;
            }
	        SpecificationNode injected = _injectionResolver.getNode(
	                template, child, DefaultInjectionChain.getInstance());
	        if(injected == null) {
	            throw new NodeNotResolvedException(template, child);
	        }
	        TemplateProcessor processor = resolveInjectedNode(
                    template, stack, original, injected);
	        saveToCycle(child);
            if(processor != null) {
	            stack.push(processor);
	            resolveChildren(template, stack, child);
	            stack.pop();
	        }
        }
    }
    
    public void resolveTemplate(Template template) {
        if(template == null) {
            throw new IllegalArgumentException();
        }
        saveToCycle(template);
        Stack stack = new Stack();
        stack.push(template);
	    SpecificationNode maya = new SpecificationNodeImpl(
                QM_MAYA, template.getLocator());
	    template.addChildNode(maya);
        resolveChildren(template, stack, template);
        if(template.equals(stack.peek()) == false) {
            throw new IllegalStateException();
        }
        saveToCycle(template);
    }
    
}
