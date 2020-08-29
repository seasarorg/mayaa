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
package org.seasar.mayaa.impl.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class PathAdjusterImplTest {

    @Test
    public void testIsTargetNodeHtml() {
        PathAdjusterImpl adjuster = new PathAdjusterImpl();
        assertTrue("a", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "a")));
        assertTrue("link", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "link")));
        assertTrue("area", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "area")));
        assertTrue("base", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "base")));
        assertTrue("img", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "img")));
        assertTrue("embed", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "embed")));
        assertTrue("iframe", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "iframe")));
        assertTrue("frame", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "frame")));
        assertTrue("script", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "script")));
        assertTrue("applet", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "applet")));
        assertTrue("form", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "form")));
        assertTrue("object", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "object")));
        assertFalse("body", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "body")));
        assertFalse("m:a", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_MAYAA, "a")));
    }

    @Test
    public void testIsTargetNodeXhtml() {
        PathAdjusterImpl adjuster = new PathAdjusterImpl();
        assertTrue("a", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "a")));
        assertTrue("link", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "link")));
        assertTrue("area", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "area")));
        assertTrue("base", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "base")));
        assertTrue("img", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "img")));
        assertTrue("embed", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "embed")));
        assertTrue("iframe", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "iframe")));
        assertTrue("frame", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "frame")));
        assertTrue("script", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "script")));
        assertTrue("applet", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "applet")));
        assertTrue("form", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "form")));
        assertTrue("object", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "object")));
        assertFalse("body", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "body")));
        assertFalse("m:a", adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_MAYAA, "a")));
    }

    @Test
    public void testIsTargetAttributeHtml() {
        PathAdjusterImpl adjuster = new PathAdjusterImpl();
        assertTrue("a href", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "a"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "href")));
        assertFalse("a name", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "a"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "name")));
        assertTrue("link href", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "link"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "href")));
        assertFalse("link type", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "link"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "type")));
        assertTrue("area href", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "area"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "href")));
        assertFalse("area shape", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "area"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "shape")));
        assertTrue("base href", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "base"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "href")));
        assertTrue("img src", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "img"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "src")));
        assertFalse("img alt", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "img"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "alt")));
        assertTrue("embed src", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "embed"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "src")));
        assertFalse("embed name", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "embed"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "name")));
        assertTrue("iframe src", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "iframe"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "src")));
        assertFalse("iframe name", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "iframe"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "name")));
        assertTrue("frame", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "src")));
        assertTrue("frame", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "longdesc")));
        assertFalse("frame name", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "name")));
        assertTrue("script src", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "script"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "src")));
        assertFalse("script type", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "script"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "type")));
        assertTrue("applet code", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "applet"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "code")));
        assertFalse("applet width", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "applet"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "width")));
        assertTrue("form action", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "form"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "action")));
        assertFalse("form name", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "form"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "name")));
        assertTrue("object data", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "object"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "data")));
        assertFalse("object type", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "object"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "type")));
        assertFalse("body", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "body"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "href")));
        // isTargetAttributeでは名前空間を見ない
        assertTrue("m:a", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_MAYAA, "a"),
                QNameImpl.getInstance(CONST_IMPL.URI_MAYAA, "href")));
    }

    @Test
    public void testIsTargetAttributeXhtml() {
        PathAdjusterImpl adjuster = new PathAdjusterImpl();
        assertTrue("a href", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "a"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "href")));
        assertFalse("a name", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "a"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "name")));
        assertTrue("link href", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "link"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "href")));
        assertFalse("link type", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "link"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "type")));
        assertTrue("area href", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "area"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "href")));
        assertFalse("area shape", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "area"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "shape")));
        assertTrue("base href", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "base"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "href")));
        assertTrue("img src", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "img"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "src")));
        assertFalse("img alt", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "img"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "alt")));
        assertTrue("embed src", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "embed"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "src")));
        assertFalse("embed name", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "embed"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "name")));
        assertTrue("iframe src", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "iframe"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "src")));
        assertFalse("iframe name", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "iframe"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "name")));
        assertTrue("frame", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "src")));
        assertTrue("frame", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "longdesc")));
        assertFalse("frame name", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "name")));
        assertTrue("script src", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "script"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "src")));
        assertFalse("script type", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "script"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "type")));
        assertTrue("applet code", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "applet"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "code")));
        assertFalse("applet width", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "applet"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "width")));
        assertTrue("form action", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "form"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "action")));
        assertFalse("form name", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "form"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "name")));
        assertTrue("object data", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "object"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "data")));
        assertFalse("object type", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "object"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "type")));
        assertFalse("body", adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "body"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "href")));
    }

    @Test
    public void testAdjustRelativePath() {
        // see test for StringUtil.adjustRelativePath
        PathAdjusterImpl adjuster = new PathAdjusterImpl();
        assertEquals("", adjuster.adjustRelativePath("/page/foo.html", ""));
        assertEquals(" ", adjuster.adjustRelativePath("/page/foo.html", " "));
        assertEquals("/page/test_component",
                adjuster.adjustRelativePath("/page/foo.html", "./test_component"));
        assertEquals("/page/test_component",
                adjuster.adjustRelativePath("/page/foo.html", "./test_component "));
        assertEquals("test_component",
                adjuster.adjustRelativePath("/page/foo.html", "test_component"));
        assertEquals("/test_component",
                adjuster.adjustRelativePath("/page/foo.html", "/test_component"));
        assertEquals("http://foo.bar.com/test_component",
                adjuster.adjustRelativePath("/page/foo.html", "http://foo.bar.com/test_component"));
        assertEquals("#test_component",
                adjuster.adjustRelativePath("/page/foo.html", "#test_component"));
    }

    @Test
    public void testAdjustRelativePath_force() {
        // see test for StringUtil.adjustRelativePath
        PathAdjusterImpl adjuster = new PathAdjusterImpl();
        adjuster.setParameter("force", "true");
        assertEquals("", adjuster.adjustRelativePath("/page/foo.html", ""));
        assertEquals(" ", adjuster.adjustRelativePath("/page/foo.html", " "));
        assertEquals("/page/test_component",
                adjuster.adjustRelativePath("/page/foo.html", "./test_component"));
        assertEquals("/page/test_component",
                adjuster.adjustRelativePath("/page/foo.html", "./test_component "));
        assertEquals("/page/test_component",
                adjuster.adjustRelativePath("/page/foo.html", "test_component"));

        assertEquals("http://foo.bar.com/test_component",
                adjuster.adjustRelativePath("/page/foo.html", "http://foo.bar.com/test_component"));
        assertEquals("https://foo.bar.com/test_component",
                adjuster.adjustRelativePath("/page/foo.html", "https://foo.bar.com/test_component"));
        assertEquals("ftp://foo.bar.com/test_component",
                adjuster.adjustRelativePath("/page/foo.html", "ftp://foo.bar.com/test_component"));
        assertEquals("/test_component",
                adjuster.adjustRelativePath("/page/foo.html", "/test_component"));
        assertEquals("#test_component",
                adjuster.adjustRelativePath("/page/foo.html", "#test_component"));
        assertEquals("about: blank",
                adjuster.adjustRelativePath("/page/foo.html", "about: blank"));
    }

}
