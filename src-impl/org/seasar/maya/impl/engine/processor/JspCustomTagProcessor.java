/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.engine.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TryCatchFinally;

import org.seasar.maya.cycle.CycleWriter;
import org.seasar.maya.engine.processor.ChildEvaluationProcessor;
import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.engine.processor.TryCatchFinallyProcessor;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.jsp.BodyContentImpl;
import org.seasar.maya.impl.cycle.jsp.PageContextImpl;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * カスタムタグ用プロセッサ.
 * @author suga
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspCustomTagProcessor extends TemplateProcessorSupport
        implements ChildEvaluationProcessor, TryCatchFinallyProcessor, CONST_IMPL {
    
	private static final long serialVersionUID = -4416320364576454337L;
	private static PageContext _pageContext = new PageContextImpl();
    
    private Class _tagClass;
    private List _properties;
    private String _attributesKey;

    // MLD method
    public void setTagClass(Class tagClass) {
        if(tagClass == null) {
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
    
    private Iterator iterateProperties() {
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
                buffer.append("%").append(property.getQName().getLocalName());
            }
            _attributesKey = buffer.toString();
        }
        return _attributesKey;
    }
    
    protected Tag getLoadedTag() {
        TagContext tagContext = TagContext.getTagContext();
        Tag tag = tagContext.getLoadedTag(this);
        if(tag == null) {
            throw new IllegalStateException();
        }
        return tag;
    }
    
    private ProcessStatus getProcessStatus(int status, boolean doStart) {
        if(status == Tag.EVAL_BODY_INCLUDE) {
            return TemplateProcessor.EVAL_BODY_INCLUDE;
        } else if(status == Tag.SKIP_BODY) {
            return TemplateProcessor.SKIP_BODY;
        } else if(status == Tag.EVAL_PAGE) {
            return TemplateProcessor.EVAL_PAGE;
        } else if(status == Tag.SKIP_PAGE) {
            return TemplateProcessor.SKIP_PAGE;
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
        TagContext tagContext = TagContext.getTagContext();
        Tag customTag = tagContext.loadTag(_tagClass, getAttributesKey());
        TagData tagData = new TagData((Object[][])null);
        for(Iterator it = iterateProperties(); it.hasNext(); ) {
            ProcessorProperty property = (ProcessorProperty)it.next();
            String propertyName = property.getQName().getLocalName();
            Object value = property.getValue();
            ObjectUtil.setProperty(customTag, propertyName, value);
            if(value != null) {
                tagData.setAttribute(propertyName, value);
            } else {
                tagData.setAttribute(propertyName, Void.class);
            }
        }
        customTag.setPageContext(_pageContext);
        TemplateProcessor processor = this;
        while ((processor = processor.getParentProcessor()) != null) {
            if (processor instanceof JspCustomTagProcessor) {
                Tag parentTag = tagContext.getLoadedTag(processor);
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
            tagContext.putLoadedTag(this, customTag);
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
                TagContext tagContext = TagContext.getTagContext();
                tagContext.releaseTag(customTag, getAttributesKey());
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
                TagContext tagContext = TagContext.getTagContext();
                tagContext.releaseTag(tag, getAttributesKey());
            }
        } else {
            throw new IllegalStateException();
        }
    }

}
