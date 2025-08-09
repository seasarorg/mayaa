/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.test.tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.JspTag;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import jakarta.servlet.jsp.tagext.Tag;

import org.seasar.mayaa.impl.util.StringUtil;

/**
 * SimpleTagのテスト用。
 * @author Koji Suga (Gluegent, Inc.)
 */
public class SimpleTestTag extends SimpleTagSupport {

    private String _simpleName;
    private String _styleClass;

    public void setSimpleName(String simpleName) {
        _simpleName = simpleName;
    }

    public String getSimpleName() {
        return _simpleName;
    }

    public void setStyleClass(String styleClass) {
        _styleClass = styleClass;
    }

    public String getStyleClass() {
        return _styleClass;
    }

    /**
     * getAttribute("SimpleTestTag") を String として取り、出力。
     * その後 simpleName, styleClass の値を出力。
     * 親を順に辿って{@link org.apache.taglibs.standard.tag.rt.core.IfTag}を探し、見つけたらクラス名を出力する。
     * 見つからなければ直上の親のクラス名を出力する。
     *
     * @see jakarta.servlet.jsp.tagext.SimpleTag#doTag()
     */
    public void doTag() throws JspException, IOException {
        String echo = (String) getJspContext().getAttribute("SimpleTestTag");
        JspWriter writer = getJspContext().getOut();
        writer.print(echo);
        writer.print(_simpleName);
        writer.print(StringUtil.escapeXml(_styleClass));

        JspTag parent = getParent();
        while (parent != null) {
            if (parent instanceof org.apache.taglibs.standard.tag.rt.core.IfTag) {
                writer.print(" parent:");
                writer.print(parent.getClass().getName());
                break;
            }
            if (parent instanceof Tag) {
                parent = ((Tag) parent).getParent();
            } else {
                parent = null;
            }
        }
    }

}
