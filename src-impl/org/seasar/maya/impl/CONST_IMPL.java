/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl;

import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.impl.engine.specification.QNameImpl;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface CONST_IMPL {

    String CHECK_TIMESTAMP = "checkTimestamp";
    String SUFFIX_SEPARATOR = "suffixSeparator";
    String WELCOME_FILE_NAME = "welcomeFileName";
    
    String URI_MAYA = "http://maya.seasar.org";
    String URI_HTML = "http://www.w3.org/TR/html4";
    String URI_XHTML = "http://www.w3.org/1999/xhtml";

    String ATTR_ID = "id";
    String ATTR_INJECT = "inject";

    String PUBLIC_MLD10 = "-//The Seasar Foundation//DTD Maya Library Definition 1.0//EN";
    String PUBLIC_PROVIDER10 = "-//The Seasar Foundation//DTD Maya Provider 1.0//EN";
    
    QName QH_CONTENT = new QNameImpl(URI_HTML, "content");
    QName QH_HTML = new QNameImpl(URI_HTML, "html");
    QName QH_HTTP_EQUIV = new QNameImpl(URI_HTML, "http-equiv");    
    QName QH_ID = new QNameImpl(URI_HTML, ATTR_ID);
    QName QH_META = new QNameImpl(URI_HTML, "meta");
    
    QName QX_CONTENT = new QNameImpl(URI_XHTML, "content");
    QName QX_HTML = new QNameImpl(URI_XHTML, "html");
    QName QX_HTTP_EQUIV = new QNameImpl(URI_XHTML, "http-equiv");    
    QName QX_ID = new QNameImpl(URI_XHTML, ATTR_ID);
    QName QX_META = new QNameImpl(URI_XHTML, "meta");

    QName QM_ATTRIBUTE = new QNameImpl("attribute");
    QName QM_CDATA = new QNameImpl("cdata");
    QName QM_CHARACTERS = new QNameImpl("characters");
    QName QM_COMMENT = new QNameImpl("comment");
    QName QM_DOCTYPE = new QNameImpl("doctype");
    QName QM_DUPLECATED_ELEMENT = new QNameImpl("duplecatedElement");
    QName QM_ELEMENT = new QNameImpl("element");
    QName QM_ENGINE = new QNameImpl("engine");
    QName QM_INSERT = new QNameImpl("insert");
    QName QM_MAYA = new QNameImpl("maya");
    QName QM_NULL = new QNameImpl("null");
    QName QM_PROCESSING_INSTRUCTION = new QNameImpl("processingInstruction");
    QName QM_TEMPLATE = new QNameImpl("template");
    QName QM_TEMPLATE_ELEMENT = new QNameImpl("templateElement");

    QName QM_AFTER_RENDER = new QNameImpl("afterRender");
	QName QM_BEFORE_RENDER = new QNameImpl("beforeRender");
    QName QM_CONTENT_TYPE = new QNameImpl("contentType");
    QName QM_DATA = new QNameImpl("data");
    QName QM_EXTENDS = new QNameImpl("extends");
    QName QM_ID = new QNameImpl("id");
    QName QM_IGNORE = new QNameImpl("ignore");
    QName QM_INJECT = new QNameImpl(ATTR_INJECT);
    QName QM_NAME = new QNameImpl("name");
    QName QM_PATH = new QNameImpl("path");
    QName QM_PUBLIC_ID = new QNameImpl("publicID");
    QName QM_RENDERED = new QNameImpl("rendered");
    QName QM_SYSTEM_ID = new QNameImpl("systemID");
    QName QM_TARGET = new QNameImpl("target");
    QName QM_TEMPLATE_SUFFIX = new QNameImpl("templateSuffix");
    QName QM_TEXT = new QNameImpl("text");    
    QName QM_XPATH = new QNameImpl("xpath");
    
}
