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
package org.seasar.maya.impl.builder.library.scanner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebAppTagHandler extends TagHandler {

    private List _locations = new ArrayList();
    
    public WebAppTagHandler() {
        putHandler("taglib", new TagHandler() {
            private String _taglibURI;
            private String _taglibLocation;
            
            // initialize
            {
                putHandler("taglib-uri", new TagHandler() {
                    protected void end(String body) {
                        _taglibURI = body;
                    }
                });
                putHandler("taglib-location", new TagHandler() {
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
                if(StringUtil.isEmpty(_taglibURI) || StringUtil.isEmpty(_taglibLocation)) {
                    // TODO WEB.XMLの記述ミスの例外。
                    throw new IllegalStateException();
                }
                _locations.add(new SourceAlias(_taglibURI, _taglibLocation));
            }
        });
    }
    
    protected void start(Attributes attributes) {
        _locations.clear();
    }    

    public Iterator iterateTaglibLocation() {
        return _locations.iterator();
    }
    
}
