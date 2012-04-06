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

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.processor.JspProcessor;
import org.seasar.mayaa.impl.engine.specification.serialize.SerializeThreadManager;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.ReferenceCache;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MayaaServlet extends HttpServlet {

    private static final long serialVersionUID = 2L;
    private static boolean _initialized;

    private static final Log LOG = LogFactory.getLog(MayaaServlet.class);

    public void init() {
        if (_initialized == false) {
            LOG.info("init start");
            FactoryFactory.setInstance(new FactoryFactoryImpl());
            FactoryFactory.setContext(getServletContext());
            _initialized = true;
        }
        LOG.info("prepareLibraries start");
        ProviderUtil.getLibraryManager().prepareLibraries();
        LOG.info("prepareLibraries end");
        initAutoPageBuilder();
        LOG.info("init end");

        /*
         * debugモードならapplicationスコープにデバッグフラグをセットする。
         */
        if (getServletConfig() != null) {
            String debugValue = getServletConfig().getInitParameter("debug");
            if (ObjectUtil.booleanValue(debugValue, false)) {
                getServletContext().setAttribute(CONST_IMPL.DEBUG, Boolean.TRUE);
            }
        }
    }

    /**
     * AutoPageBuilderを初期化する。
     */
    protected void initAutoPageBuilder() {
        AutoPageBuilder.INSTANCE.init(getServletConfig(), getServletContext().getServletContextName());
    }

    public void destroy() {
        ReferenceCache.finishThreads();
        AutoPageBuilder.INSTANCE.destroy();
        ProviderUtil.getEngine().destroy();
        SerializeThreadManager.destroy();
        JspProcessor.clear();
        ObjectUtil.clearCaches();

        LogFactory.releaseAll();
    }

    public void doGet(
            HttpServletRequest request, HttpServletResponse response) {
        doService(request, response);
    }

    public void doPost(
            HttpServletRequest request, HttpServletResponse response) {
        doService(request, response);
    }

    protected void doService(
            HttpServletRequest request, HttpServletResponse response) {
        CycleUtil.initialize(request, response);
        try {
            Engine engine = ProviderUtil.getEngine();

            setupCharacterEncoding(request,
                    engine.getParameter("requestCharacterEncoding"));

            engine.doService(null, true);
        } finally {
            CycleUtil.cycleFinalize();
        }
    }

    protected void setupCharacterEncoding(
            HttpServletRequest request, String encoding) {
        if (request.getCharacterEncoding() == null) {
            try {
                request.setCharacterEncoding(encoding);
            } catch (UnsupportedEncodingException e) {
                String message =
                    StringUtil.getMessage(MayaaServlet.class, 0, encoding);
                LOG.warn(message, e);
            }
        }
    }

}
