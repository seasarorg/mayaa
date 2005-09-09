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
package org.seasar.maya.impl.engine.processor;

import java.util.ArrayList;
import java.util.List;

import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.SpecificationNode;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateProcessorSupport implements TemplateProcessor {

	private static final long serialVersionUID = -3521980479718620027L;

	private TemplateProcessor _parent;
    private int _index;
    private List _children = new ArrayList();
    private SpecificationNode _originalNode;
    private SpecificationNode _injectedNode;
    private boolean _rendered = true;
    
    // MLD property, defaultValue=true 
    public void setRendered(boolean rendered) {
        _rendered = rendered;
    }
    
    public void setParentProcessor(TemplateProcessor parent, int index) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        _index = index;
    }

    public void addChildProcessor(TemplateProcessor child) {
        if(child == null) {
            throw new IllegalArgumentException();
        }
        _children.add(child);
        child.setParentProcessor(this, _children.size() - 1);
    }

    public Template getTemplate() {
        for(TemplateProcessor current = this;
                current != null; current = current.getParentProcessor()) {
            if(current instanceof Template &&
                    current.getParentProcessor() == null) {
                return (Template)current;
            }
        }
        throw new IllegalStateException();
    }

    public TemplateProcessor getParentProcessor() {
        return _parent;
    }

    public int getIndex() {
        return _index;
    }

    public ProcessStatus doEndProcess() {
        return EVAL_PAGE;
    }

    public ProcessStatus doStartProcess() {
        if(_rendered) {
            return EVAL_BODY_INCLUDE;
        }
        return SKIP_BODY;
    }

    public int getChildProcessorSize() {
        synchronized(_children) {
            return _children.size();
        }
    }

    public TemplateProcessor getChildProcessor(int index) {
        return (TemplateProcessor)_children.get(index);
    }

    public void setOriginalNode(SpecificationNode node) {
        if(node == null) {
            throw new IllegalArgumentException();
        }
        _originalNode = node;
    }

    public SpecificationNode getOriginalNode() {
        return _originalNode;
    }

    public void setInjectedNode(SpecificationNode node) {
        if(node == null) {
            throw new IllegalArgumentException();
        }
        _injectedNode = node;
    }

    public SpecificationNode getInjectedNode() {
        return _injectedNode;
    }

}
