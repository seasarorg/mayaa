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

import org.seasar.maya.builder.TemplateBuilder;
import org.seasar.maya.builder.injection.InjectionResolver;
import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.injection.CompositeInjectionResolver;
import org.seasar.maya.impl.builder.parser.AdditionalHandler;
import org.seasar.maya.impl.builder.parser.TemplateParser;
import org.seasar.maya.impl.builder.parser.TemplateScanner;
import org.seasar.maya.source.SourceDescriptor;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateBuilderImpl extends SpecificationBuilderImpl
		implements TemplateBuilder, CONST_IMPL {

	private static final long serialVersionUID = 4578697145887676787L;

	private TemplateProcessorInjecter _injecter;
    private CompositeInjectionResolver _injectionResolver = new CompositeInjectionResolver();
    private LibraryManager _libraryManager; 
    
    public void addInjectionResolver(InjectionResolver resolver) {
    	if(resolver == null) {
    		throw new IllegalArgumentException();
    	}
        _injectionResolver.add(resolver);
    }
    
    public InjectionResolver getInjectionResolver() {
        return _injectionResolver;
    }

    public void setLibraryManager(LibraryManager libraryManager) {
        if(libraryManager == null) {
            throw new IllegalArgumentException();
        }
        _libraryManager = libraryManager;
    }
    
    public boolean hasLibraryManager() {
        return _libraryManager != null;
    }
    
    public LibraryManager getLibraryManager() {
        if(_libraryManager == null) {
            throw new IllegalStateException();
        }
        return _libraryManager;
    }

    private TemplateProcessorInjecter getInjecter() {
        if(_injecter == null) {
            _injecter = new TemplateProcessorInjecter(
                    getInjectionResolver(), getLibraryManager());
        }
        return _injecter;
    }
    
    protected XMLReader createXMLReader() {
        return new TemplateParser(new TemplateScanner());
    }

    protected String getPublicID() {
        return URI_MAYA + "/template";
    }
    
    protected void setContentHander(XMLReader xmlReader, ContentHandler handler) {
        super.setContentHander(xmlReader, handler);
        if(handler instanceof AdditionalHandler) {
            try {
                xmlReader.setProperty(AdditionalHandler.ADDITIONAL_HANDLER, handler);
            } catch(SAXException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void build(Specification specification) {
        if((specification instanceof Template) == false) {
            throw new IllegalArgumentException();
        }
        SourceDescriptor source = specification.getSource();
        if(source.exists()) {
	        super.build(specification);
	        try {
	            getInjecter().resolveTemplate((Template)specification);
	        } catch(Throwable t) {
	            specification.kill();
				if(t instanceof RuntimeException) {
				    throw (RuntimeException)t;
				}
				throw new RuntimeException(t);
	        }
        }
    }
    
}
