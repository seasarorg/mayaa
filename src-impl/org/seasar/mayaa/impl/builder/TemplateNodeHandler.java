/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.xml.sax.SAXParseException;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TemplateNodeHandler extends SpecificationNodeHandler {

    private static final Log LOG = LogFactory.getLog(TemplateNodeHandler.class);

    private boolean _outputTemplateWhitespace = true;

    public TemplateNodeHandler(Specification specification) {
        super(specification);
    }

    public void setOutputTemplateWhitespace(boolean outputTemplateWhitespace) {
        _outputTemplateWhitespace = outputTemplateWhitespace;
    }

    protected SpecificationNode createChildNode(
            QName qName, String systemID, int lineNumber, int sequenceID) {
        return SpecificationUtil.createSpecificationNode(
                qName, systemID, lineNumber, true, sequenceID);
    }

    protected boolean isRemoveWhitespace() {
        return _outputTemplateWhitespace == false;
    }

    protected String removeIgnorableWhitespace(String characters) {
        StringBuffer buffer = new StringBuffer(characters.length());
        String[] line = characters.split("\n");
        for (int i = 0; i < line.length; i++) {
            if (line[i].trim().length() > 0) {
                String token = line[i].replaceAll("^[ \t]+", "");
                token = token.replaceAll("[ \t]+$", "");
                buffer.append(token.replaceAll("[ \t]+", " "));
                buffer.append("\n");
            } else if (i == 0) {
                buffer.append("\n");
            }
        }
        return buffer.toString();
    }

    protected void processEntity(String name) {
        String entityRef = "&" + name + ";";
        appendCharactersBuffer(entityRef);
    }

    public void comment(char[] buffer, int start, int length) {
        addCharactersNode();
        String comment = new String(buffer, start, length);
        SpecificationNode node = addNode(QM_COMMENT);
        node.addAttribute(QM_TEXT, comment);
    }

    public void warning(SAXParseException e) {
        if (LOG.isWarnEnabled()) {
            LOG.warn(e);
        }
    }

    public void fatalError(SAXParseException e) {
        if (LOG.isFatalEnabled()) {
            LOG.fatal(e);
        }
        throw new RuntimeException(e);
    }

    public void error(SAXParseException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error(e);
        }
        throw new RuntimeException(e);
    }

}
