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
package org.seasar.mayaa.impl.builder.library.scanner;

import java.util.Date;

import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SourceAlias {

    public static final String ALIAS = SourceAlias.class + ".ALIAS";

    private String _alias;
    private String _systemID;
    private Date _timestamp;

    public SourceAlias(String alias, String systemID, Date timestamp) {
        if (StringUtil.isEmpty(alias) || StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        _alias = alias;
        _systemID = systemID;
        _timestamp = timestamp;
    }

    public String getAlias() {
        return _alias;
    }

    public String getSystemID() {
        return _systemID;
    }

    public Date getTimestamp() {
        return _timestamp;
    }

}
