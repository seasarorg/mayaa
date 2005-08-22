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

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.cycle.script.ScriptCompiler;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.source.PageSourceDescriptor;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.ScriptUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptletProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -3442033812529712223L;
    private static final Object LOADED = new Object();

    private ProcessorProperty _exec;
    private String _src;
    private CompiledScript _script;
    private String _encoding;
    private ThreadLocal _loaded = new ThreadLocal();
    
    // MLD property
    public void setSrc(String src) {
        _src = src;
    }
    
    // MLD property
    public void setEncoding(String encoding) {
        _encoding = encoding;
    }

    // MLD property, expectedType=java.lang.String
    public void setExec(ProcessorProperty exec) {
        _exec = exec;
    }
    
    public ProcessStatus doStartProcess() {
        if(StringUtil.hasValue(_src)) {
            if(_script == null) {
                PageSourceDescriptor source = new PageSourceDescriptor(_src);
                ServiceProvider provider = ProviderFactory.getServiceProvider();
                ScriptCompiler compiler = provider.getScriptCompiler();
                _script = compiler.compile(source, _encoding, Void.class);
            }
            if(_loaded.get() == null) {
                ScriptUtil.execute(_script);
                _loaded.set(LOADED);
            }
        }
        if(_exec != null) {
            String ret = (String)_exec.getValue();
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(ret);
        }
        return SKIP_BODY;
    }
    
}
