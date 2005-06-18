/*
 * Copyright (c) 2004 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 *
 * Created on 2005/03/19
 */
package org.seasar.maya.standard.engine.processor.jstl.core;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.engine.processor.ProcessorProperty;

public class ForEachProcessor extends ForLoopProcessor {
    private ProcessorProperty 	_expressionValue ;
	private ReadOnlyList 		_readOnlyList 	= null ;

    public int doStartProcess(PageContext context) {
        _readOnlyList = ForEachSupportUtil.toForEachList(
                _expressionValue.getValue(context));
        return super.doStartProcess(context);
    }
    
    protected int initEndParameter(PageContext context) {
        Integer endValue = getEndParameterValue(context);
        int end = 0 ;
        if( endValue != null ){
            end = endValue.intValue();
            if( end >= _readOnlyList.size() ){
                end = _readOnlyList.size() -1 ;
            }
        }else{
            end = _readOnlyList.size() - 1;
        }
        return end ;
    }
    
    protected void setCurrentObjectToVarValue(PageContext context) {
        setVarValue(context,_readOnlyList.get(getIndexValue()));
	}
    
    ////
    public void setExpressionValue(ProcessorProperty expressionValue) {
        _expressionValue = expressionValue;
    }
}

