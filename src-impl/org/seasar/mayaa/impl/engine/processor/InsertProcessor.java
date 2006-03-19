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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.TemplateRenderer;
import org.seasar.mayaa.engine.processor.InformalPropertyAcceptable;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.RenderNotCompletedException;
import org.seasar.mayaa.impl.engine.RenderUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class InsertProcessor
        extends TemplateProcessorSupport implements CONST_IMPL,
        InformalPropertyAcceptable, TemplateRenderer {

    private static final long serialVersionUID = -1240398725406503403L;

    private String _path;
    private String _name;
    private SoftReference _page;
    private String _suffix;
    private String _extension;
    private List _attributes;
    private boolean _rendering;
    private Map _renderingParams;

    // MLD property, required
    public void setPath(String path) {
        _path = path;
    }

    // MLD property
    public void setName(String name) {
        _name = name;
    }

    // MLD method
    public void addInformalProperty(PrefixAwareName name, Object attr) {
        if (_attributes == null) {
            _attributes = new ArrayList();
        }
        _attributes.add(attr);
    }

    public Class getPropertyClass() {
        return ProcessorProperty.class;
    }

    public Class getExpectedClass() {
        return Object.class;
    }

    public List getInformalProperties() {
        if (_attributes == null) {
            _attributes = new ArrayList();
        }
        return _attributes;
    }

    protected Page getPage() {
        Page page = null;
        if (_page != null) {
            page = (Page) _page.get();
            if (page == null) {
                _page = null;
            }
        }
        return page;
    }

    protected void preparePage() {
        if (StringUtil.hasValue(_path)) {
            synchronized (this) {
                Page page = getPage();
                if (page == null) {
                    Engine engine = ProviderUtil.getEngine();
                    String suffixSeparator =
                        engine.getParameter(SUFFIX_SEPARATOR);
                    String[] pagePath =
                        StringUtil.parsePath(_path, suffixSeparator);

                    String sourcePath =
                        EngineUtil.getSourcePath(getParentProcessor());
                    page = engine.getPage(
                            StringUtil.adjustRelativePath(sourcePath, pagePath[0]));
                    _page = new SoftReference(page);
                    _suffix = pagePath[1];
                    _extension = pagePath[2];
                }
            }
        }
    }

    public Map getRenderingParameters() {
        if (_rendering) {
            return _renderingParams;
        }
        return null;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        preparePage();
        Page renderPage = getPage();
        String requestedSuffix = _suffix;
        String extension = _extension;
        boolean fireEvent = true;
        if (renderPage == null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            renderPage = topLevelPage;
            RequestScope request = cycle.getRequestScope();
            requestedSuffix = request.getRequestedSuffix();
            extension = request.getExtension();
            fireEvent = false;
        }
        if (renderPage == null) {
            throw new IllegalStateException();
        }
        renderPage.checkTimestamp();

        _renderingParams = new LinkedHashMap();
        for (int i = 0; i < getInformalProperties().size(); i++) {
            Object object = getInformalProperties().get(i);
            if (object instanceof ProcessorProperty) {
                ProcessorProperty prop = (ProcessorProperty)object;
                _renderingParams.put(
                        prop.getName().getQName().getLocalName(),
                        prop.getValue().execute(null));
            }
        }

        _rendering = true;
        ProcessStatus ret = RenderUtil.renderPage(fireEvent, this,
                getVariables(), renderPage, requestedSuffix, extension);
        if (ret == null) {
            Response response = CycleUtil.getResponse();
            if (response.getWriter().isDirty() == false) {
                throw new RenderNotCompletedException(
                        renderPage.getPageName(), extension);
            }
        }
        if (ret == ProcessStatus.EVAL_PAGE) {
            ret = ProcessStatus.SKIP_BODY;
        }
        return ret;
    }

    public ProcessStatus doEndProcess() {
        _rendering = false;
        return super.doEndProcess();
    }

    // TemplateRenderer implements ----------------------------------

    protected DoRenderProcessor findDoRender(
            ProcessorTreeWalker proc, String name) {
        DoRenderProcessor doRender = null;
        for (int i = 0; i < proc.getChildProcessorSize(); i++) {
            ProcessorTreeWalker child = proc.getChildProcessor(i);
            if (child instanceof DoRenderProcessor) {
                doRender =  (DoRenderProcessor) child;
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
        for (int i = templates.length - 1; 0 <= i; i--) {
            templates[i].checkTimestamp();
            DoRenderProcessor doRender = findDoRender(templates[i], name);
            if (doRender != null) {
                return doRender;
            }
        }
        return null;
    }

    protected TemplateProcessor getRenderRoot(DoRenderProcessor doRender) {
        if (doRender.isReplace() == false) {
            ProcessorTreeWalker duplecated = doRender.getParentProcessor();
            if (duplecated == null
                    || duplecated instanceof TemplateProcessor == false) {
                return doRender;
            }
            return (TemplateProcessor) duplecated;
        }
        return doRender;
    }

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
        doRender.pushInsertProcessor(this);
        ProcessStatus ret =
                RenderUtil.renderTemplateProcessor(topLevelPage, insertRoot);
        doRender.popInsertProcessor();
        return ret;
    }

}
