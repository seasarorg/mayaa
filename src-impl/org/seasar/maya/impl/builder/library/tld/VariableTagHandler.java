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

import org.seasar.maya.impl.builder.library.TagVariableInfoImpl;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * tldのvariableタグ用ハンドラ.
 * @author suga
 */
public class VariableTagHandler extends TagHandler {

    private TagVariableInfoImpl _variable;
    private TagTagHandler _parent;

    public VariableTagHandler(TagTagHandler parent) {
        super("variable");
        _parent = parent;
        putHandler(new TagHandler("name-given") {
            public void end(String body) {
                _variable.setNameGiven(body);
            }
        });
        putHandler(new TagHandler("name-from-attribute") {
            public void end(String body) {
                _variable.setNameFromAttribute(body);
            }
        });
        putHandler(new TagHandler("variable-class") {
            public void end(String body) {
                _variable.setClassName(body);
            }
        });
        putHandler(new TagHandler("declare") {
            public void end(String body) {
                _variable.setDeclare(ObjectUtil.booleanValue(body, false));
            }
        });
        putHandler(new TagHandler("scope") {
            public void end(String body) {
                _variable.setScopeType(body);
            }
        });
    }

    protected void start(Attributes attributes) {
        _variable = new TagVariableInfoImpl();
    }

    protected void end(String body) {
        _parent.getProcessorDefinition().addTagVariableInfo(_variable);
    }

}
