/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.util.xml;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.seasar.mayaa.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.mayaa.impl.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class XMLHandler extends DefaultHandler {

    private TagHandlerStack _stack;

    private Log _log;

    private Locator _locator;

    private Map _entities;

    private Class _neighborClass;

    protected void setRootHandler(TagHandler rootHandler) {
        if (rootHandler == null) {
            throw new IllegalArgumentException();
        }
        _stack = new TagHandlerStack(rootHandler);
    }

    protected void setLog(Log log) {
        _log = log;
    }

    protected Log getLog() {
        return _log;
    }

    protected void setNeighborClass(Class neighborClass) {
        if (neighborClass == null) {
            throw new IllegalArgumentException();
        }
        _neighborClass = neighborClass;
    }

    protected Class getNeighborClass() {
        return _neighborClass;
    }

    protected Map getEntityMap() {
        if (_entities == null) {
            _entities = new HashMap();
        }
        return _entities;
    }

    protected String getSystemID() {
        if (_locator != null) {
            return _locator.getSystemId();
        }
        return null;
    }

    protected int getLineNumber() {
        if (_locator != null) {
            return _locator.getLineNumber();
        }
        return 0;
    }

    public void setDocumentLocator(Locator locator) {
        _locator = locator;
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        String path = systemId;
        Map entities = getEntityMap();
        if (entities != null && entities.containsKey(publicId)) {
            path = (String) entities.get(publicId);
        } else if (entities != null && entities.containsKey(systemId)) {
            path = (String) entities.get(systemId);
        } else {
            int pos = systemId.lastIndexOf('/');
            if (pos != -1) {
                path = systemId.substring(pos);
            }
        }
        Class neighborClass = getNeighborClass();
        if (neighborClass == null) {
            neighborClass = getClass();
        }
        ClassLoaderSourceDescriptor source = new ClassLoaderSourceDescriptor();
        source.setSystemID(path);
        source.setNeighborClass(neighborClass);
        if (source.exists()) {
            InputSource ret = new InputSource(source.getInputStream());
            ret.setPublicId(publicId);
            ret.setSystemId(path);
            return ret;
        }
        Log log = getLog();
        if (log != null && log.isWarnEnabled()) {
            String message = StringUtil.getMessage(
                    XMLHandler.class, 0, publicId, systemId);
            log.warn(message);
        }
        return null;
    }

    public void startDocument() {
        if (_stack == null) {
            throw new IllegalStateException();
        }
    }

    public void startElement(String namespaceURI, String localName, String qName,
            Attributes attributes) {
        _stack.startElement(localName, attributes, getSystemID(), getLineNumber());
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        _stack.endElement();
    }

    public void characters(char[] ch, int start, int length) {
        _stack.characters(ch, start, length);
    }

    public void warning(SAXParseException e) {
        Log log = getLog();
        if (log != null && log.isWarnEnabled()) {
            log.warn(e.getMessage(), e);
        }
    }

    public void error(SAXParseException e) {
        Log log = getLog();
        if (log != null && log.isErrorEnabled()) {
            log.error(e.getMessage(), e);
        }
        throw new RuntimeException(e);
    }

    public void fatalError(SAXParseException e) {
        Log log = getLog();
        if (log != null && log.isFatalEnabled()) {
            log.fatal(e.getMessage(), e);
        }
        throw new RuntimeException(e);
    }

}
