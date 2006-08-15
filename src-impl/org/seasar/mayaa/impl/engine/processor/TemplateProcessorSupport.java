/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.SequenceIDGenerator;
import org.seasar.mayaa.builder.library.LibraryManager;
import org.seasar.mayaa.builder.library.ProcessorDefinition;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.OptimizableProcessor;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.engine.specification.serialize.NodeReferenceResolver;
import org.seasar.mayaa.engine.specification.serialize.NodeResolveListener;
import org.seasar.mayaa.engine.specification.serialize.ProcessorReferenceResolver;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.TemplateImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;
import org.seasar.mayaa.impl.engine.specification.serialize.NodeSerializeController;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateProcessorSupport
        implements TemplateProcessor, OptimizableProcessor {

    private static final long serialVersionUID = -3521980479718620027L;
    private static final Log LOG = LogFactory.getLog(TemplateProcessorSupport.class);
    private static final String PREFIX_UNIQUE_ID = "_m";

    private transient ProcessorTreeWalker _parent;
    private List/*<ProcessorTreeWalker>*/ _children;
    protected SpecificationNode _originalNode;
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
            throw new IllegalArgumentException();
        }
        _originalNode = node;
    }

    public SpecificationNode getOriginalNode() {
        if (_originalNode == null) {
            throw new IllegalStateException();
        }
        return _originalNode; 
    }

    public void setInjectedNode(SpecificationNode node) {
        if (node == null) {
            throw new IllegalArgumentException();
        }
        _injectedNode = node;
    }

    public SpecificationNode getInjectedNode() {
        if (_injectedNode == null) {
            throw new IllegalStateException();
        }
        return _injectedNode;
    }

    public void setProcessorDefinition(ProcessorDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException();
        }
        _definition = definition;
    }

    public ProcessorDefinition getProcessorDefinition() {
        if (_definition == null) {
            throw new IllegalStateException();
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

    public Map getVariables() {
        return null;
    }

    public void setParentProcessor(ProcessorTreeWalker parent) {
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }

    public ProcessorTreeWalker getParentProcessor() {
        return _parent;
    }

    public void addChildProcessor(ProcessorTreeWalker child) {
        insertProcessor(Integer.MAX_VALUE, child);
    }

    public void insertProcessor(int index, ProcessorTreeWalker child) {
        if (child == null) {
            throw new IllegalArgumentException();
        }
        synchronized (this) {
            if (_children == null) {
                _children = new ArrayList();
            }
        }
        synchronized (_children) {
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
    }

    public int getChildProcessorSize() {
        if (_children == null) {
            return 0;
        }
        synchronized (_children) {
            return _children.size();
        }
    }

    public ProcessorTreeWalker getChildProcessor(int index) {
        if (index < 0 || index >= getChildProcessorSize()) {
            throw new IndexOutOfBoundsException(); 
        }
        synchronized (_children) {
            return (ProcessorTreeWalker) _children.get(index);
        }
    }
    
    public boolean removeProcessor(ProcessorTreeWalker child) {
        if (_children != null) {
            synchronized (_children) {
                return _children.remove(child);
            }
        }
        return false;
    }

    public void clearChildProcessors() {
        if (_children != null) {
            try {
                synchronized (this) {
                    _children.clear();
                }
            } finally {
                _children = null;
            }
        }
    }
    
    public void kill() {
        synchronized (this) {
            _originalNode = null;
            _injectedNode = null;
            _parent = null;
            if (_children != null) {
                synchronized (_children) {
                    for (Iterator it = _children.iterator(); it.hasNext(); ) {
                        TemplateProcessor processor = (TemplateProcessor) it.next();
                        processor.kill();
                    }
                }
            }
            clearChildProcessors();
        }
    }

    public ProcessorTreeWalker[] divide(SequenceIDGenerator sequenceIDGenerator) {
        return new ProcessorTreeWalker[] { this };
    }
    
    public void notifyBeginRender(Page topLevelPage) {
        // no operation
    }
    
    protected void finalize() throws Throwable {
        if (LOG.isTraceEnabled()) {
            String name = ObjectUtil.getSimpleClassName(getClass());
            if (_definition != null) {
                LOG.trace(name + " " + getProcessorDefinition().getName() + " unloaded.");
            } else {
                LOG.trace(name + " " + " unloaded.");
            }
        }
    }
    
    // for serialize
    private static final String DUPLICATE_ROOT_MARK = "<<root>>";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        String originalNodeID = NodeSerializeController.makeKey(_originalNode);
        String uniqueID = getUniqueID();
        out.writeUTF(originalNodeID);
        out.writeUTF(uniqueID);
        out.writeObject(_injectedNode.getQName());
        
        if (_injectedNode.getParentNode() != null) {
            String injectedNodeID = NodeSerializeController.makeKey(_injectedNode);
            out.writeUTF(injectedNodeID);
        } else {
            out.writeUTF(DUPLICATE_ROOT_MARK);
            out.writeObject(_injectedNode);
        }
    }

    protected void nodeLoadAfter() {
        if (_originalNode != null && _injectedNode != null) {
            Namespace parentSpace = getOriginalNode().getParentSpace();
            if (parentSpace != null
                    && getInjectedNode().getParentSpace() == null) {
                getInjectedNode().setParentSpace(parentSpace);
            }
        }
    }

    private static class TemplateProcessorSupportOriginalNodeListener
            implements NodeResolveListener {
        
        private TemplateProcessorSupport _target;
        
        public TemplateProcessorSupportOriginalNodeListener(
                TemplateProcessorSupport target) {
            _target = target;
        }
        
        public void notify(
                String uniqueID, NodeTreeWalker loadedInstance) {
            if (loadedInstance instanceof SpecificationNode) {
                SpecificationNode specNode =
                    (SpecificationNode)loadedInstance;
                _target._originalNode = specNode;
                _target.nodeLoadAfter();
            }
        }

        public void release() {
            _target = null;
        }
    }

    private static class TemplateProcessorSupportInjectedNodeListener
            implements NodeResolveListener {
        
        private TemplateProcessorSupport _target;
        
        public TemplateProcessorSupportInjectedNodeListener(
                TemplateProcessorSupport target) {
            _target = target;
        }
        
        public void notify(
                String uniqueID, NodeTreeWalker loadedInstance) {
            if (loadedInstance instanceof SpecificationNode) {
                SpecificationNode specNode =
                    (SpecificationNode)loadedInstance; 
                _target._injectedNode = specNode;
                _target.nodeLoadAfter();
            }
        }

        public void release() {
            _target = null;
        }
    }
    
    private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String originalNodeID = in.readUTF();
        String processorUniqueID = in.readUTF();
        QName qName = (QName)in.readObject();
        LibraryManager libraryManager = ProviderUtil.getLibraryManager();
        setProcessorDefinition(libraryManager.getProcessorDefinition(qName));
        
        String injectedNodeID = in.readUTF();
        NodeResolveListener listener =
            new TemplateProcessorSupportOriginalNodeListener(this);
        findNodeResolver().registResolveNodeListener(
                originalNodeID, listener);
        
        if (DUPLICATE_ROOT_MARK.equals(injectedNodeID)) {
            _injectedNode = (SpecificationNode) in.readObject();
        } else {
            listener = new TemplateProcessorSupportInjectedNodeListener(this);
            findNodeResolver().registResolveNodeListener(injectedNodeID, listener);
        }

        ProcessorReferenceResolver resolver = findProcessorResolver();
        resolver.processorLoaded(processorUniqueID, this);

        // readResolveはextendsした最終クラスでしか発生しないので、ここで解決
        if (_children != null) {
            for (int i = _children.size() - 1; i >= 0 ; i--) {
                ProcessorTreeWalker child =
                    (ProcessorTreeWalker) _children.get(i);
                child.setParentProcessor(this);
            }
        }
        LOG.debug("templateProcessorSupport loaded");
    }
    
    public ProcessorReferenceResolver findProcessorResolver() {
        return TemplateImpl.processorSerializer();
    }
    
    public NodeReferenceResolver findNodeResolver() {
        return SpecificationImpl.nodeSerializer();
    }

}
