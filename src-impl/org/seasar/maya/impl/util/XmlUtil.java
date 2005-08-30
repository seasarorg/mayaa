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
package org.seasar.maya.impl.util;

import java.io.InputStream;

import org.seasar.maya.impl.util.xml.XmlReaderPool;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public final class XmlUtil {
    
    private XmlUtil() {
    }

    public static void parse(DefaultHandler handler, 
            InputStream stream, String publicID, String systemID, 
            boolean namespace, boolean validation, boolean xmlSchema) {
        if(stream == null) {
            throw new IllegalArgumentException();
        }
        XmlReaderPool pool = XmlReaderPool.getPool();
        XMLReader xmlReader = 
            pool.borrowXMLReader(handler, namespace, validation, xmlSchema);
        InputSource input = new InputSource(stream);
        input.setPublicId(publicID);
        input.setSystemId(systemID);
        try {
            xmlReader.parse(input);
        } catch(Throwable t) {
			if(t instanceof RuntimeException) {
			    throw (RuntimeException)t;
			}
			throw new RuntimeException(t);
        } finally {
            pool.returnXMLReader(xmlReader);
        }
    }
    
    public static String getStringValue(Attributes attr, String localName, String defaultValue) {
        String value = attr.getValue(localName);
        if(StringUtil.hasValue(value)) {
            return value;
        }
        return defaultValue;
    }
    
    public static int getIntValue(Attributes attr, String localName, int defaultValue) {
        String value = attr.getValue(localName);
        if(StringUtil.hasValue(value)) {
            return Integer.parseInt(value);
        }
        return defaultValue;
    }
    
	public static boolean getBooleanValue(Attributes attr, 
	        String localName, boolean defaultValue) {
	    String value = attr.getValue(localName);
        return ObjectUtil.booleanValue(value, defaultValue);
	}

	public static Class getClassValue(Attributes attr, 
            String localName, Class defaultValue) {
        String className = attr.getValue(localName);
        if(StringUtil.hasValue(className)) {
            return ObjectUtil.loadClass(className);
        }
        return defaultValue;
	}
	
	public static Object getObjectValue(Attributes attr, 
	        String localName, Class defaultValue, Class expectedType) {
        Class clazz = getClassValue(attr, localName, defaultValue);
        if(clazz == null) {
            throw new NoTypeValueException(localName);
        }
        if(expectedType.isAssignableFrom(clazz)) {
            return ObjectUtil.newInstance(clazz);
        }
        throw new IllegalTypeException(expectedType, clazz);
	}

}
