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
package org.seasar.maya.impl.source;

import java.io.File;
import java.util.Iterator;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.source.SourceScanner;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class ServletSourceScanner implements SourceScanner {

	private Application _application;
    private String _protocol;
    private String _root;
	
	public Iterator scan() {
		return null;
	}

    private class FileToSourceIterator implements Iterator {
        
        private Iterator _iterator;
        
        private FileToSourceIterator(Iterator iterator) {
            if(iterator == null) {
                throw new IllegalArgumentException();
            }
            _iterator = iterator;
        }
        
        public boolean hasNext() {
            return _iterator.hasNext();
        }

        private String getSystemID(File file) {
            String root = _application.getRealPath(_root);
            String absolutePath = file.getAbsolutePath();
            return absolutePath.substring(root.length());
        }
        
        public Object next() {
            Object ret = _iterator.next();
            if(ret instanceof File) {
                File file = (File)ret;
                String systemID = getSystemID(file);
                return new ServletSourceDescriptor(_application, _protocol, _root, systemID, file);
            }
            throw new IllegalStateException();
        }

        public void remove() {
            _iterator.remove();
        }

    }

}
