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
package org.seasar.mayaa.impl.standalone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.FactoryFactoryImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.web.MockHttpServletRequest;
import org.seasar.mayaa.impl.cycle.web.MockHttpServletResponse;
import org.seasar.mayaa.impl.cycle.web.MockServletContext;
import org.seasar.mayaa.impl.engine.EngineImpl;
import org.seasar.mayaa.impl.engine.processor.JspProcessor;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.source.ApplicationSourceDescriptor;
import org.seasar.mayaa.impl.source.SourceHolderFactory;
import org.seasar.mayaa.impl.util.ReferenceCache;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceHolder;

/**
 * コンテキストルートからレンダリング対象のファイルを探し、レンダリングした結果を
 * ファイルに書き出します。
 * コンテキストパスはROOTの扱い(空文字列)になります。
 *
 * 必要な情報はbasePathとoutputPathです。
 * basePathはWebアプリケーションでいうところのコンテキストルートで、レンダリング
 * するファイルを探すルートフォルダです。
 * 存在しない場合はIllegalArgumentExceptionが発生します。
 * outputPathはレンダリング結果を出力する先のフォルダ名です。
 * 存在しない場合は自動的に作成します。
 *
 * また、AutoPageBuilderと同様の形式でファイル名フィルタを指定できます。
 * フィルタを指定しない場合は".html"の拡張子を持つファイルが対象になります。
 * フィルタの指定方法は2パターンあり、セミコロン(";")で区切ることで複数指定できます。
 * <ol>
 * <li>"."で始まる英数字のみの文字列の場合は拡張子とみなし、一致するものを対象とします。
 *    (大文字小文字を区別しない)</li>
 * <li>1以外の場合は正規表現とみなし、絶対パスがマッチするものを対象とします。</li>
 * </ol>
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class FileSearchRenderer {

    private static final Log LOG = LogFactory.getLog(FileSearchRenderer.class);

    private static final String OPTION_AUTO_BUILD_FILE_FILTERS =
        "autoBuild.fileNameFilters";
    private static final String CONTEXT_PATH = "";

    private String _outputPath;
    private MockServletContext _servletContext;
    private String[] _fileFilters;
    private long _buildTimeSum;

    /**
     * インスタンスを初期化します。
     *
     * basePath, outputPath, contextPathは末尾が"/"でないパスを指定してください。
     * contextPathには空文字列または"/"始まりの文字列で渡してください。
     * ファイル名フィルタにはEngine設定のautoBuildのものを利用します。
     *
     * @param basePath WebContentにあたるフォルダ。
     * @param outputPath 出力先のルートフォルダ。
     * @throws IllegalArgumentException basePath, outputPathがnullまたは空文字列の場合,
     * basePathが存在しない場合
     */
    public void init(final String basePath, final String outputPath) {
        Engine engine = ProviderUtil.getEngine();
        String filters = engine.getParameter(OPTION_AUTO_BUILD_FILE_FILTERS);

        init(basePath, outputPath, filters);
    }

    /**
     * インスタンスを初期化します。
     *
     * basePath, outputPath, contextPathは末尾が"/"でないパスを指定してください。
     * contextPathには空文字列または"/"始まりの文字列で渡してください。
     * ファイル名フィルタがnullまたは空文字列の場合、".html"が適用されます。
     *
     * @param basePath WebContentにあたるフォルダ。
     * @param outputPath 出力先のルートフォルダ。
     * @param filters ファイル名フィルタ
     * @throws IllegalArgumentException basePath, outputPathがnullまたは空文字列の場合,
     * basePathが存在しない場合
     */
    public void init(
            final String basePath, final String outputPath, final String filters) {
        if (StringUtil.isEmpty(basePath)) {
            throw new IllegalArgumentException("basePath cannot be null.");
        }
        if (StringUtil.isEmpty(outputPath)) {
            throw new IllegalArgumentException("outputPath cannot be null.");
        }

        String absoluteBasePath = preparePath(basePath);
        File base = new File(absoluteBasePath);
        if (base.exists() == false) {
            throw new IllegalArgumentException(absoluteBasePath + " is not exists.");
        } else if (base.isDirectory() == false) {
            throw new IllegalArgumentException(absoluteBasePath + " is not directory.");
        }

        _servletContext = new MockServletContext(absoluteBasePath, CONTEXT_PATH);
        _outputPath = preparePath(outputPath);
        if (StringUtil.hasValue(filters)) {
            _fileFilters = filters.split(";");
        } else {
            _fileFilters = new String[]{".html"};
        }

        LOG.info("init start");
        FactoryFactory.setInstance(new FactoryFactoryImpl());
        FactoryFactory.setContext(_servletContext);
        LOG.info("prepareLibraries start");
        ProviderUtil.getLibraryManager().prepareLibraries();
        LOG.info("prepareLibraries end");

        // 不要な設定を強制的に止める
        Engine engine = ProviderUtil.getEngine();
        engine.setParameter(EngineImpl.SURVIVE_LIMIT, "1");
        engine.setParameter(EngineImpl.PAGE_SERIALIZE, "false");

        LOG.info("init end");
    }

    /**
     * パスの前処理として、"\"を"/"に置き換え、末尾の"/"を削って返します。
     * パスが"."で始まっている場合、カレントのパスからの相対パスと見なします。
     *
     * @param targetPath 対象のパス文字列
     * @return 処理済みのパス文字列
     */
    protected String preparePath(final String targetPath) {
        if (StringUtil.isEmpty(targetPath)) {
            return targetPath;
        }
        String path = targetPath.replace(File.separatorChar, '/');
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.startsWith(".")) {
            File pathFile = new File(System.getProperty("user.dir") + "/" + path);
            try {
                path = pathFile.getCanonicalPath();
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
        return path;
    }

    /**
     * ServletContextに属性を追加します。
     * キーがStringのもののみ追加対象となります。
     *
     * @param attributes 属性のマップ
     */
    public void addAttributes(final Map<Object, Object> attributes) {
        if (attributes != null && attributes.size() > 0) {
            for (Object key : attributes.keySet()) {
                if (key instanceof String) {
                    _servletContext.setAttribute((String) key, attributes.get(key));
                }
            }
        }
    }

    /**
     * ファイルへのレンダリングを開始します。
     * ファイルを探し、filtersに適合するものを順番にレンダリングしていきます。
     */
    public void start() {
        long engineBuildTime = diffMillis(0);
        Specification defaultSpec = SpecificationUtil.getDefaultSpecification();
        reportTime(defaultSpec, diffMillis(engineBuildTime));

        _buildTimeSum = 0;
        for (Iterator<SourceHolder> it = SourceHolderFactory.iterator(); it.hasNext();) {
            SourceHolder holder = it.next();
            for (Iterator<String> itSystemID = holder.iterator(_fileFilters);
                    itSystemID.hasNext();) {
                String systemID = itSystemID.next();
                try {
                    buildPage(systemID);
                } catch (Throwable e) {
                    LOG.error("page load failed: " + systemID, e);
                }
            }
        }
        LOG.info("page all build time: " + _buildTimeSum + " msec.");
    }

    public void destroy() {
        ReferenceCache.finishThreads();
        ProviderUtil.getEngine().destroy();
        JspProcessor.clear();

        LogFactory.releaseAll();
    }

    protected void reportTime(final Specification spec, final long time) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(spec.getSystemID() + " build time: " + time + " msec.");
        }
    }

    protected long diffMillis(final long millis) {
        if (millis == 0) {
            return System.currentTimeMillis();
        }
        return System.currentTimeMillis() - millis;
    }

    /**
     * レンダリング結果を出力するOutputStreamを作成します。
     *
     * @param request HttpServletRequest
     * @param systemID レンダリング対象のsystemID
     * @return レンダリング結果を出力するOutputStream
     */
    protected OutputStream createOutputStream(
            final MockHttpServletRequest request, final String systemID) {
        String preparedSystemID;
        if (systemID.startsWith("/") == false) {
            preparedSystemID = "/" + systemID;
        } else {
            preparedSystemID = systemID;
        }
        final String filePath =
                _outputPath + preparedSystemID.replace('/', File.separatorChar);

        new File(filePath).getParentFile().mkdirs();

        try {
            LOG.info("output: " + filePath);
            return new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            LOG.info(e.getMessage());
            return null;
        }
    }

    /**
     * ページをビルドしてレンダリングし、結果をファイルに出力します。
     *
     * @param systemID 対象ページのSystemID
     */
    protected void buildPage(final String systemID) {
        if (systemID.indexOf(ApplicationSourceDescriptor.WEB_INF) >= 0) {
            return;
        }
        final MockHttpServletRequest request =
            new MockHttpServletRequest(_servletContext, CONTEXT_PATH);
        final MockHttpServletResponse response = new MockHttpServletResponse();

        // 結果をファイルに出力するようOutputStreamをセットする
        response.setOnCommitOutputStream(createOutputStream(request, systemID));

        request.setPathInfo(systemID);
        CycleUtil.initialize(request, response);
        try {
            final String pageName = CycleUtil.getRequestScope().getPageName();
            Engine engine = ProviderUtil.getEngine();
            if (engine.isPageRequested()) {
                long pageBuildTime = diffMillis(0);
                final Page page = engine.getPage(pageName);
                pageBuildTime = diffMillis(pageBuildTime);
                reportTime(page, pageBuildTime);

                _buildTimeSum += pageBuildTime;

                long templateBuildTime = diffMillis(0);
                page.getTemplate(
                        CycleUtil.getRequestScope().getRequestedSuffix(),
                        CycleUtil.getRequestScope().getExtension());
                templateBuildTime = diffMillis(templateBuildTime);

                _buildTimeSum += templateBuildTime;

                long renderTime = diffMillis(0);
                engine.doService(null, true);
                renderTime = diffMillis(renderTime);

                _buildTimeSum += renderTime;

                if (LOG.isDebugEnabled()) {
                    if (response.getStatus() ==
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                        LOG.debug(systemID + " (render: ----"
                                + " / build: " + templateBuildTime + " msec)");
                    } else {
                        LOG.debug(systemID + " (render: " + renderTime
                                            + " / build: " + templateBuildTime + " msec)");
                    }
                }
            }
        } finally {
            CycleUtil.cycleFinalize();
        }
    }

    /**
     * @return the buildTimeSum
     */
    public long getBuildTimeSum() {
        return _buildTimeSum;
    }

    /**
     * @param buildTimeSum the buildTimeSum to set
     */
    public void setBuildTimeSum(long buildTimeSum) {
        _buildTimeSum = buildTimeSum;
    }

    /**
     * fileFiltersの浅いコピーを取得します。
     *
     * @return the fileFilters
     */
    public String[] getFileFilters() {
        return StringUtil.arraycopy(_fileFilters);
    }

    /**
     * fileFiltersに浅いコピーをセットします。
     *
     * @param fileFilters the fileFilters to set
     */
    public void setFileFilters(String[] fileFilters) {
        _fileFilters = StringUtil.arraycopy(fileFilters);
    }

    /**
     * @return the outputPath
     */
    public String getOutputPath() {
        return _outputPath;
    }

    /**
     * @param outputPath the outputPath to set
     */
    public void setOutputPath(String outputPath) {
        _outputPath = outputPath;
    }

    /**
     * @return the servletContext
     */
    public MockServletContext getServletContext() {
        return _servletContext;
    }

    /**
     * @param servletContext the servletContext to set
     */
    public void setServletContext(MockServletContext servletContext) {
        _servletContext = servletContext;
    }

}
