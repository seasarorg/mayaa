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
package org.seasar.mayaa.impl.provider;

import org.seasar.mayaa.builder.PathAdjuster;
import org.seasar.mayaa.builder.SpecificationBuilder;
import org.seasar.mayaa.builder.TemplateBuilder;
import org.seasar.mayaa.builder.library.LibraryManager;
import org.seasar.mayaa.builder.library.TemplateAttributeReader;
import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.specification.ParentSpecificationResolver;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.provider.ServiceProvider;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceProviderImpl extends ParameterAwareImpl
        implements ServiceProvider, CONST_IMPL {

    private static final long serialVersionUID = -8659297907641816962L;

    private Engine _engine;
    private ScriptEnvironment _scriptEnvironment;
    private LibraryManager _libraryManager;
    private SpecificationBuilder _specificationBuilder;
    private TemplateBuilder _templateBuilder;
    private PathAdjuster _pathAdjuster;
    private TemplateAttributeReader _templateAttributeReader;
    private ParentSpecificationResolver _parentSpecificationResolver;

    public void setEngine(Engine engine) {
        if (engine == null) {
            throw new IllegalArgumentException();
        }
        _engine = engine;
    }

    public Engine getEngine() {
        if (_engine == null) {
            throw new IllegalStateException();
        }
        return _engine;
    }

    public void setScriptEnvironment(ScriptEnvironment scriptEnvironment) {
        if (scriptEnvironment == null) {
            throw new IllegalArgumentException();
        }
        _scriptEnvironment = scriptEnvironment;
    }

    public ScriptEnvironment getScriptEnvironment() {
        if (_scriptEnvironment == null) {
            throw new IllegalStateException();
        }
        return _scriptEnvironment;
    }

    public void setLibraryManager(LibraryManager libraryManager) {
        if (libraryManager == null) {
            throw new IllegalArgumentException();
        }
        _libraryManager = libraryManager;
    }

    public LibraryManager getLibraryManager() {
        if (_libraryManager == null) {
            throw new IllegalStateException();
        }
        return _libraryManager;
    }

    public void setSpecificationBuilder(
            SpecificationBuilder specificationBuilder) {
        if (specificationBuilder == null) {
            throw new IllegalArgumentException();
        }
        _specificationBuilder = specificationBuilder;
    }

    public SpecificationBuilder getSpecificationBuilder() {
        if (_specificationBuilder == null) {
            throw new IllegalStateException();
        }
        return _specificationBuilder;
    }

    public void setTemplateBuilder(TemplateBuilder templateBuilder) {
        if (templateBuilder == null) {
            throw new IllegalArgumentException();
        }
        _templateBuilder = templateBuilder;
    }

    public TemplateBuilder getTemplateBuilder() {
        if (_templateBuilder == null) {
            throw new IllegalStateException();
        }
        return _templateBuilder;
    }

    public void setPathAdjuster(PathAdjuster pathAdjuster) {
        if (pathAdjuster == null) {
            throw new IllegalArgumentException();
        }
        _pathAdjuster = pathAdjuster;
    }

    public PathAdjuster getPathAdjuster() {
        if (_pathAdjuster == null) {
            throw new IllegalStateException();
        }
        return _pathAdjuster;
    }

    public void setTemplateAttributeReader(
            TemplateAttributeReader templateAttributeReader) {
        if (templateAttributeReader == null) {
            throw new IllegalArgumentException();
        }
        _templateAttributeReader = templateAttributeReader;
    }

    public TemplateAttributeReader getTemplateAttributeReader() {
        if (_templateAttributeReader == null) {
            throw new IllegalStateException();
        }
        return _templateAttributeReader;
    }

    public void setParentSpecificationResolver(ParentSpecificationResolver parentSpecificationResolver) {
        if (parentSpecificationResolver == null) {
            throw new IllegalStateException();
        }
        _parentSpecificationResolver = parentSpecificationResolver;
    }

    public ParentSpecificationResolver getParentSpecificationResolver() {
        if (_parentSpecificationResolver == null) {
            throw new IllegalStateException();
        }
        return _parentSpecificationResolver;
    }

}

