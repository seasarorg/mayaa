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
import org.seasar.mayaa.impl.IllegalParameterValueException;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.IteratorIterator;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class MetaInfSourceScanner extends ParameterAwareImpl
        implements SourceScanner {

    private static final long serialVersionUID = -7285416169718204350L;

    private FolderSourceScanner _folderScanner = new FolderSourceScanner();
    private Set<String> _ignores = new HashSet<>();

    private String _jarScanFolder;
    private Set<String> _jarScanIgnores = new HashSet<>();
    private Set<String> _jarScanExtensions = new HashSet<>();

    private void initJarScanOptions() {
        if (_jarScanFolder == null) {
            _jarScanFolder = "META-INF/";
        }
        if (_jarScanIgnores.size() == 0) {
            _jarScanIgnores.add("META-INF/MANIFEST.MF");
        }
        if (_jarScanExtensions.size() == 0) {
            _jarScanExtensions.add(".mld");
            _jarScanExtensions.add(".tld");
        }
    }

    private boolean isIgnored(SourceDescriptor descriptor) {
        String filename = descriptor.getSystemID();
        if (filename.charAt(0) == '/') {
            filename = filename.substring(1);
        }
        for (String matcher : _ignores) {
            if (filename.startsWith(matcher)) {
                return true;
            }
        }
        return false;
    }

    /**
     * META-INF内の複数のJARファイルごとに生成される{@code SourceScanner}を集約することで
     * JARに含まれる{@code SourceDescriptor}を横断して走査するためのイテレータを返す。
     * 
     * @see SourceScanner
     */
    @SuppressWarnings("unchecked")
    public Iterator<SourceDescriptor> scan() {
        initJarScanOptions();
        IteratorIterator itit = new IteratorIterator();
        for (Iterator<SourceDescriptor> it = _folderScanner.scan(); it.hasNext(); ) {
            SourceDescriptor descriptor = it.next();
            if (isIgnored(descriptor)) {
                continue;
            }

            JarSourceScanner scanner = new JarSourceScanner();
            scanner.setDescriptor(descriptor);
            scanner.setParameter("root", _folderScanner.getFolder());
            scanner.setParameter("folder", _jarScanFolder);
            for (String ignore : _jarScanIgnores) {
                scanner.setParameter("ignore", ignore);
            }
            for (String extension : _jarScanExtensions) {
                scanner.setParameter("extension", extension);
            }
            itit.add(scanner);
        }
        return itit;
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if ("ignore".equals(name)) {
            if (StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            _ignores.add(value);
        } else if (name.startsWith("jar.")) {
            name = name.substring("jar.".length());
            if ("folder".equals(name)) {
                _jarScanFolder = value;
            } else if ("ignore".equals(name)) {
                _jarScanIgnores.add(value);
            } else if ("extension".equals(name)) {
                _jarScanExtensions.add(value);
            }
        } else {
            _folderScanner.setParameter(name, value);
            return;
        }
        super.setParameter(name, value);
    }

}
