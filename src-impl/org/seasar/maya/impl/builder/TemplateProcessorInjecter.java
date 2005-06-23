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
package org.seasar.maya.impl.builder;

import java.util.Iterator;
import java.util.Stack;

import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.builder.specification.InjectionResolver;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.specification.DefaultInjectionChain;
import org.seasar.maya.impl.engine.processor.AttributeProcessor;
import org.seasar.maya.impl.engine.processor.CharactersProcessor;
import org.seasar.maya.impl.engine.processor.DoBodyProcessor;
import org.seasar.maya.impl.engine.processor.ElementProcessor;
import org.seasar.maya.impl.engine.specification.SpecificationNodeImpl;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.xml.NullLocator;

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
	
	private TemplateProcessor getConnectPoint(TemplateProcessor processor) {
	    if(processor instanceof ElementProcessor &&
	            ((ElementProcessor)processor).isDuplicated()) {
	        // m:rendered="true"のとき、duplicatedなelementを剥き取る。
	        return getConnectPoint(processor.getChildProcessor(0));
	    }
	    for(int i = 0; i < processor.getChildProcessorSize(); i++) {
	        TemplateProcessor child = processor.getChildProcessor(i);
		    if(child instanceof CharactersProcessor) {
		        String value = ((CharactersProcessor)child).getText().getLiteral();
		        if(StringUtil.hasValue(value.trim())) {
			        // .maya上のノードにボディテキストがあるとき
		            return null;
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
            SpecificationNode injected) {
        if(injected == null) {
            throw new IllegalArgumentException();
        }
        TemplateProcessor processor = null;
        ProcessorDefinition def = _libraryManger.getProcessorDefinition(injected.getQName());
        if(def != null) {
            processor = def.createTemplateProcessor(template, injected);
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
            TemplateProcessor child = resolveInjectedNode(template, stack, childNode);
            if(child instanceof DoBodyProcessor) {
                if(connectionPoint != null) {
                    // TODO 適切な例外に変更（m:doBodyが複数みつかったとき）
                    throw new IllegalStateException();
                }
                connectionPoint = child;
            }
        }
        stack.pop();
        if(connectionPoint != null) {
            return connectionPoint;
        }
        return getConnectPoint(processor);
    }
    
    private void resolveChildren(Template template, Stack stack, SpecificationNode original) {
        if(original == null) {
            throw new IllegalArgumentException();
        }
        Iterator it;
        it = original.iterateChildNode();
        while(it.hasNext()) {
            SpecificationNode child = (SpecificationNode)it.next();
            if(QM_MAYA.equals(child.getQName())) {
                continue;
            }
	        SpecificationNode injected = _injectionResolver.getNode(
	                template, child, DefaultInjectionChain.getInstance());
	        if(injected == null) {
	            throw new NodeNotResolvedException(template, child);
	        }
	        TemplateProcessor processor = resolveInjectedNode(template, stack, injected);
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
        Stack stack = new Stack();
        stack.push(template);
	    SpecificationNode maya = new SpecificationNodeImpl(QM_MAYA, NullLocator.getInstance());
	    template.addChildNode(maya);
        resolveChildren(template, stack, template);
        if(template.equals(stack.peek()) == false) {
            throw new IllegalStateException();
        }
    }
    
}
