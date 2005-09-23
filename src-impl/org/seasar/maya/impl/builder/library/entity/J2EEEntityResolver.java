/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.builder.library.entity;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;
import org.xml.sax.InputSource;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class J2EEEntityResolver implements CONST_J2EE {

    private static final Log LOG = LogFactory.getLog(J2EEEntityResolver.class);
    
    private static Map _entities;
    static {
        _entities = new HashMap();
        _entities.put(PUBLIC_WEB_DTD_22, FILE_WEB_DTD_22);
        _entities.put(SYSTEM_WEB_DTD_22, FILE_WEB_DTD_22);
        _entities.put(PUBLIC_WEB_DTD_23, FILE_WEB_DTD_23);
        _entities.put(SYSTEM_WEB_DTD_23, FILE_WEB_DTD_23);
        _entities.put(LOCATION_WEB_DTD_24, FILE_WEB_DTD_24);
        _entities.put(PUBLIC_JSP_TAGLIB_11, FILE_JSP_TAGLIB_11);
        _entities.put(SYSTEM_JSP_TAGLIB_11, FILE_JSP_TAGLIB_11);
        _entities.put(PUBLIC_JSP_TAGLIB_12, FILE_JSP_TAGLIB_12);
        _entities.put(SYSTEM_JSP_TAGLIB_12, FILE_JSP_TAGLIB_12);
        _entities.put(LOCATION_JSP_TAGLIB_20, FILE_JSP_TAGLIB_20);
        _entities.put(PUBLIC_DATATYPES, FILE_DATATYPES);
        _entities.put(SYSTEM_DATATYPES, FILE_DATATYPES);
        _entities.put(PUBLIC_XML_SCHEMA, FILE_XML_SCHEMA);
        _entities.put(SYSTEM_XML_SCHEMA, FILE_XML_SCHEMA);
        _entities.put(LOCATION_XML, FILE_XML);
        _entities.put(LOCATION_WEB_SERVICE, FILE_WEB_SERVICE);
        _entities.put(LOCATION_WEB_SERVICE_CLIENT, 
                FILE_WEB_SERVICE_CLIENT);
        _entities.put(LOCATION_J2EE_14, FILE_J2EE_14);
    }
    
    public static InputSource resolveEntity(
            String publicId, String systemId) {
        String path = systemId;
        if(_entities.containsKey(publicId)) {
            path = (String)_entities.get(publicId);
        } else if(_entities.containsKey(systemId)) {
            path = (String)_entities.get(systemId);
        } else {
            int pos = systemId.lastIndexOf('/');
            if(pos != -1) {
                path = systemId.substring(pos);
            }
        }
        ClassLoaderSourceDescriptor source = 
            new ClassLoaderSourceDescriptor();
        source.setSystemID(path);
        source.setNeighborClass(J2EEEntityResolver.class);
        if(source.exists()) {
            InputSource ret = new InputSource(source.getInputStream());
            ret.setPublicId(publicId);
            ret.setSystemId(path);
            return ret;
        }
        if (LOG.isWarnEnabled()) {
            LOG.warn("Entity not resolved locally, publicId=" + publicId + 
                    ", systemId=" + systemId);
        }
        return null;
    }

}
