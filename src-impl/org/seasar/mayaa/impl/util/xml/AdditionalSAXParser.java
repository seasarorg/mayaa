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
package org.seasar.mayaa.impl.util.xml;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.xni.Augmentations;
import org.seasar.mayaa.impl.builder.parser.AdditionalHandlerFilter;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class AdditionalSAXParser extends SAXParser {

    private AdditionalHandlerFilter _filter;

    public AdditionalSAXParser() {
        super();

        _filter = new AdditionalHandlerFilter();
        fConfiguration.addRecognizedProperties(
                _filter.getRecognizedProperties()
        );
    }

    public void setProperty(String propertyId, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        super.setProperty(propertyId, value);

        _filter.setProperty(propertyId, value);
    }

    public void xmlDecl(
            String version, String encoding, String standalone,
            Augmentations augs) {
        super.xmlDecl(version, encoding, standalone, augs);

        _filter.xmlDecl(version, encoding, standalone, augs);
    }

}
