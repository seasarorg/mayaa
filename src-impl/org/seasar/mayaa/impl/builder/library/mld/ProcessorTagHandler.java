/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
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
import org.seasar.mayaa.impl.builder.library.LibraryDefinitionImpl;
import org.seasar.mayaa.impl.builder.library.ProcessorDefinitionImpl;
import org.seasar.mayaa.impl.builder.library.PropertySetImpl;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProcessorTagHandler
        extends PropertySetTagHandler {

    public ProcessorTagHandler(LibraryTagHandler parent) {
        super("processor", parent, parent);
        putHandler(new PropertyTagHandler(this, parent));
        putHandler(new PropertySetTagHandler("propertySet", this, parent));
    }

    protected PropertySetImpl createPropertySet() {
        return new ProcessorDefinitionImpl();
    }

    protected void addToLibrary(LibraryDefinitionImpl library) {
        library.addProcessorDefinition(getProcessorDefinition());
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        super.start(attributes, systemID, lineNumber);
        ProcessorDefinitionImpl processorDef = getProcessorDefinition();
        Class processorClass =
            XMLUtil.getClassValue(attributes, "class", null);
        if (processorClass == null) {
            throw new IllegalStateException();
        }
        processorDef.setProcessorClass(processorClass);
    }

    public ProcessorDefinitionImpl getProcessorDefinition() {
        PropertySetImpl propertySet = getPropertySet();
        if (propertySet instanceof ProcessorDefinitionImpl) {
            return (ProcessorDefinitionImpl) propertySet;
        }
        throw new IllegalStateException();
    }

    public ParameterAware getParameterAware() {
        return getProcessorDefinition();
    }

}
