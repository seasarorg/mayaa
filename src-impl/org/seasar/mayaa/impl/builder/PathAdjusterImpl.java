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
package org.seasar.mayaa.impl.builder;

import org.seasar.mayaa.builder.PathAdjuster;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class PathAdjusterImpl
        extends ParameterAwareImpl
        implements PathAdjuster, CONST_IMPL {

    private String[][] _adjustTarget;

    private boolean _enabled = true;

    public PathAdjusterImpl() {
        this(new String[][] {
            { "a", "href" },
            { "link", "href" },
            { "area", "href" },
            { "base", "href" },
            { "img", "src" },
            { "embed", "src" },
            { "iframe", "src" },
            { "frame", "src" },
            { "frame", "longdesc" },
            { "script", "src" },
            { "applet", "code" },
            { "form", "action" },
            { "object", "data" }
        });
    }

    public PathAdjusterImpl(String[][] adjustTarget) {
        _adjustTarget = adjustTarget;
    }

    public void setParameter(String name, String value) {
        if ("enabled".equals(name)) {
            _enabled = Boolean.getBoolean(value);
        }
        super.setParameter(name, value);
    }

    public boolean isTargetNode(QName nodeName) {
        if (_enabled == false) {
            return false;
        }

        String uri = nodeName.getNamespaceURI();
        if (URI_HTML.equals(uri) || URI_XHTML.equals(uri)) {
            String local = nodeName.getLocalName().toLowerCase();
            for (int i = 0; i < _adjustTarget.length; i++) {
                if (_adjustTarget[i][0].equals(local)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isTargetAttribute(QName nodeName, QName attributeName) {
        if (_enabled == false) {
            return false;
        }

        String nodeLocal = nodeName.getLocalName().toLowerCase();
        String attributeLocal = attributeName.getLocalName().toLowerCase();
        for (int i = 0; i < _adjustTarget.length; i++) {
            if (_adjustTarget[i][0].equals(nodeLocal)
                    && _adjustTarget[i][1].equals(attributeLocal)) {
                return true;
            }
        }
        return false;
    }

    public String adjustRelativePath(String base, String path) {
        return StringUtil.adjustRelativePath(base, path);
    }

}
