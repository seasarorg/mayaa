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
package org.seasar.maya.impl.builder.library.scanner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.seasar.maya.builder.library.scanner.SourceScanner;
import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WalkaroundSourceScanner extends ParameterAwareImpl
		implements SourceScanner {

    private Set _sources;
    
    public WalkaroundSourceScanner() {
        _sources = new HashSet();
        ClassLoaderSourceDescriptor loader = 
            new ClassLoaderSourceDescriptor();
        loader.setRoot(ClassLoaderSourceDescriptor.META_INF);
        loader.setSystemID("maya.mld");
        _sources.add(loader);
    }

    public Iterator scan() {
        return _sources.iterator();
    }

}
