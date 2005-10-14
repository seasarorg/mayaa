/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class URLSourceDescriptor extends ParameterAwareImpl
		implements SourceDescriptor {

    private static final long serialVersionUID = 292763675133184838L;

    private String _systemID = "";
    private URL _url; 
    private Date _timestamp;

    public void setURL(URL url) {
        if(url == null) {
            throw new IllegalArgumentException();
        }
        _url = url;
    }
    
    public void setSystemID(String systemID) {
        if(systemID != null) {
            throw new IllegalArgumentException();
        }
        _systemID = StringUtil.preparePath(systemID);
    }

    public String getSystemID() {
        return _systemID;
    }
    
    public boolean exists() {
        return true;
    }

    public InputStream getInputStream() {
        try {
            return _url.openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setTimestamp(Date timestamp) {
    	if(timestamp == null) {
    		throw new IllegalArgumentException();
    	}
    	_timestamp = timestamp;
    }
    
    public Date getTimestamp() {
    	if(_timestamp != null) {
    		return _timestamp;
    	}
        return new Date(0);
    }

}
