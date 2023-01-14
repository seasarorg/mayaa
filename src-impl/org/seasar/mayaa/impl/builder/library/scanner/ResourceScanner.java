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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
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
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class ResourceScanner extends ParameterAwareImpl implements SourceScanner {

    private static final long serialVersionUID = 9001235862576049476L;
    private static final Log LOG = LogFactory.getLog(ResourceScanner.class);

    private String _root;
    private List<String> _classPath = new ArrayList<>();
    private List<String> _jars = new ArrayList<>();
    private Set<String> _extensions = new HashSet<>();
    private Set<String> _ignores = new HashSet<>();
    private JarMatcher _jarMatcher = new JarMatcher();

    public ResourceScanner() {
    }

    @SuppressWarnings("unchecked")
    public Iterator<SourceDescriptor> scan() {
        // classpath要素を分解してスキャン対象を決定。
        // includeJar, excludeJarの指定が存在しない場合は互換性のため全てのJarを含める
        String[] pathArray = System.getProperty("java.class.path", ".").split(File.pathSeparator);
        for (String path: pathArray) {
            File file = new File(path);
            if (!file.exists()) {
                continue;
            }
            if (file.isDirectory()) {
                _classPath.add(path);
            } else if (file.getName().endsWith(".jar")) {
                if (_jarMatcher.matches(file.toPath().getFileName())) {
                    _jars.add(path);
                }
            }
        }

        IteratorIterator itit = new IteratorIterator();
        for (String path : _classPath) {
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
            for (String extension : _extensions) {
                folderScanner.setParameter("extension", extension);
            }
            itit.add(folderScanner.scan());
            if (LOG.isDebugEnabled()) {
                LOG.debug("scan path: " + path);
            }
        }
        for (String path : _jars) {
            if (StringUtil.isEmpty(path)) {
                continue;
            }
            File jarFile = new File(path).getAbsoluteFile();
            JarSourceScanner jarScanner = new JarSourceScanner();
            FileSourceDescriptor descriptor = new FileSourceDescriptor();
            descriptor.setFile(jarFile);
            descriptor.setSystemID(jarFile.getName());
            jarScanner.setDescriptor(descriptor);
            if (jarFile.getParent() != null) {
                jarScanner.setParameter("appPath", jarFile.getParent());
            }
            if (_root != null) {
                jarScanner.setParameter("folder", _root);
            }
            for (String ignore : _ignores) {
                jarScanner.setParameter("ignore", ignore);
            }
            for (String extension : _extensions) {
                jarScanner.setParameter("extension", extension);
            }
            itit.add(jarScanner);
            if (LOG.isDebugEnabled()) {
                LOG.debug("scan jar: " + jarFile.getAbsolutePath());
            }
        }
        return (Iterator<SourceDescriptor>) itit;
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
        } else if ("includeJar".equals(name)) {
            _jarMatcher.include(value);
        } else if ("excludeJar".equals(name)) {
            _jarMatcher.exclude(value);
        } else {
            super.setParameter(name, value);
        }
    }

    /**
     * Jarファイル名に対してGlobパターンで含む(include)・除外する(exclude)を順番に追加管理する。
     * 評価時は指定された順番で行い、最初にパターンに一致した結果を返す
     */
    class JarMatcher {
        List<JarMatcherElement> _jarMatchers = new ArrayList<>();
        class JarMatcherElement {
            boolean isInclude;
            PathMatcher matcher;
            JarMatcherElement(String globPattern, boolean isInclude) {
                this.isInclude = isInclude;
                this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
            }
        }

        void include(String globPattern) {
            _jarMatchers.add(new JarMatcherElement(globPattern, true));
        }
        void exclude(String globPattern) {
            _jarMatchers.add(new JarMatcherElement(globPattern, false));
        }

        boolean matches(Path jarPath) {
            for (JarMatcherElement elm: _jarMatchers) {
                final boolean match = elm.matcher.matches(jarPath);
                if (match) {
                    // 最初にマッチした結果を返す。
                    return elm.isInclude;
                }
            }
            return true;
        }
    }
}

