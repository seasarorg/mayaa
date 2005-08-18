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
package org.seasar.maya.impl.provider.factory;

import org.seasar.maya.builder.library.scanner.SourceScanner;
import org.seasar.maya.impl.util.XmlUtil;
import org.seasar.maya.provider.Parameterizable;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LibrarySourceTagHandler extends AbstractParameterizableTagHandler {
    
    private LibraryTagHandler _parent;
    private SourceScanner _scanner;
    
    public LibrarySourceTagHandler(LibraryTagHandler parent) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }

    public void start(Attributes attributes) {
        _scanner = (SourceScanner)XmlUtil.getObjectValue(
                attributes, "class", null, SourceScanner.class);
        _parent.getLibraryManager().addSourceScanner(_scanner);
    }
    
    public void end(String body) {
        _scanner = null;
    }
    
    public Parameterizable getParameterizable() {
        if(_scanner == null) {
            throw new IllegalStateException();
        }
        return _scanner;
    }

}
