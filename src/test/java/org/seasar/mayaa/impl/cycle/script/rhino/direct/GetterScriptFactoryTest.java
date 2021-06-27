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
package org.seasar.mayaa.impl.cycle.script.rhino.direct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.scope.BindingScope;
import org.seasar.mayaa.impl.cycle.scope.HeaderScope;
import org.seasar.mayaa.impl.cycle.scope.ParamScope;
import org.seasar.mayaa.impl.cycle.script.rhino.TextCompiledScriptImpl;
import org.seasar.mayaa.impl.cycle.script.rhino.WalkStandardScope;
import org.seasar.mayaa.test.util.ManualProviderFactory;
import org.seasar.mayaa.test.util.TestObjectFactory.CompositeList;

/**
 * @author Koji Suga (Gluegent Inc.)
 */
public class GetterScriptFactoryTest {

    private PositionAware _position;

    @Before
    public void setUp() {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
        ManualProviderFactory.SCRIPT_ENVIRONMENT.startScope(null);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.addAttributeScope(new ParamScope());
        ManualProviderFactory.SCRIPT_ENVIRONMENT.addAttributeScope(new HeaderScope());
        ManualProviderFactory.SCRIPT_ENVIRONMENT.addAttributeScope(new BindingScope());
        ManualProviderFactory.SCRIPT_ENVIRONMENT.addAttributeScope(new WalkStandardScope());
        _position = new PositionAware() {

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
        };
    }

    @After
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    public void microBenchmark() {
        CompositeList dto = new CompositeList("complist");
        Map<String, String> map = new HashMap<>();
        map.put("name", "mapvalue");
        CycleUtil.getPageScope().setAttribute("dto", dto);
        CycleUtil.getPageScope().setAttribute("map", map);
        String dtoScriptText = " page.dto.name ";
        String mapScriptText = " page.map.name ";

        TextCompiledScriptImpl dtoScript = new TextCompiledScriptImpl(
                dtoScriptText, _position, 1);
        dtoScript.setExpectedClass(Object.class);
        TextCompiledScriptImpl mapScript = new TextCompiledScriptImpl(
                mapScriptText, _position, 1);
        mapScript.setExpectedClass(Object.class);

        int count = 10000;

        long first = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            assertEquals("complist", dtoScript.execute(null));
        }

        long second = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            assertEquals("mapvalue", mapScript.execute(null));
        }

        long third = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            try {
                Object obj = CycleUtil.getPageScope().getAttribute("dto");
                assertEquals("complist", BeanUtils.getProperty(obj, "name"));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                fail();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                fail();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                fail();
            }
        }

        long fourth = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            try {
                Object obj = CycleUtil.getPageScope().getAttribute("map");
                assertEquals("mapvalue", BeanUtils.getProperty(obj, "name"));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                fail();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                fail();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                fail();
            }
        }

        long fifth = System.currentTimeMillis();

        System.out.println("rd■" + (second - first));
        System.out.println("rm■" + (third - second));
        System.out.println("bd■" + (fourth - third));
        System.out.println("bm■" + (fifth - fourth));
    }

    @Test
    public void testIsGetterScript() {
        assertTrue(isGetterScript("text"));
        assertTrue(isGetterScript("page.text"));
        assertTrue(isGetterScript("request.text"));
        assertTrue(isGetterScript("session.text"));
        assertTrue(isGetterScript("application.text"));
        assertTrue(isGetterScript("_.text"));

        assertTrue(isGetterScript(" _.text "));
        assertTrue(isGetterScript(" _.text; "));
        assertTrue(isGetterScript(" _.text ; "));
        assertTrue(isGetterScript("\n _ \n . \n text \n"));

        // スコープ名がStandardScopeにないので前に"_"が付く
        // 静的な正規表現では対応できないのでテストは除外。
        // assertFalse(isGetterScript("foo.text"));

        assertTrue(isGetterScript("page['text']"));
        assertTrue(isGetterScript("page[\"text\"]"));
        assertTrue(isGetterScript("page[ 'text' ]"));
        assertTrue(isGetterScript("page[ \"text\" ]"));

        // プロパティ有り
        assertTrue(isGetterScript("page.text['test']"));
        assertTrue(isGetterScript("page.text[ 'test' ]"));
        assertTrue(isGetterScript("page.text[\"test\"]"));
        assertTrue(isGetterScript("page.text[ \"test\" ]"));
        assertTrue(isGetterScript("page.text.test"));
        assertTrue(isGetterScript("page['text']['test']"));
        assertTrue(isGetterScript("page[ 'text' ][ 'test' ]"));

        // TODO getAttributeに対応する
        assertFalse(isGetterScript("page.getAttribute('text')"));
        assertFalse(isGetterScript("page.getAttribute(\"text\")"));
        assertFalse(isGetterScript("page.getAttribute ( 'text' ) "));
        assertFalse(isGetterScript("page.getAttribute ( \"text\" ) "));
        assertFalse(isGetterScript("page.getAttribute('text').test"));
        assertFalse(isGetterScript("page.getAttribute(\"text\")[ ' test ' ] "));

        // TODO getterも対象にする
        assertFalse(isGetterScript("page['text'].getTest()"));

        // 配列も対象にしたい
        assertFalse(isGetterScript("text[0]"));
        assertFalse(isGetterScript("page.text[0]"));

        assertFalse(isGetterScript("text()"));
        assertFalse(isGetterScript("page.text()"));
    }

    private boolean isGetterScript(String script) {
        return GetterScriptFactory.GETTER_SCRIPT_PATTERN.matcher(script).matches();
    }

    @Test
    public void testSplitScope1() {
        assertNull(GetterScriptFactory.splitScope("text()"));

        String[] result1 = GetterScriptFactory.splitScope("text");
        assertNull(result1[0]);
        assertEquals("text", result1[1]);

        String[] result2 = GetterScriptFactory.splitScope("page.text");
        assertEquals("page", result2[0]);
        assertEquals("text", result2[1]);

        String[] result3 = GetterScriptFactory.splitScope(" page . text ; ");
        assertEquals("page", result3[0]);
        assertEquals("text", result3[1]);

        String[] result4 = GetterScriptFactory.splitScope(" \n page \n . \n text \n ");
        assertEquals("page", result4[0]);
        assertEquals("text", result4[1]);

        assertNull(GetterScriptFactory.splitScope("page.0"));
    }

    @Test
    public void testSplitScope2() {
        String[] result1 = GetterScriptFactory.splitScope("page['0']");
        assertEquals("page", result1[0]);
        assertEquals("0", result1[1]);

        String[] result2 = GetterScriptFactory.splitScope("page['text']");
        assertEquals("page", result2[0]);
        assertEquals("text", result2[1]);

        String[] result3 = GetterScriptFactory.splitScope("page[ 'text' ] ; ");
        assertEquals("page", result3[0]);
        assertEquals("text", result3[1]);

        String[] result4 = GetterScriptFactory.splitScope(" \n page \n [ \n 'text' \n ] \n ");
        assertEquals("page", result4[0]);
        assertEquals("text", result4[1]);

        String[] result5 = GetterScriptFactory.splitScope("page[\"0\"]");
        assertEquals("page", result5[0]);
        assertEquals("0", result5[1]);

        String[] result6 = GetterScriptFactory.splitScope("page[\"text\"]");
        assertEquals("page", result6[0]);
        assertEquals("text", result6[1]);

        String[] result7 = GetterScriptFactory.splitScope("page[ \"text\" ]");
        assertEquals("page", result7[0]);
        assertEquals("text", result7[1]);

        String[] result8 = GetterScriptFactory.splitScope(" page[ \"text\" ] ; ");
        assertEquals("page", result8[0]);
        assertEquals("text", result8[1]);

        String[] result9 = GetterScriptFactory.splitScope(" \n page \n [ \n \"text\" \n ] \n ; \n");
        assertEquals("page", result9[0]);
        assertEquals("text", result9[1]);
    }

    @Test
    public void testSplitScope3() {
        String[] result1 = GetterScriptFactory.splitScope("page['0']");
        assertEquals("page", result1[0]);
        assertEquals("0", result1[1]);

        String[] result2 = GetterScriptFactory.splitScope("page['text']");
        assertEquals("page", result2[0]);
        assertEquals("text", result2[1]);

        String[] result3 = GetterScriptFactory.splitScope("page [ 'text' ] ; ");
        assertEquals("page", result3[0]);
        assertEquals("text", result3[1]);

        String[] result4 = GetterScriptFactory.splitScope(
                " \n page \n [ \n 'text' \n ] \n ");
        assertEquals("page", result4[0]);
        assertEquals("text", result4[1]);

        String[] result5 = GetterScriptFactory.splitScope("page[\"0\"]");
        assertEquals("page", result5[0]);
        assertEquals("0", result5[1]);

        String[] result6 = GetterScriptFactory.splitScope("page[\"text\"]");
        assertEquals("page", result6[0]);
        assertEquals("text", result6[1]);

        String[] result7 = GetterScriptFactory.splitScope("page[ \"text\" ]");
        assertEquals("page", result7[0]);
        assertEquals("text", result7[1]);

        String[] result8 = GetterScriptFactory.splitScope(" page[ \"text\" ] ; ");
        assertEquals("page", result8[0]);
        assertEquals("text", result8[1]);

        String[] result9 = GetterScriptFactory.splitScope(
                " \n page \n [ \n \"text\" \n ] \n ");
        assertEquals("page", result9[0]);
        assertEquals("text", result9[1]);
    }

    @Test
    public void testSplitScope1Property() {
        String[] result1 = GetterScriptFactory.splitScope("text.name");
        assertNull(result1[0]);
        assertEquals("text", result1[1]);

        String[] result2 = GetterScriptFactory.splitScope("page.text.name");
        assertEquals("page", result2[0]);
        assertEquals("text", result2[1]);
        assertEquals("name", result2[2]);

        String[] result3 = GetterScriptFactory.splitScope(" page . text . name ; ");
        assertEquals("page", result3[0]);
        assertEquals("text", result3[1]);
        assertEquals("name", result3[2]);

        String[] result4 = GetterScriptFactory.splitScope(" \n page \n . \n text \n . \n name \n ; ");
        assertEquals("page", result4[0]);
        assertEquals("text", result4[1]);
        assertEquals("name", result4[2]);

        assertNull(GetterScriptFactory.splitScope("page.text.0"));
    }

    @Test
    public void testSplitScope2Property() {
        String[] result1 = GetterScriptFactory.splitScope("page['0']['1']");
        assertEquals("page", result1[0]);
        assertEquals("0", result1[1]);
        assertEquals("1", result1[2]);

        String[] result2 = GetterScriptFactory.splitScope("page['text']['name']");
        assertEquals("page", result2[0]);
        assertEquals("text", result2[1]);
        assertEquals("name", result2[2]);

        String[] result3 = GetterScriptFactory.splitScope("page[ 'text' ] [ 'name' ]  ; ");
        assertEquals("page", result3[0]);
        assertEquals("text", result3[1]);
        assertEquals("name", result3[2]);

        String[] result4 = GetterScriptFactory.splitScope(
                " \n page \n [ \n 'text' \n ] \n [ \n 'name' \n ] \n ;");
        assertEquals("page", result4[0]);
        assertEquals("text", result4[1]);
        assertEquals("name", result4[2]);

        String[] result5 = GetterScriptFactory.splitScope("page[\"0\"][\"1\"]");
        assertEquals("page", result5[0]);
        assertEquals("0", result5[1]);
        assertEquals("1", result5[2]);

        String[] result6 = GetterScriptFactory.splitScope("page[\"text\"][\"name\"]");
        assertEquals("page", result6[0]);
        assertEquals("text", result6[1]);
        assertEquals("name", result6[2]);

        String[] result7 = GetterScriptFactory.splitScope("page[ \"text\" ][ \"name\" ]");
        assertEquals("page", result7[0]);
        assertEquals("text", result7[1]);
        assertEquals("name", result7[2]);

        String[] result8 = GetterScriptFactory.splitScope(" page[ \"text\" ] [ \"name\" ] ; ");
        assertEquals("page", result8[0]);
        assertEquals("text", result8[1]);
        assertEquals("name", result8[2]);

        String[] result9 = GetterScriptFactory.splitScope(
                " \n page \n [ \n \"text\" \n ] \n [ \n \"name\" \n ] \n ; \n");
        assertEquals("page", result9[0]);
        assertEquals("text", result9[1]);
        assertEquals("name", result9[2]);
    }

    @Test
    public void testSplitScope3Property() {
        String[] result1 = GetterScriptFactory.splitScope("page['0']['1']");
        assertEquals("page", result1[0]);
        assertEquals("0", result1[1]);
        assertEquals("1", result1[2]);

        String[] result2 = GetterScriptFactory.splitScope("page['text'].name");
        assertEquals("page", result2[0]);
        assertEquals("text", result2[1]);
        assertEquals("name", result2[2]);

        String[] result3 = GetterScriptFactory.splitScope("page.text[ 'name' ]  ; ");
        assertEquals("page", result3[0]);
        assertEquals("text", result3[1]);
        assertEquals("name", result3[2]);

        String[] result4 = GetterScriptFactory.splitScope(
                " \n page \n . \n text \n \n [ \n 'name' \n ] \n ;");
        assertEquals("page", result4[0]);
        assertEquals("text", result4[1]);
        assertEquals("name", result4[2]);

        String[] result5 = GetterScriptFactory.splitScope("page[\"0\"]['1']");
        assertEquals("page", result5[0]);
        assertEquals("0", result5[1]);
        assertEquals("1", result5[2]);

        String[] result6 = GetterScriptFactory.splitScope("page[\"text\"] . name ");
        assertEquals("page", result6[0]);
        assertEquals("text", result6[1]);
        assertEquals("name", result6[2]);

        String[] result7 = GetterScriptFactory.splitScope("page.text[ \"name\" ]");
        assertEquals("page", result7[0]);
        assertEquals("text", result7[1]);
        assertEquals("name", result7[2]);
    }

    @Test
    public void testSplitScope10Property() {
        String[] result1 = GetterScriptFactory.splitScope(
                "page['0']['1'].fooBar.FOOBAR.FooBar.foo_bar\n" +
                "[ \n 'foo_0_bar' \n ] \n . \n a \n . b");
        assertEquals("page", result1[0]);
        assertEquals("0", result1[1]);
        assertEquals("1", result1[2]);
        assertEquals("fooBar", result1[3]);
        assertEquals("FOOBAR", result1[4]);
        assertEquals("FooBar", result1[5]);
        assertEquals("foo_bar", result1[6]);
        assertEquals("foo_0_bar", result1[7]);
        assertEquals("a", result1[8]);
        assertEquals("b", result1[9]);
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.cycle.script.rhino.direct.GetterScriptFactory#create(java.lang.String)}.
     */
    @Test
    public void testCreateStandard1() {
        CycleUtil.getCurrentPageScope().setAttribute("text", "foo");
        String script = " text ";

        AbstractGetterScript result =
            (AbstractGetterScript) GetterScriptFactory.create(script, _position, 1);
        assertTrue(result instanceof StandardGetterScript);
        assertEquals("text", result.getAttributeName());
        assertEquals("foo", result.execute(null));


        // TODO 多段に対応したらGetterScriptになるようにする
        assertNull(GetterScriptFactory.create("text.test.foo", _position, 1));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.cycle.script.rhino.direct.GetterScriptFactory#create(java.lang.String)}.
     */
    @Test
    public void testCreateStandard2() {
        CycleUtil.getCurrentPageScope().setAttribute("text", "foo");
        String script = " _.text ";

        AbstractGetterScript result =
            (AbstractGetterScript) GetterScriptFactory.create(script, _position, 1);
        assertTrue(result instanceof StandardGetterScript);
        assertEquals("text", result.getAttributeName());
        assertEquals("foo", result.execute(null));


        // TODO 多段に対応したらGetterScriptになるようにする
        assertNull(GetterScriptFactory.create("_.text.test.foo", _position, 1));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.cycle.script.rhino.direct.GetterScriptFactory#create(java.lang.String)}.
     */
    @Test
    public void testCreatePage() {
        CycleUtil.getCurrentPageScope().setAttribute("text", "bar");
        CycleUtil.getPageScope().setAttribute("text", "foo");

        String script = " page.text ";
        AbstractGetterScript result =
            (AbstractGetterScript) GetterScriptFactory.create(script, _position, 1);
        assertTrue(result instanceof PageGetterScript);
        assertEquals("text", result.getAttributeName());
        assertEquals("foo", result.execute(null));

        String script2 = " this.text ";
        AbstractGetterScript result2 =
            (AbstractGetterScript) GetterScriptFactory.create(script2, _position, 1);
        assertTrue(result2 instanceof PageGetterScript);
        assertEquals("text", result2.getAttributeName());
        assertEquals("foo", result2.execute(null));


        // TODO 多段に対応したらGetterScriptになるようにする
        assertNull(GetterScriptFactory.create("page.text.test.foo", _position, 1));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.cycle.script.rhino.direct.GetterScriptFactory#create(java.lang.String)}.
     */
    @Test
    public void testCreateRequest() {
        CycleUtil.getPageScope().setAttribute("text", "bar");
        CycleUtil.getServiceCycle().getRequestScope().setAttribute("text", "foo");
        String script = " request.text ";

        AbstractGetterScript result =
            (AbstractGetterScript) GetterScriptFactory.create(script, _position, 1);
        assertTrue(result instanceof RequestGetterScript);
        assertEquals("text", result.getAttributeName());
        assertEquals("foo", result.execute(null));


        // TODO 多段に対応したらGetterScriptになるようにする
        assertNull(GetterScriptFactory.create("request.text.test.foo", _position, 1));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.cycle.script.rhino.direct.GetterScriptFactory#create(java.lang.String)}.
     */
    @Test
    public void testCreateSession() {
        CycleUtil.getPageScope().setAttribute("text", "bar");
        CycleUtil.getServiceCycle().getSessionScope().setAttribute("text", "foo");
        String script = " session.text ";

        AbstractGetterScript result =
            (AbstractGetterScript) GetterScriptFactory.create(script, _position, 1);
        assertTrue(result instanceof SessionGetterScript);
        assertEquals("text", result.getAttributeName());
        assertEquals("foo", result.execute(null));


        // TODO 多段に対応したらGetterScriptになるようにする
        assertNull(GetterScriptFactory.create("session.text.test.foo", _position, 1));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.cycle.script.rhino.direct.GetterScriptFactory#create(java.lang.String)}.
     */
    @Test
    public void testCreateApplication() {
        CycleUtil.getPageScope().setAttribute("text", "bar");
        CycleUtil.getServiceCycle().getApplicationScope().setAttribute("text", "foo");
        String script = " application.text ";

        AbstractGetterScript result =
            (AbstractGetterScript) GetterScriptFactory.create(script, _position, 1);
        assertTrue(result instanceof ApplicationGetterScript);
        assertEquals("text", result.getAttributeName());
        assertEquals("foo", result.execute(null));


        // TODO 多段に対応したらGetterScriptになるようにする
        assertNull(GetterScriptFactory.create("application.text.test.foo", _position, 1));
    }

    /**
     * プロパティ呼び出しのテスト(Bean)
     */
    @Test
    public void testPropertyCall1() {
        CycleUtil.getPageScope().setAttribute("text", String.class);
        String script = " page.text.name ";

        TextCompiledScriptImpl result1 = new TextCompiledScriptImpl(script, _position, 1);
        assertEquals("java.lang.String", result1.execute(null));

        AbstractGetterScript result2 =
            (AbstractGetterScript) GetterScriptFactory.create(script, _position, 1);
        assertTrue(result2 instanceof PageGetterScript);
        assertEquals("text", result2.getAttributeName());
        assertEquals("name", result2.getPropertyName());
        assertEquals("java.lang.String", result2.execute(null));

        AbstractGetterScript resultUndefinde =
            (AbstractGetterScript) GetterScriptFactory.create(
                    " page.text.nothing ", _position, 1);
        assertTrue(resultUndefinde instanceof PageGetterScript);
        assertEquals("text", resultUndefinde.getAttributeName());
        assertEquals("nothing", resultUndefinde.getPropertyName());
        assertNull(resultUndefinde.execute(null));
    }

    /**
     * プロパティ呼び出しのテスト(Map)
     */
    @Test
    public void testPropertyCall2() {
        Map<String, String> map = new HashMap<>();
        map.put("name", "mapvalue");
        CycleUtil.getPageScope().setAttribute("text", map);
        String script = " page.text.name ";

        TextCompiledScriptImpl result1 = new TextCompiledScriptImpl(script, _position, 1);
        assertEquals("mapvalue", result1.execute(null));

        AbstractGetterScript result2 =
            (AbstractGetterScript) GetterScriptFactory.create(script, _position, 1);
        assertTrue(result2 instanceof PageGetterScript);
        assertEquals("text", result2.getAttributeName());
        assertEquals("name", result2.getPropertyName());
        assertEquals("mapvalue", result2.execute(null));

        AbstractGetterScript resultUndefinde =
            (AbstractGetterScript) GetterScriptFactory.create(
                    " page.text.nothing ", _position, 1);
        assertTrue(resultUndefinde instanceof PageGetterScript);
        assertEquals("text", resultUndefinde.getAttributeName());
        assertEquals("nothing", resultUndefinde.getPropertyName());
        assertNull(resultUndefinde.execute(null));
    }

    /**
     * プロパティ呼び出しのテスト(JavaScript)
     */
    @Test
    public void testPropertyCall3() {
        TextCompiledScriptImpl prepare = new TextCompiledScriptImpl(
                "page.text = { 'name' : 'jsvalue' };", _position, 1);
        prepare.execute(null);

        String script = " page.text.name ";

        TextCompiledScriptImpl result1 = new TextCompiledScriptImpl(script, _position, 1);
        assertEquals("jsvalue", result1.execute(null));

        AbstractGetterScript result2 =
            (AbstractGetterScript) GetterScriptFactory.create(script, _position, 1);
        assertTrue(result2 instanceof PageGetterScript);
        assertEquals("text", result2.getAttributeName());
        assertEquals("name", result2.getPropertyName());
        assertEquals("jsvalue", result2.execute(null));

        AbstractGetterScript resultUndefinde =
            (AbstractGetterScript) GetterScriptFactory.create(
                    " page.text.nothing ", _position, 1);
        assertTrue(resultUndefinde instanceof PageGetterScript);
        assertEquals("text", resultUndefinde.getAttributeName());
        assertEquals("nothing", resultUndefinde.getPropertyName());
        assertNull(resultUndefinde.execute(null));
    }

    /**
     * 多段階プロパティ呼び出しのテスト(JavaScript)
     */
    /* TODO 多段階に対応する
    @Test
    public void testPropertyCallMany3() {
        TextCompiledScriptImpl prepare = new TextCompiledScriptImpl(
                "page.text = { 'name1' : { 'name2' : { 'name3' : 'jsvalue' } } };",
                _position, 1);
        prepare.execute(null);

        String script = " page.text.name1.name2.name3 ";

        TextCompiledScriptImpl result1 = new TextCompiledScriptImpl(script, _position, 1);
        assertEquals("jsvalue", result1.execute(null));

        AbstractGetterScript result2 =
            (AbstractGetterScript) GetterScriptFactory.create(script, _position, 1);
        assertTrue(result2 instanceof PageGetterScript);
        assertEquals("text", result2.getAttributeName());
        assertEquals("name", result2.getPropertyName());
        assertEquals("jsvalue", result2.execute(null));

        AbstractGetterScript resultUndefinde =
            (AbstractGetterScript) GetterScriptFactory.create(
                    " page.text.nothing ", _position, 1);
        assertTrue(resultUndefinde instanceof PageGetterScript);
        assertEquals("text", resultUndefinde.getAttributeName());
        assertEquals("nothing", resultUndefinde.getPropertyName());
        assertNull(resultUndefinde.execute(null));
    }
    */

}
