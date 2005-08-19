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
package org.seasar.maya.impl.builder.library.scanner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.maya.impl.util.xml.TagHandlerStack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebXMLHandler extends DefaultHandler {

    private static final Log LOG = LogFactory.getLog(WebXMLHandler.class);

    private static final String WEB_DTD_PUBLIC_ID_22 = "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    private static final String WEB_DTD_RESOURCE_PATH_22 = "/web-app_2_2.dtd";

    private static final String WEB_DTD_PUBLIC_ID_23 = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    private static final String WEB_DTD_RESOURCE_PATH_23 = "/web-app_2_3.dtd";

    private static final String WEB_DTD_PUBLIC_ID_24 = "web-app_2_4.xsd";
    private static final String WEB_DTD_RESOURCE_PATH_24 = "/web-app_2_4.xsd";
    
    private static Map _entities;
    static {
        _entities = new HashMap();
        _entities.put(WEB_DTD_PUBLIC_ID_22, WEB_DTD_RESOURCE_PATH_22);
        _entities.put(WEB_DTD_PUBLIC_ID_23, WEB_DTD_RESOURCE_PATH_23);
        _entities.put(WEB_DTD_PUBLIC_ID_24, WEB_DTD_RESOURCE_PATH_24);
    }
    
    private TagHandlerStack _stack;
    private WebAppTagHandler _handler;

    public WebXMLHandler() {
        _handler = new WebAppTagHandler();
        _stack = new TagHandlerStack("web-app", _handler);
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        String path;
        if(_entities.containsKey(publicId)) {
            path = (String)_entities.get(publicId);
        } else {
            return null;
        }
        ClassLoaderSourceDescriptor source = new ClassLoaderSourceDescriptor(
                null, path, WebXMLHandler.class);
        if(source.exists()) {
            InputSource ret = new InputSource(source.getInputStream());
            ret.setPublicId(publicId);
            ret.setSystemId(path);
            return ret;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("cann't resolve locally, publicId=" + publicId + ", systemId=" + systemId);
        }
        return null;
    }
    
    public void startElement(
            String namespaceURI, String localName, String qName, Attributes attributes) {
        _stack.startElement(localName, attributes);
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        _stack.endElement();
    }

    public void characters(char[] ch, int start, int length) {
        _stack.characters(ch, start, length);
    }

    public void warning(SAXParseException e) {
        LOG.warn(e.getMessage(), e);
    }

    public void error(SAXParseException e) {
        if(LOG.isErrorEnabled()) {
            LOG.error(e.getMessage(), e);
        }
        throw new RuntimeException(e);
    }

    public void fatalError(SAXParseException e) {
        if(LOG.isFatalEnabled()) {
            LOG.fatal(e.getMessage(), e);
        }
        throw new RuntimeException(e);
    }
    
    public Iterator iterateTaglibLocations() {
        return _handler.iterateTaglibLocation();
    }
    
}
