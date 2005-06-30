/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.standard.engine.processor.jstl.xml;

import java.io.IOException;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.standard.alert.AlertThrowerFactory;
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author maruo_syunsuke
 */
public class ParseProcessor extends TemplateProcessorSupport {

    private String 	_resourceXml;
    private String 	_var ;
    private int 	_scope;

    public void setXml(String resourceXml) {
        _resourceXml = resourceXml ;
    }

    public int doStartProcess(PageContext context) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(_resourceXml);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("XML parse exception",e);
        } catch (SAXException e) {
            throw new RuntimeException("XML parse exception",e);
        } catch (IOException e) {
            throw new RuntimeException("XML parse exception",e);
        }
        AttributeValue var = AttributeValueFactory.create(_var,_scope);
        var.setValue(context,doc);
        return Tag.EVAL_PAGE;
    }
}
