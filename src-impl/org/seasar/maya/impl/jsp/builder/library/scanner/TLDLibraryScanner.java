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
package org.seasar.maya.impl.jsp.builder.library.scanner;

import java.io.InputStream;
import java.util.Iterator;

import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.builder.library.scanner.LibraryScanner;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.jsp.builder.library.JspLibraryDefinition;
import org.seasar.maya.impl.jsp.builder.library.handler.TLDHandler;
import org.seasar.maya.impl.jsp.builder.library.handler.TaglibDirective;
import org.seasar.maya.impl.jsp.builder.library.handler.WebXMLHandler;
import org.seasar.maya.impl.source.MetaInfSourceDescriptor;
import org.seasar.maya.impl.util.FileUtil;
import org.seasar.maya.impl.util.XmlUtil;
import org.seasar.maya.provider.factory.ServiceProviderFactory;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * 暗黙的なTLDを読み込む。
 * <ul>
 * <li>web.xmlのtaglibディレクティブ</li>
 * <li>/WEB-INF以下の全サブディレクトリの.tld</li>
 * <li>/WEB-INF/lib以下の.jarファイル内の、META-INF/以下の.tld</li>
 * </ul>
 * @author suga
 */
public class TLDLibraryScanner implements LibraryScanner, CONST_IMPL {
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    protected JspLibraryDefinition parseTLD(InputStream stream, String systemID) {
        if(stream == null) {
            throw new IllegalArgumentException();
        }
        TLDHandler handler = new TLDHandler(systemID);
        // FIXME validation="true" だと、JSTLのc.tld/x.tldなどXSDを利用するものでSAX例外
        XmlUtil.parse(handler, stream, "tld", systemID, true, false, false);
        return handler.getLibraryDefinition();
    }
    
    /*
     * taglib-locationがjarファイルの場合、tldファイルは
     * META-INF/taglib.tldを指すものとする。
     */
    protected void processTaglib(LibraryManager manager, TaglibDirective taglib) {
        String location = taglib.getLocation();
        if(location == null) {
            return;
        }
        String protocol;
        if(location.startsWith("/")) {
            protocol = PREFIX_CONTEXT;
        } else {
            protocol = PREFIX_WEB_INF;
        }
        SourceFactory factory = ServiceProviderFactory.getServiceProvider().getSourceFactory();
        SourceDescriptor source = factory.createSourceDescriptor(protocol + location);
        if(source.exists()) {
            InputStream stream = source.getInputStream();
            if(location.startsWith("/WEB-INF/lib/") && 
                    location.toLowerCase().endsWith(".jar")) {
                stream = FileUtil.getJarInputStream(stream);
            }
            JspLibraryDefinition library = parseTLD(stream, source.getSystemID());
            manager.addLibraryDefinition(library);
            library.setLibraryManager(manager);
            library.setAssignedURI(taglib.getURI());
        }
    }

    protected TaglibDirective[] getTaglibsFromWebXml(InputStream webXml) {
        WebXMLHandler handler = new WebXMLHandler();
        XmlUtil.parse(handler, webXml, "web.xml", "/WEB-INF/web.xml", true, false, false);
        return handler.getTaglibs();
    }
    
    protected void scanWebXml(LibraryManager manager) {
        SourceFactory factory = ServiceProviderFactory.getServiceProvider().getSourceFactory();
        SourceDescriptor source = factory.createSourceDescriptor("web-inf:/web.xml");
        if(source.exists()) {
            InputStream stream = source.getInputStream();
            TaglibDirective[] taglibs = getTaglibsFromWebXml(stream);
            for (int i = 0; i < taglibs.length; i++) {
                processTaglib(manager, taglibs[i]);
            }
        }
    }

    protected void scanWebInfFolder(LibraryManager manager) {
        SourceFactory factory = ServiceProviderFactory.getServiceProvider().getSourceFactory();
        SourceDescriptor webInf = factory.createSourceDescriptor("web-inf:/");
        for(Iterator it = webInf.iterateChildren("tld"); it.hasNext(); ) {
            SourceDescriptor tld = (SourceDescriptor)it.next();
            InputStream stream = tld.getInputStream();
            JspLibraryDefinition library = parseTLD(stream, tld.getSystemID());
            manager.addLibraryDefinition(library);
            library.setLibraryManager(manager);
        }
    }
    
    protected void scanJars(LibraryManager manager) {
        if(manager == null) {
        	throw new IllegalArgumentException();
        }
        SourceFactory factory = ServiceProviderFactory.getServiceProvider().getSourceFactory();
    	SourceDescriptor metaInf = factory.createSourceDescriptor("meta-inf:/");
        for(Iterator it = metaInf.iterateChildren("tld"); it.hasNext(); ) {
            SourceDescriptor tld = (SourceDescriptor)it.next();
            InputStream stream = tld.getInputStream();
            String systemID = tld.getSystemID();
            JspLibraryDefinition library = parseTLD(stream, systemID);
            if(tld instanceof MetaInfSourceDescriptor) {
                MetaInfSourceDescriptor jar = (MetaInfSourceDescriptor)tld;
	            if (jar.getJarEntryName().equals("META-INF/taglib.tld")) {
	                String uri = jar.getJarFileName();
	                library.setAssignedURI(uri);
	            }
            }
            manager.addLibraryDefinition(library);
            library.setLibraryManager(manager);
        }
    }
    
    /*
     * WEB-INF/lib下のjarファイル、WEB-INF下のtldファイル、web.xmlを読み込む。
     */
    public synchronized void scanLibrary(LibraryManager manager) {
        if(manager == null) {
        	throw new IllegalArgumentException();
        }
        scanWebXml(manager);
        scanWebInfFolder(manager);
        scanJars(manager);
    }

}
