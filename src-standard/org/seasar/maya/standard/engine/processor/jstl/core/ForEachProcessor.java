/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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
 */
package org.seasar.maya.standard.engine.processor.jstl.core;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.ProcessorProperty;

public class ForEachProcessor extends ForLoopProcessor {

    private static final long serialVersionUID = 5392396535368214943L;

    private static final String LOCAL_LIST = "LOCAL_LIST" ;
    private ProcessorProperty 	_expressionValue ;

    public int doStartProcess(ServiceCycle context) {
        setReadOnlyList(context,initReadOnlyList(context));
        return super.doStartProcess(context);
    }
    
    protected ReadOnlyList initReadOnlyList(ServiceCycle context) {
        return ForEachSupportUtil.toForEachList(
                _expressionValue.getValue(context));
    }
    
    protected ReadOnlyList getReadOnlyList(ServiceCycle context){
        ReadOnlyList varName = (ReadOnlyList)ProcessorLocalValueUtil.getObject(context,this,LOCAL_LIST);
        return varName;
    }
    
    protected void setReadOnlyList(ServiceCycle context,ReadOnlyList value){
        ProcessorLocalValueUtil.setObject(context,this,LOCAL_LIST,value);
    }
    
    protected int initEndParameter(ServiceCycle context) {
        Integer endValue = getEndParameterValue(context);
        int end = 0 ;
        if( endValue != null ){
            end = endValue.intValue();
            if( end >= getReadOnlyList(context).size() ){
                end = getReadOnlyList(context).size() -1 ;
            }
        }else{
            end = getReadOnlyList(context).size() - 1;
        }
        return end ;
    }
    
    protected Object getCurrentItem(ServiceCycle context) {
        return getReadOnlyList(context).get(getIndexValue(context));
	}
    
    public void setExpressionValue(ProcessorProperty expressionValue) {
        _expressionValue = expressionValue;
    }

}

