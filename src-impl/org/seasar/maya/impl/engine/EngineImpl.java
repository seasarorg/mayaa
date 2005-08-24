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
package org.seasar.maya.impl.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.error.ErrorHandler;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.specification.SpecificationImpl;
import org.seasar.maya.impl.provider.IllegalParameterValueException;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.source.PageSourceDescriptor;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.ScriptUtil;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * Engineの実装クラス。ファクトリを通じて生成される。
 * 生成後、デフォルト設定XMLのSourceDescriptorをセットする。 
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class EngineImpl extends SpecificationImpl
        implements Engine, CONST_IMPL {
    
	private static final long serialVersionUID = 1428444571422324206L;

    private static Set _paramNames;
    static {
        _paramNames = new HashSet();
        _paramNames.add(CHECK_TIMESTAMP);
        _paramNames.add(OUTPUT_WHITE_SPACE);
        _paramNames.add(REPORT_UNRESOLVED_ID);
        _paramNames.add(SUFFIX_SEPARATOR);
        _paramNames.add(WELCOME_FILE_NAME);
    }

    private Map _parameters;
    private ErrorHandler _errorHandler;
    
    /**
     * ファクトリにて呼び出される。
     */
    public EngineImpl() {
        super(QM_ENGINE, null);
    }
    
    public void setParameter(String name, String value) {
        if(_paramNames.contains(name)) {
            if(_parameters == null) {
                _parameters = new HashMap();
            }
            if(StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(name);
            }
            _parameters.put(name, value);
        } else {
            throw new UnsupportedParameterException(name);
        }
    }

    public String getParameter(String name) {
        if(_paramNames.contains(name)) {
            if(_parameters == null) {
                return null;
            }
            return (String)_parameters.get(name);
        }
        throw new UnsupportedParameterException(name);
    }
    
    public void setErrorHandler(ErrorHandler errorHandler) {
        _errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
	    if(_errorHandler == null) {
	        throw new IllegalStateException();
	    }
        return _errorHandler;
    }
    
    public String getKey() {
        return "/engine";
    }
    
    public synchronized Page getPage(String pageName, String extension) {
        String key = SpecificationUtil.createPageKey(pageName, extension);
        Page page = SpecificationUtil.getPage(this, key);
        if(page == null) {
            String path = pageName + ".maya";
            SourceDescriptor source = new PageSourceDescriptor(path);
            page = new PageImpl(this, pageName, extension);
            page.setSource(source);
            addChildSpecification(page);
        }
        return page;
    }
   
	public void doService() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setCurrentNode(this);
        ScriptUtil.execEvent(this, QM_BEFORE_RENDER);
        String pageName = cycle.getRequest().getPageName();
        String extension = cycle.getRequest().getExtension();
        Page page = getPage(pageName, extension);
        page.doPageRender();
        cycle.setCurrentNode(this);
        ScriptUtil.execEvent(this, QM_AFTER_RENDER);
	}

}
