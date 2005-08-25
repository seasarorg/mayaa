/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.source;

import java.io.InputStream;
import java.util.Date;

import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NullSourceDescriptor implements SourceDescriptor {

	private static final long serialVersionUID = -6864473214459610814L;
    private static final Date ZERO = new Date(0); 

    private static NullSourceDescriptor _instance = new NullSourceDescriptor();
    
    public static NullSourceDescriptor getInstance() {
        return _instance;
    }

    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(name);
    }
    
	public boolean exists() {
        return false;
    }

    public InputStream getInputStream() {
        return null;
    }
    
    public void setSystemID(String systemID) {
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
