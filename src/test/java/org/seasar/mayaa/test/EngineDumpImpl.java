/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.test;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.impl.engine.EngineImpl;
import org.seasar.mayaa.impl.engine.PageNotFoundException;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class EngineDumpImpl extends EngineImpl {

    private static final long serialVersionUID = -1901417389019083666L;

    private static final Log LOG = LogFactory.getLog(EngineDumpImpl.class);

    protected void doPageService(
            ServiceCycle cycle, Map<?, ?> pageScopeValues, boolean pageFlush) {
        super.doPageService(cycle, pageScopeValues, pageFlush);

        RequestScope request = cycle.getRequestScope();
        String pageName = request.getPageName();
        String requestedSuffix = request.getRequestedSuffix();
        String extension = request.getExtension();
        Page page = getPage(pageName);

        print(pageName);

        try {
            Template template = page.getTemplate(requestedSuffix, extension);
            for (int i = 0; i < template.getChildProcessorSize(); i++) {
                printTree(0, template.getChildProcessor(i));
            }
        } catch (PageNotFoundException ignore) {
            // do nothing
        }
    }

    protected void printTree(int indent, ProcessorTreeWalker walker) {
        int childSize = walker.getChildProcessorSize();
        if (walker instanceof TemplateProcessor) {
            printTag(indent, (TemplateProcessor) walker,
                    "<", childSize > 0 ? ">" : " />");
        }
        if (childSize > 0) {
            for (int i = 0; i < childSize; i++) {
                printTree(indent + 4, walker.getChildProcessor(i));
            }
            if (walker instanceof TemplateProcessor) {
                printTag(indent, (TemplateProcessor) walker, "</", ">");
            }
        }
    }

    protected void printTag(
            int indent, TemplateProcessor processor, String start, String end) {
        StringBuffer sb = new StringBuffer(128);
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        sb.append(start);
        sb.append(processor.getProcessorDefinition().getName());
        sb.append(" (");
        sb.append(processor.getOriginalNode().getQName().getLocalName());
        sb.append(")");
        sb.append(end);

        print(sb.toString());
    }

    protected void print(String value) {
//        System.out.println(value);
        LOG.info(value);
    }

}
