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
import jakarta.servlet.jsp.tagext.DynamicAttributes;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 * DynamicAttributeをimplementsしたSimpleTagのテストのためのクラス。
 *
 * @author Koji Suga (Gluegent, Inc.)
 */
public class SimpleDynamicAttributeTestTag
        extends SimpleTagSupport
        implements DynamicAttributes {

    private String dynamicName = null;
    private Object dynamicValue = null;

    public String getDynamicName() {
        return dynamicName;
    }

    public void setDynamicName(String dynamicName) {
        this.dynamicName = dynamicName;
    }

    public Object getDynamicValue() {
        return dynamicValue;
    }

    public void setDynamicValue(Object dynamicValue) {
        this.dynamicValue = dynamicValue;
    }

    /**
     * dynamicName + "=" + dynamicValue を String として取り、出力。
     *
     * @see jakarta.servlet.jsp.tagext.SimpleTag#doTag()
     */
    public void doTag() throws JspException, IOException {
        getJspContext().getOut().print(dynamicName);
        getJspContext().getOut().print("=");
        getJspContext().getOut().print(dynamicValue);
    }

    public void setDynamicAttribute(String uri, String localName, Object value)
            throws JspException {
        setDynamicName(localName);
        setDynamicValue(value);
    }

}
