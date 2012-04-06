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
package org.seasar.mayaa.impl.engine;

import java.io.IOException;

import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.source.SourceUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class EngineUtil implements CONST_IMPL {

    private EngineUtil() {
        // no instantiation.
    }

    private static String _mayaaExtensionName;

    /**
     * 現在デバッグモードかどうかを返します。
     * @return 現在がデバッグモードなら{@code true}、そうでなければ{@code false}。
     */
    public static boolean isDebugMode() {
        ApplicationScope scope = FactoryFactory.getApplicationScope();
        if (scope != null) {
            return ObjectUtil.booleanValue(scope.getAttribute(DEBUG), false);
        }
        return false;
    }

    public static boolean isClientAbortException(Throwable t) {
        if (t instanceof IOException) {
            IOException e = (IOException) t;
            String simpleClassName = ObjectUtil.getSimpleClassName(e.getClass());
            switch (simpleClassName.charAt(0)) {
            case 'E':
                return simpleClassName.equals("EOFException");
            case 'C':
                return simpleClassName.equals("ClientAbortException");
            }
        }
        return false;
    }

    /**
     * 高速化のため、Mayaaファイルの拡張子("."を含まない)を{@EngineUtil}内に
     * キャッシュします。
     *
     * @return Mayaaファイルの拡張子("."を含まない)
     */
    public static String getMayaaExtensionName() {
        if (_mayaaExtensionName == null) {
            renewMayaaExtensionName();
        }
        return _mayaaExtensionName;
    }

    public static void renewMayaaExtensionName() {
        _mayaaExtensionName =
            getEngineSetting(MAYAA_EXTENSION, ".mayaa").substring(1);
    }

    public static String getEngineSetting(
            String name, String defaultValue) {
        Engine engine = ProviderUtil.getEngine();
        String value = engine.getParameter(name);
        if (value != null) {
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

    public static String getSourcePath() {
        Specification spec = SpecificationUtil.findSpecification();
        if (spec == null) {
            String path = CycleUtil.getServiceCycle().getRequestScope().getRequestedPath();
            SourceDescriptor source = SourceUtil.getSourceDescriptor(path);
            return source.getSystemID();
        }
        return spec.getSystemID();
    }

    public static String getSourcePath(ProcessorTreeWalker proc) {
        for (ProcessorTreeWalker current = proc;
                current != null; current = current.getStaticParentProcessor()) {
            if (current instanceof Template) {
                return ((Template) current).getSystemID();
            }
        }
        throw new IllegalStateException("unknown sourcePath from processor");
    }

    public static String getSourcePath(NodeTreeWalker node) {
        return node.getSystemID();
    }

    public static Template getTemplate() {
        Specification spec = SpecificationUtil.findSpecification();
        if (spec instanceof Page) {
            NodeTreeWalker parent = spec.getParentNode();
            if (parent != null) {
                spec = SpecificationUtil.findSpecification();
            } else {
                return null;
            }
        }
        if (spec instanceof Template) {
            return (Template) spec;
        }
        throw new IllegalStateException("template not found");
    }

    public static Template getTemplate(ProcessorTreeWalker proc) {
        for (ProcessorTreeWalker current = proc;
                current != null; current = current.getStaticParentProcessor()) {
            if (current instanceof Template) {
                return (Template) current;
            }
        }
        throw new IllegalStateException("template not found from current processor");
    }

    public static Specification getParentSpecification(Specification spec) {
        return ProviderUtil.getParentSpecificationResolver().getParentSpecification(spec);
    }

}
