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

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ExecProcessor extends TemplateProcessorSupport {

	private static final long serialVersionUID = -1413583265341468324L;
	private static final Object LOADED = new Object();

    private ProcessorProperty _exec;
    private ProcessorProperty _src;
    private CompiledScript _compiled;
    private ProcessorProperty _encoding;
    private ThreadLocal _loaded = new ThreadLocal();
    
    // MLD property, expectedType=java.lang.String
    public void setSrc(ProcessorProperty src) {
        _src = src;
    }
    
    // MLD property, expectedType=java.lang.String
    public void setEncoding(ProcessorProperty encoding) {
        _encoding = encoding;
    }

    // MLD property, expectedType=void
    public void setScript(ProcessorProperty script) {
        _exec = script;
    }
    
    public ProcessStatus doStartProcess() {
        if(_src != null) {
            if(_compiled == null) {
                ServiceProvider provider = ProviderFactory.getServiceProvider();
                String srcValue = (String)_src.getValue().execute();
                SourceDescriptor source = provider.getPageSourceDescriptor(srcValue);
                ScriptEnvironment environment = provider.getScriptEnvironment();
                String encValue = (String)_encoding.getValue().execute();
                _compiled = environment.compile(source, encValue);
                _compiled.setExpectedType(Void.class);
            }
            if(_loaded.get() == null) {
                _compiled.execute();
                _loaded.set(LOADED);
            }
        }
        if(_exec != null) {
            _exec.getValue().execute();
        }
        return EVAL_BODY_INCLUDE;
    }
    
}
