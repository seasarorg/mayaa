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

import jakarta.servlet.jsp.JspFactory;

import org.seasar.mayaa.builder.library.ProcessorDefinition;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TLDLibraryDefinition extends LibraryDefinitionImpl {

    private static final String VERSION_JSP;

    static {
        JspFactory factory = JspFactory.getDefaultFactory();
        if (factory != null) {
            VERSION_JSP = factory.getEngineInfo().getSpecificationVersion();
        } else {
            VERSION_JSP = "2.0";
        }
    }

    private String _requiredVersion;

    public void setRequiredVersion(String requiredVersion) {
        if (StringUtil.isEmpty(requiredVersion)) {
            throw new IllegalArgumentException();
        }
        _requiredVersion = requiredVersion;
    }

    public String getRequiredVersion() {
        return _requiredVersion;
    }

    public ProcessorDefinition getProcessorDefinition(String name) {
        if (_requiredVersion != null
                && VERSION_JSP.compareTo(_requiredVersion) < 0) {
            return null;
        }
        return super.getProcessorDefinition(name);
    }

}
