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
package org.seasar.maya.standard.engine.processor.jsp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.servlet.jsp.tagext.VariableInfo;

import org.seasar.maya.cycle.CycleWriter;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.ChildEvaluationProcessor;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.engine.processor.TryCatchFinallyProcessor;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.el.PropertyNotFoundException;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.collection.NullIterator;
import org.seasar.maya.standard.cycle.CycleBodyContent;
import org.seasar.maya.standard.cycle.CyclePageContext;
import org.seasar.maya.standard.util.JspUtil;

/**
 * カスタムタグ用プロセッサ.
 * @author suga
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspCustomTagProcessor extends TemplateProcessorSupport
        implements ChildEvaluationProcessor, TryCatchFinallyProcessor, 
				CONST_IMPL {
    
	private static final long serialVersionUID = -4416320364576454337L;
	private static final String PAGE_CONTEXT = "pageContext";
    
    private Class _tagClass;
    private TagExtraInfo _tei;
    private List _variableInfo;
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
    public void setTEI(TagExtraInfo tei) {
        _tei = tei;
    }

    // MLD method
    public void setTagVariableInfo(List variableInfo) {
        _variableInfo = variableInfo;
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

    protected Collection getNestedVariableNames(TagData tagData) {
        List list = new ArrayList();
        if(_tei != null) {
            VariableInfo[] _variableInfos = _tei.getVariableInfo(tagData);
            if (_variableInfos.length > 0) {
                for (int i = 0; i < _variableInfos.length; i++) {
                    if (_variableInfos[i].getScope() == VariableInfo.NESTED) {
                        list.add(_variableInfos[i].getVarName());
                    }
                }
            }
            return list;
        }
        if(_variableInfo != null) {
	        for(Iterator it = _variableInfo.iterator(); it.hasNext(); ) {
	            TagVariableInfo tagVariableInfo = (TagVariableInfo)it.next();
	            if (tagVariableInfo.getScope() != VariableInfo.NESTED) {
	                continue;
	            }
	            String name = tagVariableInfo.getNameGiven();
	            String nameFromAttribute = tagVariableInfo.getNameFromAttribute();
	            if (name == null) {
	                name = tagData.getAttributeString(nameFromAttribute);
	            } else if (nameFromAttribute != null) {
	                continue;
	            }
	            if (name != null) {
	                list.add(name);
	            }
	        }
        }
        return list;
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
    
    protected Tag getLoadedTag(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        TagContext tagContext = TagContext.getTagContext(cycle);
        Tag tag = tagContext.getLoadedTag(this);
        if(tag == null) {
            throw new IllegalStateException();
        }
        return tag;
    }

    protected void saveForNestedVariable(ServiceCycle cycle, Tag customTag) {
        TagContext tagContext = TagContext.getTagContext(cycle);
        Collection vars = tagContext.getNestedVariableNames(customTag);
        if (vars != null) {
            Map nestedVariables = new HashMap();
            for(Iterator it = vars.iterator(); it.hasNext(); ) {
                String var = (String)it.next();
                nestedVariables.put(var, cycle.getAttribute(var));
            }
            tagContext.putNestedVariables(customTag, nestedVariables);
        }
    }

    protected void restoreForNestedVariable(ServiceCycle cycle, Tag customTag) {
        TagContext tagContext = TagContext.getTagContext(cycle);
        Collection vars = tagContext.getNestedVariableNames(customTag);
        if (vars != null) {
            Map nestedVariables = tagContext.getNestedVariables(customTag);
            for(Iterator it = vars.iterator(); it.hasNext(); ) {
                String var = (String)it.next();
                Object previousValue = nestedVariables.get(var);
                if (previousValue != null) {
                    cycle.setAttribute(var, previousValue);
                } else {
                    cycle.setAttribute(var, null);
                }
            }
            nestedVariables.clear();
        }
    }
    
    protected PageContext getPageContext(ServiceCycle cycle) {
        PageContext pageContext = (PageContext)cycle.getAttribute(PAGE_CONTEXT);
        if(pageContext == null) {
            pageContext = new CyclePageContext(cycle, null);
            cycle.setAttribute(PAGE_CONTEXT, pageContext);
        }
        return pageContext;
    }
    
    public ProcessStatus doStartProcess(ServiceCycle cycle) {
        if (cycle == null) {
            throw new IllegalArgumentException();
        }
        if(_tagClass == null) {
            throw new IllegalStateException();
        }
        TagContext tagContext = TagContext.getTagContext(cycle);
        Tag customTag = tagContext.loadTag(_tagClass, getAttributesKey());
        TagData tagData = new TagData((Object[][])null);
        for(Iterator it = iterateProperties(); it.hasNext(); ) {
            ProcessorProperty property = (ProcessorProperty)it.next();
            String propertyName = property.getQName().getLocalName();
            Object value = null;
            try {
                value = property.getValue(cycle);
            } catch (PropertyNotFoundException ignore) {
            }
            ObjectUtil.setProperty(customTag, propertyName, value);
            if(value != null) {
                tagData.setAttribute(propertyName, value);
            } else {
                tagData.setAttribute(propertyName, Void.class);
            }
        }
        tagContext.putNestedVariableNames(customTag, getNestedVariableNames(tagData));
        customTag.setPageContext(getPageContext(cycle));
        saveForNestedVariable(cycle, customTag);
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
            return JspUtil.getProcessStatus(result);
        } catch (JspException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isChildEvaluation(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        return getLoadedTag(cycle) instanceof BodyTag;
    }

    public boolean isIteration(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        return getLoadedTag(cycle) instanceof IterationTag;
    }

    public boolean canCatch(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        try {
            return getLoadedTag(cycle) instanceof TryCatchFinally;
        } catch(Exception e) {
            return false;
        }
    }

    public void setBodyContent(ServiceCycle cycle, CycleWriter body) {
        if(cycle == null || body == null) {
            throw new IllegalArgumentException();
        }
        Tag tag = getLoadedTag(cycle);
        if(tag instanceof BodyTag) {
            ((BodyTag)tag).setBodyContent(new CycleBodyContent(body));
        }
    }

    public void doInitChildProcess(ServiceCycle cycle) {
        if (cycle == null) {
            throw new IllegalArgumentException();
        }
        Tag tag = getLoadedTag(cycle);
        if(tag instanceof BodyTag) {
            BodyTag bodyTag = (BodyTag)tag;
            try {
                bodyTag.doInitBody();
            } catch (JspException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ProcessStatus doAfterChildProcess(ServiceCycle cycle) {
        if (cycle == null) {
            throw new IllegalArgumentException();
        }
        Tag tag = getLoadedTag(cycle);
        if(tag instanceof IterationTag) {
            IterationTag iterationTag = (IterationTag)tag;
            try {
            	int ret = iterationTag.doAfterBody();
                return JspUtil.getProcessStatus(ret);
            } catch (JspException e) {
                throw new RuntimeException(e);
            }
        }
        return SKIP_BODY;
    }

    public ProcessStatus doEndProcess(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        Tag customTag = getLoadedTag(cycle);
        try {
        	int ret = customTag.doEndTag();
            return JspUtil.getProcessStatus(ret);
        } catch (JspException e) {
            throw new RuntimeException(e);
        } finally {
            if (!canCatch(cycle)) {
                restoreForNestedVariable(cycle, customTag);
                TagContext tagContext = TagContext.getTagContext(cycle);
                tagContext.releaseTag(customTag, getAttributesKey());
            }
        }
    }

    public void doCatchProcess(ServiceCycle cycle, Throwable t) {
        if (cycle == null || t == null) {
            throw new IllegalArgumentException();
        }
        Tag customTag = getLoadedTag(cycle);
        try {
            ((TryCatchFinally) customTag).doCatch(t);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void doFinallyProcess(ServiceCycle cycle) {
        if (cycle == null) {
            throw new IllegalArgumentException();
        }
        Tag customTag = getLoadedTag(cycle);
        try {
            ((TryCatchFinally) customTag).doFinally();
        } finally {
            restoreForNestedVariable(cycle, customTag);
            TagContext tagContext = TagContext.getTagContext(cycle);
            tagContext.releaseTag(customTag, getAttributesKey());
        }
    }

}
