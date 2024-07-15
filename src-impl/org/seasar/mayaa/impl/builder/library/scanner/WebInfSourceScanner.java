/*
 * Copyright 2004-2024 the Seasar Foundation and the Others.
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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.library.scanner.SourceScanner;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.impl.NonSerializableParameterAwareImpl;
import org.seasar.mayaa.impl.source.ApplicationFileSourceDescriptor;
import org.seasar.mayaa.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.mayaa.impl.source.HavingAliasSourceDescriptor;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * Webアプリケーションのコンテキスト内のリソースを走査してライブラリファイルを検出するための{@code SourceScanner}実装。
 * 走査対象は WEB-INF配下のリソースで、lib内のJARファイルも走査対象となる。
 * 
 * <p>
 * ServiceProviderの設定ファイルで以下のように設定することで、走査対象のライブラリファイルを指定することができる。
 * 設定は記述順で評価され、最初に合致したものが採用される。
 * <p>
 * {@code include}と{@code exclude}は {@code /WEB-INF/}を基準としてライブラリファイル(tld,mld)を検出対象(include)または除外対象(exclude)するためのGlobパターンを指定する。記述順に評価され最初に合致したもので判定される。</br>
 * どちらのパターンも指定されない場合は{@code include="&#42;&#42;/&#42;.&#123;tld,mld&#125;"}のみが指定されたものとみなす。
 * 
 * <p>
 * {@code includeJar}と{@code excludeJar}で走査対象JARファイルのWEB-INF/libを基準としたファイル名部分のGlobパターンを指定する。<br>
 * どちらのパターンも指定されない場合は {@code includeJar="*.jar"} のみが指定されたものとみなす。
 * 
 * <p>
 * {@code includeInJarMetaInf}と{@code excludeInJarMetaInf}で読み込み対象とするライブラリファイルパスのJARファイル内のMETA-INFを基準としたGlobパターンを指定する。<br>
 * どちらのパターンも指定されない場合は {@code includeInJarMetaInf="&#42;.&#123;tld,mld&#125;"} のみが指定されたものとみなす。
 * 
 * <p>
 * 記述例
 * <pre>
 * {@code
 *<scanner class="org.seasar.mayaa.impl.builder.library.scanner.WebInfSourceScanner">
 *   <parameter name="exclude" value="&#123;classes,lib&#125;/"/>
 *   <parameter name="include" value="&#42;&#42;/&#42;.&#123;tld,mld&#125;"/>
 *   <parameter name="includeJar" value="taglibs-*.jar"/>
 *   <parameter name="excludeInJarMetaInf" value="#123;x,sql,scriptfree,fn,permittedTaglibs#125;*"/>
 *   <parameter name="includeInJarMetaInf" value="*.tld"/>
 *</scanner>
 * }
 * </pre>
 * 
 * @param include    読み込み対象とするライブラリファイルパスのWEB-INFを基準としたGlobパターン
 * @param exclude    読み込み対象外とするライブラリファイルパスのWEB-INFを基準としたGlobパターン
 * @param includeJar 走査対象JARファイルのWEB-INF/libを基準としたファイル名部分のGlobパターン
 * @param excludeJar 走査対象外のJARファイルのWEB-INF/libを基準としたファイル名部分のGlobパターン
 * @param includeInJarMetaInf 読み込み対象とするライブラリファイルパスのJARファイル内のMETA-INFを基準としたGlobパターン
 * @param excludeInJarMetaInf 読み込み対象外とするライブラリファイルパスのJARファイル内のMETA-INFを基準としたGlobパターン
 * 
 * @author Mitsutaka Watanabe <https://github.com/mitonize>
 */
public class WebInfSourceScanner extends NonSerializableParameterAwareImpl implements SourceScanner {
    private static final Log LOG = LogFactory.getLog(WebInfSourceScanner.class.getName());

    private ApplicationFileSourceDescriptor _appSource;
    private Path _basePath;
    private ApplicationScope _appScope;
    private FileMatcher _fileMatchers;
    private FileMatcher _jarMatchers;
    private FileMatcher _inJarMetaInfMatchers;

    public WebInfSourceScanner() {
        _appSource = new ApplicationFileSourceDescriptor();
        _appScope = _appSource.getApplicationScope();

        _basePath = Paths.get(_appSource.getFile().toPath().toString(), "WEB-INF");
        _fileMatchers = new FileMatcher(_basePath);

        _jarMatchers = new FileMatcher(Paths.get(_basePath.toString(), "lib"));
        _inJarMetaInfMatchers = new FileMatcher(Paths.get("META-INF"));
    }

    /**
     * META-INF内の複数のJARファイルごとに生成される{@code SourceScanner}を集約することで
     * JARに含まれる{@code SourceDescriptor}を横断して走査するためのイテレータを返す。
     * 
     * @see SourceScanner
     */
    public Iterator<SourceDescriptor> scan() {
        // 条件が未設定の時はデフォルト値を設定する
        if (_fileMatchers.isEmpty()) {
            _fileMatchers.include("**/*.{tld,mld}");
        }
        if (_jarMatchers.isEmpty()) {
            _jarMatchers.include("*.jar");
        }
        if (_inJarMetaInfMatchers.isEmpty()) {
            _inJarMetaInfMatchers.include("*.{tld,mld}");
        }

        List<SourceDescriptor> sources = new ArrayList<>();
        try {
            // ディレクトリを再起的に走査する
            Files.walkFileTree(_basePath, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // PathMatcherでディレクトリ名に対するパターンが指定されていたらスキップする。
                    if (_fileMatchers.matchesDirectory(dir)) {
                        LOG.debug("TRAVERSE " + dir.toString());
                        return FileVisitResult.CONTINUE;
                    } else if (_jarMatchers.matchesDirectory(dir)) {
                        LOG.debug("TRAVERSE " + dir.toString());
                        return FileVisitResult.CONTINUE;
                    }
                    LOG.debug("SKIP " + dir.toString());
                    return FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (_fileMatchers.matches(file)) {
                        SourceDescriptor descriptor = toSourceDescriptor(_appScope, file);
                        sources.add(descriptor);
                        logSourceFound(descriptor);
                    } else if (_jarMatchers.matches(file)) {
                        sources.addAll(scanInJar(toSourceDescriptor(_appScope, file), _inJarMetaInfMatchers));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sources.iterator();
    }

    /**
     * 指定されたJARファイルを走査して指定されたMatcherに合致するライブラリファイルを検索する。
     *
     * @param sourceDescriptor JARファイルの{@code SourceDescriptor}を指定。
     * @param matchers JAR内で検索するライブラリファイルの条件を定義した{@code SourceDescriptor}を指定。
     * @return 合致するライブラリファイルの{@code SourceDescriptor}のリスト。
     * @throws RuntimeException JARファイルのスキャン中にIOエラーが発生した場合。
     */
    List<SourceDescriptor> scanInJar(SourceDescriptor sourceDescriptor, FileMatcher matchers) {
        final String systemID = sourceDescriptor.getSystemID();
        final Path jarFilePath = Paths.get(systemID);

        List<SourceDescriptor> sources = new ArrayList<>();
        try (JarInputStream jar = new JarInputStream(sourceDescriptor.getInputStream())) {
            JarEntry entry;
            while ((entry = jar.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.startsWith("META-INF") && matchers.matches(Paths.get(entryName))) {

                    Date timestamp = sourceDescriptor.getTimestamp();

                    SourceAlias alias = new SourceAlias(jarFilePath.toString(), entryName, timestamp);
                    ClassLoaderSourceDescriptor descriptor = new ClassLoaderSourceDescriptor();
                    descriptor.setSystemID(alias.getSystemID());
                    descriptor.setAlias(alias);
                    descriptor.setTimestamp(alias.getTimestamp());
                    sources.add(descriptor);
                    logSourceFound(descriptor);
                }
            }
            return sources;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void logSourceFound(SourceDescriptor descriptor) {
        if (LOG.isDebugEnabled()) {
            if (descriptor instanceof HavingAliasSourceDescriptor) {
                SourceAlias alias = ((HavingAliasSourceDescriptor) descriptor).getAlias();
                if (alias != null) {
                    LOG.debug("FOUND " + alias.getAlias() + "!" + alias.getSystemID());
                    return;
                }
            }
            LOG.debug("FOUND " + descriptor.getSystemID());
        }
    }

    /**
     * 指定されたパスを{@code SourceDescriptor}に変換する。
     * @param appScope アプリケーションスコープ
     * @param path ファイルパス
     * @return 変換された{@code SourceDescriptor}
     */
    private SourceDescriptor toSourceDescriptor(ApplicationScope appScope, Path path) {
        Path p = _basePath.relativize(path);
        final String systemID = StringUtil.preparePath("/WEB-INF/" + p.toString());

        ApplicationFileSourceDescriptor source = new ApplicationFileSourceDescriptor();
        source.setRoot(null);
        source.setSystemID(systemID);
        source.setFile(path.toFile());

        return source;
    }

    // Parameterizable implements ------------------------------------
    public void setParameter(String name, String value) {
        LOG.info("CONFIG " + name + "=" + value);
        if ("include".equals(name)) {
            _fileMatchers.include(value);
        } else if ("includeJar".equals(name)) {
            _jarMatchers.include(value);
        } else if ("includeInJarMetaInf".equals(name)) {
            _inJarMetaInfMatchers.include(value);
        } else if ("exclude".equals(name)) {
            _fileMatchers.exclude(value);
        } else if ("excludeJar".equals(name)) {
            _jarMatchers.exclude(value);
        } else if ("excludeInJarMetaInf".equals(name)) {
            _inJarMetaInfMatchers.exclude(value);
        }
        super.setParameter(name, value);
    }


    /**
     * ファイル名に対してGlobパターンで含む(include)・除外する(exclude)を順番に追加管理する。
     * 評価時は指定された順番で行い、最初にパターンに一致した結果を返す.
     * いずれにもマッチしない場合はデフォルト値を返す。デフォルト値の初期値は {@code false} (マッチしない)である。
     */
    final class FileMatcher implements PathMatcher {
        private List<FileMatcherElement> _fileMatchers = new ArrayList<>();
        private List<FileMatcherElement> _directoryMatchers = new ArrayList<>();
        private boolean _defaultResult = true;
        private Path _basePath;

        FileMatcher(Path basePath) {
            this(basePath, false);
        }

        FileMatcher(Path basePath, boolean defaultResult) {
            _basePath = basePath;
            _defaultResult = defaultResult;
        }

        class FileMatcherElement {
            boolean isInclude;
            PathMatcher matcher;
            FileMatcherElement(String globPattern, boolean isInclude) {
                this.isInclude = isInclude;
                this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
            }
        }

        void include(String globPattern) {
            _fileMatchers.add(new FileMatcherElement(globPattern, true));
            int lastSlashIndex = globPattern.lastIndexOf('/');
            if (lastSlashIndex > 0) {
                _directoryMatchers.add(new FileMatcherElement(globPattern.substring(0, lastSlashIndex), true));
            }
        }
        void exclude(String globPattern) {
            _fileMatchers.add(new FileMatcherElement(globPattern, false));
            int lastSlashIndex = globPattern.lastIndexOf('/');
            if (lastSlashIndex > 0) {
                _directoryMatchers.add(new FileMatcherElement(globPattern.substring(0, lastSlashIndex), false));
            }
        }

        @Override
        public boolean matches(Path path) {
            path = _basePath.relativize(path);
            for (FileMatcherElement elm: _fileMatchers) {
                final boolean match = elm.matcher.matches(path);
                if (match) {
                    // 最初にマッチした結果を返す。
                    return elm.isInclude;
                }
            }
            return _defaultResult;
        }

        public boolean matchesDirectory(Path path) {
            path = _basePath.relativize(path);
            if (path.toString().isEmpty()) {
                return true;
            }
            for (FileMatcherElement elm: _directoryMatchers) {
                final boolean match = elm.matcher.matches(path);
                if (match) {
                    // 最初にマッチした結果を返す。
                    return elm.isInclude;
                }
            }
            return _defaultResult;
        }

        boolean isEmpty() {
            return _fileMatchers.isEmpty();
        }
    }

}
