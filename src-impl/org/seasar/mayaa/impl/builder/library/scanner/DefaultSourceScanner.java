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
package org.seasar.mayaa.impl.builder.library.scanner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.seasar.mayaa.builder.library.scanner.SourceScanner;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.engine.processor.TemplateProcessorSupport;
import org.seasar.mayaa.impl.source.ClassLoaderSourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DefaultSourceScanner extends ParameterAwareImpl
        implements SourceScanner {

    private static final long serialVersionUID = 690422240376318319L;

    private Set _sources;

    public DefaultSourceScanner() {
        _sources = new HashSet();
        ClassLoaderSourceDescriptor loader =
            new ClassLoaderSourceDescriptor();
        loader.setNeighborClass(TemplateProcessorSupport.class);
        loader.setSystemID("mayaa.mld");
        _sources.add(loader);
    }

    public Iterator scan() {
        return _sources.iterator();
    }

}
