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
import org.seasar.maya.engine.processor.NullProcessorProperty;
import org.seasar.maya.engine.processor.ProcessorProperty;

public class ForEachProcessor extends ForLoopProcessor {

    private static final long serialVersionUID = 5392396535368214943L;
    private static final String LOCAL_LIST = "LOCAL_LIST" ;
    
    private ProcessorProperty _items = null;

    public ProcessStatus doStartProcess(ServiceCycle context) {
    	initReadOnlyList(context);
        return super.doStartProcess(context);
    }
    
    protected void initReadOnlyList(ServiceCycle cycle) {
        Object itemsValue = getItemsValue(cycle);
        if( itemsValue == null ) return ;
		setReadOnlyList(cycle,ForEachSupportUtil.toForEachList(itemsValue));
    }
    
    protected ReadOnlyList getReadOnlyList(ServiceCycle cycle){
        return (ReadOnlyList)cycle.getAttribute(createAttributeKey(LOCAL_LIST));
    }
    
    protected void setReadOnlyList(ServiceCycle cycle,ReadOnlyList value){
        cycle.setAttribute(createAttributeKey(LOCAL_LIST),value);
    }
    
    protected int initEndParameter(ServiceCycle context) {
        Integer endValue = getEndParameterValue(context);
        int end = 0 ;
        if( endValue != null ){
            end = endValue.intValue();
            if( getReadOnlyList(context) != null 
            		&& end >= getReadOnlyList(context).size() ){
                end = getReadOnlyList(context).size() -1 ;
            }
        }else{
        	if( getReadOnlyList(context) == null )
        		throw new IllegalStateException("end is required.");
            end = getReadOnlyList(context).size() - 1;
        }
        return end ;
    }
    
    protected Object getCurrentItem(ServiceCycle cycle) {
        ReadOnlyList readOnlyList = getReadOnlyList(cycle);
        if( readOnlyList == null ){
        	return super.getCurrentItem(cycle);
        }
		return readOnlyList.get(getIndexValue(cycle));
	}
    
    private String createAttributeKey(String postFix) {
        return getClass().getName() + "@" + hashCode() +":"+ postFix;
    }

    public void setItems(ProcessorProperty items) {
        _items = items;
    }
    protected Object getItemsValue(ServiceCycle cycle) {
    	if( _items == null ) return null ;
        return _items.getValue(cycle);
    }

}

