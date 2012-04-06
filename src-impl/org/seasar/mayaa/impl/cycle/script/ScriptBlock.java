/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.cycle.script;

import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptBlock {

    private String _blockString;
    private boolean _literal;
    private String _blockSign;

    public ScriptBlock(String blockString, boolean literal, String blockSign) {
        if (blockString == null || StringUtil.isEmpty(blockSign)) {
            throw new IllegalArgumentException();
        }
        _blockString = blockString;
        _literal = literal;
        _blockSign = blockSign;
    }

    public String getBlockString() {
        return _blockString;
    }

    public boolean isLiteral() {
        return _literal;
    }

    public String getBlockSign() {
        return _blockSign;
    }

}
