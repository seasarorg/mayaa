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
package org.seasar.maya.engine.processor;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Template;

/**
 * TemplateProcessorの基本実装。直接用いるのではなく、継承して
 * 具体的な機能が盛り込まれることが想定されている。継承クラスでは、
 * doStartProcess()およびdoEndProcess()を実装することになる。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateProcessorSupport implements TemplateProcessor {

	private static final long serialVersionUID = -3521980479718620027L;

	private TemplateProcessor _parent;
    private int _index;
    private List _children = new ArrayList();

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

    /**
     * 所属テンプレートの取得を行う。
     * @return 所属テンプレート。
     */
    public Template getTemplate() {
        for(TemplateProcessor current = this;
                current != null; current = current.getParentProcessor()) {
            if(current instanceof Template &&
                    current.getParentProcessor() == null) {
                return (Template)current;
            }
        }
        throw new IllegalStateException("getTemplate() is null");
    }

    public TemplateProcessor getParentProcessor() {
        return _parent;
    }

    public int getIndex() {
        return _index;
    }

    public int doEndProcess(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        return Tag.EVAL_PAGE;
    }

    public int doStartProcess(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        return Tag.EVAL_BODY_INCLUDE;
    }

    public int getChildProcessorSize() {
        synchronized(_children) {
            return _children.size();
        }
    }

    public TemplateProcessor getChildProcessor(int index) {
        return (TemplateProcessor)_children.get(index);
    }

}
