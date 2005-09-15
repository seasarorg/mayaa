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

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DoRenderProcessor extends TemplateProcessorSupport {

	private static final long serialVersionUID = 4309532215454978747L;

    private boolean _rendered = false;
    private String _name = "";
    private ThreadLocal _insertProcs = new ThreadLocal();
    
    // MLD property, default=false
    public void setRendered(boolean rendered) {
        _rendered = rendered;
    }
    
    public boolean isRendered() {
        return _rendered;
    }
    
    // MLD property, default=""
    public void setName(String name) {
        if(name == null) {
            _name = name;
        }
        _name = name;
    }
    
    public String getName() {
        return _name;
    }

    public void setInsertProcessor(InsertProcessor proc) {
        _insertProcs.set(proc);
    }
    
    public InsertProcessor getInsertProcessor() {
        InsertProcessor proc = (InsertProcessor)_insertProcs.get();
        if(proc == null) {
            throw new IllegalStateException();
        }
        return proc;
    }
    
}
