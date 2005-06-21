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
package org.seasar.maya.impl.builder;

import java.io.Serializable;

import org.apache.xerces.parsers.SAXParser;
import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.collection.AbstractSoftReferencePool;
import org.seasar.maya.source.SourceDescriptor;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationBuilderImpl 
		implements SpecificationBuilder, CONST_IMPL {

    private XMLReaderPool _xmlReaderPool;

    public XMLReaderPool getXmlReaderPool() {
        return _xmlReaderPool;
    }
    
    public SpecificationBuilderImpl() {
        _xmlReaderPool = new XMLReaderPool();
    }

    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    protected XMLReader createXMLReader() {
        XMLReader xmlReader = new SAXParser();
        try {
            xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException(e);
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return xmlReader;
    }

    protected ContentHandler createContentHandler(Specification specification) {
        return new SpecificationNodeHandler(specification);
    }

    protected String getPublicID() {
        return URI_MAYA + "/specification";
    }
    
    private void setContentHander(XMLReader xmlReader, ContentHandler handler) {
        xmlReader.setContentHandler(handler);
        if(handler instanceof EntityResolver) {
            xmlReader.setEntityResolver((EntityResolver)handler);
        }
        if(handler instanceof ErrorHandler) {
            xmlReader.setErrorHandler((ErrorHandler)handler);
        }
        if(handler instanceof DTDHandler) {
            xmlReader.setDTDHandler((DTDHandler)handler);
        }
        if(handler instanceof LexicalHandler) {
            try {
	            xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
            } catch(SAXException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public void build(Specification specification) {
        if(specification == null) {
            throw new IllegalArgumentException();
        }
        SourceDescriptor source = specification.getSource();
        if(source.exists()) {
            ContentHandler handler = createContentHandler(specification);
            XMLReader xmlReader = _xmlReaderPool.borrowXMLReader();
            setContentHander(xmlReader, handler);
            InputSource input = new InputSource(source.getInputStream());
            input.setPublicId(getPublicID());
            input.setSystemId(source.toString());
            try {
                xmlReader.parse(input);
            } catch(Throwable t) {
				specification.kill();
				if(t instanceof RuntimeException) {
				    throw (RuntimeException)t;
				}
				throw new RuntimeException(t);
            } finally {
                _xmlReaderPool.returnXMLReader(xmlReader);
            }
        }
    }

    private class XMLReaderPool extends AbstractSoftReferencePool implements Serializable {

        protected Object createObject() {
            return createXMLReader();
        }
        
        protected boolean validateObject(Object object) {
            return object instanceof XMLReader;
        }
    
    	XMLReader borrowXMLReader() {
    	    return (XMLReader)borrowObject();
    	}
    	
    	void returnXMLReader(XMLReader xmlReader) {
    	    returnObject(xmlReader);
    	}
    	
    }
}
