/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.builder;

import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.xml.XmlReaderPool;
import org.seasar.maya.source.SourceDescriptor;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationBuilderImpl 
		implements SpecificationBuilder, CONST_IMPL {

	private static final long serialVersionUID = 7852577574830768959L;

    protected XmlReaderPool getXmlReaderPool(	String systemID) {
        return XmlReaderPool.getPool();
    }

    protected ContentHandler createContentHandler(
    		Specification specification) {
        return new SpecificationNodeHandler(specification);
    }

    protected String getPublicID() {
        return URI_MAYA + "/specification";
    }

    protected void afterBuild(Specification specification) {
    }
    
    public void build(Specification specification) {
        if(specification == null) {
            throw new IllegalArgumentException();
        }
        SourceDescriptor source = specification.getSource();
        if(source.exists()) {
            ContentHandler handler = createContentHandler(specification);
            XmlReaderPool pool = getXmlReaderPool(source.getSystemID());
            XMLReader xmlReader = 
            	pool.borrowXMLReader(handler, true, false, false);
            InputSource input = new InputSource(source.getInputStream());
            input.setPublicId(getPublicID());
            input.setSystemId(source.getSystemID());
            try {
                xmlReader.parse(input);
                afterBuild(specification);
            } catch(Throwable t) {
				specification.kill();
				if(t instanceof RuntimeException) {
				    throw (RuntimeException)t;
				}
				throw new RuntimeException(t);
            } finally {
                pool.returnXMLReader(xmlReader);
            }
        }
    }
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(getClass(), name);
    }

}
