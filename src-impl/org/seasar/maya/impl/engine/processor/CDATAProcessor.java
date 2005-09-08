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
import org.seasar.maya.impl.cycle.AbstractServiceCycle;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CDATAProcessor extends TemplateProcessorSupport {
    
	private static final long serialVersionUID = -4267623139201513906L;

	public ProcessStatus doStartProcess() {
        ServiceCycle cycle = AbstractServiceCycle.getServiceCycle();
        cycle.getResponse().write("<![CDATA[");
        return EVAL_BODY_INCLUDE;
    }

    public ProcessStatus doEndProcess() {
        ServiceCycle cycle = AbstractServiceCycle.getServiceCycle();
        cycle.getResponse().write("]]>");
        return EVAL_PAGE;
    }
    
}
