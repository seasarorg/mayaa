/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.impl.util.xml;

import java.util.Stack;

import org.seasar.maya.impl.util.StringUtil;
import org.xml.sax.Attributes;

/**
 * @author suga
 */
public class TagHandlerStack {

	private String _rootName;
    private TagHandler _rootHandler;
    private Stack _stack;

    public TagHandlerStack(String rootName, TagHandler rootHandler) {
    	if(StringUtil.isEmpty(rootName) || rootHandler == null) {
    		throw new IllegalArgumentException();
    	}
        _stack = new Stack();
        _rootName = rootName;
        _rootHandler = rootHandler; 
    }
    
    public void startElement(String name, Attributes attributes) {
    	if(StringUtil.isEmpty(name)) {
    		throw new IllegalArgumentException();
    	}
    	if(_stack.size() == 0) {
    		if(_rootName.equalsIgnoreCase(name)) {
    		    _rootHandler.start(attributes);
    	        _stack.push(_rootHandler);
    		} else {
    			throw new IllegalStateException();
    		}
    	} else {
	        TagHandler top = (TagHandler)_stack.peek();
	        TagHandler handler = top.startElement(name, attributes);
	        _stack.push(handler);
    	}
    }

    public void endElement() {
        if (_stack.size() > 0) {
            TagHandler top = (TagHandler) _stack.pop();
            if(top.isValid()) {
                top.endElement();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public void characters(char[] buffer, int offset, int length) {
        TagHandler top = (TagHandler)_stack.peek();
        top.characters(new String(buffer, offset, length).trim());
    }

    public TagHandler getRoot() {
        return _rootHandler;
    }

}
