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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import jakarta.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * セキュアなWebサーバー環境のために直接Fileを扱わずにContext経由で読み出すSourceDescriptor。
 *
 * @author Taro KATO (Gluegent Inc.)
 */
public class ApplicationResourceSourceDescriptor extends ParameterAwareImpl
        implements ChangeableRootSourceDescriptor {

    private static final long serialVersionUID = -2775274363708858237L;
    static final Log LOG = LogFactory.getLog(ApplicationResourceSourceDescriptor.class);

    private transient ApplicationScope _application;

    /** Fileベースで取得できるかどうか */
    private transient URL _url;

    private transient String _systemID;

    public ApplicationResourceSourceDescriptor() {
        // do nothing.
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

    public void setRoot(String root) {
        // ignore
    }

    protected URL getURL() {
    	if (_url == null) {
            String path = _systemID;
            if (StringUtil.isEmpty(path)) {
                path = "/";
            }
            // TODO ここにServletContextクラスが登場しないようにする
            ServletContext context =
                (ServletContext) getApplicationScope().getUnderlyingContext();
            try {
            	// TODO GAE for Javaの場合、nullが返る場合がある。どうしたもんか...
            	_url = context.getResource(path);
            	if (_url == null) {
            		LOG.debug("NG. getResource failed. - " + path);
            	} else {
            		LOG.debug("OK. getResource url succeed. - " + path);
            	}
                return _url;
            } catch (MalformedURLException e) {
                throw new IllegalStateException("invalid: " + path);
            }
    	}
    	return _url;
    }


    /* (non-Javadoc)
     * @see org.seasar.mayaa.source.SourceDescriptor#exists()
     */
    public boolean exists() {
    	URL url = getURL();
        if (url != null) {
        	return true;
        }
        // GAE for Java 対応。_urlはnullなのでStreamで確認する。
        ServletContext context =
            (ServletContext) getApplicationScope().getUnderlyingContext();
        InputStream resourceAsStream = context.getResourceAsStream(_systemID);
        try {
        	return resourceAsStream != null;
        } finally {
        	try {
        		if (resourceAsStream != null) {
        			resourceAsStream.close();
        		}
			} catch (IOException e) {
				return false;
			}
        }
    }

    /* (non-Javadoc)
     * @see org.seasar.mayaa.source.SourceDescriptor#getInputStream()
     */
    public InputStream getInputStream() {
        return IOUtil.openStream(getURL());
    }

    /* (non-Javadoc)
     * @see org.seasar.mayaa.source.SourceDescriptor#getTimestamp()
     */
    public Date getTimestamp() {
        return new Date(IOUtil.getLastModified(getURL()));
    }


    /* (non-Javadoc)
     * @see org.seasar.mayaa.impl.ParameterAwareImpl#setSystemID()
     */
    public void setSystemID(String systemID) {
    	if (!StringUtil.equals(_systemID, systemID)) {
    		_systemID = systemID;
    		_url = null;
    	}
    }

    /* (non-Javadoc)
     * @see org.seasar.mayaa.impl.ParameterAwareImpl#getSystemID()
     */
    public String getSystemID() {
        return _systemID;
    }

    // for serialize

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        _application = CycleUtil.getServiceCycle().getApplicationScope();
    }

}
