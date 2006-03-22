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
package org.seasar.mayaa.impl.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.seasar.mayaa.builder.TemplateBuilder;
import org.seasar.mayaa.builder.injection.InjectionChain;
import org.seasar.mayaa.builder.injection.InjectionResolver;
import org.seasar.mayaa.builder.library.LibraryManager;
import org.seasar.mayaa.builder.library.ProcessorDefinition;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.processor.OptimizableProcessor;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.builder.injection.DefaultInjectionChain;
import org.seasar.mayaa.impl.builder.parser.AdditionalHandler;
import org.seasar.mayaa.impl.builder.parser.TemplateParser;
import org.seasar.mayaa.impl.builder.parser.TemplateScanner;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.script.LiteralScript;
import org.seasar.mayaa.impl.engine.processor.AttributeProcessor;
import org.seasar.mayaa.impl.engine.processor.CharactersProcessor;
import org.seasar.mayaa.impl.engine.processor.DoBodyProcessor;
import org.seasar.mayaa.impl.engine.processor.ElementProcessor;
import org.seasar.mayaa.impl.engine.processor.LiteralCharactersProcessor;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.xml.XMLReaderPool;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateBuilderImpl extends SpecificationBuilderImpl
        implements TemplateBuilder, CONST_IMPL {

    private static final long serialVersionUID = -1031702086020145692L;

    private List _resolvers = new ArrayList();
    private List _unmodifiableResolvers =
        Collections.unmodifiableList(_resolvers);
    private HtmlReaderPool _htmlReaderPool = new HtmlReaderPool();
    private InjectionChain _chain = new DefaultInjectionChain();
    private boolean _optimize = true;

    public void addInjectionResolver(InjectionResolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException();
        }
        synchronized (_resolvers) {
            _resolvers.add(resolver);
        }
    }

    protected List getInjectionResolvers() {
        return _unmodifiableResolvers;
    }

    protected boolean isHTML(String mimeType) {
        return mimeType != null && (mimeType.indexOf("html") != -1);
    }

    protected XMLReaderPool getXMLReaderPool(String systemID) {
        ApplicationScope application = CycleUtil.getServiceCycle().getApplicationScope();
        String mimeType = application.getMimeType(systemID);
        if (isHTML(mimeType)) {
            return _htmlReaderPool;
        }
        return super.getXMLReaderPool(systemID);
    }

    protected String getPublicID() {
        return URI_MAYAA + "/template";
    }

    protected void afterBuild(Specification specification) {
        if ((specification instanceof Template) == false) {
            throw new IllegalArgumentException();
        }
        doInjection((Template) specification);
    }

    protected void saveToCycle(NodeTreeWalker originalNode,
            NodeTreeWalker injectedNode) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(originalNode);
        cycle.setInjectedNode(injectedNode);
    }

    protected TemplateProcessor findConnectPoint(
            TemplateProcessor processor) {
        if (processor instanceof ElementProcessor
                && ((ElementProcessor) processor).isDuplicated()) {
            // "processor"'s m:replace is true, ripping duplicated element.
            return findConnectPoint(
                    (TemplateProcessor) processor.getChildProcessor(0));
        }
        for (int i = 0; i < processor.getChildProcessorSize(); i++) {
            ProcessorTreeWalker child = processor.getChildProcessor(i);
            if (child instanceof CharactersProcessor) {
                CharactersProcessor charsProc = (CharactersProcessor) child;
                CompiledScript script = charsProc.getText().getValue();
                if (script.isLiteral()) {
                    String value = script.getScriptText();
                    if (StringUtil.hasValue(value.trim())) {
                        // "processor" has child which is not empty.
                        return null;
                    }
                } else {
                    // "processor" has child which is scriptlet.
                    return null;
                }
            } else if (child instanceof AttributeProcessor == false) {
                // "processor" has child which is implicit m:characters or
                // nested child node, but is NOT m:attribute
                return null;
            }
        }
        return processor;
    }

    protected TemplateProcessor createProcessor(
            SpecificationNode original, SpecificationNode injected) {
        QName name = injected.getQName();
        LibraryManager libraryManager = ProviderUtil.getLibraryManager();
        ProcessorDefinition def = libraryManager.getProcessorDefinition(name);
        if (def != null) {
            TemplateProcessor proc =
                def.createTemplateProcessor(original, injected);
            proc.setOriginalNode(original);
            proc.setInjectedNode(injected);
            return proc;
        }
        return null;
    }

    protected InjectionChain getDefaultInjectionChain() {
        return _chain;
    }

    protected TemplateProcessor resolveInjectedNode(
            Template template, Stack stack, SpecificationNode original,
            SpecificationNode injected, ProcessorTray processorTray) {
        if (injected == null) {
            throw new IllegalArgumentException();
        }
        saveToCycle(original, injected);
        TemplateProcessor processor = createProcessor(original, injected);
        if (processor == null) {
            PrefixMapping mapping = original.getMappingFromPrefix("", true);
            if (mapping == null) {
                throw new IllegalStateException();
            }
            String defaultURI = mapping.getNamespaceURI();
            if (defaultURI.equals(injected.getQName().getNamespaceURI())) {
                InjectionChain chain = getDefaultInjectionChain();
                SpecificationNode retry = chain.getNode(injected);
                processor = createProcessor(original, retry);
            }
            if (processor == null) {
                throw new ProcessorNotInjectedException(injected.toString());
            }
        }
        ProcessorTreeWalker parent = (ProcessorTreeWalker) stack.peek();
        parent.addChildProcessor(processor);
        processor.initialize();
        processorTray.object = processor;
        Iterator it = injected.iterateChildNode();
        if (it.hasNext() == false) {
            return processor;
        }
        // "injected" node has children, nested node definition on .mayaa
        stack.push(processor);
        TemplateProcessor connectionPoint = null;
        while (it.hasNext()) {
            SpecificationNode childNode = (SpecificationNode) it.next();
            saveToCycle(original, childNode);
            ProcessorTray childProcessorTray = new ProcessorTray();
            TemplateProcessor childProcessor = resolveInjectedNode(
                    template, stack, original, childNode, childProcessorTray);
            if (childProcessor instanceof DoBodyProcessor) {
                if (connectionPoint != null) {
                    throw new TooManyDoBodyException();
                }
                connectionPoint = childProcessor;
            }
        }
        stack.pop();
        saveToCycle(original, injected);
        if (connectionPoint != null) {
            return connectionPoint;
        }
        return findConnectPoint(processor);
    }

    protected void optimizeProcessors(ProcessorTreeWalker parent, List collecter) {
        List expands = new ArrayList();
        Iterator it = collecter.iterator();
        while (it.hasNext()) {
            ProcessorTreeWalker processor = (ProcessorTreeWalker) it.next();
            if (processor == null) {
                throw new IllegalStateException("processor is null");
            }
            if (processor instanceof OptimizableProcessor) {
                ProcessorTreeWalker[] processors =
                    ((OptimizableProcessor) processor).divide();
                if (processors != null) {
                    for (int i = 0; i < processors.length; i++) {
                        expands.add(processors[i]);
                    }
                } else {
                    expands.add(processor);
                }
            } else {
                expands.add(processor);
            }
        }

        List packs = new ArrayList();
        for (int i = 0; i < expands.size(); i++) {
            ProcessorTreeWalker node = (ProcessorTreeWalker) expands.get(i);
            node = convertCharactersProcessor(node);

            if (packs.size() > 0 && node instanceof LiteralCharactersProcessor) {
                Object last = packs.get(packs.size()-1);
                if (last instanceof LiteralCharactersProcessor) {
                    LiteralCharactersProcessor rawLast =
                        (LiteralCharactersProcessor) last;
                    rawLast.setText(rawLast.getText()
                            + ((LiteralCharactersProcessor) node).getText());
                } else {
                    packs.add(node);
                }
            } else {
                packs.add(node);
            }
        }
        for (int i = 0; i < packs.size(); i++) {
            ProcessorTreeWalker child = (ProcessorTreeWalker)packs.get(i);
            child.setParentProcessor(parent, parent.getChildProcessorSize());
            parent.addChildProcessor(child);
        }

    }

    protected ProcessorTreeWalker convertCharactersProcessor(
            ProcessorTreeWalker processor) {
        if (processor instanceof CharactersProcessor) {
            CharactersProcessor cnode = (CharactersProcessor)processor;
            if (cnode.getText().getValue() instanceof LiteralScript) {
                processor = new LiteralCharactersProcessor(
                        cnode, cnode.getText().getValue().getScriptText());
            }
        }
        return processor;
    }

    protected SpecificationNode resolveOriginalNode(
            SpecificationNode original, InjectionChain chain) {
        if (original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        if (_resolvers.size() > 0) {
            InjectionChainImpl first = new InjectionChainImpl(chain);
            return first.getNode(original);
        }
        return chain.getNode(original);
    }

    // FIXME m:attribute Ç™ìÆçÏÇµÇ»Ç¢
    protected void walkParsedTree(
            Template template, Stack stack, NodeTreeWalker original) {
        if (original == null) {
            throw new IllegalArgumentException();
        }
        List childProcessors = null;
        Iterator it = original.iterateChildNode();
        while (it.hasNext()) {
            SpecificationNode child = (SpecificationNode) it.next();
            saveToCycle(child, child);
            if (QM_MAYAA.equals(child.getQName())) {
                continue;
            }
            InjectionChain chain = getDefaultInjectionChain();
            SpecificationNode injected = resolveOriginalNode(child, chain);
            if (injected == null) {
                throw new TemplateNodeNotResolvedException(original.toString());
            }
            saveToCycle(child, injected);
            ProcessorTray processorTray = new ProcessorTray();
            ProcessorTreeWalker processor = resolveInjectedNode(
                    template, stack, child, injected, processorTray);
            if (processor != null) {
                stack.push(processor);
                walkParsedTree(template, stack, child);
                stack.pop();
            }
            if (_optimize) {
                if (childProcessors == null) {
                    childProcessors = new ArrayList();
                }
                if (processorTray.object instanceof OptimizableProcessor) {
                    ProcessorTreeWalker[] parts =
                        ((OptimizableProcessor) processorTray.object).divide();
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i] == null) {
                            throw new IllegalStateException(
                                    "divice part is null " + processor.toString());
                        }
                        childProcessors.add(parts[i]);
                    }
                } else {
                    childProcessors.add(processorTray.object);
                }
            }
        }
        if (_optimize) {
            if (childProcessors != null && childProcessors.size() > 0) {
                ProcessorTreeWalker parent = (ProcessorTreeWalker) stack.peek();
                if (parent == null) {
                    parent = template;
                }
                parent.clearChildProcessors();
                optimizeProcessors(parent, childProcessors);
            }
        }
    }

    protected void doInjection(Template template) {
        if (template == null) {
            throw new IllegalArgumentException();
        }
        saveToCycle(template, template);
        Stack stack = new Stack();
        stack.push(template);
        SpecificationNode mayaa = SpecificationUtil.createSpecificationNode(
                QM_MAYAA, template.getSystemID(), 0, true, 0);
        template.addChildNode(mayaa);
        walkParsedTree(template, stack, template);
        if (template.equals(stack.peek()) == false) {
            throw new IllegalStateException();
        }
        saveToCycle(template, template);
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if ("optimize".equals(name)) {
            _optimize = ObjectUtil.booleanValue(value, true);
        }
        super.setParameter(name, value);
    }

    // support class --------------------------------------------------

    protected class HtmlReaderPool extends XMLReaderPool {

        private static final long serialVersionUID = -5203349759797583368L;

        protected Object createObject() {
            return new TemplateParser(new TemplateScanner());
        }

        protected boolean validateObject(Object object) {
            return object instanceof TemplateParser;
        }

        public XMLReader borrowXMLReader(ContentHandler handler,
                boolean namespaces, boolean validation, boolean xmlSchema) {
            XMLReader htmlReader = super.borrowXMLReader(
                    handler, namespaces, validation, xmlSchema);
            if (handler instanceof AdditionalHandler) {
                try {
                    htmlReader.setProperty(
                            AdditionalHandler.ADDITIONAL_HANDLER, handler);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                }
            }
            return htmlReader;
        }

    }

    protected class InjectionChainImpl implements InjectionChain {

        private int _index;
        private InjectionChain _external;

        public InjectionChainImpl(InjectionChain external) {
            _external = external;
        }

        public SpecificationNode getNode(SpecificationNode original) {
            if (original == null) {
                throw new IllegalArgumentException();
            }
            if (_index < getInjectionResolvers().size()) {
                InjectionResolver resolver =
                    (InjectionResolver) getInjectionResolvers().get(_index);
                _index++;
                InjectionChain chain;
                if (_index == getInjectionResolvers().size()) {
                    chain = _external;
                } else {
                    chain = this;
                }
                return resolver.getNode(original, chain);
            }
            throw new IndexOutOfBoundsException();
        }

    }

    private class ProcessorTray {
        TemplateProcessor object;
    }

}
