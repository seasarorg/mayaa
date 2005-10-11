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

import java.io.InputStream;
import java.util.Date;

import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NullSourceDescriptor extends ParameterAwareImpl
		implements SourceDescriptor {

	private static final long serialVersionUID = -6864473214459610814L;
    private static final Date ZERO = new Date(0); 

    private static NullSourceDescriptor _instance = new NullSourceDescriptor();
    
    public static NullSourceDescriptor getInstance() {
        return _instance;
    }
    
	public boolean exists() {
        return false;
    }

    public InputStream getInputStream() {
        return null;
    }
    
    public void setSystemID(String systemID) {
        // do nothing.
    }

    public String getSystemID() {
        return "/";
    }
    
    public Date getTimestamp() {
        return ZERO;
    }

    public String getAttribute(String name) {
        return null;
    }

}
