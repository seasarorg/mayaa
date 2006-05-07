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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.ErrorData;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TryCatchFinally;

import org.seasar.mayaa.cycle.CycleWriter;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ChildEvaluationProcessor;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.processor.TryCatchFinallyProcessor;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.builder.library.TLDScriptingVariableInfo;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.jsp.BodyContentImpl;
import org.seasar.mayaa.impl.cycle.jsp.PageContextImpl;
import org.seasar.mayaa.impl.cycle.script.rhino.PageAttributeScope;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.collection.AbstractSoftReferencePool;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Koji Suga (Gluegent, Inc.)
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspProcessor extends TemplateProcessorSupport
        implements
            ChildEvaluationProcessor,
            TryCatchFinallyProcessor,
            CONST_IMPL {

    private static final long serialVersionUID = -4416320364576454337L;
    private static PageContext _pageContext = new PageContextImpl();
    private static Map _tagPools = new HashMap();

    private Class _tagClass;
    private List _properties;
    private String _attributesKey;
    private ThreadLocal _loadedTag = new ThreadLocal();
    private PageContextWrapper _wrappedContext =
        new PageContextWrapper(_pageContext);

    private TLDScriptingVariableInfo _variableInfo =
        new TLDScriptingVariableInfo();

    public static boolean isSupportClass(Class test) {
        return test != null &&
                (Tag.class.isAssignableFrom(test)
                || SimpleTag.class.isAssignableFrom(test));
    }

    public void setTLDScriptingVariableInfo(
            TLDScriptingVariableInfo variableInfo) {
        if (variableInfo == null) {
            throw new IllegalArgumentException();
        }
        _variableInfo = variableInfo;
    }

    public TLDScriptingVariableInfo getTLDScriptingVariableInfo() {
        return _variableInfo;
    }

    // MLD method
    public void setTagClass(Class tagClass) {
        if (isSupportClass(tagClass) == false) {
            throw new IllegalArgumentException();
        }
        _tagClass = tagClass;
    }

    // MLD method
    public void addProcessorProperty(ProcessorProperty property) {
        if (property == null) {
            throw new IllegalArgumentException();
        }
        if (_attributesKey != null) {
            throw new IllegalStateException();
        }
        if (_properties == null) {
            _properties = new ArrayList();
        }
        _properties.add(property);
    }

    protected Iterator iterateProperties() {
        if (_properties == null) {
            return NullIterator.getInstance();
        }
        return _properties.iterator();
    }

    protected String getAttributesKey() {
        if (_attributesKey == null) {
            StringBuffer buffer = new StringBuffer();
            for (Iterator it = iterateProperties(); it.hasNext();) {
                ProcessorProperty property = (ProcessorProperty) it.next();
                String localName =
                    property.getName().getQName().getLocalName();
                buffer.append("%").append(localName);
            }
            _attributesKey = buffer.toString();
        }
        return _attributesKey;
    }

    protected TagPool getTagPool() {
        synchronized (_tagPools) {
            String key = _tagClass.getName() + getAttributesKey();
            TagPool pool = (TagPool) _tagPools.get(key);
            if (pool == null) {
                pool = new TagPool(_tagClass);
                _tagPools.put(key, pool);
            }
            return pool;
        }
    }

    protected void clearLoadedTag() {
        _loadedTag.set(null);
    }

    protected Tag getLoadedTag() {
        Tag tag = (Tag) _loadedTag.get();
        if (tag == null) {
            tag = getTagPool().borrowTag();
            tag.setPageContext(_wrappedContext);
            _loadedTag.set(tag);
        }
        return tag;
    }

    protected void releaseLoadedTag() {
        Tag tag = (Tag) _loadedTag.get();
        _loadedTag.set(null);
        tag.release();
        getTagPool().returnTag(tag);
    }

    protected ProcessStatus getProcessStatus(int status, boolean doStart) {
        if (status == Tag.EVAL_BODY_INCLUDE) {
            return ProcessStatus.EVAL_BODY_INCLUDE;
        } else if (status == Tag.SKIP_BODY) {
            return ProcessStatus.SKIP_BODY;
        } else if (status == Tag.EVAL_PAGE) {
            return ProcessStatus.EVAL_PAGE;
        } else if (status == Tag.SKIP_PAGE) {
            return ProcessStatus.SKIP_PAGE;
        } else if (!doStart && status == IterationTag.EVAL_BODY_AGAIN) {
            return ProcessStatus.EVAL_BODY_AGAIN;
        } else if (doStart && status == BodyTag.EVAL_BODY_BUFFERED) {
            return ProcessStatus.EVAL_BODY_BUFFERED;
        }
        throw new IllegalArgumentException();
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if (_tagClass == null) {
            throw new IllegalStateException();
        }
        clearLoadedTag();
        Tag customTag = getLoadedTag();
        for (Iterator it = iterateProperties(); it.hasNext();) {
            ProcessorProperty property = (ProcessorProperty) it.next();
            String propertyName = property.getName().getQName().getLocalName();
            Object value = property.getValue().execute(null);
            setPropertyTo(customTag, propertyName, value);
        }
        ProcessorTreeWalker processor = this;
        while ((processor = processor.getParentProcessor()) != null) {
            if (processor instanceof JspProcessor) {
                JspProcessor jspProcessor = (JspProcessor) processor;
                Tag parentTag = jspProcessor.getLoadedTag();
                if (parentTag == null) {
                    throw new IllegalStateException(
                            "the parent processor has no custom tag.");
                }
                customTag.setParent(parentTag);
                break;
            }
        }
        try {
            final int result = customTag.doStartTag();
            return getProcessStatus(result, true);
        } catch (JspException e) {
            throw createJspRuntimeException(
                    getOriginalNode(), getInjectedNode(), e);
        }
    }

    private void setPropertyTo(Tag customTag, String name, Object value) {
        if (customTag instanceof SimpleTagWrapper) {
            ObjectUtil.setProperty(
                    ((SimpleTagWrapper) customTag).getSimpleTag(), name, value);
        } else {
            ObjectUtil.setProperty(customTag, name, value);
        }
    }

    private RuntimeException createJspRuntimeException(
            SpecificationNode originalNode, SpecificationNode injectedNode,
            Throwable cause) {
        return new RuntimeException(
                new JspRuntimeException(
                        originalNode.getSystemID(), originalNode.getLineNumber(),
                        injectedNode.getSystemID(), injectedNode.getLineNumber(),
                        cause));
    }

    public ProcessStatus doEndProcess() {
        Tag customTag = getLoadedTag();
        try {
            int ret = customTag.doEndTag();
            return getProcessStatus(ret, true);
        } catch (JspException e) {
            throw createJspRuntimeException(
                    getOriginalNode(), getInjectedNode(), e);
        } finally {
            if (!canCatch()) {
                releaseLoadedTag();
            }
        }
    }

    public boolean isChildEvaluation() {
        return getLoadedTag() instanceof BodyTag;
    }

    public void setBodyContent(CycleWriter body) {
        if (body == null) {
            throw new IllegalArgumentException();
        }
        Tag tag = getLoadedTag();
        if (tag instanceof BodyTag) {
            BodyTag bodyTag = (BodyTag) tag;
            bodyTag.setBodyContent(new BodyContentImpl(body));
        } else {
            throw new IllegalStateException();
        }
    }

    public void doInitChildProcess() {
        Tag tag = getLoadedTag();
        if (tag instanceof BodyTag) {
            BodyTag bodyTag = (BodyTag) tag;
            try {
                bodyTag.doInitBody();
            } catch (JspException e) {
                throw createJspRuntimeException(
                        getOriginalNode(), getInjectedNode(), e);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean isIteration() {
        return getLoadedTag() instanceof IterationTag;
    }

    public ProcessStatus doAfterChildProcess() {
        Tag tag = getLoadedTag();
        if (tag instanceof IterationTag) {
            IterationTag iterationTag = (IterationTag) tag;
            try {
                int ret = iterationTag.doAfterBody();
                return getProcessStatus(ret, false);
            } catch (JspException e) {
                throw createJspRuntimeException(
                        getOriginalNode(), getInjectedNode(), e);
            }
        }
        throw new IllegalStateException();
    }

    public boolean canCatch() {
        try {
            return getLoadedTag() instanceof TryCatchFinally;
        } catch (Exception e) {
            return false;
        }
    }

    public void doCatchProcess(Throwable t) {
        if (t == null) {
            throw new IllegalArgumentException();
        }
        Tag tag = getLoadedTag();
        if (tag instanceof TryCatchFinally) {
            TryCatchFinally tryCatch = (TryCatchFinally) tag;
            try {
                tryCatch.doCatch(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public void doFinallyProcess() {
        Tag tag = getLoadedTag();
        if (tag instanceof TryCatchFinally) {
            TryCatchFinally tryCatch = (TryCatchFinally) tag;
            try {
                tryCatch.doFinally();
            } finally {
                releaseLoadedTag();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    protected class TagPool extends AbstractSoftReferencePool {

        private static final long serialVersionUID = -4519484537723904500L;

        private Class _clazz;

        public TagPool(Class clazz) {
            if (isSupportClass(clazz) == false) {
                throw new IllegalArgumentException();
            }
            _clazz = clazz;
        }

        protected Object createObject() {
            if (Tag.class.isAssignableFrom(_clazz)) {
                return ObjectUtil.newInstance(_clazz);
            }
            return new SimpleTagWrapper((SimpleTag) ObjectUtil.newInstance(_clazz));
        }

        protected boolean validateObject(Object object) {
            return object instanceof Tag;
        }

        public Tag borrowTag() {
            return (Tag) borrowObject();
        }

        public void returnTag(Tag tag) {
            if (tag != null) {
                returnObject(tag);
            }
        }

    }

    protected class PageContextWrapper extends PageContext {

        private PageContext _context;

        public PageContextWrapper(PageContext context) {
            _context = context;
        }

        public boolean useTop(String name, int scope) {
            TLDScriptingVariableInfo variableInfo =
                getTLDScriptingVariableInfo();
            return scope == PageContext.PAGE_SCOPE
                    && (variableInfo.hasNestedVariable() == false
                            || variableInfo.isNestedVariable(name) == false);
        }

        protected PageAttributeScope findTopPageAttributeScope() {
            PageAttributeScope pageScope =
                (PageAttributeScope) CycleUtil.getServiceCycle().getPageScope();
            PageAttributeScope top = pageScope;
            while (top.getParentScope() instanceof PageAttributeScope) {
                top = (PageAttributeScope) top.getParentScope();
            }
            return top;
        }

        public void removeAttributeFromPageTop(String name) {
            if (name == null) {
                throw new IllegalArgumentException();
            }
            PageAttributeScope pageScope = findTopPageAttributeScope();
            pageScope.removeAttribute(name);
        }

        public void setAttributeOnPageTop(String name, Object value) {
            if (name == null) {
                throw new IllegalArgumentException();
            }
            PageAttributeScope pageScope = findTopPageAttributeScope();
            pageScope.setAttribute(name, value);
        }

        public void removeAttribute(String name, int scope) {
            if (name == null) {
                throw new IllegalArgumentException();
            }

            if (useTop(name, scope)) {
                removeAttributeFromPageTop(name);
            } else {
                _context.removeAttribute(name, scope);
            }
        }

        public void setAttribute(String name, Object value, int scope) {
            if (name == null) {
                throw new IllegalArgumentException();
            }

            if (useTop(name, scope)) {
                setAttributeOnPageTop(name, value);
            } else {
                _context.setAttribute(name, value, scope);
            }
        }

        public void initialize(Servlet servlet, ServletRequest request,
                ServletResponse response, String errorPageURL,
                boolean needsSession, int bufferSize, boolean autoFlush)
                throws IOException {
            _context.initialize(servlet, request, response, errorPageURL,
                    needsSession, bufferSize, autoFlush);
        }

        public void release() {
            _context.release();
        }

        public HttpSession getSession() {
            return _context.getSession();
        }

        public Object getPage() {
            return _context.getPage();
        }

        public ServletRequest getRequest() {
            return _context.getRequest();
        }

        public ServletResponse getResponse() {
            return _context.getResponse();
        }

        public Exception getException() {
            return _context.getException();
        }

        public ServletConfig getServletConfig() {
            return _context.getServletConfig();
        }

        public ServletContext getServletContext() {
            return _context.getServletContext();
        }

        public void forward(String relativeUrlPath)
                throws ServletException, IOException {
            _context.forward(relativeUrlPath);
        }

        public void include(String relativeUrlPath)
                throws ServletException, IOException {
            _context.include(relativeUrlPath);
        }

        public void include(String relativeUrlPath, boolean flush)
                throws ServletException, IOException {
            _context.include(relativeUrlPath, flush);
        }

        public void handlePageException(Exception e)
                throws ServletException, IOException {
            _context.handlePageException(e);
        }

        public void handlePageException(Throwable t)
                throws ServletException, IOException {
            _context.handlePageException(t);
        }

        public void setAttribute(String name, Object value) {
            _context.setAttribute(name, value);
        }

        public Object getAttribute(String name) {
            return _context.getAttribute(name);
        }

        public Object getAttribute(String name, int scope) {
            return _context.getAttribute(name, scope);
        }

        public Object findAttribute(String name) {
            return _context.findAttribute(name);
        }

        public void removeAttribute(String name) {
            _context.removeAttribute(name);
        }

        public int getAttributesScope(String name) {
            return _context.getAttributesScope(name);
        }

        public Enumeration getAttributeNamesInScope(int scope) {
            return _context.getAttributeNamesInScope(scope);
        }

        public JspWriter getOut() {
            return _context.getOut();
        }

        public ExpressionEvaluator getExpressionEvaluator() {
            return _context.getExpressionEvaluator();
        }

        public VariableResolver getVariableResolver() {
            return _context.getVariableResolver();
        }

        public ErrorData getErrorData() {
            return _context.getErrorData();
        }

    }

    public class SimpleTagWrapper implements Tag {

        private SimpleTag _simpleTag;

        private Tag _parent;

        private boolean _parentDetermined;

        public SimpleTagWrapper(SimpleTag simpleTag) {
            if (simpleTag == null) {
                throw new IllegalArgumentException("simpleTag is null");
            }
            this._simpleTag = simpleTag;
        }

        public JspTag getSimpleTag() {
            return _simpleTag;
        }

        public Tag getParent() {
            if (!_parentDetermined) {
                JspTag simpleTagParent = _simpleTag.getParent();
                if (simpleTagParent != null) {
                    if (simpleTagParent instanceof Tag) {
                        _parent = (Tag) simpleTagParent;
                    } else {
                        _parent = new SimpleTagWrapper((SimpleTag) simpleTagParent);
                    }
                }
                _parentDetermined = true;
            }

            return _parent;
        }

        public void setPageContext(PageContext context) {
            _simpleTag.setJspContext(context);
        }

        public void setParent(Tag parentTag) {
            /* no-op */
        }

        public int doStartTag() throws JspException {
            try {
                _simpleTag.doTag();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Tag.SKIP_BODY;
        }

        public int doEndTag() throws JspException {
            /* no-op */
            return Tag.EVAL_PAGE;
        }

        public void release() {
            /* no-op */
        }

    }

}
