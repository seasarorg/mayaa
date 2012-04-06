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
package org.seasar.mayaa.impl.builder.library;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

import org.seasar.mayaa.builder.library.PropertyDefinition;
import org.seasar.mayaa.builder.library.PropertySet;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.engine.processor.JspProcessor;
import org.seasar.mayaa.impl.util.ObjectUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author Hisayoshi Sasaki (Gluegent, Inc.)
 */
public class TLDProcessorDefinition extends ProcessorDefinitionImpl {

    private static final long serialVersionUID = -646585734601397162L;
    public static final String BODY_CONTENT_EMPTY = "empty";
    public static final String BODY_CONTENT_JSP = "JSP";
    public static final String BODY_CONTENT_SCRIPTLESS = "scriptless";
    public static final String BODY_CONTENT_TAGDEPENDENT = "tagdependent";

    private Class _tagClass;
    private Class _teiClass;
    private boolean _dynamicAttribute;
    private String _bodyContent = BODY_CONTENT_JSP;

    public void setProcessorClass(Class processorClass) {
        if (JspProcessor.isSupportClass(processorClass) == false) {
            throw new IllegalArgumentException();
        }
        _tagClass = processorClass;
    }

    public Class getProcessorClass() {
        if (_tagClass == null) {
            throw new IllegalStateException();
        }
        return _tagClass;
    }

    public void setExtraInfoClass(Class teiClass) {
        if (teiClass == null
                || TagExtraInfo.class.isAssignableFrom(teiClass) == false) {
            throw new IllegalArgumentException();
        }
        _teiClass = teiClass;
    }

    public Class getExtraInfoClass() {
        return _teiClass;
    }

    public boolean isDynamicAttribute() {
        return _dynamicAttribute;
    }

    public void setDynamicAttribute(boolean dynamicAttribute) {
        this._dynamicAttribute = dynamicAttribute;
    }

    public String getBodyContent() {
        if (_bodyContent == null) {
            throw new IllegalStateException();
        }
        return _bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        if ((BODY_CONTENT_EMPTY.equals(bodyContent) ||
                BODY_CONTENT_JSP.equals(bodyContent) ||// for JSP 1.2
                BODY_CONTENT_SCRIPTLESS.equals(bodyContent) ||
                BODY_CONTENT_TAGDEPENDENT.equals(bodyContent)) == false) {
            throw new IllegalArgumentException();
        }
        _bodyContent = bodyContent;
    }

    protected TemplateProcessor newInstance() {
        JspProcessor processor = new JspProcessor();
        processor.setTagClass(getProcessorClass());
        return processor;
    }

    protected void settingPropertySet(
            SpecificationNode original, SpecificationNode injected,
            TemplateProcessor processor, PropertySet propertySet) {
        Hashtable tagDataSeed = new Hashtable();

        JspProcessor jspProcessor = (JspProcessor) processor;
        for (Iterator it = propertySet.iteratePropertyDefinition();
                it.hasNext();) {
            PropertyDefinition property = (PropertyDefinition) it.next();
            Object prop =
                property.createProcessorProperty(this, jspProcessor, original, injected);
            if (prop != null) {
                ProcessorProperty currentProp = (ProcessorProperty) prop;
                jspProcessor.addProcessorProperty(currentProp);

                CompiledScript value = currentProp.getValue();
                tagDataSeed.put(
                        currentProp.getName().getQName().getLocalName(),
                        prepareScript(value));
            }
        }

        if (getExtraInfoClass() != null) {
            settingExtraInfo(jspProcessor, tagDataSeed);
        }

        if (getBodyContent().equals(BODY_CONTENT_EMPTY)) {
            jspProcessor.setForceBodySkip(true);
        }
        // TODO 他のbody-content対応
    }

    private Object prepareScript(CompiledScript script) {
        if (script.isLiteral()) {
            return script.execute(null);
        }
        return script;
    }

    protected void settingExtraInfo(
            JspProcessor processor, Hashtable seed) {
        TagExtraInfo tei =
                (TagExtraInfo) ObjectUtil.newInstance(getExtraInfoClass());

        boolean hasNestedVariable = existsNestedVariable(tei, seed);
        boolean hasDynamicName = existsDynamicName(seed);

        TLDScriptingVariableInfo variableInfo = new TLDScriptingVariableInfo();

        variableInfo.setTagExtraInfo(tei);
        variableInfo.setNestedVariable(hasNestedVariable);
        variableInfo.setDynamicName(hasDynamicName);
        if (hasNestedVariable) {
            ScriptableTagData tagData = new ScriptableTagData(seed);
            if (hasDynamicName) {
                variableInfo.setTagData(tagData);
            } else {
                variableInfo.setVariableInfos(tei.getVariableInfo(tagData));
            }
        }

        processor.setTLDScriptingVariableInfo(variableInfo);
    }

    protected boolean existsNestedVariable(
            TagExtraInfo tei, Hashtable seed) {
        VariableInfo[] dummy = tei.getVariableInfo(new DummyTagData(seed));
        if (dummy != null) {
            for (int i = 0; i < dummy.length; i++) {
                if (dummy[i].getScope() == VariableInfo.NESTED) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean existsDynamicName(Hashtable seed) {
        Enumeration keys = seed.keys();
        while (keys.hasMoreElements()) {
            if (seed.get(keys.nextElement()) instanceof CompiledScript) {
                return true;
            }
        }
        return false;
    }

    protected static class DummyTagData extends TagData {
        public DummyTagData(Hashtable seed) {
            super(seed);
        }

        public Object getAttribute(String attName) {
            return attName;
        }

        public String getAttributeString(String attName) {
            return attName;
        }
    }

    protected static class ScriptableTagData extends TagData {
        public ScriptableTagData(Hashtable seed) {
            super(seed);
        }

        public boolean isDynamicAttribute(String attName) {
            return (super.getAttribute(attName) instanceof CompiledScript);
        }

        public Object getAttribute(String attName) {
            Object value = super.getAttribute(attName);
            if (value instanceof CompiledScript) {
                return ((CompiledScript) value).execute(null);
            }
            return value;
        }

        public String getAttributeString(String attName) {
            Object value = getAttribute(attName);
            if (value != null) {
                return String.valueOf(value);
            }
            return (String) value;
        }
    }

}
