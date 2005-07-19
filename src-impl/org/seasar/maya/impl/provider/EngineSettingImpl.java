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
package org.seasar.maya.impl.provider;

import java.util.HashMap;
import java.util.Map;

import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.EngineSetting;
import org.seasar.maya.provider.PageContextSetting;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class EngineSettingImpl implements EngineSetting {

    private Map _parameters; 
    private PageContextSetting _pageContextSetting;
    private boolean _checkTimestamp = true;
    private boolean _outputWhitespace = true;
    private boolean _reportUnresolvedID = true;
    private String _suffixSeparator = "$";
    
    public void putParameter(String name, String value) {
    	if(StringUtil.isEmpty(name)) {
    		throw new IllegalArgumentException();
    	}
    	if(_parameters == null) {
    		_parameters = new HashMap();
    	}
    	_parameters.put(name, value);
    }
    
    public String getParameter(String name) {
    	if(StringUtil.isEmpty(name)) {
    		throw new IllegalArgumentException();
    	}
    	if(_parameters == null) {
    		return null;
    	}
    	return (String)_parameters.get(name);
    }

    public void setCheckTimestamp(boolean checkTimestamp) {
    	_checkTimestamp = checkTimestamp;
    }
    
    public boolean isCheckTimestamp() {
        return _checkTimestamp;
    }
    
    public void setOutputWhitespace(boolean outputWhitespace) {
    	_outputWhitespace = outputWhitespace;
    }
    
    public boolean isOutputWhitespace() {
        return _outputWhitespace; 
    }

    public void setReportUnresolvedID(boolean reportUnresolvedID) {
    	_reportUnresolvedID = reportUnresolvedID;
    }
    
    public boolean isReportUnresolvedID() {
        return _reportUnresolvedID; 
    }
    
    public void setSuffixSeparator(String suffixSeparator) {
    	_suffixSeparator = suffixSeparator;
    }
    
    public String getSuffixSeparator() {
    	return _suffixSeparator;
    }
    
    public void setPageContextSetting(PageContextSetting pageContextSetting) {
    	_pageContextSetting = pageContextSetting; 
    }
    
    public PageContextSetting getPageContextSetting() {
    	if(_pageContextSetting == null) {
    		_pageContextSetting = new PageContextSettingImpl();
    	}
    	return _pageContextSetting;
    }
    
}
