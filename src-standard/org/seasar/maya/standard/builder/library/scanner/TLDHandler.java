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
package org.seasar.maya.standard.builder.library.scanner;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.impl.source.JavaSourceDescriptor;
import org.seasar.maya.impl.util.xml.TagHandlerStack;
import org.seasar.maya.standard.builder.library.JspLibraryDefinition;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * TLDをパースするためのハンドラ.
 * @author suga
 */
public class TLDHandler extends DefaultHandler {
    
    private static final Log LOG = LogFactory.getLog(TLDHandler.class);

    private static final String PUBLIC_ID_11 = "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN";
    private static final String DTD_PATH_11 = "web-jsptaglibrary_1_1.dtd";

    private static final String PUBLIC_ID_12 = "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN";
    private static final String DTD_PATH_12 = "web-jsptaglibrary_1_2.dtd";

    private static final String SCHEMA_LOCATION_20 = "http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd";
    private static final String SCHEMA_PATH_20 = "web-jsptaglibrary_2_0.xsd";

    private static final String DATATYPES_PUBLIC_ID = "datatypes";
    private static final String DATATYPES_PATH = "datatypes.dtd";
    
    private static final String XML_SCHEMA_DTD_PUBLIC_ID = "-//W3C//DTD XMLSCHEMA 200102//EN";
    private static final String XML_SCHEMA_DTD_PATH = "XMLSchema.dtd";
    
    private static final String XML_SCHEMA_LOCATION = "http://www.w3.org/2001/xml.xsd";
    private static final String XML_SCHEMA_PATH = "xml.xsd";
    
    private static final String WEB_SERVICE_CLIENT_LOCATION = "http://www.ibm.com/webservices/xsd/j2ee_web_services_client_1_1.xsd";
    private static final String WEB_SERVICE_CLIENT_PATH = "j2ee_web_services_client_1_1.xsd";

    private static final String J2EE_LOCATION = "http://java.sun.com/xml/ns/j2ee/j2ee_1_4.xsd";
    private static final String J2EE_PATH = "j2ee_1_4.xsd";
    
    private static Map _entities;
	static {
	    _entities = new HashMap();
	    _entities.put(PUBLIC_ID_11, DTD_PATH_11);
	    _entities.put(PUBLIC_ID_12, DTD_PATH_12);
	    _entities.put(SCHEMA_LOCATION_20, SCHEMA_PATH_20);
	    _entities.put(XML_SCHEMA_DTD_PUBLIC_ID, XML_SCHEMA_DTD_PATH);
	    _entities.put(XML_SCHEMA_LOCATION, XML_SCHEMA_PATH);
	    _entities.put(XML_SCHEMA_PATH, XML_SCHEMA_PATH);
	    _entities.put(WEB_SERVICE_CLIENT_LOCATION, WEB_SERVICE_CLIENT_PATH);
	    _entities.put(WEB_SERVICE_CLIENT_PATH, WEB_SERVICE_CLIENT_PATH);
	    _entities.put(J2EE_LOCATION, J2EE_PATH);
	    _entities.put(J2EE_PATH, J2EE_PATH);
	    _entities.put(DATATYPES_PUBLIC_ID, DATATYPES_PATH);
	}
	
	private TagHandlerStack _stack;
	private String _systemID;
	
    public TLDHandler(String systemID) {
        _stack = new TagHandlerStack("taglib", new TaglibTagHandler());
        _systemID = systemID;
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        String path;
        if(_entities.containsKey(publicId)) {
            path = (String)_entities.get(publicId);
        } else if(_entities.containsKey(systemId)) {
            path = (String)_entities.get(systemId);
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
        throw new RuntimeException(_systemID);
	}

	public void fatalError(SAXParseException e) {
        error(e);
	}
    
    public JspLibraryDefinition getResult() {
        return ((TaglibTagHandler)_stack.getRoot()).getLibraryDefinition();
    }
	
}
