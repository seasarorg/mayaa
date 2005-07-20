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
package org.seasar.maya.standard.util;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.VariableInfo;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.ChildEvaluationProcessor;
import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.processor.TemplateProcessor.ProcessStatus;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspUtil {
    
	private JspUtil() {
	}

    public static ProcessStatus getProcessStatus(int status, boolean doStart) {
        if(status == Tag.EVAL_BODY_INCLUDE) {
            return TemplateProcessor.EVAL_BODY_INCLUDE;
        } else if(status == Tag.SKIP_BODY) {
            return TemplateProcessor.SKIP_BODY;
        } else if(status == Tag.EVAL_PAGE) {
            return TemplateProcessor.EVAL_PAGE;
        } else if(status == Tag.SKIP_PAGE) {
            return TemplateProcessor.SKIP_PAGE;
        } else if(!doStart && status == IterationTag.EVAL_BODY_AGAIN) {
        	return IterationProcessor.EVAL_BODY_AGAIN;
        } else if(doStart && status == BodyTag.EVAL_BODY_BUFFERED) {
        	return ChildEvaluationProcessor.EVAL_BODY_BUFFERED;
        }
        throw new IllegalArgumentException();
    }
    
    public static String getScopeFromInt(int scope) {
        if(scope == PageContext.APPLICATION_SCOPE) {
            return ServiceCycle.SCOPE_APPLICATION;
        } else if(scope == PageContext.SESSION_SCOPE) {
            return ServiceCycle.SCOPE_SESSION;
        } else if(scope == PageContext.REQUEST_SCOPE) {
            return ServiceCycle.SCOPE_REQUEST;
        } else if(scope == PageContext.PAGE_SCOPE) {
            return ServiceCycle.SCOPE_PAGE;
        }
        throw new IllegalArgumentException();
    }
	
    public static int getVariableScopeFromString(String scope) {
        if("AT_BEGIN".equalsIgnoreCase(scope)) {
            return VariableInfo.AT_BEGIN;
        } else if("AT_END".equalsIgnoreCase(scope)) {
            return VariableInfo.AT_END;
        } if("NESTED".equalsIgnoreCase(scope)) {
            return VariableInfo.NESTED;
        }
        throw new IllegalArgumentException();
    }
    
}
