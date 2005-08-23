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
package org.seasar.maya.impl.builder.library;

import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * @author suga
 */
public class TagVariableInfoImpl extends TagVariableInfo {

    private String _nameGiven;
    private String _nameFromAttribute;
    private String _className = "java.lang.String";
    private boolean _declare = true;
    private int _scope = VariableInfo.NESTED;

    public TagVariableInfoImpl() {
        super(null, null, null, false, 0);
    }

    public String getClassName() {
        return _className;
    }

    public void setClassName(String className) {
        _className = className;
    }

    public boolean getDeclare() {
        return _declare;
    }

    public void setDeclare(boolean declare) {
        _declare = declare;
    }

    public String getNameFromAttribute() {
        return _nameFromAttribute;
    }

    public void setNameFromAttribute(String nameFromAttribute) {
        _nameFromAttribute = nameFromAttribute;
    }

    public String getNameGiven() {
        return _nameGiven;
    }

    public void setNameGiven(String nameGiven) {
        _nameGiven = nameGiven;
    }

    public int getScope() {
        return _scope;
    }

    public void setScopeType(String scope) {
        if("AT_BEGIN".equalsIgnoreCase(scope)) {
            _scope =  VariableInfo.AT_BEGIN;
        } else if("AT_END".equalsIgnoreCase(scope)) {
            _scope =  VariableInfo.AT_END;
        } if("NESTED".equalsIgnoreCase(scope)) {
            _scope =  VariableInfo.NESTED;
        } else {
            throw new IllegalArgumentException();
        }
    }

}