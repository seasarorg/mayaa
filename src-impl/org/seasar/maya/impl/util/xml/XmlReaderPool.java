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
package org.seasar.maya.impl.util.xml;

import org.apache.xerces.parsers.SAXParser;
import org.seasar.maya.impl.util.collection.AbstractSoftReferencePool;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class XmlReaderPool extends AbstractSoftReferencePool {

	private static final long serialVersionUID = 1736077679163143852L;

	private static XmlReaderPool _xmlReaderPool;
    
    public static XmlReaderPool getPool() {
        if(_xmlReaderPool == null) {
            _xmlReaderPool = new XmlReaderPool();
        }
        return _xmlReaderPool;
    }
    
    private XmlReaderPool() {
    }
    
    protected Object createObject() {
        XMLReader xmlReader = new SAXParser();
        return xmlReader;
    }
    
    protected boolean validateObject(Object object) {
        return object instanceof XMLReader;
    }

	public XMLReader borrowXMLReader(DefaultHandler handler, 
	        boolean namespaces, boolean validation, boolean xmlSchema) {
	    XMLReader xmlReader = (XMLReader)borrowObject();
        try {
            xmlReader.setFeature("http://xml.org/sax/features/namespaces", namespaces);
            xmlReader.setFeature("http://xml.org/sax/features/validation", validation);
            xmlReader.setFeature("http://apache.org/xml/features/validation/schema", xmlSchema);
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException(e);
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException(e);
        }
        xmlReader.setContentHandler(handler);
        xmlReader.setDTDHandler(handler);
        xmlReader.setEntityResolver(handler);
        xmlReader.setErrorHandler(handler);
	    return xmlReader;
	}
	
	public void returnXMLReader(XMLReader xmlReader) {
	    returnObject(xmlReader);
	}
	
}