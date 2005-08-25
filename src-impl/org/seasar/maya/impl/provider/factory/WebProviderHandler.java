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

import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.maya.impl.util.xml.TagHandlerStack;
import org.seasar.maya.provider.ServiceProvider;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebProviderHandler extends DefaultHandler
        implements CONST_IMPL {

    private TagHandlerStack _stack;
    
    public ServiceProvider getResult() {
        return ((ServiceTagHandler)_stack.getRoot()).getServiceProvider();
    }
    
    public WebProviderHandler(ServletContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        _stack = new TagHandlerStack(new ServiceTagHandler(context));
    }
    
	public InputSource resolveEntity(String publicId, String systemId) {
        if(PUBLIC_PROVIDER10.equals(publicId)) {
            ClassLoaderSourceDescriptor source = new ClassLoaderSourceDescriptor();
            source.setSystemID("maya-provider_1_0.dtd");
            source.setNeighborClass(WebProviderHandler.class);
            if(source.exists()) {
                return new InputSource(source.getInputStream());
            }
        }
        return null;
    }

    public void startElement(String namespaceURI, 
            String localName, String qName, Attributes attributes) {
        _stack.startElement(localName, attributes);
    }
    
    public void endElement(String namespaceURI, String localName, String qName) {
        _stack.endElement();
    }

    public void fatalError(SAXParseException e) {
        error(e);
    }

    public void error(SAXParseException e) {
        throw new RuntimeException(e);
    }
    
}
