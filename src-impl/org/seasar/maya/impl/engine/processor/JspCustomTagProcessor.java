/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.engine.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TryCatchFinally;

import org.seasar.maya.cycle.CycleWriter;
import org.seasar.maya.engine.processor.ChildEvaluationProcessor;
import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.engine.processor.TryCatchFinallyProcessor;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.jsp.BodyContentImpl;
import org.seasar.maya.impl.cycle.jsp.PageContextImpl;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.collection.AbstractSoftReferencePool;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author suga
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspCustomTagProcessor extends TemplateProcessorSupport
        implements ChildEvaluationProcessor, TryCatchFinallyProcessor, CONST_IMPL {
    
	private static final long serialVersionUID = -4416320364576454337L;
	private static PageContext _pageContext = new PageContextImpl();
    private static Map _tagPools = new HashMap();
    
    private Class _tagClass;
    private List _properties;
    private String _attributesKey;
    private ThreadLocal _loadedTag = new ThreadLocal();

    // MLD method
    public void setTagClass(Class tagClass) {
        if(tagClass == null || 
    			Tag.class.isAssignableFrom(tagClass) == false) {
            throw new IllegalArgumentException();
        }
        _tagClass = tagClass;
    }
    
    // MLD method
    public void addProcessorProperty(ProcessorProperty property) {
        if(property == null) {
            throw new IllegalArgumentException();
        }
        if(_attributesKey != null) {
            throw new IllegalStateException();
        }
        if(_properties == null) {
            _properties = new ArrayList();
        }
        _properties.add(property);
    }
    
    protected Iterator iterateProperties() {
        if(_properties == null) {
            return NullIterator.getInstance();
        }
        return _properties.iterator();
    }

    protected String getAttributesKey() {
        if (_attributesKey == null) {
            StringBuffer buffer = new StringBuffer();
            for (Iterator it = iterateProperties(); it.hasNext();) {
                ProcessorProperty property = (ProcessorProperty) it.next();
                String localName = property.getName().getQName().getLocalName();
                buffer.append("%").append(localName);
            }
            _attributesKey = buffer.toString();
        }
        return _attributesKey;
    }
    
    protected TagPool getTagPool() {
        synchronized (_tagPools) {
	        String key = _tagClass.getName() + getAttributesKey();
	        TagPool pool = (TagPool)_tagPools.get(key);
	        if(pool == null) {
                pool = new TagPool(_tagClass);
                _tagPools.put(key, pool);
            }
	        return pool;
        }
    }
    
    protected Tag getLoadedTag() {
        Tag tag = (Tag)_loadedTag.get();
        if(tag == null) {
            tag = getTagPool().borrowTag();
            tag.setPageContext(_pageContext);
            _loadedTag.set(tag);
        }
        return tag;
    }
    
    protected void releaseLoadedTag() {
        Tag tag = (Tag)_loadedTag.get();
        _loadedTag.set(null);
        tag.release();
        getTagPool().returnTag(tag);
    }
    
    protected ProcessStatus getProcessStatus(int status, boolean doStart) {
        if(status == Tag.EVAL_BODY_INCLUDE) {
            return EVAL_BODY_INCLUDE;
        } else if(status == Tag.SKIP_BODY) {
            return SKIP_BODY;
        } else if(status == Tag.EVAL_PAGE) {
            return EVAL_PAGE;
        } else if(status == Tag.SKIP_PAGE) {
            return SKIP_PAGE;
        } else if(!doStart && status == IterationTag.EVAL_BODY_AGAIN) {
            return IterationProcessor.EVAL_BODY_AGAIN;
        } else if(doStart && status == BodyTag.EVAL_BODY_BUFFERED) {
            return ChildEvaluationProcessor.EVAL_BODY_BUFFERED;
        }
        throw new IllegalArgumentException();
    }
    
    public ProcessStatus doStartProcess() {
        if(_tagClass == null) {
            throw new IllegalStateException();
        }
        Tag customTag = getLoadedTag();
        for(Iterator it = iterateProperties(); it.hasNext(); ) {
            ProcessorProperty property = (ProcessorProperty)it.next();
            String propertyName = property.getName().getQName().getLocalName();
            Object value = property.getValue().execute();
            ObjectUtil.setProperty(customTag, propertyName, value);
        }
        ProcessorTreeWalker processor = this;
        while ((processor = processor.getParentProcessor()) != null) {
            if (processor instanceof JspCustomTagProcessor) {
            	JspCustomTagProcessor jspProcessor = 
            		(JspCustomTagProcessor)processor;
                Tag parentTag = jspProcessor.getLoadedTag();
                if(parentTag == null) {
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
            throw new RuntimeException(e);
        }
    }

    public ProcessStatus doEndProcess() {
        Tag customTag = getLoadedTag();
        try {
            int ret = customTag.doEndTag();
            return getProcessStatus(ret, true);
        } catch (JspException e) {
            throw new RuntimeException(e);
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
        if(body == null) {
            throw new IllegalArgumentException();
        }
        Tag tag = getLoadedTag();
        if(tag instanceof BodyTag) {
            BodyTag bodyTag = (BodyTag)tag;
            bodyTag.setBodyContent(new BodyContentImpl(body));
        } else {
            throw new IllegalStateException();
        }
    }

    public void doInitChildProcess() {
        Tag tag = getLoadedTag();
        if(tag instanceof BodyTag) {
            BodyTag bodyTag = (BodyTag)tag;
            try {
                bodyTag.doInitBody();
            } catch (JspException e) {
                throw new RuntimeException(e);
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
        if(tag instanceof IterationTag) {
            IterationTag iterationTag = (IterationTag)tag;
            try {
            	int ret = iterationTag.doAfterBody();
                return getProcessStatus(ret, false);
            } catch (JspException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException();
    }

    public boolean canCatch() {
        try {
            return getLoadedTag() instanceof TryCatchFinally;
        } catch(Exception e) {
            return false;
        }
    }

    public void doCatchProcess(Throwable t) {
        if (t == null) {
            throw new IllegalArgumentException();
        }
        Tag tag = getLoadedTag();
        if(tag instanceof TryCatchFinally) {
            TryCatchFinally tryCatch = (TryCatchFinally)tag;
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
        if(tag instanceof TryCatchFinally) {
            TryCatchFinally tryCatch = (TryCatchFinally)tag;
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

		protected TagPool(Class clazz) {
        	if(clazz == null || 
        			Tag.class.isAssignableFrom(clazz) == false) {
        		throw new IllegalArgumentException();
        	}
            _clazz = clazz;
        }

        protected Object createObject() {
            return ObjectUtil.newInstance(_clazz);
        }

        protected boolean validateObject(Object object) {
            return object instanceof Tag;
        }
        
        protected Tag borrowTag() {
        	return (Tag)borrowObject();
        }
        
        protected void returnTag(Tag tag) {
        	if(tag != null) {
        		returnObject(tag);
        	}
        }
        
    }

}
