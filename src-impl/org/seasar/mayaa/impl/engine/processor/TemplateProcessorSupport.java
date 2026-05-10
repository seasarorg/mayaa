/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.engine.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.seasar.mayaa.builder.SequenceIDGenerator;
import org.seasar.mayaa.builder.library.ProcessorDefinition;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.OptimizableProcessor;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateProcessorSupport
        implements TemplateProcessor, OptimizableProcessor {

    private static final long serialVersionUID = -2563169515122616036L;
    private static final String PREFIX_UNIQUE_ID = "_m";

    private transient ProcessorTreeWalker _parent;
    // _childrenはビルド時のみ変更される。そのときはEngineのインスタンスでガードされる。
    private List<ProcessorTreeWalker> _children;

    /** ファイルから読み込んだオリジナルのノードツリー */
    protected SpecificationNode _originalNode;

    /** オリジナルのノードツリーに対してInjectionResolverで変換されたTemplateProcessorのツリー */
    protected transient SpecificationNode _injectedNode;

    private boolean _evalBodyInclude = true;
    private transient ProcessorDefinition _definition;


    public void initialize() {
        // no operation
    }

    public String getUniqueID() {
        int sequenceID = getOriginalNode().getSequenceID();
        return PREFIX_UNIQUE_ID + sequenceID;
    }

    // MLD property
    public void setEvalBodyInclude(boolean evalBodyInclude) {
        _evalBodyInclude = evalBodyInclude;
    }

    public boolean isEvalBodyInclude() {
        return _evalBodyInclude;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if (_evalBodyInclude) {
            return ProcessStatus.EVAL_BODY_INCLUDE;
        }
        return ProcessStatus.SKIP_BODY;
    }

    public ProcessStatus doEndProcess() {
        return ProcessStatus.EVAL_PAGE;
    }

    public void setOriginalNode(SpecificationNode node) {
        if (node == null) {
            throw new IllegalArgumentException("originalNode must not be null");
        }
        _originalNode = node;
    }

    public SpecificationNode getOriginalNode() {
        if (_originalNode == null) {
            throw new IllegalStateException("originalNode is null");
        }
        return _originalNode;
    }

    public void setInjectedNode(SpecificationNode node) {
        if (node == null) {
            throw new IllegalArgumentException("injectedNode must not be null");
        }
        _injectedNode = node;
    }

    public SpecificationNode getInjectedNode() {
        if (_injectedNode == null) {
            throw new IllegalStateException("injectedNode is null");
        }
        return _injectedNode;
    }

    public void setProcessorDefinition(ProcessorDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("processorDefinition must not be null");
        }
        _definition = definition;
    }

    public ProcessorDefinition getProcessorDefinition() {
        if (_definition == null) {
            throw new IllegalStateException("processorDefinition is null");
        }
        return _definition;
    }

    protected boolean isHTML(QName qName) {
        URI namespaceURI = qName.getNamespaceURI();
        return CONST_IMPL.URI_HTML.equals(namespaceURI);
    }

    protected boolean isXHTML(QName qName) {
        URI namespaceURI = qName.getNamespaceURI();
        return CONST_IMPL.URI_XHTML.equals(namespaceURI);
    }

    // ProcessorTreeWalker implements --------------------------------

    public Map<?,?> getVariables() {
        return null;
    }

    public void setParentProcessor(ProcessorTreeWalker parent) {
        if (parent == null) {
            throw new IllegalArgumentException("parentProcessor must not be null");
        }
        _parent = parent;
    }

    public ProcessorTreeWalker getParentProcessor() {
        return _parent;
    }

    public ProcessorTreeWalker getStaticParentProcessor() {
        return _parent;
    }

    public void addChildProcessor(ProcessorTreeWalker child) {
        insertProcessor(Integer.MAX_VALUE, child);
    }

    public synchronized void insertProcessor(int index, ProcessorTreeWalker child) {
        if (child == null) {
            throw new IllegalArgumentException("child is null");
        }
        if (_children == null) {
            _children = new ArrayList<>();
        }
        if (index == Integer.MAX_VALUE) {
            index = _children.size();
        }
        _children.add(index, child);
        child.setParentProcessor(this);
        for (index += 1; index < _children.size(); index++) {
            child = (ProcessorTreeWalker)_children.get(index);
            child.setParentProcessor(this);
        }
    }

    public int getChildProcessorSize() {
        if (_children == null) {
            return 0;
        }
        return _children.size();
    }

    public ProcessorTreeWalker getChildProcessor(int index) {
        if (index < 0 || index >= getChildProcessorSize()) {
            throw new IndexOutOfBoundsException();
        }
        return (ProcessorTreeWalker) _children.get(index);
    }

    public synchronized boolean removeProcessor(ProcessorTreeWalker child) {
        if (_children != null) {
            return _children.remove(child);
        }
        return false;
    }

    public synchronized void clearChildProcessors() {
        if (_children != null) {
            try {
                _children.clear();
            } finally {
                _children = null;
            }
        }
    }

    public ProcessorTreeWalker[] divide(SequenceIDGenerator sequenceIDGenerator) {
        return new ProcessorTreeWalker[] { this };
    }

    @Override
    @Deprecated
    public void notifyBeginRender(Page topLevelPage) {
        // no operation
    }

// デバッグのときだけ有効にすること。finalize()をオーバーライドするとFinalizerなどから特別扱いされる。
//    protected void finalize() throws Throwable {
//        if (LOG.isTraceEnabled()) {
//            String name = ObjectUtil.getSimpleClassName(getClass());
//            if (_definition != null) {
//                LOG.trace(name + " " + getProcessorDefinition().getName() + " unloaded.");
//            } else {
//                LOG.trace(name + " " + " unloaded.");
//            }
//        }
//        super.finalize();
//    }

    public void kill() {
        // TODO deprecated のため削除予定
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof TemplateProcessorSupport))
            return false;
        TemplateProcessorSupport other = (TemplateProcessorSupport) obj;
        return Objects.deepEquals(_children, other._children)
         && Objects.equals(_definition, other._definition)
         && _evalBodyInclude == other._evalBodyInclude
         && Objects.equals(_injectedNode, other._injectedNode)
         && Objects.equals(_originalNode, other._originalNode);
        //  && Objects.equals(_parent, other._parent);
    }

}
