/*
 * Copyright 2004-2022 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.source;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Mitsutaka Watanabe
 */
public class InMemorySourceDescriptor implements ChangeableRootSourceDescriptor {

    private static final Date NOTFOUND_TIMESTAMP = new Date(0);

    private DynamicRegisteredSourceHolder sourceHolder;

    private String _systemID = "";

    public InMemorySourceDescriptor(DynamicRegisteredSourceHolder sourceHolder) {
        this.sourceHolder = sourceHolder;
    }

    public void setRoot(String root) {
    }

    public String getRoot() {
        return "";
    }

    public void setSystemID(String systemID) {
        _systemID = StringUtil.preparePath(systemID);
    }

    @Override
    public String getSystemID() {
        return _systemID;
    }

    public boolean exists() {
        String content = sourceHolder.getContents(_systemID);
        return content != null && !content.isEmpty();
    }

    public InputStream getInputStream() {
        String content = sourceHolder.getContents(_systemID);
        if (content != null && !content.isEmpty()) {
            try {
                return new ByteArrayInputStream(content.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
        return null;
    }

    public Date getTimestamp() {
        Date timestamp = sourceHolder.getTimestamp(_systemID);
        if (timestamp != null) {
            return timestamp;
        }
        return NOTFOUND_TIMESTAMP;
    }
}
