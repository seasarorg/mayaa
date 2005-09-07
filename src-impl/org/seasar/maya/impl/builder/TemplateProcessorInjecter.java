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

import org.seasar.maya.builder.injection.InjectionChain;
import org.seasar.maya.builder.injection.InjectionResolver;
import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.injection.DefaultInjectionChain;
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
    private LibraryManager _libraryManager;

    public TemplateProcessorInjecter(
            InjectionResolver injectionResolver, LibraryManager libraryManager) {
        if(injectionResolver == null || libraryManager == null) {
            throw new IllegalArgumentException();
        }
        _injectionResolver = injectionResolver;
        _libraryManager = libraryManager;
    }
    
    private void saveToCycle(SpecificationNode originalNode,
    		SpecificationNode injectedNode) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(originalNode);
        cycle.setInjectedNode(injectedNode);
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
                CharactersProcessor charsProc = (CharactersProcessor)child;
                CompiledScript script = charsProc.getText().getValue(); 
		        if(script.isLiteral()) {
                    String value = (String)script.execute();
    		        if(StringUtil.hasValue(value.trim())) {
    			        // .maya上のノードボディが空白でないとき
    		            return null;
    		        }
                } else {
                    // .maya上のノードボディに、動的式が書かれているとき
                    return null;
                }
	        } else if(child instanceof AttributeProcessor == false) {
	            // .maya上のノードにm:charactersもしくは、
	            // m:attribute以外のネストした子ノードがあるとき
	            return null;
	        }
		}
	    return processor;
	}
    
    private TemplateProcessor createProcessor(
            SpecificationNode original, SpecificationNode injected) {
        QName name = injected.getQName();
        ProcessorDefinition def = _libraryManager.getProcessorDefinition(name);
        if(def != null) {
            TemplateProcessor proc = def.createTemplateProcessor(injected);
            proc.setOriginalNode(original);
            proc.setInjectedNode(injected);
            return proc;
        }
        return null;
    }
    
    private TemplateProcessor resolveInjectedNode(Template template, 
            Stack stack, SpecificationNode original, SpecificationNode injected) {
        if(injected == null) {
            throw new IllegalArgumentException();
        }
        saveToCycle(original, injected);
        TemplateProcessor processor = createProcessor(original, injected);
        if(processor == null) {
            NodeNamespace ns = original.getNamespace("", true);
            if(ns == null) {
                throw new IllegalStateException();
            }
            String defaultURI = ns.getNamespaceURI();
            if(defaultURI.equals(injected.getQName().getNamespaceURI())) {
                InjectionChain chain = DefaultInjectionChain.getInstance(); 
                SpecificationNode retry = chain.getNode(injected);
                processor = createProcessor(original, retry);
            }
            if(processor == null) {
                throw new ProcessorNotInjectedException();
            }
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
            saveToCycle(original, childNode);
            TemplateProcessor childProcessor = resolveInjectedNode(
                    template, stack, original, childNode);
            if(childProcessor instanceof DoBodyProcessor) {
                if(connectionPoint != null) {
                    throw new TooManyDoBodyException();
                }
                connectionPoint = childProcessor;
            }
        }
        stack.pop();
        saveToCycle(original, injected);
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
            saveToCycle(child, child);
            if(QM_MAYA.equals(child.getQName())) {
                continue;
            }
            InjectionChain chain = DefaultInjectionChain.getInstance(); 
	        SpecificationNode injected = _injectionResolver.getNode(child, chain);
	        if(injected == null) {
	            throw new TemplateNodeNotResolvedException();
	        }
            saveToCycle(child, injected);
	        TemplateProcessor processor = resolveInjectedNode(
                    template, stack, original, injected);
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
        saveToCycle(template, template);
        Stack stack = new Stack();
        stack.push(template);
	    SpecificationNode maya = new SpecificationNodeImpl(
                QM_MAYA, template.getSystemID(), 0);
	    template.addChildNode(maya);
        resolveChildren(template, stack, template);
        if(template.equals(stack.peek()) == false) {
            throw new IllegalStateException();
        }
        saveToCycle(template, template);
    }
    
}
