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
package org.seasar.mayaa.impl;

import java.util.Iterator;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.web.MockHttpServletRequest;
import org.seasar.mayaa.impl.cycle.web.MockHttpServletResponse;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.source.ApplicationSourceDescriptor;
import org.seasar.mayaa.impl.source.SourceHolderFactory;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceHolder;

/**
 * ファイルとしてコンテキストルート以下にあるテンプレートを探して順にビルドする。
 * 単独のスレッドで動作する。
 * ServiceProviderファイルのEngine設定でautoBuildオプションをtrueにすると有効。
 * TODO AutoPageBuilderの詳細な設定説明
 * TODO AutoPageBuilderを抽象化し、処理のカスタマイズを可能にする
 *
 * @author Taro Kato (Gluegent, Inc.)
 * @author Koji Suga (Gluegent Inc.)
 */
public class AutoPageBuilder implements Runnable {
    private static final Log LOG = LogFactory.getLog(AutoPageBuilder.class);

    public static final String OPTION_AUTO_BUILD = "autoBuild";
    private static final String OPTION_AUTO_BUILD_REPRAT =
        OPTION_AUTO_BUILD + ".repeat";
    private static final String OPTION_AUTO_BUILD_WAIT =
        OPTION_AUTO_BUILD + ".wait";
    private static final String OPTION_AUTO_BUILD_FILE_FILTERS =
        OPTION_AUTO_BUILD + ".fileNameFilters";
    private static final String OPTION_AUTO_BUILD_RENDER_MATE =
        OPTION_AUTO_BUILD + ".renderMate";
    private static final String OPTION_AUTO_BUILD_CONTEXT_PATH =
        OPTION_AUTO_BUILD + ".contextPath";
    private static final boolean REPEAT_DEFAULT = false;
    private static final int WAIT_DEFAULT = 60;
    private static final boolean RENDER_MATE_DEFAULT = false;
    private static final String CONTEXT_PATH_DEFAULT = "/";

    public static final AutoPageBuilder INSTANCE = new AutoPageBuilder();

    private AutoPageBuilder() {
        // no operation
    }

    private Thread _thread;
    private boolean _repeat;
    private int _wait;
    private boolean _renderMate;
    private String[] _fileFilters;
    private ServletContext _servletContext;
    private String _contextPath;
    private long _buildTimeSum;
    private long _renderTimeSum;

    public void init(ServletConfig servletConfig) {
        init(servletConfig, null);
    }

    /**
     * 初期化処理。
     * Engine設定の各種パラメータを取得した後、別スレッドで自動ビルドを開始する。
     * ただしautoBuildがfalseに設定されている場合は何もしない。
     *
     * contextPathは"/mayaa"のように"/"始まり、"/"無しで終わる。
     *
     * @param servletConfig ServletConfig
     * @param contextPath コンテキストパス
     */
    public void init(ServletConfig servletConfig, String contextPath) {
        Engine engine = ProviderUtil.getEngine();
        boolean autoBuild = ObjectUtil.booleanValue(
                engine.getParameter(OPTION_AUTO_BUILD), false);
        if (autoBuild && !EngineUtil.isInSecureWeb()) {
            _servletContext = servletConfig.getServletContext();
            _contextPath = prepareContextPath(contextPath, engine);
            _repeat = ObjectUtil.booleanValue(
                    engine.getParameter(OPTION_AUTO_BUILD_REPRAT),
                    REPEAT_DEFAULT);
            _wait = ObjectUtil.numberValue(
                    engine.getParameter(OPTION_AUTO_BUILD_WAIT),
                    Integer.valueOf(WAIT_DEFAULT)).intValue() * 1000;
            _renderMate = ObjectUtil.booleanValue(
                    engine.getParameter(OPTION_AUTO_BUILD_RENDER_MATE),
                    RENDER_MATE_DEFAULT);

            String filters = engine.getParameter(OPTION_AUTO_BUILD_FILE_FILTERS);
            if (filters != null) {
                _fileFilters = filters.split(";");
            } else {
                _fileFilters = new String[]{".html"};
            }
            _thread = new Thread(this) {
                {
                    setDaemon(true);
                    setName("mayaa.AutoPageBuilder");
                    setPriority(Thread.MIN_PRIORITY);
                    start();
                }
            };
            LOG.info("mayaa.AutoPageBuilder start");
        }
    }

    /**
     * コンテキストパスの前処理をする。
     * contextPathがnullならEngine設定のautoBuild.contextPathから値を取得する。
     * どちらもnullまたは空文字列ならデフォルトの"/"を返す。
     * そうでない場合、"/foobar"の形式になるよう"/"を調整して返す。
     *
     * @param contextPath initメソッドのパラメータに渡されたcontextPath
     * @param engine 現在のEngineインスタンス
     * @return コンテキストパス
     */
    protected String prepareContextPath(String contextPath, Engine engine) {
        if (contextPath == null) {
            contextPath = engine.getParameter(OPTION_AUTO_BUILD_CONTEXT_PATH);
        }
        if (StringUtil.isEmpty(contextPath)) {
            return CONTEXT_PATH_DEFAULT;
        }
        if (contextPath.charAt(0) != '/') {
            contextPath = '/' + contextPath;
        }
        int length = contextPath.length();
        if (length > 1 && contextPath.charAt(length - 1) == '/') {
            contextPath = contextPath.substring(0, length - 1);
        }
        return contextPath;
    }

    public void destroy() {
        if (_thread != null) {
            _thread = null;
            LOG.info("mayaa.AutoPageBuilder end");
        }
    }

    protected void reportTime(Specification spec, long time) {
        LOG.info(spec.getSystemID() + " build time: " + time + " msec.");
    }

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        try {
            long engineBuildTime = diffMillis(0);
            reportTime(SpecificationUtil.getDefaultSpecification(), diffMillis(engineBuildTime));

            // prepare SourceHolderFactory
            FactoryFactory.getPageSourceFactory();

            while (currentThread == _thread) {
                _buildTimeSum = 0;
                _renderTimeSum = 0;
                for (Iterator<SourceHolder> it = SourceHolderFactory.iterator(); it.hasNext();) {
                    SourceHolder holder = it.next();
                    for (Iterator<String> itSystemID = holder.iterator(_fileFilters);
                            itSystemID.hasNext();) {
                        String systemID = (String) itSystemID.next();
                        if (systemID.startsWith("/") == false) {
                            systemID = "/" + systemID;
                        }
                        try {
                            buildPage(systemID);
                        } catch (Throwable e) {
                            LOG.error("page load failed: " + systemID, e);
                        }
                        Thread.sleep(100);
                    }
                }
                LOG.info("page all build time: " + _buildTimeSum + " msec.");
                if (_renderMate) {
                    LOG.info("page all render time: " + _renderTimeSum + " msec.");
                }
                if (_repeat == false) {
                    break;
                }
                Thread.sleep(_wait);
            }

        } catch (InterruptedException ignore) {
            // no operation
        }
    }

    protected long diffMillis(long millis) {
        if (millis == 0) {
            return System.currentTimeMillis();
        }
        return System.currentTimeMillis() - millis;
    }

    protected void buildPage(String systemID) {
        if (systemID.indexOf(ApplicationSourceDescriptor.WEB_INF) >= 0) {
            return;
        }
        MockHttpServletRequest request =
            new MockHttpServletRequest(_servletContext, _contextPath);
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setPathInfo(systemID);
        CycleUtil.initialize(request, response);
        try {
            String pageName = CycleUtil.getRequestScope().getPageName();
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            Engine engine = ProviderUtil.getEngine();
            Specification defaultSpec = SpecificationUtil.getDefaultSpecification();

            cycle.setOriginalNode(defaultSpec);
            cycle.setInjectedNode(defaultSpec);
            if (engine.isPageRequested()) {
                long pageBuildTime = diffMillis(0);
                Page page = engine.getPage(pageName);
                reportTime(page, diffMillis(pageBuildTime));

                long templateBuildTime = diffMillis(0);
                Template template = page.getTemplate(
                        CycleUtil.getRequestScope().getRequestedSuffix(),
                        CycleUtil.getRequestScope().getExtension());

                templateBuildTime = diffMillis(templateBuildTime);
                _buildTimeSum += templateBuildTime;

                if (_renderMate) {
                    long renderTime = diffMillis(0);
                    engine.doService(null, true);
                    renderTime = diffMillis(renderTime);
                    if (response.getStatus() ==
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                        LOG.debug(systemID + " (render: ----"
                                + " / build: " + templateBuildTime + " msec)");
                    } else {
                        _renderTimeSum += renderTime;
                        LOG.debug(systemID + " (render: " + renderTime
                                            + " / build: " + templateBuildTime + " msec)");
                    }
                } else {
                    reportTime(template, templateBuildTime);
                }
            }
        } finally {
            CycleUtil.cycleFinalize();
        }
    }

}
