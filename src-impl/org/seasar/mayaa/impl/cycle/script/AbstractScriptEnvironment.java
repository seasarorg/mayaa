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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractScriptEnvironment
        extends ParameterAwareImpl implements ScriptEnvironment {

    private static final long serialVersionUID = -1647884867508562923L;

    private transient List _attributeScopes;
    private String _blockSign = "$";

    public synchronized void addAttributeScope(AttributeScope attrs) {
        if (attrs == null) {
            throw new IllegalArgumentException();
        }
        if (_attributeScopes == null) {
            _attributeScopes = new ArrayList();
        }
        _attributeScopes.add(attrs);
    }

    public Iterator iterateAttributeScope() {
        if (_attributeScopes == null) {
            return NullIterator.getInstance();
        }
        return _attributeScopes.iterator();
    }

    public void setBlockSign(String blockSign) {
        if (StringUtil.isEmpty(blockSign)) {
            throw new IllegalArgumentException();
        }
        _blockSign = blockSign;
    }

    public String getBlockSign() {
        return _blockSign;
    }

    protected abstract CompiledScript compile(
            ScriptBlock scriptBlock, PositionAware position, int offsetLine);

    public CompiledScript compile(String script, PositionAware position) {
        if (StringUtil.isEmpty(script)) {
            return LiteralScript.NULL_LITERAL_SCRIPT;
        }
        int offsetLine = 0;
        List list = new ArrayList();
        for (Iterator it = new ScriptBlockIterator(
                script, _blockSign, position.isOnTemplate()); it.hasNext();) {
            ScriptBlock block = (ScriptBlock) it.next();
            list.add(compile(block, position, offsetLine));
            offsetLine += lineCount(block.getBlockString());
        }
        if (list.size() == 1) {
            return (CompiledScript) list.get(0);
        }
        CompiledScript[] compiled =
            (CompiledScript[]) list.toArray(new CompiledScript[list.size()]);
        return new ComplexScript(compiled);
    }

    private int lineCount(String str) {
        String[] lines = str.split(System.getProperty("line.separator"));
        return lines.length;
    }

}
