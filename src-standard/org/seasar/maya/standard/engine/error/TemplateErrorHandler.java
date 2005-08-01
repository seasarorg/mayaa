/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.standard.engine.error;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.error.ErrorHandler;
import org.seasar.maya.impl.builder.PageNotFoundException;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * 例外クラス名に応じたテンプレートによって例外情報を表示する。
 * errorTemplateRoot値のフォルダ直下に置かれた、例外クラス名ページを利用する。たとえば、
 * /WEB-INF/error/java.lang.IllegalArgumentException.html など。
 * テンプレートページ中では、ページスコープのオブジェクト「THROWABLE」
 * で発生した例外を取得することができる。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateErrorHandler  implements ErrorHandler {

    public static final String THROWABLE = "THROWABLE";
    
    private String _errorTemplateRoot = "/WEB-INF/error"; 
    
    private String getPageName(Class throwableClass) {
    	return _errorTemplateRoot + "/" + throwableClass.getName();
    }
    
    public void putParameter(String name, String value) {
        if("errorTemplateRoot".equals(name)) {
        	if(StringUtil.isEmpty(value)) {
        		throw new IllegalArgumentException();
        	}
            _errorTemplateRoot = StringUtil.preparePath(value);
        }
    }
    
    public void doErrorHandle(ServiceCycle cycle, Throwable t) {
        if(t == null || cycle == null) {
            throw new IllegalArgumentException();
        }
        cycle.setAttribute(THROWABLE, t);
        Engine engine = SpecificationUtil.getEngine(cycle);
        try {
            for(Class throwableClass = t.getClass(); 
            		throwableClass != null; 
            		throwableClass = throwableClass.getSuperclass()) {
                try {
                	Page page = engine.getPage(getPageName(throwableClass), "html");
                    page.doPageRender(cycle);
    	            break;
                } catch(PageNotFoundException ignore) {
                }
            }
        } finally {
            cycle.setAttribute(THROWABLE, null);
        }
    }
    
}
