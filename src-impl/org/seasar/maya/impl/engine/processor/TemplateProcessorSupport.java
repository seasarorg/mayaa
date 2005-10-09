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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.processor.ProcessStatus;
import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.SpecificationNode;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateProcessorSupport implements TemplateProcessor {

	private static final long serialVersionUID = -3521980479718620027L;

	private ProcessorTreeWalker _parent;
    private int _index;
    private List _children = new ArrayList();
    private SpecificationNode _originalNode;
    private SpecificationNode _injectedNode;
    private boolean _evalBodyInclude = true;
    private ProcessorDefinition _definition;
    
    // MLD property
    public void setEvalBodyInclude(boolean evalBodyInclude) {
        _evalBodyInclude = evalBodyInclude;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if(_evalBodyInclude) {
            return ProcessStatus.EVAL_BODY_INCLUDE;
        }
        return ProcessStatus.SKIP_BODY;
    }

    public ProcessStatus doEndProcess() {
        return ProcessStatus.EVAL_PAGE;
    }

    public void setOriginalNode(SpecificationNode node) {
        if(node == null) {
            throw new IllegalArgumentException();
        }
        _originalNode = node;
    }

    public SpecificationNode getOriginalNode() {
        return _originalNode;
    }

    public void setInjectedNode(SpecificationNode node) {
        if(node == null) {
            throw new IllegalArgumentException();
        }
        _injectedNode = node;
    }

    public SpecificationNode getInjectedNode() {
        return _injectedNode;
    }

    public void setProcessorDefinition(ProcessorDefinition definition) {
        if(definition == null) {
            throw new IllegalArgumentException();
        }
        _definition = definition;
    }
    
    public ProcessorDefinition getProcessorDefinition() {
        if(_definition == null) {
            throw new IllegalStateException();
        }
        return _definition;
    }
    
    // ProcessorTreeWalker implements --------------------------------

    public Map getVariables() {
        return null;
    }
    
    public void setParentProcessor(ProcessorTreeWalker parent, int index) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        _index = index;
    }

    public ProcessorTreeWalker getParentProcessor() {
        return _parent;
    }

    public int getIndex() {
        return _index;
    }

    public void addChildProcessor(ProcessorTreeWalker child) {
        if(child == null) {
            throw new IllegalArgumentException();
        }
        _children.add(child);
        child.setParentProcessor(this, _children.size() - 1);
    }

    public int getChildProcessorSize() {
        synchronized(_children) {
            return _children.size();
        }
    }

    public ProcessorTreeWalker getChildProcessor(int index) {
        return (ProcessorTreeWalker)_children.get(index);
    }

}
