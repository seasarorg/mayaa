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
    public void testCompile_rewriteMayaaScopeMacros_toScopeHelpers_onlyScriptContext() {
        CompiledScript scope = ScriptUtil.compile("var a = MAYAA_SCOPE(user.name);");
        assertEquals(LiteralScript.class, scope.getClass());
        assertEquals("var a = MAYAA_SCOPE(user.name);", scope.getScriptText());

        SpecificationNodeImpl scriptNode = new SpecificationNodeImpl(
                QNameImpl.getInstance("http://www.w3.org/TR/html4", "script"));
        CycleUtil.getServiceCycle().setInjectedNode(scriptNode);

        scope = ScriptUtil.compile("var a = MAYAA_SCOPE(user.name);");
        assertEquals(LiteralScript.class, scope.getClass());
        assertEquals("var a = MAYAA_SCOPE(user.name);", scope.getScriptText());

        CompiledScript asString = ScriptUtil.compile("var b = MAYAA_SCOPE_AS_STRING(user.name);");
        assertEquals(LiteralScript.class, asString.getClass());
        assertEquals("var b = MAYAA_SCOPE_AS_STRING(user.name);", asString.getScriptText());

        CompiledScript withStringify = ScriptUtil.compile("var d = MAYAA_SCOPE_WITH_STRINGIFY(user);");
        assertEquals(LiteralScript.class, withStringify.getClass());
        assertEquals("var d = MAYAA_SCOPE_WITH_STRINGIFY(user);", withStringify.getScriptText());

        CompiledScript raw = ScriptUtil.compile("var c = MAYAA_SCOPE_RAW(user.name);");
        assertEquals(LiteralScript.class, raw.getClass());
        assertEquals("var c = MAYAA_SCOPE_RAW(user.name);", raw.getScriptText());
    }

    @Test
    public void testCompile_scopeWithStringify_isNotRewrittenOutsideScriptContext() {
        CompiledScript withStringify = ScriptUtil.compile("var payload = MAYAA_SCOPE_WITH_STRINGIFY(user);");
        assertEquals(LiteralScript.class, withStringify.getClass());
        assertEquals("var payload = MAYAA_SCOPE_WITH_STRINGIFY(user);", withStringify.getScriptText());
    }

    @Test
    public void testCompile_scopeWithStringify_embedsObjectAsJsonTextInScriptContext() {
        SpecificationNodeImpl scriptNode = new SpecificationNodeImpl(
                QNameImpl.getInstance("http://www.w3.org/TR/html4", "script"));
        CycleUtil.getServiceCycle().setInjectedNode(scriptNode);

        CompiledScript script = ScriptUtil.compile("var payload = MAYAA_SCOPE_WITH_STRINGIFY(user);");
        assertEquals(LiteralScript.class, script.getClass());
        assertEquals("var payload = MAYAA_SCOPE_WITH_STRINGIFY(user);", script.getScriptText());
    }

    @Test
    public void testRewriteScopeMacrosForScriptContext() {
        String source = "var a = MAYAA_SCOPE(user.name);"
                + "var b = MAYAA_SCOPE_AS_STRING(user.name);"
                + "var c = MAYAA_SCOPE_RAW(user.name);"
                + "var d = MAYAA_SCOPE_WITH_STRINGIFY(user);";

        String rewritten = ScriptUtil.rewriteScopeMacrosForScriptContext(source);
        assertEquals("var a = ${=_mayaa_scope(user.name)};"
                + "var b = ${=_mayaa_scope_as_string(user.name)};"
                + "var c = ${=user.name};"
                + "var d = ${=_mayaa_scope_with_stringify(user)};", rewritten);
    }

}
