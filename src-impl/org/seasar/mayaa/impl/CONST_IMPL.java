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
package org.seasar.mayaa.impl;

import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;
import org.seasar.mayaa.impl.engine.specification.URIImpl;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface CONST_IMPL {

    /**
     * IS_SECURE_WEBのキー。
     */
    String SECURE_WEB_KEY = "org.seasar.mayaa.secure.web";

    /** デフォルトのSpecificationをGlobalVariableに保持する際のキー */
    String DEFAULT_SPECIFICATION_KEY = Engine.class.getName() + "#default";

    String DEBUG = "org.seasar.mayaa.debug";

    String CHECK_TIMESTAMP = "checkTimestamp";
    String DEFAULT_SPECIFICATION = "defaultSpecification";
    String SUFFIX_SEPARATOR = "suffixSeparator";
    String WELCOME_FILE_NAME = "welcomeFileName";
    String MAYAA_EXTENSION = "mayaaExtension";
    String TEMPLATE_PATH_PATTERN = "templatePathPattern";
    String NOT_TEMPLATE_PATH_PATTERN = "notTemplatePathPattern";

    String TEMPLATE_DEFAULT_CHARSET = "UTF-8";
    String SCRIPT_DEFAULT_CHARSET = "UTF-8";

    String URI_MAYAA_STR = "http://mayaa.seasar.org";
    String URI_HTML_STR = "http://www.w3.org/TR/html4";
    String URI_XHTML_STR = "http://www.w3.org/1999/xhtml";
    String URI_XML_STR = "http://www.w3.org/XML/1998/namespace";


    URI URI_MAYAA = URIImpl.getInstance(URI_MAYAA_STR);
    URI URI_HTML = URIImpl.getInstance(URI_HTML_STR);
    URI URI_XHTML = URIImpl.getInstance(URI_XHTML_STR);
    URI URI_XML = URIImpl.getInstance(URI_XML_STR);

    String PUBLIC_FACTORY10 =
            "-//The Seasar Foundation//DTD Mayaa Factory 1.0//EN";
    String PUBLIC_MLD10 =
            "-//The Seasar Foundation//DTD Mayaa Library Definition 1.0//EN";
    String PUBLIC_PROVIDER10 =
            "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN";

    QName QH_ID = QNameImpl.getInstance(URI_HTML, "id");
    QName QX_ID = QNameImpl.getInstance(URI_XHTML, "id");
    /** Void elements in HTML */
    QName QH_AREA = QNameImpl.getInstance(URI_HTML, "area");
    QName QH_BASE = QNameImpl.getInstance(URI_HTML, "base");
    QName QH_BR = QNameImpl.getInstance(URI_HTML, "br");
    QName QH_COL = QNameImpl.getInstance(URI_HTML, "col");
    QName QH_COMMAND = QNameImpl.getInstance(URI_HTML, "command");
    QName QH_EMBED = QNameImpl.getInstance(URI_HTML, "embed");
    QName QH_HR = QNameImpl.getInstance(URI_HTML, "hr");
    QName QH_IMG = QNameImpl.getInstance(URI_HTML, "img");
    QName QH_INPUT = QNameImpl.getInstance(URI_HTML, "input");
    QName QH_KEYGEN = QNameImpl.getInstance(URI_HTML, "keygen");
    QName QH_LINK = QNameImpl.getInstance(URI_HTML, "link");
    QName QH_META = QNameImpl.getInstance(URI_HTML, "meta");
    QName QH_PARAM = QNameImpl.getInstance(URI_HTML, "param");
    QName QH_SOURCE = QNameImpl.getInstance(URI_HTML, "source");
    QName QH_TRACK = QNameImpl.getInstance(URI_HTML, "track");
    QName QH_WBR = QNameImpl.getInstance(URI_HTML, "wbr");
    QName QX_AREA = QNameImpl.getInstance(URI_XHTML, "area");
    QName QX_BASE = QNameImpl.getInstance(URI_XHTML, "base");
    QName QX_BR = QNameImpl.getInstance(URI_XHTML, "br");
    QName QX_COL = QNameImpl.getInstance(URI_XHTML, "col");
    QName QX_COMMAND = QNameImpl.getInstance(URI_XHTML, "command");
    QName QX_EMBED = QNameImpl.getInstance(URI_XHTML, "embed");
    QName QX_HR = QNameImpl.getInstance(URI_XHTML, "hr");
    QName QX_IMG = QNameImpl.getInstance(URI_XHTML, "img");
    QName QX_INPUT = QNameImpl.getInstance(URI_XHTML, "input");
    QName QX_KEYGEN = QNameImpl.getInstance(URI_XHTML, "keygen");
    QName QX_LINK = QNameImpl.getInstance(URI_XHTML, "link");
    QName QX_META = QNameImpl.getInstance(URI_XHTML, "meta");
    QName QX_PARAM = QNameImpl.getInstance(URI_XHTML, "param");
    QName QX_SOURCE = QNameImpl.getInstance(URI_XHTML, "source");
    QName QX_TRACK = QNameImpl.getInstance(URI_XHTML, "track");
    QName QX_WBR = QNameImpl.getInstance(URI_XHTML, "wbr");

    QName QX_BASEFONT = QNameImpl.getInstance(URI_XHTML, "basefont");  // transitional
    QName QX_ISINDEX = QNameImpl.getInstance(URI_XHTML, "isindex");    // transitional
    QName QX_FRAME = QNameImpl.getInstance(URI_XHTML, "frame");        // nonstandard
    QName QX_BGSOUND = QNameImpl.getInstance(URI_XHTML, "bgsound");    // nonstandard
    QName QX_NEXTID = QNameImpl.getInstance(URI_XHTML, "nextid");      // nonstandard
    QName QX_SOUND = QNameImpl.getInstance(URI_XHTML, "sound");        // nonstandard
    QName QX_SPACER = QNameImpl.getInstance(URI_XHTML, "spacer");      // nonstandard

    QName QM_CDATA = QNameImpl.getInstance("cdata");
    QName QM_CHARACTERS = QNameImpl.getInstance("characters");
    QName QM_COMMENT = QNameImpl.getInstance("comment");
    QName QM_DOCTYPE = QNameImpl.getInstance("doctype");
    QName QM_DUPLECATED = QNameImpl.getInstance("duplecatedElement");
    QName QM_MAYAA = QNameImpl.getInstance("mayaa");
    QName QM_PI = QNameImpl.getInstance("processingInstruction");
    QName QM_TEMPLATE_ELEMENT = QNameImpl.getInstance("templateElement");
    QName QM_LITERALS = QNameImpl.getInstance("literals");

    QName QM_AFTER_RENDER = QNameImpl.getInstance("afterRender");
    QName QM_BEFORE_RENDER = QNameImpl.getInstance("beforeRender");
    QName QM_AFTER_RENDER_PAGE = QNameImpl.getInstance("afterRenderPage");
    QName QM_BEFORE_RENDER_PAGE = QNameImpl.getInstance("beforeRenderPage");
    QName QM_AFTER_RENDER_COMPONENT = QNameImpl.getInstance("afterRenderComponent");
    QName QM_BEFORE_RENDER_COMPONENT = QNameImpl.getInstance("beforeRenderComponent");
    QName QM_AFTER_RENDER_PROCESSOR = QNameImpl.getInstance("afterRenderProcessor");
    QName QM_BEFORE_RENDER_PROCESSOR = QNameImpl.getInstance("beforeRenderProcessor");

    QName QM_CONTENT_TYPE = QNameImpl.getInstance("contentType");
    QName QM_ID = QNameImpl.getInstance("id");
    QName QM_IGNORE = QNameImpl.getInstance("ignore");
    QName QM_NAME = QNameImpl.getInstance("name");
    QName QM_NO_CACHE = QNameImpl.getInstance("noCache");
    QName QM_TEMPLATE_SUFFIX = QNameImpl.getInstance("templateSuffix");
    QName QM_EXTENDS = QNameImpl.getInstance("extends");
    QName QM_TEXT = QNameImpl.getInstance("text");
    QName QM_INJECT = QNameImpl.getInstance("inject");
    QName QM_CACHE_CONTROL = QNameImpl.getInstance("cacheControl");

    long NOFILE_DATE_MILLIS = 0;
    long NULL_DATE_MILLIS = 1;
}
