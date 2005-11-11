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
package org.seasar.maya.impl.builder.library;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TLDScriptingVariableInfo {

    private TagExtraInfo _tei;
    private TagData _tagData;
    private boolean _hasNestedVariable;
    private boolean _hasDynamicName;
    private VariableInfo[] _variableInfos;

    public boolean hasNestedVariable() {
        return _hasNestedVariable;
    }

    public boolean isNestedVariable(String name) {
        if (_hasNestedVariable == false) {
            return false;
        }

        VariableInfo[] variableInfos;
        if (_hasDynamicName) {
            variableInfos = _tei.getVariableInfo(_tagData);
        } else {
            variableInfos = _variableInfos;
        }

        for (int i = 0; i < variableInfos.length; i++) {
            if (name.equals(variableInfos[i].getVarName())) {
                return variableInfos[i].getScope() == VariableInfo.NESTED;
            }
        }
        return false;
    }

    public void setTagExtraInfo(TagExtraInfo tei) {
        _tei = tei;
    }

    public void setNestedVariable(boolean hasNestedVariable) {
        _hasNestedVariable = hasNestedVariable;
    }

    public void setDynamicName(boolean hasDynamicName) {
        _hasDynamicName = hasDynamicName;
    }

    public void setTagData(TagData tagData) {
        _tagData = tagData;
    }

    public void setVariableInfos(VariableInfo[] variableInfos) {
        _variableInfos = variableInfos;
    }

}
