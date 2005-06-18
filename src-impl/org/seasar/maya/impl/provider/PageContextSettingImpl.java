/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.impl.provider;

import org.seasar.maya.provider.PageContextSetting;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageContextSettingImpl implements PageContextSetting {

	private String _errorPageURL;
	private boolean _needSession = true;
	private int _bufferSize = 1024 * 8;
	private boolean _autoFlush = true;
	
	public void setErrorPageURL(String errorPageURL) {
		_errorPageURL = errorPageURL;
	}
	
	public String getErrorPageURL() {
		return _errorPageURL;
	}

	public void setNeedSession(	boolean needSession) {
		_needSession = needSession;
	}

	public boolean isNeedSession() {
		return _needSession;
	}

	public void setBufferSize(int bufferSize) {
		_bufferSize = bufferSize;
	}

	public int getBufferSize() {
		return _bufferSize;
	}

	public void setAutoFush(boolean autoFlush) {
		_autoFlush = autoFlush;
	}

	public boolean isAutoFlush() {
		return _autoFlush;
	}

}
