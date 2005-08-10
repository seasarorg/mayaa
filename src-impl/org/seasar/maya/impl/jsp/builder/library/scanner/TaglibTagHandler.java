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
package org.seasar.maya.impl.jsp.builder.library.scanner;

import org.seasar.maya.impl.jsp.builder.library.JspLibraryDefinition;
import org.seasar.maya.impl.jsp.builder.library.JspProcessorDefinition;
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
        putHandler("tag", new TagTagHandler(this));
        putHandler("jsp-version", new JspVersionSetter());
        putHandler("uri", new TagHandler() {
            protected void end(String body) {
                _library.setNamespaceURI(body);
            }
        });
        // JSP1.1
        putHandler("jspversion", new JspVersionSetter());
    }
    
    protected void start(Attributes attributes) {
        _library = new JspLibraryDefinition();
    }

    public void addProcessorDefinition(JspProcessorDefinition processor) {
        _library.addProcessorDefinition(processor);
        processor.setLibraryDefinition(_library);
    }
    
    public JspLibraryDefinition getLibraryDefinition() {
        return _library;
    }

    private class JspVersionSetter extends TagHandler {

        protected void end(String body) {
            _library.setRequiredVersion(body);
        }
        
    }
    
}
