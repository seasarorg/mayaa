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
import org.seasar.maya.impl.util.CycleUtil;

public class ForEachProcessor extends ForLoopProcessor {

    private static final long serialVersionUID = 5392396535368214943L;
    private static final String LOCAL_LIST = "LOCAL_LIST" ;
    
    private ProcessorProperty _items = null;

    public ProcessStatus doStartProcess() {
    	initReadOnlyList();
        return super.doStartProcess();
    }
    
    protected void initReadOnlyList() {
        Object itemsValue = getItemsValue();
        if( itemsValue == null ) return ;
		setReadOnlyList(ForEachSupportUtil.toForEachList(itemsValue));
    }
    
    protected ReadOnlyList getReadOnlyList() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        return (ReadOnlyList)cycle.getAttribute(createAttributeKey(LOCAL_LIST));
    }
    
    protected void setReadOnlyList(ReadOnlyList value) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setAttribute(createAttributeKey(LOCAL_LIST),value);
    }
    
    protected int initEndParameter() {
        Integer endValue = getEndParameterValue();
        int end = 0 ;
        if( endValue != null ){
            end = endValue.intValue();
            if( getReadOnlyList() != null 
            		&& end >= getReadOnlyList().size() ){
                end = getReadOnlyList().size() -1 ;
            }
        } else {
        	if( getReadOnlyList() == null )
        		throw new IllegalStateException("end is required.");
            end = getReadOnlyList().size() - 1;
        }
        return end ;
    }
    
    protected Object getCurrentItem() {
        ReadOnlyList readOnlyList = getReadOnlyList();
        if( readOnlyList == null ){
        	return super.getCurrentItem();
        }
		return readOnlyList.get(getIndexValue());
	}
    
    private String createAttributeKey(String postFix) {
        return getClass().getName() + "@" + hashCode() +":"+ postFix;
    }

    public void setItems(ProcessorProperty items) {
        _items = items;
    }
    
    protected Object getItemsValue() {
    	if( _items == null ) {
    	    return null;
        }
        return _items.getValue();
    }

}

