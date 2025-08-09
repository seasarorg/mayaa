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

import jakarta.servlet.jsp.tagext.TagData;
import jakarta.servlet.jsp.tagext.TagExtraInfo;
import jakarta.servlet.jsp.tagext.VariableInfo;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class ScopeTestTagExtraInfo extends TagExtraInfo {

    public VariableInfo[] getVariableInfo(TagData data) {
        VariableInfo atbegin = new VariableInfo(
                data.getAttributeString("atbegin"),
                "java.lang.Object",
                true,
                VariableInfo.AT_BEGIN);
        VariableInfo nested = new VariableInfo(
                data.getAttributeString("nested"),
                "java.lang.Object",
                true,
                VariableInfo.NESTED);
        VariableInfo atend = new VariableInfo(
                data.getAttributeString("atend"),
                "java.lang.Object",
                true,
                VariableInfo.AT_END);

        return new VariableInfo[] {
                    atbegin,
                    nested,
                    atend
                };
    }

}
