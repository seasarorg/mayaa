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
package org.seasar.mayaa.impl.source;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import jakarta.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.impl.builder.library.scanner.SourceAlias;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * ApplicationScopeを基準としたSourceDescriptor。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author Koji Suga (Gluegent Inc.)
 */
public class ApplicationSourceDescriptor implements ChangeableRootSourceDescriptor, HavingAliasSourceDescriptor {

    private static final Log LOG = LogFactory.getLog(ApplicationSourceDescriptor.class);

    public static final String WEB_INF = "/WEB-INF";

    private ApplicationScope _application;

    /** Fileベースで取得できるかどうか */
    private Boolean _useFile;
    private URL _url;

    private FileSourceDescriptor _fileSourceDescriptor;

    private SourceAlias _alias;

    public ApplicationSourceDescriptor() {
        _fileSourceDescriptor = new FileSourceDescriptor();
        _fileSourceDescriptor.setRoot("");
    }

    // use while building ServiceProvider.
    public void setApplicationScope(ApplicationScope application) {
        if (application == null) {
            throw new IllegalArgumentException();
        }
        _application = application;
    }

    public ApplicationScope getApplicationScope() {
        if (_application == null) {
            _application = FactoryFactory.getApplicationScope();
        }
        return _application;
    }

    @Override
    public SourceAlias getAlias() {
        return _alias;
    }

    public void setAlias(SourceAlias alias) {
        _alias = alias;
    }

    @Override
    public void setRoot(String root) {
        _fileSourceDescriptor.setRoot(root);
    }

    protected URL getURL() {
        String path = _fileSourceDescriptor.getRealPath();
        if (StringUtil.isEmpty(path)) {
            path = "/";
        }
        // TODO ここにServletContextクラスが登場しないようにする
        ServletContext context =
            (ServletContext) getApplicationScope().getUnderlyingContext();
        try {
            URL url = context.getResource(path);
            return url;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("invalid: " + path);
        }
    }

    /**
     * ファイルが見つからない場合、ServletContextからURLで探す。
     * ServletContextから見つかった場合はそれを利用するようにする。
     * それでも見つからなかった場合、存在しないリソースとして処理する。
     *
     * @return ファイルとして扱うならtrue
     */
    protected final boolean canUseFile() {
        if (_useFile == null) {
            _useFile = Boolean.TRUE;
            if (_fileSourceDescriptor.exists() == false) {
                _useFile = Boolean.FALSE;
                URL url = getURL();
                if (url != null) {
                    _url = url;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(getSystemID() + " is read by URL (" +
                                _url + ")");
                    }
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(getSystemID() + " is read by FILE (" +
                            _fileSourceDescriptor.getFile() + ")");
                }
            }
        }
        return _useFile.booleanValue();
    }

    @Override
    public boolean exists() {
        if (canUseFile()) {
            return _fileSourceDescriptor.exists();
        }
        return _url != null;
    }

    @Override
    public InputStream getInputStream() {
        if (canUseFile()) {
            return _fileSourceDescriptor.getInputStream();
        }
        return IOUtil.openStream(_url);
    }

    @Override
    public Date getTimestamp() {
        if (canUseFile()) {
            return _fileSourceDescriptor.getTimestamp();
        }
        return new Date(IOUtil.getLastModified(_url));
    }


    @Override
    public void setSystemID(String systemID) {
        _fileSourceDescriptor.setSystemID(systemID);
    }

    @Override
    public String getSystemID() {
        return _fileSourceDescriptor.getSystemID();
    }
}
