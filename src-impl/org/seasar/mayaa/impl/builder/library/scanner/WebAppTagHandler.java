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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebAppTagHandler extends TagHandler {

    private List _locations = new ArrayList();

    public WebAppTagHandler() {
        super("web-app");
        final TagHandler taglibHandler = new TagHandler("taglib") {
            private String _taglibURI;
            private String _taglibLocation;
            private int _lineNumber;

            protected void setTaglibURI(String taglibURI) {
                _taglibURI = taglibURI;
            }

            protected void setTaglibLocation(String taglibLocation) {
                _taglibLocation = taglibLocation;
            }

            // initialize
            {
                putHandler(new TagHandler("taglib-uri") {
                    protected void end(String body) {
                        setTaglibURI(body);
                    }
                });
                putHandler(new TagHandler("taglib-location") {
                    protected void end(String body) {
                        setTaglibLocation(body);
                    }
                });
            }

            protected void start(
                    Attributes attributes, String systemID, int lineNumber) {
                _taglibURI = null;
                _taglibLocation = null;
                _lineNumber = lineNumber;
            }

            protected void end(String body) {
                if (StringUtil.isEmpty(_taglibURI)
                        || StringUtil.isEmpty(_taglibLocation)) {
                    throw new IllegalTaglibDefinitionException(
                            "/WEB-INF/web.xml", _lineNumber);
                }
                addLocation(new SourceAlias(_taglibURI, _taglibLocation, null));
            }
        };

        // servlet2.3
        putHandler(taglibHandler);

        // servlet2.4
        putHandler(new TagHandler("jsp-config") {
            // initialize
            {
                putHandler(taglibHandler);
            }
        });
    }

    protected void addLocation(SourceAlias location) {
        _locations.add(location);
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        _locations.clear();
    }

    public Iterator iterateTaglibLocation() {
        return _locations.iterator();
    }

}
