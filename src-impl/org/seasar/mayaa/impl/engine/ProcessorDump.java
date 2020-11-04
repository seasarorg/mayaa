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
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
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

    private static final long serialVersionUID = 8044884422670533823L;

    private static final PrintStream DEFAULT_OUT = System.out;

    private PrintStream _out = DEFAULT_OUT;
    private String _headerLine = "DUMPSTART =======================================";
    private String _footerLine = "DUMPEND =========================================";
    private String _indentChar = "    ";
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

    public void printSource(Page topLevelPage) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        RequestScope request = cycle.getRequestScope();
        String requestedSuffix = request.getRequestedSuffix();
        String extension = request.getExtension();

        Template template = topLevelPage.getTemplate(requestedSuffix, extension);

        print(_headerLine);
        print(template.getSystemID());
        printTree(0, template);
        print(_footerLine);
    }

    protected void printTree(int indentCount, ProcessorTreeWalker walker) {
// TODO レイアウト機能を使う場合
        int childSize = walker.getChildProcessorSize();
        if (walker instanceof TemplateProcessor) {
            printTag(indentCount, (TemplateProcessor) walker,
                    "<", (childSize > 0 ? ">" : " />"), true);
        }
        if (childSize > 0) {
            for (int i = 0; i < childSize; i++) {
                printTree(indentCount + 1, walker.getChildProcessor(i));
            }
            if (walker instanceof TemplateProcessor) {
                printTag(indentCount, (TemplateProcessor) walker, "</", ">", false);
            }
        }
    }

    protected void printTag(
            int indentCount, TemplateProcessor processor,
            String start, String end, boolean printAttributes) {
        if (_printContents && processor instanceof LiteralCharactersProcessor) {
            print(((LiteralCharactersProcessor) processor).getText());
// TODO insert の場合
// TODO echo の場合
        } else {
            StringBuilder sb = new StringBuilder(128);

            for (int i = 0; i < indentCount; i++) {
                sb.append(_indentChar);
            }

            sb.append(start);

            SpecificationNode node = getNode(processor);
            String prefix = "";
            PrefixMapping mapping =
                node.getMappingFromURI(node.getQName().getNamespaceURI(), true);
            if (mapping != null && StringUtil.hasValue(mapping.getPrefix())) {
                prefix = mapping.getPrefix() + ":";
            }
            sb.append(prefix);
            sb.append(node.getQName().getLocalName());

            if (printAttributes) {
                writeAttributes(sb, processor);
            }

            sb.append(end);

            // TODO original
            // sb.append("<!-- mayaa/original[");
            // sb.append(processor.getOriginalNode().getQName().getLocalName());
            // sb.append("] -->");

            print(sb.toString());
        }
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

    protected void writeAttributes(StringBuilder sb, TemplateProcessor processor) {
        if (processor instanceof ElementProcessor) {
            writeElementAttributes(sb, (ElementProcessor) processor);
        } else {
            SpecificationNode node = processor.getInjectedNode();
            URI namespace = node.getQName().getNamespaceURI();
            for (Iterator<NodeAttribute> it = processor.getInjectedNode().iterateAttribute();
                    it.hasNext();) {
                NodeAttribute prop = it.next();
                QName propName = prop.getQName();
                String prefix = "";
                if (namespace.equals(propName.getNamespaceURI()) == false) {
                    PrefixMapping mapping =
                        node.getMappingFromURI(propName.getNamespaceURI(), true);
                    if (mapping != null && StringUtil.hasValue(mapping.getPrefix())) {
                        prefix = mapping.getPrefix() + ":";
                    }
                }
                writeProcessorAttributeString(
                        sb, prefix, propName.getLocalName(), prop.getValue());
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
            if (hasProcesstimeProperty(prop) == false
                    && prop.getValue().isLiteral() == false) {
                appendAttributeString(sb, prop.getName(), prop.getValue());
            }
        }
        for (Iterator<Serializable> it = processor.iterateInformalProperties(); it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            if (hasProcesstimeProperty(prop) == false
                    && prop.getValue().isLiteral()) {
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
	                Object result = script.execute(null);
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
            StringBuilder sb, String prefix, String localName, Object value) {
        sb.append(" ");
        sb.append(prefix);
        sb.append(localName);
        sb.append("=\"");
        if (value instanceof CompiledScript) {
            CompiledScript script = (CompiledScript) value;
            sb.append(script.getScriptText());
        } else {
            sb.append(value.toString());
        }
        sb.append("\"");
    }

    protected void print(String value) {
        _out.println(value);
    }

}
