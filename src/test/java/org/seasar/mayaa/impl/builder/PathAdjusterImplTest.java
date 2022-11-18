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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class PathAdjusterImplTest {

    @Test
    public void testIsTargetNodeHtml() {
        PathAdjusterImpl adjuster = new PathAdjusterImpl();
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "a")), "a");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "link")), "link");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "area")), "area");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "base")), "base");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "img")), "img");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "embed")), "embed");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "iframe")), "iframe");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "frame")), "frame");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "script")), "script");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "applet")), "applet");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "form")), "form");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "object")), "object");
        assertFalse(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "body")), "body");
        assertFalse(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_MAYAA, "a")), "m:a");
    }

    @Test
    public void testIsTargetNodeXhtml() {
        PathAdjusterImpl adjuster = new PathAdjusterImpl();
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "a")), "a");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "link")), "link");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "area")), "area");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "base")), "base");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "img")), "img");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "embed")), "embed");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "iframe")), "iframe");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "frame")), "frame");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "script")), "script");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "applet")), "applet");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "form")), "form");
        assertTrue(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "object")), "object");
        assertFalse(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "body")), "body");
        assertFalse(adjuster.isTargetNode(
                QNameImpl.getInstance(CONST_IMPL.URI_MAYAA, "a")), "m:a");
    }

    @Test
    public void testIsTargetAttributeHtml() {
        PathAdjusterImpl adjuster = new PathAdjusterImpl();
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "a"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "href")), "a href");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "a"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "name")), "a name");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "link"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "href")), "link href");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "link"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "type")), "link type");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "area"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "href")), "area href");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "area"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "shape")), "area shape");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "base"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "href")), "base href");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "img"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "src")), "img src");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "img"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "alt")), "img alt");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "embed"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "src")), "embed src");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "embed"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "name")), "embed name");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "iframe"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "src")), "iframe src");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "iframe"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "name")), "iframe name");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "src")), "frame");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "longdesc")), "frame");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "name")), "frame name");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "script"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "src")), "script src");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "script"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "type")), "script type");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "applet"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "code")), "applet code");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "applet"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "width")), "applet width");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "form"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "action")), "form action");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "form"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "name")), "form name");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "object"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "data")), "object data");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "object"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "type")), "object type");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "body"),
                QNameImpl.getInstance(CONST_IMPL.URI_HTML, "href")), "body");
        // isTargetAttributeでは名前空間を見ない
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_MAYAA, "a"),
                QNameImpl.getInstance(CONST_IMPL.URI_MAYAA, "href")), "m:a");
    }

    @Test
    public void testIsTargetAttributeXhtml() {
        PathAdjusterImpl adjuster = new PathAdjusterImpl();
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "a"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "href")), "a href");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "a"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "name")), "a name");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "link"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "href")), "link href");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "link"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "type")), "link type");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "area"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "href")), "area href");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "area"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "shape")), "area shape");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "base"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "href")), "base href");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "img"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "src")), "img src");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "img"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "alt")), "img alt");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "embed"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "src")), "embed src");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "embed"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "name")), "embed name");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "iframe"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "src")), "iframe src");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "iframe"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "name")), "iframe name");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "src")), "frame");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "longdesc")), "frame");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "frame"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "name")), "frame name");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "script"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "src")), "script src");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "script"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "type")), "script type");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "applet"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "code")), "applet code");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "applet"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "width")), "applet width");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "form"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "action")), "form action");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "form"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "name")), "form name");
        assertTrue(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "object"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "data")), "object data");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "object"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "type")), "object type");
        assertFalse(adjuster.isTargetAttribute(
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "body"),
                QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "href")), "body");
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
