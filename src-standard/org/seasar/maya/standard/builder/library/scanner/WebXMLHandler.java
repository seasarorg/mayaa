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
package org.seasar.maya.standard.builder.library.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.impl.source.JavaSourceDescriptor;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * web.xmlからtaglibディレクティブの情報を取り出すためのSAXハンドラ.
 * @author suga
 */
public class WebXMLHandler extends DefaultHandler {

    private static final Log LOG = LogFactory.getLog(WebXMLHandler.class);

    private static final String WEB_DTD_PUBLIC_ID_22 = "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    private static final String WEB_DTD_RESOURCE_PATH_22 = "/javax/servlet/resources/web-app_2_2.dtd";

    private static final String WEB_DTD_PUBLIC_ID_23 = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    private static final String WEB_DTD_RESOURCE_PATH_23 = "/javax/servlet/resources/web-app_2_3.dtd";

    private static final String WEB_DTD_PUBLIC_ID_24 = "web-app_2_4.xsd";
    private static final String WEB_DTD_RESOURCE_PATH_24 = "/javax/servlet/resources/web-app_2_4.xsd";
    
    private static Map _entities;
    static {
        _entities = new HashMap();
        _entities.put(WEB_DTD_PUBLIC_ID_22, WEB_DTD_RESOURCE_PATH_22);
        _entities.put(WEB_DTD_PUBLIC_ID_23, WEB_DTD_RESOURCE_PATH_23);
        _entities.put(WEB_DTD_PUBLIC_ID_24, WEB_DTD_RESOURCE_PATH_24);
    }

    private List _taglibs;
    private boolean _inTaglib;
    private boolean _inTaglibElement;
    private StringBuffer _buffer = new StringBuffer();
    private String _uri;
    private String _location;

    public void startDocument() {
        _taglibs = new ArrayList();
        _inTaglib = false;
        _inTaglibElement = false;
        _buffer.setLength(0);
        _uri = null;
        _location = null;
    }
    
    public InputSource resolveEntity(String publicId, String systemId) {
        String path;
        if(_entities.containsKey(publicId)) {
            path = (String)_entities.get(publicId);
        } else {
            return null;
        }
        JavaSourceDescriptor source = 
            new JavaSourceDescriptor(path, WebXMLHandler.class);
        if(source.exists()) {
            InputSource ret = new InputSource(source.getInputStream());
            ret.setPublicId(publicId);
            ret.setSystemId(path);
            return ret;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("cann't resolve locally, publicId=" + publicId + ", systemId=" + systemId);
        }
        return null;
    }
    
    public void startElement(
            String namespaceURI, String localName, String qName, Attributes attributes) {
        if (qName.equalsIgnoreCase("taglib")) {
            _inTaglib = true;
        } else if (_inTaglib) {
            if (qName.equalsIgnoreCase("taglib-uri") ||
                    qName.equalsIgnoreCase("taglib-location")) {
                _inTaglibElement = true;
                _buffer.setLength(0);
            }
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        if (qName.equalsIgnoreCase("taglib")) {
            _taglibs.add(new TaglibDirective(_uri, _location));
            _inTaglib = false;
        } else if (_inTaglib) {
            if (qName.equalsIgnoreCase("taglib-uri")) {
                _uri = new String(_buffer);
                _inTaglibElement = false;
            } else if (qName.equalsIgnoreCase("taglib-location")) {
                _location = new String(_buffer);
                _inTaglibElement = false;
            }
        }
    }

    public void characters(char[] ch, int start, int length) {
        if (_inTaglibElement) {
            _buffer.append(new String(ch, start, length).trim());
        }
    }

    public TaglibDirective[] getTaglibs() {
        return (TaglibDirective[]) _taglibs.toArray(
                new TaglibDirective[_taglibs.size()]);
    }

}
