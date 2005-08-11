/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.builder.parser;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.cyberneko.html.filters.DefaultFilter;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AdditionalHandlerFilter extends DefaultFilter {

    private static final String[] _recognizedProps = new String[] {
        AdditionalHandler.ADDITIONAL_HANDLER
    };
    
    private AdditionalHandler _handler;
    
    public String[] getRecognizedProperties() {
        return _recognizedProps;
    }

    public void setProperty(String propertyId, Object value) throws XMLConfigurationException {
        if(AdditionalHandler.ADDITIONAL_HANDLER.equals(propertyId)) {
            if(value instanceof AdditionalHandler == false) {
                throw new IllegalArgumentException();
            }
            _handler = (AdditionalHandler)value;
        }
    }

    public void xmlDecl(String version, String encoding, String standalone, 
            Augmentations augs) throws XNIException {
        if(_handler != null) {
            _handler.xmlDecl(version, encoding, standalone);
            return;
        }
        super.xmlDecl(version, encoding, standalone, augs);
    }
    
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs) {
        if(_handler != null && "%".equals(element.localpart)) {
            _handler.startCodelet();
            return;
        }
        super.startElement(element, attributes, augs);
    }

    public void endElement(QName element, Augmentations augs) {
        if(_handler != null && "%".equals(element.localpart)) {
            _handler.endCodelet();
            return;
        }
        super.endElement(element, augs);
    }
    
}
