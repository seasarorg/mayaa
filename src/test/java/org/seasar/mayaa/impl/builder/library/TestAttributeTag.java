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
package org.seasar.mayaa.impl.builder.library;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * 属性セットテスト用のカスタムタグ.
 * @author suga
 */
public class TestAttributeTag extends TagSupport {

    private static final long serialVersionUID = 5869391007151195282L;

    /** attribute テスト用属性 */
    private String _name;
    private int _value;

    public String getName() {
        return _name;
    }
    public void setName(String name) {
        _name = name;
    }

    public int getValue() {
        return _value;
    }
    public void setValue(int value) {
        _value = value;
    }

    @Override
    public int doStartTag() {
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doAfterBody() throws JspException {
        return super.doAfterBody();
    }

    @Override
    public int doEndTag() {
        return EVAL_PAGE;
    }

    @Override
    public void release() {
        super.release();
    }
}
