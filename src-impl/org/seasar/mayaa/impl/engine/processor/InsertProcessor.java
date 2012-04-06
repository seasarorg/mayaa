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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.InformalPropertyAcceptable;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.VirtualPropertyAcceptable;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.DefaultCycleLocalInstantiator;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.PageImpl;
import org.seasar.mayaa.impl.engine.RenderNotCompletedException;
import org.seasar.mayaa.impl.engine.RenderUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class InsertProcessor
        extends TemplateProcessorSupport
        implements CONST_IMPL, InformalPropertyAcceptable, VirtualPropertyAcceptable {

    private static final long serialVersionUID = -945900326182620192L;

    private static final String RENDERING_INSERT_CHAIN =
        InsertProcessor.class.getName() + "#renderingInsertChain";
    private static final String INSERT_PARAMS =
        InsertProcessor.class.getName() + "#insertParams";
    static {
        CycleUtil.registVariableFactory(RENDERING_INSERT_CHAIN, new DefaultCycleLocalInstantiator() {
            public Object create(Object[] params) {
                return new Stack/*<InsertRenderingParams>*/();
            }
        });
        CycleUtil.registVariableFactory(INSERT_PARAMS, new DefaultCycleLocalInstantiator() {
            public Object create(Object owner, Object[] params) {
                return new Stack/*<InsertRenderingParams>*/();
            }
        });
    }

    class PathPart {
    	String _pathValue;
    	String _pageName;
    	String _suffix;
    	String _extension;
    	boolean _needAdjustPath;
    }

    private ProcessorProperty _path;
    private ProcessorProperty _name;
    private transient PathPart _pathPart;

    private List/*<Serializable(ProcessorProperty or PrefixAwareName)>*/
                    _attributes;

    /**
     * 相対パスを解決、分解して拡張子などをあらかじめ取得する。
     */
    public void initialize() {
    	if (_path != null && _path.getValue() != null) {
	        if (_path.getValue().isLiteral()) {
	        	_pathPart = parsePath(_path.getValue().getScriptText());
	        }
    	}
    }

    private PathPart parsePath(String path) {
        Engine engine = ProviderUtil.getEngine();
        String suffixSeparator = engine.getParameter(SUFFIX_SEPARATOR);

        PathPart result = new PathPart();
    	if (path != null) {
    		String[] pagePath = StringUtil.parsePath(path, suffixSeparator);

	        String sourcePath = EngineUtil.getSourcePath(getStaticParentProcessor());

	        result._pathValue = path;
	        result._pageName = StringUtil.adjustRelativePath(sourcePath, pagePath[0]);
	        result._suffix = pagePath[1];
	        result._extension = pagePath[2];
	        if (path.startsWith("/") == false && path.startsWith("./") == false) {
	        	result._needAdjustPath = true;
	        }
    	}
    	return result;
    }

    // MLD property, required
    public void setPath(ProcessorProperty path) {
        if (path == null || path.getValue() == null) {
            throw new IllegalArgumentException();
        }
        String pathScript = path.getValue().getScriptText();
		if (StringUtil.isEmpty(pathScript)) {
			throw new IllegalArgumentException();
		}
        _path = path;
    }

    // MLD property
    public void setName(ProcessorProperty name) {
        if (name != null) {
            _name = name;
        }
    }

    // MLD method
    public void addInformalProperty(PrefixAwareName name, Serializable attr) {
        if (_attributes == null) {
            _attributes = new ArrayList();
        }
        _attributes.add(attr);
    }

    /**
     * @deprecated 1.1.9 代わりに{@link #getInformalPropertyClass()}を使う
     */
    public Class getPropertyClass() {
        return getInformalPropertyClass();
    }

    /**
     * @deprecated 1.1.9 代わりに{@link #getInformalPropertyClass()}を使う
     */
    public Class getExpectedClass() {
        return getInformalExpectedClass();
    }

    public Class getInformalPropertyClass() {
        return ProcessorProperty.class;
    }

    public Class getInformalExpectedClass() {
        return Object.class;
    }

    /**
     * VirtualPropertyもInformalPropertyとして扱います。
     * @param name プロパティ名。
     * @param property プロパティ値。
     */
    public void addVirtualProperty(PrefixAwareName name, Serializable attr) {
        addInformalProperty(name, attr);
    }

    /**
     * VirtualPropertyもInformalPropertyとして扱います。
     */
    public Class getVirtualPropertyClass() {
        return getInformalPropertyClass();
    }

    /**
     * VirtualPropertyもInformalPropertyとして扱います。
     */
    public Class getVirtualExpectedClass() {
        return getInformalExpectedClass();
    }

    public List getInformalProperties() {
        if (_attributes == null) {
            _attributes = new ArrayList();
        }
        return _attributes;
    }

    private PathPart getPathPart() {
        PathPart pathPart = _pathPart;
        if (pathPart == null) {
        	if (_path == null || StringUtil.isEmpty(_path.getValue().getScriptText())) {
        		return new PathPart();
        	}
       		String path = StringUtil.valueOf(_path.getValue().execute(null));
       		if (StringUtil.isEmpty(path)) {
       			return new PathPart();
       		}
       		pathPart = parsePath(path);
        }
        return pathPart;
    }

    private Page pathPartToPage(PathPart pathPart) {
    	if (pathPart._pageName == null) {
    		return null;
    	}
        if (pathPart._needAdjustPath) {
            // "/" 始まりでも "./" 始まりでもない場合は相対パス解決をする
            String sourcePath = EngineUtil.getSourcePath(getStaticParentProcessor());
            return ProviderUtil.getEngine().getPage(
                    StringUtil.adjustRelativePath(sourcePath, "./" + pathPart._pageName));
        }
        return ProviderUtil.getEngine().getPage(pathPart._pageName);
    }

    /**
     * pathを指定されていない場合(レイアウト側)はnullを返す。
     * pathが指定されている場合(コンポーネント利用)はEngineからPageを
     * 取得して返す。その際、必要ならばパスの自動解決をする。
     *
     * @return ページオブジェクト
     */
    protected Page getPage() {
    	return pathPartToPage(getPathPart());
    }

    protected Stack getRenderingParams() {
        return (Stack) CycleUtil.getLocalVariable(INSERT_PARAMS, this, null);
    }

    protected InsertRenderingParams pushRenderingParams() {
        InsertRenderingParams newParams = new InsertRenderingParams();
        return (InsertRenderingParams) getRenderingParams().push(newParams);
    }

    protected InsertRenderingParams peekRenderingParams() {
        return (InsertRenderingParams) getRenderingParams().peek();
    }

    protected InsertRenderingParams popRenderingParams() {
        return (InsertRenderingParams) getRenderingParams().pop();
    }

    public Map getRenderingParameters() {
        InsertRenderingParams params = peekRenderingParams();
        if (params.isRendering()) {
            return params.getParams();
        }
        return null;
    }

    protected void invokeBeforeRenderComponent(Page component) {
        InsertRenderingParams params = peekRenderingParams();
        params.setRendering(true);
        params.setStackComponent(PageImpl.getCurrentComponent());
        params.setCurrentComponent(component);
        PageImpl.setCurrentComponent(component);
        SpecificationUtil.execEvent(ProviderUtil.getEngine(), QM_BEFORE_RENDER_COMPONENT);
    }

    protected void invokeAfterRenderComponent() {
        InsertRenderingParams params = peekRenderingParams();
        PageImpl.setCurrentComponent(params.getCurrentComponent());
        SpecificationUtil.execEvent(ProviderUtil.getEngine(), QM_AFTER_RENDER_COMPONENT);
        PageImpl.setCurrentComponent(params.getStackComponent());
        params.setRendering(false);
        params.clear();
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        PathPart pathPart = getPathPart();
        Page renderPage = pathPartToPage(pathPart);

        boolean fireEvent = true;

        // layoutからcontentを呼ぶ場合
        if (renderPage == null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            renderPage = topLevelPage;
            RequestScope request = cycle.getRequestScope();
            pathPart._suffix = request.getRequestedSuffix();
            pathPart._extension = request.getExtension();
            fireEvent = false;
        }

        if (renderPage == null) {
            throw new IllegalStateException();
        }

        Map properties = new LinkedHashMap(getInformalProperties().size());
        for (int i = 0; i < getInformalProperties().size(); i++) {
            Object object = getInformalProperties().get(i);
            if (object instanceof ProcessorProperty) {
                ProcessorProperty prop = (ProcessorProperty) object;
                properties.put(
                        prop.getName().getQName().getLocalName(),
                        prop.getValue().execute(null));
            }
        }
        Map params = pushRenderingParams().getParams();
        params.clear();
        params.putAll(properties);
        properties.clear();

        invokeBeforeRenderComponent(renderPage);
        try {
            ProcessStatus ret;
            getRenderingInsertChain().push(this);
            try {
            	String name;
            	if (_name == null) {
            		name = "";
            	} else {
            		name = StringUtil.valueOf(_name.getValue().execute(null));
            	}
                ComponentRenderer renderer = new ComponentRenderer(this, name);
                ret = RenderUtil.renderPage(fireEvent, renderer,
                        getVariables(), renderPage, pathPart._suffix, pathPart._extension);
            } finally {
                getRenderingInsertChain().pop();
            }
            if (ret == null) {
                Response response = CycleUtil.getResponse();
                if (response.getWriter().isDirty() == false) {
                    throw new RenderNotCompletedException(
                            renderPage.getPageName(), pathPart._extension);
                }
            }
            if (ret == ProcessStatus.EVAL_PAGE) {
                ret = ProcessStatus.SKIP_BODY;
            }
            return ret;
        } catch(RuntimeException e) {
            invokeAfterRenderComponent();
            throw e;
        }
    }

    protected static Stack getRenderingInsertChain() {
        return (Stack) CycleUtil.getGlobalVariable(RENDERING_INSERT_CHAIN, null);
    }

    public static InsertProcessor getRenderingCurrent() {
        if (getRenderingInsertChain().size() == 0) {
            return null;
        }
        return (InsertProcessor) getRenderingInsertChain().peek();
    }

    public ProcessStatus doEndProcess() {
        invokeAfterRenderComponent();
        popRenderingParams();
        return super.doEndProcess();
    }

    // for serialize

    private void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

}
