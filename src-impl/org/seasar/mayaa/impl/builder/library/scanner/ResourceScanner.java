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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.library.scanner.SourceScanner;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.source.FileSourceDescriptor;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.IteratorIterator;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class ResourceScanner extends ParameterAwareImpl implements SourceScanner {

    private static final long serialVersionUID = 9001235862576049476L;
    private static final Log LOG = LogFactory.getLog(ResourceScanner.class);

    private String _root;
    private List _classPath = new ArrayList();
    private List _jars = new ArrayList();
    private Set _extensions = new HashSet();
    private Set _ignores = new HashSet();

    public ResourceScanner() {
        String[] pathArray = System.getProperty(
                "java.class.path", ".").split(File.pathSeparator);
        for (int i = 0; i < pathArray.length; i++) {
            String path = pathArray[i];
            File file = new File(path);
            if (file.exists()) {
                if (file.isDirectory()) {
                    _classPath.add(path);
                } else if (file.getName().endsWith(".jar")) {
                    _jars.add(path);
                }
            }
        }
    }

    public Iterator scan() {
        IteratorIterator itit = new IteratorIterator();
        for (Iterator it = _classPath.iterator(); it.hasNext(); ) {
            String path = (String) it.next();
            if (StringUtil.isEmpty(path)) {
                continue;
            }
            if (path.charAt(path.length()-1) != File.separatorChar) {
                path += File.separatorChar;
            }
            if (_root != null) {
                path += _root;
                File dir = new File(path);
                if (dir.exists() == false || dir.isDirectory() == false) {
                    continue;
                }
            }

            FolderSourceScanner folderScanner = new FolderSourceScanner();
            folderScanner.setParameter("folder", path);
            folderScanner.setParameter("recursive", "true");
            folderScanner.setParameter("absolute", "true");
            for (Iterator extIterator = _extensions.iterator()
                    ; extIterator.hasNext(); ) {
                folderScanner.setParameter("extension",
                        (String) extIterator.next());
            }
            itit.add(folderScanner.scan());
            if (LOG.isDebugEnabled()) {
                LOG.debug("scan path: " + path);
            }
        }
        for (Iterator it = _jars.iterator(); it.hasNext(); ) {
            String path = (String) it.next();
            if (StringUtil.isEmpty(path)) {
                continue;
            }
            File jarFile = new File(path).getAbsoluteFile();
            JarSourceScanner jarScanner = new JarSourceScanner();
            FileSourceDescriptor descriptor = new FileSourceDescriptor();
            descriptor.setParameter("absolute", "true");
            descriptor.setFile(jarFile);
            descriptor.setSystemID(jarFile.getName());
            jarScanner.setDescriptor(descriptor);
            if (jarFile.getParent() != null) {
                jarScanner.setParameter("appPath", jarFile.getParent());
            }
            if (_root != null) {
                jarScanner.setParameter("folder", _root);
            }
            for (Iterator ignoreIterator = _ignores.iterator(); ignoreIterator.hasNext(); ) {
                jarScanner.setParameter("ignore", (String) ignoreIterator.next());
            }
            for (Iterator extIterator = _extensions.iterator()
                    ; extIterator.hasNext(); ) {
                jarScanner.setParameter("extension",
                        (String) extIterator.next());
            }
            itit.add(jarScanner);
            if (LOG.isDebugEnabled()) {
                LOG.debug("scan jar: " + jarFile.getAbsolutePath());
            }
        }
        return itit;
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if ("root".equals(name)) {
            _root = value;
            if (StringUtil.isEmpty(_root) == false) {
                if (_root.charAt(0) == File.separatorChar) {
                    _root = _root.substring(1);
                }
            }
        } else if ("extension".equals(name)) {
            _extensions.add(value);
        } else if ("ignore".equals(name)) {
            _ignores.add(value);
        } else {
            super.setParameter(name, value);
        }
    }
}

