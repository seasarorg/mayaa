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
package org.seasar.mayaa.impl.builder.library.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class J2EEEntities implements CONST_J2EE {

    private static Map _entityMap;
    static {
        _entityMap = new HashMap();
        _entityMap.put(PUBLIC_WEB_DTD_22, FILE_WEB_DTD_22);
        _entityMap.put(SYSTEM_WEB_DTD_22, FILE_WEB_DTD_22);
        _entityMap.put(PUBLIC_WEB_DTD_23, FILE_WEB_DTD_23);
        _entityMap.put(SYSTEM_WEB_DTD_23, FILE_WEB_DTD_23);
        _entityMap.put(LOCATION_WEB_DTD_24, FILE_WEB_DTD_24);
        _entityMap.put(PUBLIC_JSP_TAGLIB_11, FILE_JSP_TAGLIB_11);
        _entityMap.put(SYSTEM_JSP_TAGLIB_11, FILE_JSP_TAGLIB_11);
        _entityMap.put(PUBLIC_JSP_TAGLIB_12, FILE_JSP_TAGLIB_12);
        _entityMap.put(SYSTEM_JSP_TAGLIB_12, FILE_JSP_TAGLIB_12);
        _entityMap.put(LOCATION_JSP_TAGLIB_20, FILE_JSP_TAGLIB_20);
        _entityMap.put(PUBLIC_DATATYPES, FILE_DATATYPES);
        _entityMap.put(SYSTEM_DATATYPES, FILE_DATATYPES);
        _entityMap.put(PUBLIC_XML_SCHEMA, FILE_XML_SCHEMA);
        _entityMap.put(SYSTEM_XML_SCHEMA, FILE_XML_SCHEMA);
        _entityMap.put(LOCATION_XML, FILE_XML);
        _entityMap.put(LOCATION_WEB_SERVICE, FILE_WEB_SERVICE);
        _entityMap.put(LOCATION_WEB_SERVICE_CLIENT_11,
                FILE_WEB_SERVICE_CLIENT_11);
        _entityMap.put(LOCATION_J2EE_14, FILE_J2EE_14);

        _entityMap.put(LOCATION_J2EE_5, FILE_J2EE_5);
        _entityMap.put(LOCATION_WEB_DTD_25, FILE_WEB_DTD_25);
        _entityMap.put(LOCATION_JSP_TAGLIB_21, FILE_JSP_TAGLIB_21);
        _entityMap.put(LOCATION_WEB_SERVICE_CLIENT_12,
                FILE_WEB_SERVICE_CLIENT_12);
        
        _entityMap.put(LOCATION_J2EE_6, FILE_J2EE_6);
        _entityMap.put(LOCATION_WEB_COMMON_30, FILE_WEB_COMMON_30);
        _entityMap.put(LOCATION_WEB_SERVICE_13, FILE_WEB_SERVICE_13);
        _entityMap.put(LOCATION_WEB_SERVICE_CLIENT_13, FILE_WEB_SERVICE_CLIENT_13);
        _entityMap.put(LOCATION_JSP_22, FILE_JSP_22);
        _entityMap.put(LOCATION_WEB_DTD_30, FILE_WEB_DTD_30);
    }

    public static Map getEntityMap() {
        return _entityMap;
    }

}
