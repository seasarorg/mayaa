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
package org.seasar.maya.impl.builder.library.scanner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebAppTagHandler extends TagHandler {

    private List _locations = new ArrayList();
    private Locator _locator;
    
    public WebAppTagHandler() {
        super("web-app");
        putHandler(new TagHandler("taglib") {
            private String _taglibURI;
            private String _taglibLocation;
            
            // initialize
            {
                putHandler(new TagHandler("taglib-uri") {
                    protected void end(String body) {
                        _taglibURI = body;
                    }
                });
                putHandler(new TagHandler("taglib-location") {
                    protected void end(String body) {
                        _taglibLocation = body;
                    }
                });
            }
            
            protected void start(Attributes attributes) {
                _taglibURI = null;
                _taglibLocation = null;
            }
            
            protected void end(String body) {
                if(StringUtil.isEmpty(_taglibURI) || 
                        StringUtil.isEmpty(_taglibLocation)) {
                    String systemID = "/WEB-INF/web.xml";
                    int lineNumber = -1;
                    if(_locator != null) {
                        systemID = _locator.getSystemId();
                        lineNumber = _locator.getLineNumber();
                    }
                    throw new IllegalTaglibDefinitionException(
                            systemID, lineNumber);
                }
                _locations.add(
                        new SourceAlias(_taglibURI, _taglibLocation, null));
            }
        });
    }
    
    public void setLocator(Locator locator) {
        _locator = locator;
    }
    
    protected void start(Attributes attributes) {
        _locations.clear();
    }    

    public Iterator iterateTaglibLocation() {
        return _locations.iterator();
    }
    
}
