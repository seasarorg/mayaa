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
package org.seasar.mayaa.impl.engine;

import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class EngineUtil implements CONST_IMPL {

    private EngineUtil() {
        // no instantiation.
    }

    public static String getEngineSetting(
            String name, String defaultValue) {
        Engine engine = ProviderUtil.getEngine();
        String value = engine.getParameter(name);
        if(value != null) {
            return value;
        }
        return defaultValue;
    }

    public static boolean getEngineSettingBoolean(
            String name, boolean defaultValue) {
        Engine engine = ProviderUtil.getEngine();
        String value = engine.getParameter(name);
        return ObjectUtil.booleanValue(value, defaultValue);
    }

    public static String getPageName() {
        Specification spec = SpecificationUtil.findSpecification();
        if (spec instanceof Page) {
            return ((Page) spec).getPageName();
        }
        throw new IllegalStateException();
    }

    public static String getPageName(ProcessorTreeWalker proc) {
        for (ProcessorTreeWalker current = proc;
                current != null; current = current.getParentProcessor()) {
            if (current instanceof Page) {
                return ((Page) current).getPageName();
            } else if (current instanceof Template) {
                return ((Template) current).getPage().getPageName();
            }
        }
        throw new IllegalStateException();
    }

    public static Template getTemplate() {
        Specification spec = SpecificationUtil.findSpecification();
        if(spec instanceof Page) {
            NodeTreeWalker parent = spec.getParentNode();
            if(parent != null) {
                spec = SpecificationUtil.findSpecification();
            } else {
                return null;
            }
        }
        if(spec instanceof Template) {
            return (Template)spec;
        }
        throw new IllegalStateException();
    }

    public static Template getTemplate(ProcessorTreeWalker proc) {
        for(ProcessorTreeWalker current = proc;
                current != null; current = current.getParentProcessor()) {
            if(current instanceof Template) {
                return (Template)current;
            }
        }
        throw new IllegalStateException();
    }

    public static Specification getParentSpecification(Specification spec) {
        if(spec instanceof Page) {
            return ProviderUtil.getEngine();
        } else if(spec instanceof Template) {
            return ((Template)spec).getPage();
        }
        return null;
    }
    
}
