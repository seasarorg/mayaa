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
package org.seasar.mayaa.impl.engine;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Iterator;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.processor.ElementProcessor;
import org.seasar.mayaa.impl.engine.processor.LiteralCharactersProcessor;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class ProcessorDump extends ElementProcessor {
    // TODO EchoProcessorを継承して使っている部分をまとめる
    // TODO ServiceProviderで差し替え可能にする

    private static final long serialVersionUID = 7884837912520558752L;

    private static final PrintStream DEFAULT_OUT = System.out;

    private PrintStream _out = DEFAULT_OUT;
    private String _headerLine = "DUMPSTART =======================================";
    private String _footerLine = "DUMPEND =========================================";
    private String _indentChar = "  ";
    private boolean _printContents = false;

    public void setOut(PrintStream out) {
        _out = out;
    }

    public void setIndentChar(String indentChar) {
        if (indentChar == null) {
            throw new IllegalStateException("indentChar is null");
        }

        _indentChar = indentChar;
    }

    public void setPrintContents(boolean printContents) {
        _printContents = printContents;
    }

    /**
     * 最上位のダンプメソッド
     * @param topLevelPage
     */
    public void printSource(Page topLevelPage) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        RequestScope request = cycle.getRequestScope();
        String requestedSuffix = request.getRequestedSuffix();
        String extension = request.getExtension();

        Template template = topLevelPage.getTemplate(requestedSuffix, extension);

        print(_headerLine);
        print(template.getSystemID());
        print("PROCESSOR TREE --------------------------------");
        printProcessorTree(0, template);
        print("ORIGINAL NODE TREE --------------------------------");
        printSpecificationNodeTree(0, template);
        print("PAGE NODE TREE --------------------------------");
        printSpecificationNodeTree(0, topLevelPage);
        print(_footerLine);
    }

    protected void printProcessorTree(int indentCount, ProcessorTreeWalker walker) {
        // TODO レイアウト機能を使う場合
        int childSize = walker.getChildProcessorSize();
        if (walker instanceof TemplateProcessor) {
            printTag(indentCount, (TemplateProcessor) walker,
                    "<", (childSize > 0 ? ">" : " />"), true);
        }
        if (childSize > 0) {
            for (int i = 0; i < childSize; i++) {
                printProcessorTree(indentCount + 1, walker.getChildProcessor(i));
            }
            // if (walker instanceof TemplateProcessor) {
            //     printTag(indentCount, (TemplateProcessor) walker, "</", ">", false);
            // }
        }
    }

    protected void printSpecificationNodeTree(int indentCount, NodeTreeWalker walker) {
        // TODO レイアウト機能を使う場合
        int childSize = walker.getChildNodeSize();
        if (walker instanceof SpecificationNode) {
            printNode(indentCount, (SpecificationNode) walker,
                    "<", (childSize > 0 ? ">" : " />"), true);
        } else {
            print("Non SpecificationNode:" + walker.getClass().getSimpleName());
        }
        if (childSize > 0) {
            for (int i = 0; i < childSize; i++) {
                printSpecificationNodeTree(indentCount + 1, walker.getChildNode(i));
            }
            if (walker instanceof SpecificationNode) {
                printNode(indentCount, (SpecificationNode) walker, "</", ">", false);
            } else {
                print("Non SpecificationNode:" + walker.getClass().getSimpleName());
            }
        }
    }
    protected void printNode(
        int indentCount, SpecificationNode node,
        String start, String end, boolean printAttributes) {

        StringBuilder sb = new StringBuilder(128);
        sb.append(node.getSystemID());
        sb.append(':');
        sb.append(node.getLineNumber());
        sb.append("\t");

        for (int i = 0; i < indentCount; i++) {
            sb.append(_indentChar);
        }

        sb.append(start);
        sb.append(prefixedQName(node, node.getQName()));

        if (printAttributes) {
            writeAttributes(sb, node);
        }

        sb.append(end);
        print(sb.toString());
    }

    protected void printTag(
            int indentCount, TemplateProcessor processor,
            String start, String end, boolean printAttributes) {

        StringBuilder sb = new StringBuilder(128);
        sb.append(processor.getInjectedNode().getSystemID());
        sb.append(':');
        sb.append(processor.getInjectedNode().getLineNumber());
        sb.append("\t");
        for (int i = 0; i < indentCount; i++) {
            sb.append(_indentChar);
        }


        if (processor instanceof LiteralCharactersProcessor) {
            final String value = ((LiteralCharactersProcessor) processor).getText();
            sb.append("LiteralCharacters: \"");
            if (_printContents) {
                sb.append(value.replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r").replaceAll("\t", "\\\\t"));
            } else {
                sb.append("(omit) length:").append(value.length());
            }
            sb.append("\"");

// TODO insert の場合
// TODO echo の場合
        } else {
            final String processorName = processor.getClass().getSimpleName().replace("Processor", "");
            sb.append(processorName).append(": ");
            sb.append(start);

            SpecificationNode node = getNode(processor);
            sb.append(prefixedQName(node, node.getQName()));
            if (processor instanceof ElementProcessor) {
                ElementProcessor p = (ElementProcessor) processor;
                sb.append(" name=\"").append(prefixedQName(p.getName())).append("\"");
            }

            if (printAttributes) {
                writeAttributes(sb, processor);
            }

            sb.append(end);
        }

        // TODO original
        // sb.append("\n");
        // for (int i = 0; i < indentCount; i++) {
        //     sb.append(_indentChar);
        // }
            sb.append("  original[");
            sb.append(processor.getOriginalNode().getSystemID());
            sb.append(':');
        sb.append(processor.getOriginalNode().getSequenceID());
            // sb.append(processor.getOriginalNode().getId());
            sb.append("]");

        print(sb.toString());
    }

    protected SpecificationNode getNode(TemplateProcessor processor) {
        if (processor instanceof ElementProcessor) {
            ElementProcessor ep = (ElementProcessor) processor;
            if (ep.isDuplicated()) {
                // TODO templateElementもoriginalNodeにする
                // ep.getInjectedNode().getQName().equals(QM_TEMPLATE_ELEMENT)
                return processor.getOriginalNode();
            }
        }
        return processor.getInjectedNode();
    }

    protected void writeAttributes(StringBuilder sb, SpecificationNode node) {
        for (Iterator<NodeAttribute> it = node.iterateAttribute(); it.hasNext();) {
            NodeAttribute prop = it.next();
            QName propName = prop.getQName();
            final String name = prefixedQName(node, propName);
            writeProcessorAttributeString(sb, name, prop.getValue());
        }
    }

    protected void writeAttributes(StringBuilder sb, TemplateProcessor processor) {
        if (processor instanceof ElementProcessor) {
            writeElementAttributes(sb, (ElementProcessor) processor);
        } else {
            SpecificationNode node = processor.getInjectedNode();
            for (Iterator<NodeAttribute> it = processor.getInjectedNode().iterateAttribute(); it.hasNext();) {
                NodeAttribute prop = it.next();
                QName propName = prop.getQName();
                final String name = prefixedQName(node, propName);
                writeProcessorAttributeString(sb, name, prop.getValue());
            }
        }
    }

    protected void writeElementAttributes(StringBuilder sb, ElementProcessor processor) {
        for (Iterator<ProcessorProperty> it = processor.iterateProcesstimeProperties(); it.hasNext();) {
            ProcessorProperty prop = it.next();
            appendAttributeString(sb, prop.getName(), prop.getValue());
        }
        for (Iterator<Serializable> it = processor.iterateInformalProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            if (hasProcesstimeProperty(prop) == false && prop.getValue().isLiteral() == false) {
                appendAttributeString(sb, prop.getName(), prop.getValue());
            }
        }
        for (Iterator<Serializable> it = processor.iterateInformalProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            if (hasProcesstimeProperty(prop) == false && prop.getValue().isLiteral()) {
                appendAttributeString(sb, prop.getName(), prop.getValue());
            }
        }
    }

    protected void appendAttributeString(
            StringBuilder buffer, PrefixAwareName propName, Object value) {
        QName qName = propName.getQName();
        if (URI_MAYAA.equals(qName.getNamespaceURI())) {
            return;
        }
        // TODO 正しく描画する
//        if (getInjectedNode().getQName().equals(QM_DUPLECATED)) {
//            if (getChildProcessorSize() > 0
//                    && getChildProcessor(0) instanceof JspProcessor) {
//                JspProcessor processor = (JspProcessor)getChildProcessor(0);
//                URI injectNS = processor.getInjectedNode().getQName().getNamespaceURI();
//                if (injectNS == qName.getNamespaceURI()) {
//                    return;
//                }
//            }
//        }

        String attrPrefix = propName.getPrefix();
        if (StringUtil.hasValue(attrPrefix)) {
//            attrPrefix = getResolvedPrefix(propName);
            if (StringUtil.hasValue(attrPrefix)) {
                attrPrefix = attrPrefix + ":";
            }
        }
        StringBuilder temp = new StringBuilder();
        temp.append(" ");
        temp.append(attrPrefix);
        temp.append(qName.getLocalName());
        /* 2007.03.15 valueがnullの場合を許容する(値なしを作成可能に) */
        if (value != null) {
	        temp.append("=\"");
	        if (value instanceof CompiledScript) {
	            CompiledScript script = (CompiledScript) value;
	            if (CycleUtil.isDraftWriting()) {
	                temp.append(script.getScriptText());
	            } else {
try { // TODO 修正する [JIRA: MAYAA-5]
	                Object result = script.execute(String.class, null);
	                if (StringUtil.isEmpty(result)) {
	                    return;
	                }
	                temp.append(result);
} catch (Throwable ignore) {
	    // no-op
}
	            }
	        } else {
	            temp.append(value.toString());
	        }
	        temp.append("\"");
        }
        buffer.append(temp.toString());
    }

    protected void writeProcessorAttributeString(
            StringBuilder sb, String name, Object value) {
        sb.append(" ");
        sb.append(name);
        sb.append("=\"");
        if (value instanceof CompiledScript) {
            CompiledScript script = (CompiledScript) value;
            sb.append(script.getScriptText());
        } else {
            sb.append(value.toString().replaceAll("\n", "\\\\n"));
        }
        sb.append("\"");
    }

    protected void print(String value) {
        _out.println(value);
    }

    private String prefixedQName(SpecificationNode node, QName qName) {
        PrefixMapping mapping = node.getMappingFromURI(qName.getNamespaceURI(), true);

        if (mapping != null && StringUtil.hasValue(mapping.getPrefix())) {
            return mapping.getPrefix() + ":" + qName.getLocalName();
        }

        if (CONST_IMPL.URI_MAYAA.equals(qName.getNamespaceURI())) {
            return "M:" + qName.getLocalName();
        }
        if (CONST_IMPL.URI_XHTML.equals(qName.getNamespaceURI())) {
            return "H:" + qName.getLocalName();
        }
        if (CONST_IMPL.URI_HTML.equals(qName.getNamespaceURI())) {
            return "H4:" + qName.getLocalName();
        }

        return qName.toString();
    }

    private String prefixedQName(PrefixAwareName name) {
        QName qName = name.getQName();

        if (StringUtil.hasValue(name.getPrefix())) {
            return name.getPrefix() + ":" + qName.getLocalName();
        }

        if (CONST_IMPL.URI_MAYAA.equals(qName.getNamespaceURI())) {
            return "M:" + qName.getLocalName();
        }
        if (CONST_IMPL.URI_XHTML.equals(qName.getNamespaceURI())) {
            return "H:" + qName.getLocalName();
        }
        if (CONST_IMPL.URI_HTML.equals(qName.getNamespaceURI())) {
            return "H4:" + qName.getLocalName();
        }

        return qName.toString();
    }

}
