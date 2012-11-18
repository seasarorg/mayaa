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

import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;
import org.seasar.mayaa.impl.engine.specification.URIImpl;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface CONST_IMPL {

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

    URI URI_MAYAA = URIImpl.getInstance("http://mayaa.seasar.org");
    URI URI_HTML = URIImpl.getInstance("http://www.w3.org/TR/html4");
    URI URI_XHTML = URIImpl.getInstance("http://www.w3.org/1999/xhtml");
    URI URI_XML = URIImpl.getInstance("http://www.w3.org/XML/1998/namespace");

    String PUBLIC_FACTORY10 =
            "-//The Seasar Foundation//DTD Mayaa Factory 1.0//EN";
    String PUBLIC_MLD10 =
            "-//The Seasar Foundation//DTD Mayaa Library Definition 1.0//EN";
    String PUBLIC_PROVIDER10 =
            "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN";

    QName QH_ID = QNameImpl.getInstance(URI_HTML, "id");
    QName QX_ID = QNameImpl.getInstance(URI_XHTML, "id");

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
