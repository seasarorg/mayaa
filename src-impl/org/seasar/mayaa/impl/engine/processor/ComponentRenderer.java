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

import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.TemplateRenderer;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.impl.engine.RenderUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * コンポーネントを描画します。
 * {@link TemplateRenderer}として動作し、指定した名前を持つdoRenderを見つけて
 * その内側のみをレンダリングします。
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class ComponentRenderer implements TemplateRenderer {

    private InsertProcessor _insert;
    private String _name;

    /**
     * InsertProcessorとコンポーネント名を指定するコンストラクタ。
     *
     * @param insert コンポーネントをレンダリングするInsertProcessor
     * @param name コンポーネント名
     */
    public ComponentRenderer(InsertProcessor insert, String name) {
        _insert = insert;
        _name = name;
    }

    // TemplateRenderer implements ----------------------------------

    public ProcessStatus renderTemplate(
            Page topLevelPage, Template[] templates) {
        if (topLevelPage == null || templates == null
                || templates.length == 0) {
            throw new IllegalArgumentException();
        }
        DoRenderProcessor doRender = findDoRender(templates, _name);
        if (doRender == null) {
            throw new DoRenderNotFoundException(_name);
        }

        TemplateProcessor insertRoot = getRenderRoot(doRender);

        if (_insert != null) {
            // doBaseで使うためにInsertProcessorをスタックへ積む
            doRender.pushInsertProcessor(_insert);
        }
        ProcessStatus ret =
                RenderUtil.renderTemplateProcessor(topLevelPage, insertRoot);
        if (_insert != null) {
            doRender.popInsertProcessor();
        }
        return ret;
    }

    protected DoRenderProcessor findDoRender(
            ProcessorTreeWalker proc, String name) {
        DoRenderProcessor doRender = null;
        for (int i = 0; i < proc.getChildProcessorSize(); i++) {
            ProcessorTreeWalker child = proc.getChildProcessor(i);
            if (child instanceof DoRenderProcessor) {
                doRender = (DoRenderProcessor) child;
                if (StringUtil.isEmpty(name)
                        || name.equals(doRender.getName())) {
                    break;
                }
            }
            doRender = findDoRender(child, name);
            if (doRender != null) {
                break;
            }
        }
        return doRender;
    }

    protected DoRenderProcessor findDoRender(
            Template[] templates, String name) {
        synchronized (ProviderUtil.getEngine()) {
            for (int i = templates.length - 1; 0 <= i; i--) {
                DoRenderProcessor doRender = findDoRender(templates[i], name);
                if (doRender != null) {
                    return doRender;
                }
            }
        }
        return null;
    }

    protected TemplateProcessor getRenderRoot(DoRenderProcessor doRender) {
        if (doRender.isReplace() == false) {
            ProcessorTreeWalker duplecated = doRender.getParentProcessor();
            if (duplecated == null
                    || duplecated instanceof TemplateProcessor == false) {
                throw new IllegalStateException();
            }
            return (TemplateProcessor) duplecated;
        }
        return doRender;
    }

}
