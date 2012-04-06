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
package org.seasar.mayaa.impl.builder.library.mld;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.impl.builder.library.PropertyDefinitionImpl;
import org.seasar.mayaa.impl.builder.library.PropertySetImpl;
import org.seasar.mayaa.impl.provider.factory.AbstractParameterAwareTagHandler;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertyTagHandler
        extends AbstractParameterAwareTagHandler {

    private PropertySetTagHandler _parent;
    private PropertyDefinitionImpl _propertyDefinition;

    public PropertyTagHandler(PropertySetTagHandler parent,
            LibraryTagHandler libraryTagHandler) {
        super("property");
        _parent = parent;
        putHandler(new ConverterTagHandler(this, libraryTagHandler));
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        String name = attributes.getValue("name");
        String implName = attributes.getValue("implName");
        boolean required = XMLUtil.getBooleanValue(
                attributes, "required", false);
        Class expectedClass = XMLUtil.getClassValue(
                attributes, "expectedClass", Object.class);
        String finalValue = attributes.getValue("final");
        String defaultValue = attributes.getValue("default");
        String converterName = attributes.getValue("converterName");
        _propertyDefinition = new PropertyDefinitionImpl();
        _propertyDefinition.setName(name);
        _propertyDefinition.setImplName(implName);
        _propertyDefinition.setRequired(required);
        _propertyDefinition.setExpectedClass(expectedClass);
        _propertyDefinition.setFinalValue(finalValue);
        _propertyDefinition.setDefaultValue(defaultValue);
        _propertyDefinition.setPropertyConverterName(converterName);
        _propertyDefinition.setSystemID(systemID);
        _propertyDefinition.setLineNumber(lineNumber);
        PropertySetImpl processor = _parent.getPropertySet();
        processor.addPropertyDefinitiion(_propertyDefinition);
        _propertyDefinition.setPropertySet(processor);
    }

    protected void end(String body) {
        _propertyDefinition = null;
    }

    public PropertyDefinitionImpl getPropertyDefinition() {
        if (_propertyDefinition == null) {
            throw new IllegalStateException();
        }
        return _propertyDefinition;
    }

    public ParameterAware getParameterAware() {
        return getPropertyDefinition();
    }

}
