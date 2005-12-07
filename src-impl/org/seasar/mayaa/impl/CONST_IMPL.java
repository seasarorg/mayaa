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
package org.seasar.mayaa.impl;

import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface CONST_IMPL {

    String CHECK_TIMESTAMP = "checkTimestamp";
    String SUFFIX_SEPARATOR = "suffixSeparator";
    String WELCOME_FILE_NAME = "welcomeFileName";
    String TEMPLATE_PATH_PATTERN = "templatePathPattern";
    String NOT_TEMPLATE_PATH_PATTERN = "notTemplatePathPattern";

    String URI_MAYA = "http://mayaa.seasar.org";
    String URI_HTML = "http://www.w3.org/TR/html4";
    String URI_XHTML = "http://www.w3.org/1999/xhtml";

    String PUBLIC_FACTORY10 = 
        "-//The Seasar Foundation//DTD Mayaa Factory 1.0//EN";
    String PUBLIC_MLD10 = 
        "-//The Seasar Foundation//DTD Mayaa Library Definition 1.0//EN";
    String PUBLIC_PROVIDER10 = 
        "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN";
    
    QName QH_ID = new QNameImpl(URI_HTML, "id");
    QName QX_ID = new QNameImpl(URI_XHTML, "id");

    QName QM_CDATA = new QNameImpl("cdata");
    QName QM_CHARACTERS = new QNameImpl("characters");
    QName QM_COMMENT = new QNameImpl("comment");
    QName QM_DOCTYPE = new QNameImpl("doctype");
    QName QM_DUPLECATED = new QNameImpl("duplecatedElement");
    QName QM_MAYA = new QNameImpl("mayaa");
    QName QM_PI = new QNameImpl("processingInstruction");
    QName QM_TEMPLATE_ELEMENT = new QNameImpl("templateElement");

    QName QM_AFTER_RENDER = new QNameImpl("afterRender");
	QName QM_BEFORE_RENDER = new QNameImpl("beforeRender");
    QName QM_CONTENT_TYPE = new QNameImpl("contentType");
    QName QM_ID = new QNameImpl("id");
    QName QM_IGNORE = new QNameImpl("ignore");
    QName QM_NAME = new QNameImpl("name");
    QName QM_NO_CACHE = new QNameImpl("noCache");
    QName QM_TEMPLATE_SUFFIX = new QNameImpl("templateSuffix");
    QName QM_TEXT = new QNameImpl("text");    
    
}
