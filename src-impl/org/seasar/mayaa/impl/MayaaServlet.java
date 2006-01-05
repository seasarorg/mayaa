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

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MayaaServlet extends HttpServlet {

    private static final long serialVersionUID = -5816552218525836552L;
    private static boolean _inithialized;

    public void init() {
        if(_inithialized == false) {
            FactoryFactory.setInstance(new FactoryFactoryImpl());
            FactoryFactory.setContext(getServletContext());
            _inithialized = true;
        }
        // TODO ƒ‰ƒCƒuƒ‰ƒŠ warm up
        //ProviderUtil.getLibraryManager().iterateLibraryDefinition();
    }

    public void doGet(
            HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    public void doPost(
            HttpServletRequest request, HttpServletResponse response) {
        CycleUtil.initialize(request, response);
        Engine engine = ProviderUtil.getEngine();

        setupCharacterEncoding(request,
                engine.getParameter("requestCharacterEncoding"));

        engine.doService(true);
    }

    protected void setupCharacterEncoding(
            HttpServletRequest request, String encoding) {
        if (request.getCharacterEncoding() == null) {
            try {
                request.setCharacterEncoding(encoding);
            } catch (UnsupportedEncodingException e) {
                String message =
                    StringUtil.getMessage(MayaaServlet.class, 0, encoding);
                Log log = LogFactory.getLog(MayaaServlet.class);
                log.warn(message, e);
            }
        }
    }

}
