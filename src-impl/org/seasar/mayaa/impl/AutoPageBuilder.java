/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.web.MockHttpServletRequest;
import org.seasar.mayaa.impl.cycle.web.MockHttpServletResponse;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.source.ApplicationSourceDescriptor;
import org.seasar.mayaa.impl.source.SourceHolderFactory;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.source.SourceHolder;

/**
 * @author Taro Kato (Gluegent, Inc.)
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
    private static final boolean REPEAT_DEFAULT = false;
    private static final int WAIT_DEFAULT = 60;
    private static final boolean RENDER_MATE_DEFAULT = false;

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
    private long _buildTimeSum;
    private long _renderTimeSum;

    public void init(ServletConfig servletConfig) {
        Engine engine = ProviderUtil.getEngine();
        boolean autoBuild = ObjectUtil.booleanValue(
                engine.getParameter(OPTION_AUTO_BUILD), false);
        if (autoBuild) {
            _servletContext = servletConfig.getServletContext();
            _repeat = ObjectUtil.booleanValue(
                    engine.getParameter(OPTION_AUTO_BUILD_REPRAT),
                    REPEAT_DEFAULT);
            _wait = ObjectUtil.numberValue(
                    engine.getParameter(OPTION_AUTO_BUILD_WAIT),
                    new Integer(WAIT_DEFAULT)).intValue() * 1000;
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

    public void destroy() {
        if (_thread != null) {
            _thread = null;
            LOG.info("mayaa.AutoPageBuilder end");
        }
    }

    protected void reportTime(Specification spec, long time) {
        LOG.info(spec.getSystemID() + " build time: " + time + " msec.");
    }

    public void run() {
        Thread currentThread = Thread.currentThread();
        try {
            Engine engine = ProviderUtil.getEngine();
            long engineBuildTime = diffMillis(0);
            synchronized (engine) {
                engine.build();
            }
            reportTime(engine, diffMillis(engineBuildTime));

            while (currentThread == _thread) {
                _buildTimeSum = 0;
                _renderTimeSum = 0;
                for (Iterator it = SourceHolderFactory.iterator(); it.hasNext();) {
                    SourceHolder holder = (SourceHolder) it.next();
                    for (Iterator itSystemID = holder.iterator(_fileFilters);
                            itSystemID.hasNext();) {
                        String systemID = (String) itSystemID.next();
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
        MockHttpServletRequest request = new MockHttpServletRequest(_servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setPathInfo(systemID);
        CycleUtil.initialize(request, response);
        try {
            String pageName = CycleUtil.getRequestScope().getPageName();
            Engine engine = ProviderUtil.getEngine();
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
