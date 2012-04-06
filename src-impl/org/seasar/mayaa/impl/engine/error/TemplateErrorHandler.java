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
package org.seasar.mayaa.impl.engine.error;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.error.ErrorHandler;
import org.seasar.mayaa.impl.IllegalParameterValueException;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.PageNotFoundException;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateErrorHandler extends ParameterAwareImpl
        implements ErrorHandler {

    private static final long serialVersionUID = -6643723526947091557L;

    private static final Log LOG =
        LogFactory.getLog(TemplateErrorHandler.class);

    private String _folder = "/";
    private String _extension = "html";

    protected String getFolder() {
        return _folder;
    }

    protected String getExtension() {
        return _extension;
    }

    protected String getPageName(Class throwableClass) {
        if (throwableClass == null) {
            throw new IllegalArgumentException();
        }
        String name = throwableClass.getName();
        return StringUtil.preparePath(getFolder())
                + StringUtil.preparePath(name);
    }

    public void doErrorHandle(Throwable t, boolean pageFlush) {
        if (t == null) {
            throw new IllegalArgumentException();
        }
        boolean isPageNotFound = t instanceof PageNotFoundException;
        if (isPageNotFound && LOG.isInfoEnabled()) {
            LOG.info(t.getMessage());
        }
        for (Class throwableClass = t.getClass();
                throwableClass != null;
                throwableClass = throwableClass.getSuperclass()) {
            String pageName = getPageName(throwableClass);
            try {
                Engine engine = ProviderUtil.getEngine();
                Page page = engine.getPage(pageName);
                SpecificationUtil.startScope(null);
                try {
                    page.doPageRender("", getExtension());
                } finally {
                    SpecificationUtil.endScope();
                }
                if (isPageNotFound == false && LOG.isErrorEnabled()) {
                    String msg = StringUtil.getMessage(
                            TemplateErrorHandler.class, 1, t.getMessage());
                    LOG.error(msg, t);
                }
                if (pageFlush) {
                    Response response = CycleUtil.getResponse();
                    response.flush();
                }
                break;
            } catch (PageNotFoundException ignore) {
                if (LOG.isInfoEnabled()) {
                    String msg = StringUtil.getMessage(
                            TemplateErrorHandler.class, 2, pageName);
                    LOG.info(msg);
                }
            }
        }
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if ("folder".equals(name)) {
            if (StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            _folder = value;
        } else if ("extension".equals(name)) {
            if (StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            _extension = value;
        }
        super.setParameter(name, value);
    }

}
