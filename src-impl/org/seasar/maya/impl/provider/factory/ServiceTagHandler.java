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

import org.seasar.maya.impl.provider.SimpleServiceProvider;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceTagHandler extends TagHandler {
    
    private ServletContext _context;
    private SimpleServiceProvider _provider;
    
    public ServiceTagHandler(ServletContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        _context = context;
        putHandler("engine", new EngineTagHandler(this));
        putHandler("source", new SourceTagHandler(this));
        putHandler("expression", new ScriptTagHandler(this));
        putHandler("specificationBuilder", new SpecificationBuilderTagHandler(this));
        putHandler("templateBuilder", new TemplateBuilderTagHandler(this));
    }

    protected void start(Attributes attributes) {
        _provider = new SimpleServiceProvider(_context);
    }
    
    protected void end(String body) {
        if(_provider == null) {
            throw new IllegalStateException();
        }
        if(_provider.hasEngine() == false) {
            TagHandler handler = startElement("engine", NULL_ATTR);
            handler.endElement();
        }
        if(_provider.hasSourceFactory() == false) {
            TagHandler handler = startElement("source", NULL_ATTR);
            handler.endElement();
        }
        if(_provider.hasScriptCompiler() == false) {
            TagHandler handler = startElement("expression", NULL_ATTR);
            handler.endElement();
        }
        if(_provider.hasSpecificationBuilder() == false) {
            TagHandler handler = startElement("specificationBuilder", NULL_ATTR);
            handler.endElement();
        }
        if(_provider.hasTemplateBuilder() == false) {
            TagHandler handler = startElement("templateBuilder", NULL_ATTR);
            handler.endElement();
        }
    }
    
    public SimpleServiceProvider getServiceProvider() {
        if(_provider == null) {
            throw new IllegalStateException();
        }
        return _provider;
    }
    
}
