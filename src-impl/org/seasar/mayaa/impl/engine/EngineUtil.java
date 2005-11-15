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
package org.seasar.maya.impl.engine;

import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.engine.specification.NodeTreeWalker;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.provider.ProviderUtil;
import org.seasar.maya.impl.util.ObjectUtil;

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
