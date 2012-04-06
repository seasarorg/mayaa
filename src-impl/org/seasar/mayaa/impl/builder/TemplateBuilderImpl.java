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
package org.seasar.mayaa.impl.builder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.SequenceIDGenerator;
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
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.builder.injection.DefaultInjectionChain;
import org.seasar.mayaa.impl.builder.parser.AdditionalHandler;
import org.seasar.mayaa.impl.builder.parser.TemplateParser;
import org.seasar.mayaa.impl.builder.parser.TemplateScanner;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.processor.AttributeProcessor;
import org.seasar.mayaa.impl.engine.processor.CharactersProcessor;
import org.seasar.mayaa.impl.engine.processor.CommentProcessor;
import org.seasar.mayaa.impl.engine.processor.DoBodyProcessor;
import org.seasar.mayaa.impl.engine.processor.ElementProcessor;
import org.seasar.mayaa.impl.engine.processor.LiteralCharactersProcessor;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;
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
        implements TemplateBuilder {
    public static final String DEFAULT_CHARSET = "defaultCharset";
    public static final String BALANCE_TAG = "balanceTag";
    public static final String REPLACE_SSI_INCLUDE = "replaceSSIInclude";
    public static final String OPTIMIZE = "optimize";
    public static final String OUTPUT_TEMPLATE_WHITESPACE = "outputTemplateWhitespace";

    private static final Log LOG = LogFactory.getLog(TemplateBuilderImpl.class);
    private static final long serialVersionUID = -1031702086020145692L;

    private List _resolvers = new ArrayList();
    private List _unmodifiableResolvers =
        Collections.unmodifiableList(_resolvers);
    private HtmlReaderPool _htmlReaderPool = new HtmlReaderPool();
    private transient InjectionChain _chain = new DefaultInjectionChain();
    private boolean _outputTemplateWhitespace = true;
    private boolean _optimize = true;
    private boolean _isSSIIncludeReplacementEnabled = false;

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

    protected SpecificationNodeHandler createContentHandler(
            Specification specification) {
        if (specification instanceof Template == false) {
            throw new IllegalArgumentException();
        }
        TemplateNodeHandler handler =
            new TemplateNodeHandler((Template) specification);
        handler.setOutputTemplateWhitespace(isOutputTemplateWhitespace());
        handler.setSSIIncludeReplacementEnabled(isSSIIncludeReplacementEnabled());
        return handler;
    }

    protected String getPublicID() {
        return URI_MAYAA + "/template";
    }

    /**
     * SSI includeが有効なときに自動的にm:doRenderで囲むファイルの拡張子。
     * デフォルト実装では".inc"。(ASPの慣習)
     * @return 自動的にm:doRenderで全体を囲む処理をするファイルの拡張子。
     */
    protected String getIncludeExtension() {
        return ".inc";
    }

    protected void afterBuild(Specification specification) {
        if ((specification instanceof Template) == false) {
            throw new IllegalArgumentException();
        }

        Template template = (Template) specification;
        String systemID = specification.getSystemID();
        /*
         * SSI includeが有効なとき、{@link #getIncludeExtension()}の拡張子であれば
         * divで囲んで自動的にm:doRenderになるようにする。ただしNekoHTMLParserの都合上、
         * 最低限何かのタグで囲む必要がある。(ルートに文字列があっても無視される)
         */
        if (isSSIIncludeReplacementEnabled() && systemID.endsWith(getIncludeExtension())) {
            int childCount = template.getChildNodeSize();
            if (childCount > 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("enclose " + systemID + " with m:doRender.(name=\"\")");
                }

                // 強制的にデフォルト名前空間をmime-typeからセットする。
                ApplicationScope application = CycleUtil.getServiceCycle().getApplicationScope();
                String mimeType = application.getMimeType(systemID);
                if (StringUtil.hasValue(mimeType)) {
                    if (mimeType.indexOf("xhtml") >= 0) {
                        setDefaultNamespaceURI(template, URI_XHTML);
                    } else if (mimeType.indexOf("html") >= 0) {
                        setDefaultNamespaceURI(template, URI_HTML);
                    }
                }

                URI namespace = ((SpecificationNode)template.getChildNode(0)).getDefaultNamespaceURI();
                QName qName = QNameImpl.getInstance(namespace, "div");
                SpecificationNode root = SpecificationUtil.createSpecificationNode(
                        qName, systemID, 0, true, specification.nextSequenceID());
                root.addPrefixMapping("m", URI_MAYAA);
                root.addAttribute(QM_INJECT, "m:doRender");
                for (int i = 0; i < childCount; i++) {
                    root.addChildNode(template.getChildNode(i));
                }
                template.clearChildNodes();
                template.addChildNode(root);
            }
        }

        LOG.debug("built node tree from template. " + systemID);
        doInjection((Template) specification);
        if (isOptimizeEnabled()) {
            nodeOptimize((Template) specification);
        }
        LOG.debug("built processor tree from node tree. " + specification.getSystemID());
    }

    /**
     * {@code template}の先頭子ノードのデフォルト名前空間が{@code null}かまたはXMLの名前空間の場合、
     * このテンプレート上でデフォルト名前空間がXML, HTML, XHTMLのいずれかであるものすべての
     * デフォルト名前空間を{@code namespace}に変更する。
     * @param template
     * @param namespace
     */
    protected void setDefaultNamespaceURI(Template template, URI namespace) {
        int size = template.getChildNodeSize();
        if (size > 0) {
            URI firstDefaultNS = ((SpecificationNode)template.getChildNode(0)).getDefaultNamespaceURI();
            if (firstDefaultNS == null || firstDefaultNS.equals(URI_XML)) {
                for (int i = 0; i < size; i++) {
                    setDefaultNamespaceURIToNode((SpecificationNode)template.getChildNode(i), namespace);
                }
            }
        }
    }

    /**
     * parentNodeのdefault namespaceがXML, HTML, XHTMLのいずれかなら、強制的にパラメータのnamespaceを
     * default namespaceにする。子ノードも同様に処理する。
     * @param parentNode
     * @param namespace
     */
    protected void setDefaultNamespaceURIToNode(SpecificationNode parentNode, URI namespace) {
        if (parentNode.getDefaultNamespaceURI() == null ||
                parentNode.getDefaultNamespaceURI().equals(URI_XML) ||
                parentNode.getDefaultNamespaceURI().equals(URI_HTML) ||
                parentNode.getDefaultNamespaceURI().equals(URI_XHTML)) {
            parentNode.setDefaultNamespaceURI(namespace);
        }
        int size = parentNode.getChildNodeSize();
        for (int i = 0; i < size; i++) {
            SpecificationNode node = (SpecificationNode)parentNode.getChildNode(i);
            if (node.getChildNodeSize() > 0) {
                setDefaultNamespaceURIToNode(node, namespace);
            }
        }
    }

    private static class AbsoluteCompareList extends ArrayList {
        private static final long serialVersionUID = 1L;

        protected AbsoluteCompareList() {
            // do nothing.
        }

        public int indexOf(Object elem) {
            for (int i = 0; i < size(); i++) {
                if (get(i) == elem) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static class NodeAndChildren {
        public SpecificationNode _originalNode;
        public List _list = new ArrayList();
        public NodeAndChildren(SpecificationNode originalNode) {
            _originalNode = originalNode;
        }
    }

    protected void nodeOptimize(Template template) {
        SequenceIDGenerator idGenerator = template;
        List children = new ArrayList();
        List usingNodes = new AbsoluteCompareList();
        walkTreeOptimizeNode(idGenerator, template, children, usingNodes);

        SpecificationNode mayaaNode = SpecificationUtil.getMayaaNode(template);
        breakRelationship(template);
        idGenerator.resetSequenceID(1);
        walkTreeNodeChainModify(idGenerator, template, children);
        template.addChildNode(mayaaNode);
    }

    protected void breakRelationship(NodeTreeWalker parent) {
        for (int i = 0; i < parent.getChildNodeSize(); i++) {
            breakRelationship(parent.getChildNode(i));
        }
        parent.clearChildNodes();
    }

    protected void walkTreeNodeChainModify(SequenceIDGenerator idGenerator,
            NodeTreeWalker parent, List children) {
        parent.clearChildNodes();
        for (int i = 0; i < children.size(); i++) {
            NodeAndChildren nodeAndChildren = (NodeAndChildren) children.get(i);
            SpecificationNode node = nodeAndChildren._originalNode;
            node.setSequenceID(idGenerator.nextSequenceID());
            node.setParentNode(parent);
            parent.addChildNode(node);
            walkTreeNodeChainModify(idGenerator, node, nodeAndChildren._list);
        }
    }

    protected void walkTreeOptimizeNode(
            SequenceIDGenerator idGenerator, ProcessorTreeWalker parent,
            List children, List usingNodes) {
        for (int i = 0; i < parent.getChildProcessorSize(); i++) {
            ProcessorTreeWalker walker = parent.getChildProcessor(i);
            if (walker instanceof TemplateProcessor) {
                TemplateProcessor processor = (TemplateProcessor) walker;
                SpecificationNode node = processor.getOriginalNode();
                if (processor instanceof LiteralCharactersProcessor) {
                    node = SpecificationUtil.createSpecificationNode(
                            QM_CHARACTERS, node.getSystemID(),
                            node.getLineNumber(), true,
                            idGenerator.nextSequenceID());
                    //node.addAttribute(QM_TEXT, "");   /*don't set text. because memory is not used.*/
                    processor.setOriginalNode(node);

                    SpecificationNode injectedNode = processor.getInjectedNode();
                    injectedNode = SpecificationUtil.createSpecificationNode(
                            QM_LITERALS, injectedNode.getSystemID(),
                            injectedNode.getLineNumber(), false, 1);
                    processor.setInjectedNode(injectedNode);
                } else {
                    if (processor instanceof ElementProcessor == false
                            && processor instanceof AttributeProcessor == false) {
                        //Namespace root = NamespaceImpl.getInstance("/null\n");
                        processor.getOriginalNode().setParentSpace(null);
                        processor.getInjectedNode().setParentSpace(null);
                        //System.out.println(processor.getClass().getName());
                    }
                }
                // to repair for originalNode infinite loop
                if (parent instanceof ElementProcessor
                		&& ((ElementProcessor)parent).getOriginalNode() == node) {
                	continue;
                }
                NodeAndChildren nodeAndChildren = new NodeAndChildren(node);
                children.add(nodeAndChildren);
                usingNodes.add(node);
                if (processor.getChildProcessorSize() > 0) {
                    walkTreeOptimizeNode(idGenerator, processor,
                            nodeAndChildren._list, usingNodes);
                }
            }
        }
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
            if (child instanceof CommentProcessor) {
            	return null;
            } else if (child instanceof CharactersProcessor) {
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

    protected TemplateProcessor resolveInjectedNode(Template template,
            Stack stack, SpecificationNode original, SpecificationNode injected,
            Set divided) {
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
            URI defaultURI = mapping.getNamespaceURI();
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
        Iterator it = injected.iterateChildNode();
        if (it.hasNext() == false) {
            return processor;
        }
        // "injected" node has children, nested node definition on .mayaa
        stack.push(processor);
        while (it.hasNext()) {
            SpecificationNode childNode = (SpecificationNode) it.next();
            saveToCycle(original, childNode);
            TemplateProcessor childProcessor =
                resolveInjectedNode(
                    template, stack, original, childNode, divided);
            if (childProcessor instanceof DoBodyProcessor) {
                stack.push(childProcessor);
                walkParsedTree(template, stack, original, divided);
                stack.pop();
            }
        }
        stack.pop();
        saveToCycle(original, injected);
        return findConnectPoint(processor);
    }

    /**
     * ノードを最適化する。
     * 親要素{@link parent}に子要素のリスト{@link collector}をすべて追加する。
     * その際、静的部分と動的部分を分割可能なものがあれば分割し、一度分割したという
     * マーキング的な意味合いで{@link divided}に追加する。
     * @param idGenerator
     * @param parent 親要素
     * @param collector 子要素
     * @param divided 静的部分と動的部分を分割し
     */
    protected void optimizeProcessors(SequenceIDGenerator idGenerator,
            ProcessorTreeWalker parent, List collector, Set divided) {
        List expands = new ArrayList();
        Iterator it = collector.iterator();
        while (it.hasNext()) {
            ProcessorTreeWalker processor = (ProcessorTreeWalker) it.next();
            if (processor == null) {
                throw new IllegalStateException("processor is null");
            }
            if (processor instanceof OptimizableProcessor
                    && divided.contains(processor) == false) {
                ProcessorTreeWalker[] processors =
                    ((OptimizableProcessor) processor).divide(idGenerator);
                for (int i = 0; i < processors.length; i++) {
                    expands.add(processors[i]);
                }
                divided.add(processor);
            } else {
                expands.add(processor);
            }
        }

        List packs = new ArrayList();
        for (int i = 0; i < expands.size(); i++) {
            ProcessorTreeWalker node = (ProcessorTreeWalker) expands.get(i);
            node = convertCharactersProcessor(node, idGenerator);

            if (packs.size() > 0 && node instanceof LiteralCharactersProcessor) {
                Object last = packs.get(packs.size()-1);
                if (last instanceof LiteralCharactersProcessor) {
                    LiteralCharactersProcessor rawLast =
                        (LiteralCharactersProcessor) last;
                    rawLast.setText(rawLast.getText() +
                            ((LiteralCharactersProcessor) node).getText());
                } else {
                    packs.add(node);
                }
            } else {
                packs.add(node);
            }
        }
        for (int i = 0; i < packs.size(); i++) {
            ProcessorTreeWalker child = (ProcessorTreeWalker)packs.get(i);
            child.setParentProcessor(parent);
            parent.addChildProcessor(child);
        }
    }

    protected ProcessorTreeWalker convertCharactersProcessor(
            ProcessorTreeWalker processor, SequenceIDGenerator idGenerator) {
        if (processor instanceof CharactersProcessor) {
            CharactersProcessor cnode = (CharactersProcessor)processor;
            if (cnode.getText() != null
                    && cnode.getText().getValue() != null
                    && cnode.getText().getValue().isLiteral()) {
                LiteralCharactersProcessor literalProcessor =
                    new LiteralCharactersProcessor(
                            cnode.getText().getValue().getScriptText());
                BuilderUtil.characterProcessorCopy(
                        cnode, literalProcessor, idGenerator);
                processor = literalProcessor;
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

    protected void walkParsedTree(
            Template template, Stack stack, NodeTreeWalker original,
            Set divided) {
        if (original == null) {
            throw new IllegalArgumentException();
        }
        Iterator it = original.iterateChildNode();
        while (it.hasNext()) {
            SpecificationNode child;
            try {
                child = (SpecificationNode) it.next();
            } catch(ConcurrentModificationException e) {
                LOG.error("original.childNodes をイテレート中に更新されてしまった。", e);
                throw e;
            }
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
            ProcessorTreeWalker processor = resolveInjectedNode(
                    template, stack, child, injected, divided);
            if (processor != null) {
                stack.push(processor);
                walkParsedTree(template, stack, child, divided);
                stack.pop();
            }
        }
        if (isOptimizeEnabled()) {
            ProcessorTreeWalker parent = (ProcessorTreeWalker) stack.peek();
            int count = parent.getChildProcessorSize();
            if (count > 0) {
                List childProcessors = new ArrayList();
                for (int i = 0; i < count; i++) {
                    childProcessors.add(parent.getChildProcessor(i));
                }
                parent.clearChildProcessors();
                optimizeProcessors(template, parent, childProcessors, divided);
            }
        }
    }

    protected boolean isOutputTemplateWhitespace() {
        return _outputTemplateWhitespace;
    }
    protected void setOutputTemplateWhitespace(boolean outputTemplateWhitespace) {
        _outputTemplateWhitespace = outputTemplateWhitespace;
    }
    protected boolean isSSIIncludeReplacementEnabled() {
        return _isSSIIncludeReplacementEnabled;
    }
    protected void setSSIIncludeReplacementEnabled(boolean isSSIIncludeReplacementEnabled) {
        _isSSIIncludeReplacementEnabled = isSSIIncludeReplacementEnabled;
    }
    protected boolean isOptimizeEnabled() {
        return _optimize;
    }
    protected void setOptimizeEnabled(boolean optimize) {
        _optimize = optimize;
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if (OUTPUT_TEMPLATE_WHITESPACE.equals(name)) {
            setOutputTemplateWhitespace(ObjectUtil.booleanValue(value, true));
        } else if (OPTIMIZE.equals(name)) {
            setOptimizeEnabled(ObjectUtil.booleanValue(value, true));
        } else if (REPLACE_SSI_INCLUDE.equals(name)) {
            setSSIIncludeReplacementEnabled(ObjectUtil.booleanValue(value, false));
        } else if (DEFAULT_CHARSET.equals(name)) {
            try {
                "".getBytes(value);
                _htmlReaderPool.setDefaultCharset(value);
            } catch (UnsupportedEncodingException e) {
                String message =
                    StringUtil.getMessage(TemplateBuilderImpl.class, 0, value);
                LOG.warn(message, e);
            }
        } else if (BALANCE_TAG.equals(name)) {
        	_htmlReaderPool.setBalanceTag(ObjectUtil.booleanValue(value, true));
        }
        super.setParameter(name, value);
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

        Set divided = null;
        if (isOptimizeEnabled()) {
            divided = new HashSet();
        }
        walkParsedTree(template, stack, template, divided);
        if (template.equals(stack.peek()) == false) {
            throw new IllegalStateException();
        }
        saveToCycle(template, template);
    }


    // deserialization
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        _chain = new DefaultInjectionChain();
    }

    // support class --------------------------------------------------

    protected static class HtmlReaderPool extends XMLReaderPool {

        private static final long serialVersionUID = -5203349759797583368L;
        private String _defaultCharset = TEMPLATE_DEFAULT_CHARSET;
        private boolean _balanceTag = true;

        protected void setDefaultCharset(String charset) {
            _defaultCharset = charset;
        }

        protected void setBalanceTag(boolean balanceTag) {
        	_balanceTag = balanceTag;
        }

        protected Object createObject() {
            return new TemplateParser(new TemplateScanner(), _defaultCharset, _balanceTag);
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

}
