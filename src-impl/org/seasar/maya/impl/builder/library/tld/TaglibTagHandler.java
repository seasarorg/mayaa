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
        putHandler(new TagHandler("jsp-version") {
            protected void end(String body) {
                _library.setRequiredVersion(body);
            }
        });
        putHandler(new TagHandler("jspversion") {
            protected void end(String body) {
                _library.setRequiredVersion(body);
            }
        });
        putHandler(new TagHandler("uri") {
            protected void end(String body) {
                _library.addNamespaceURI(body);
            }
        });
        putHandler(new TagHandler("short-name") {
            protected void end(String body) {
                _library.addNamespaceURI(body);
            }
        });
        putHandler(new TagHandler("shortname") {
            protected void end(String body) {
                _library.addNamespaceURI(body);
            }
        });
    }
    
    protected void start(Attributes attributes) {
        _library = new JspLibraryDefinition();
    }

    public JspLibraryDefinition getLibraryDefinition() {
        return _library;
    }
    
}
