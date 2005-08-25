/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.builder.library.scanner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.seasar.maya.builder.library.scanner.SourceScanner;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WalkaroundSourceScanner implements SourceScanner {

    private Set _sources;
    
    public WalkaroundSourceScanner() {
        _sources = new HashSet();
        ClassLoaderSourceDescriptor loader = new ClassLoaderSourceDescriptor();
        loader.setRoot(ClassLoaderSourceDescriptor.META_INF);
        loader.setSystemID("maya.mld");
        _sources.add(loader);
    }
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(name);
    }

    public Iterator scan() {
        return _sources.iterator();
    }

}
