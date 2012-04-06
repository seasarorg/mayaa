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

import org.seasar.mayaa.impl.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TagHandler {

    protected static final AttributesImpl NULL_ATTR =
        new AttributesImpl();
    private static final TagHandler NULL_HANDLER = new TagHandler("_null_");

    private Map _children = new HashMap();
    private boolean _valid = true;
    private StringBuffer _buffer = new StringBuffer();
    private String _name;

    public TagHandler(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        _name = name;
    }

    public String getName() {
        return _name;
    }

    protected void putHandler(TagHandler child) {
        if (child == null) {
            throw new IllegalArgumentException();
        }
        _children.put(child.getName(), child);
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        // override this method.
    }

    protected void end(String body) {
        // override this method.
    }

    public void invalidate() {
        _valid = false;
    }

    public boolean isValid() {
        return _valid;
    }

    // HandlerStackより呼び出される。
    public TagHandler startElement(String name, Attributes attributes,
            String systemID, int lineNumber) {
        if (_valid) {
            TagHandler child = (TagHandler) _children.get(name);
            if (child != null) {
                child._valid = true;
                child._buffer.setLength(0);
                child.start(attributes, systemID, lineNumber);
                return child;
            }
        }
        return NULL_HANDLER;
    }

    // HandlerStackより呼び出される。
    public void endElement() {
        end(_buffer.toString().trim());
    }

    // HandlerStackより呼び出される。
    public void characters(String body) {
        _buffer.append(body);
    }

}
