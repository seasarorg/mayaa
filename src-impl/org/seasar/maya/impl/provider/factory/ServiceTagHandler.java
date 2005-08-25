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
package org.seasar.maya.impl.provider.factory;

import javax.servlet.ServletContext;

import org.seasar.maya.impl.provider.WebServiceProvider;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceTagHandler extends TagHandler {
    
    private ServletContext _context;
    private WebServiceProvider _provider;
    
    public ServiceTagHandler(ServletContext context) {
        super("service");
        if(context == null) {
            throw new IllegalArgumentException();
        }
        _context = context;
        putHandler(new PageSourceTagHandler(this));
        putHandler(new EngineTagHandler(this));
        putHandler(new ScriptTagHandler(this));
        putHandler(new SpecificationBuilderTagHandler(this));
        putHandler(new TemplateBuilderTagHandler(this));
    }

    protected void start(Attributes attributes) {
        _provider = new WebServiceProvider(_context);
    }
    
    public WebServiceProvider getServiceProvider() {
        if(_provider == null) {
            throw new IllegalStateException();
        }
        return _provider;
    }
    
}
