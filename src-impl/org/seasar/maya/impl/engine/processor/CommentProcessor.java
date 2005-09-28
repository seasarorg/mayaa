/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.maya.impl.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.processor.ProcessStatus;
import org.seasar.maya.impl.cycle.CycleUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CommentProcessor extends CharactersProcessor {
    
	private static final long serialVersionUID = -5176372123366627130L;

	public ProcessStatus doStartProcess(Page topLevelPage) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.getResponse().write("<!--");
        Object value = getText().getValue().execute(null);
        if(value != null) {
            cycle.getResponse().write(value.toString());
        }
        cycle.getResponse().write("-->");
        return ProcessStatus.SKIP_BODY;
    }

}
