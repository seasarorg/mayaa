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
package org.seasar.maya.impl.builder.library.scanner;

import java.io.InputStream;
import java.util.Iterator;

import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.builder.library.scanner.LibraryScanner;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.library.LibraryDefinitionImpl;
import org.seasar.maya.impl.builder.library.handler.MLDHandler;
import org.seasar.maya.impl.source.MetaInfSourceDescriptor;
import org.seasar.maya.impl.util.XmlUtil;
import org.seasar.maya.provider.factory.ServiceProviderFactory;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MLDLibraryScanner implements LibraryScanner, CONST_IMPL {

    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    protected LibraryDefinitionImpl parseMLD(InputStream stream, String systemID) {
        if(stream == null) {
            throw new IllegalArgumentException();
        }
        MLDHandler handler = new MLDHandler();
        XmlUtil.parse(handler, stream, PUBLIC_MLD10, systemID, true, true, false);
        return handler.getLibraryDefinition();
    }

    /**
     * WEB-INF下のtldファイルを読み込む。
     */
    protected void scanWebInfFolder(LibraryManager manager) {
        if(manager == null) {
        	throw new IllegalArgumentException();
        }
        SourceFactory factory = ServiceProviderFactory.getServiceProvider().getSourceFactory();
        SourceDescriptor webInf = factory.createSourceDescriptor("web-inf:/");
        for(Iterator it = webInf.iterateChildren("mld"); it.hasNext(); ) {
            SourceDescriptor mld = (SourceDescriptor)it.next();
            InputStream stream = mld.getInputStream();
            LibraryDefinitionImpl library = parseMLD(stream, mld.getSystemID());
            manager.addLibraryDefinition(library);
            library.setLibraryManager(manager);
        }
    }

    protected void scanJars(LibraryManager manager) {
        if(manager == null) {
        	throw new IllegalArgumentException();
        }
    	SourceFactory factory = 
    	    ServiceProviderFactory.getServiceProvider().getSourceFactory(); 
    	SourceDescriptor metaInf = factory.createSourceDescriptor("meta-inf:/");
    	for(Iterator it = metaInf.iterateChildren("mld"); it.hasNext(); ) {
    	    MetaInfSourceDescriptor source = (MetaInfSourceDescriptor)it.next();
            String jarFileName = source.getJarFileName();
            String jarEntryName = source.getJarEntryName();
            InputStream stream = source.getInputStream();
            String systemID = jarFileName + "/" + jarEntryName;
		    LibraryDefinitionImpl library = parseMLD(stream, systemID);
		    manager.addLibraryDefinition(library);
            library.setLibraryManager(manager);
    	}
    }
    
    public synchronized void scanLibrary(LibraryManager manager) {
        if(manager == null) {
        	throw new IllegalArgumentException();
        }
        scanWebInfFolder(manager);
        scanJars(manager);
    }
    
}
