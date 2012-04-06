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
package org.seasar.mayaa.impl.builder.parser;

import org.apache.xerces.xni.Augmentations;
import org.cyberneko.html.filters.DefaultFilter;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AdditionalHandlerFilter extends DefaultFilter {

    private static final String[] RECOGNIZED_PROPS = new String[] {
        AdditionalHandler.ADDITIONAL_HANDLER
    };

    private AdditionalHandler _handler;

    public String[] getRecognizedProperties() {
        return RECOGNIZED_PROPS;
    }

    public void setProperty(String propertyId, Object value) {
        if (AdditionalHandler.ADDITIONAL_HANDLER.equals(propertyId)) {
            if (value instanceof AdditionalHandler == false) {
                throw new IllegalArgumentException();
            }
            _handler = (AdditionalHandler) value;
        }
    }

    public void xmlDecl(String version,
            String encoding, String standalone, Augmentations augs) {
        if (_handler != null) {
            _handler.xmlDecl(version, encoding, standalone);
            return;
        }
        super.xmlDecl(version, encoding, standalone, augs);
    }

}
