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
package org.seasar.maya.impl.builder.library.tld;

import org.seasar.maya.impl.builder.library.JspLibraryDefinition;
import org.seasar.maya.impl.builder.library.JspProcessorDefinition;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * tldのtaglibタグ用ハンドラ.
 * @author suga
 */
public class TaglibTagHandler extends TagHandler {

    private JspLibraryDefinition _library;

    /**
     * クラスローダーを受け取るコンストラクタ。
     * @param loader Tagクラスなどを取得するクラスローダー
     */
    public TaglibTagHandler() {
        super("taglib");
        putHandler(new TagTagHandler(this));
        putHandler(new JspVersionSetter("jsp-version"));
        putHandler(new TagHandler("uri") {
            protected void end(String body) {
                _library.setNamespaceURI(body);
            }
        });
        // JSP1.1
        putHandler(new JspVersionSetter("jspversion"));
    }
    
    protected void start(Attributes attributes) {
        _library = new JspLibraryDefinition();
    }

    public void addProcessorDefinition(JspProcessorDefinition processor) {
        _library.addProcessorDefinition(processor);
    }
    
    public JspLibraryDefinition getLibraryDefinition() {
        return _library;
    }

    private class JspVersionSetter extends TagHandler {

        private JspVersionSetter(String name) {
            super(name);
        }
        
        protected void end(String body) {
            _library.setRequiredVersion(body);
        }
        
    }
    
}
