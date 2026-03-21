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
package org.seasar.mayaa.impl.cycle.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Undefined;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.serialize.NodeReferenceResolver;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.scope.ParamScope;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationNodeImpl;
import org.seasar.mayaa.impl.cycle.script.rhino.ScriptEnvironmentImpl;
import org.seasar.mayaa.impl.cycle.script.rhino.TextCompiledScriptImpl;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Koji Suga (Gluegent Inc.)
 */
public class ScriptUtilTest {

    private NodeTreeWalker _node;

    @BeforeEach
    public void setUp() {
        ManualProviderFactory.setUp(this);

        ScriptEnvironmentImpl scriptEnvironment = ManualProviderFactory.SCRIPT_ENVIRONMENT;
        scriptEnvironment.setBlockSign("$");
        scriptEnvironment.addAttributeScope(new ParamScope());
        scriptEnvironment.initScope();
        scriptEnvironment.startScope(null);

        _node = new NodeTreeWalker() {
            private static final long serialVersionUID = 1L;

            public void setSystemID(String systemID) {
                // no-op
            }
            public String getSystemID() {
                return null;
            }
            public void setLineNumber(int lineNumber) {
                // no-op
            }
            public int getLineNumber() {
                return 0;
            }
            public void setOnTemplate(boolean onTemplate) {
                // no-op
            }
            public boolean isOnTemplate() {
                return false;
            }
            public void addChildNode(NodeTreeWalker childNode) {
                throw new UnsupportedOperationException();
            }
            public void clearChildNodes() {
                throw new UnsupportedOperationException();
            }
            public NodeTreeWalker getChildNode(int index) {
                throw new UnsupportedOperationException();
            }
            public int getChildNodeSize() {
                throw new UnsupportedOperationException();
            }
            public NodeTreeWalker getParentNode() {
                throw new UnsupportedOperationException();
            }
            public void insertChildNode(int index, NodeTreeWalker childNode) {
                throw new UnsupportedOperationException();
            }
            public Iterator<NodeTreeWalker> iterateChildNode() {
                throw new UnsupportedOperationException();
            }
            public boolean removeChildNode(NodeTreeWalker childNode) {
                throw new UnsupportedOperationException();
            }
            public void setParentNode(NodeTreeWalker parentNode) {
                throw new UnsupportedOperationException();

            }
            public NodeReferenceResolver findNodeResolver() {
                throw new UnsupportedOperationException();
            }
        };

        CycleUtil.getServiceCycle().setInjectedNode(_node);
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.cycle.script.ScriptUtil#getBlockSignedText(java.lang.String)}.
     */
    @Test
    public void testGetBlockSignedText() {
        assertEquals("${test\n}", ScriptUtil.getBlockSignedText("test"));
        assertEquals("", ScriptUtil.getBlockSignedText(""));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.cycle.script.ScriptUtil#isEmpty(java.lang.Object)}.
     */
    @Test
    public void testIsEmpty() {
        assertTrue(ScriptUtil.isEmpty(null));
        assertTrue(ScriptUtil.isEmpty(Undefined.instance));
        assertFalse(ScriptUtil.isEmpty(""));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.cycle.script.ScriptUtil#compile(java.lang.String, java.lang.Class)}.
     */
    @Test
    public void testCompile() {
        CompiledScript script = ScriptUtil.compile("${'test'}");
        assertEquals(TextCompiledScriptImpl.class, script.getClass());
        assertEquals("${'test'}", script.getScriptText());

        CompiledScript literal = ScriptUtil.compile("'test'");
        assertEquals(LiteralScript.class, literal.getClass());
        assertEquals("'test'", literal.getScriptText());

        CompiledScript nullScript = ScriptUtil.compile("");
        assertSame(LiteralScript.NULL_LITERAL_SCRIPT, nullScript);
    }

    @Test
    public void testCompile_rewriteMayaaScopeMacros_onlyScriptContext() {
        CompiledScript scope = ScriptUtil.compile("var a = MAYAA_SCOPE(user.name);");
        assertEquals(LiteralScript.class, scope.getClass());
        assertEquals("var a = MAYAA_SCOPE(user.name);", scope.getScriptText());

        SpecificationNodeImpl scriptNode = new SpecificationNodeImpl(
                QNameImpl.getInstance("http://www.w3.org/TR/html4", "script"));
        CycleUtil.getServiceCycle().setInjectedNode(scriptNode);

        scope = ScriptUtil.compile("var a = MAYAA_SCOPE(user.name);");
        assertFalse(scope.isLiteral());
        assertEquals("var a = ${user.name};", scope.getScriptText());
        assertEquals(ComplexScript.class, scope.getClass());
        CompiledScript[] scopeBlocks = ((ComplexScript) scope).getCompiledScripts();
        assertEquals(3, scopeBlocks.length);
        assertEquals(LiteralScript.class, scopeBlocks[0].getClass());
        assertEquals("var a = ", scopeBlocks[0].getScriptText());
        assertEquals(TextCompiledScriptImpl.class, scopeBlocks[1].getClass());
        assertEquals("${user.name}", scopeBlocks[1].getScriptText());
        assertEquals(LiteralScript.class, scopeBlocks[2].getClass());
        assertEquals(";", scopeBlocks[2].getScriptText());

        CompiledScript asString = ScriptUtil.compile("var b = MAYAA_SCOPE_AS_STRING(user.name);");
        assertFalse(asString.isLiteral());
        assertEquals("var b = ${String(user.name)};", asString.getScriptText());
        assertEquals(ComplexScript.class, asString.getClass());
        CompiledScript[] asStringBlocks = ((ComplexScript) asString).getCompiledScripts();
        assertEquals(3, asStringBlocks.length);
        assertEquals("var b = ", asStringBlocks[0].getScriptText());
        assertEquals("${String(user.name)}", asStringBlocks[1].getScriptText());
        assertEquals(";", asStringBlocks[2].getScriptText());

        CompiledScript raw = ScriptUtil.compile("var c = MAYAA_SCOPE_RAW(user.name);");
        assertFalse(raw.isLiteral());
        assertEquals("var c = ${user.name};", raw.getScriptText());
        assertEquals(ComplexScript.class, raw.getClass());
        CompiledScript[] rawBlocks = ((ComplexScript) raw).getCompiledScripts();
        assertEquals(3, rawBlocks.length);
        assertEquals("var c = ", rawBlocks[0].getScriptText());
        assertEquals(RawOutputCompiledScript.class, rawBlocks[1].getClass());
        assertEquals("${user.name}", rawBlocks[1].getScriptText());
        assertEquals(";", rawBlocks[2].getScriptText());
    }

}
