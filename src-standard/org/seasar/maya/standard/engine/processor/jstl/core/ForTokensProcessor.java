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


/**
 * @version $Revision: 1.1 $ $Date: 2005/06/07 00:30:46 $
 * @author maruo_syunsuke
 */
public class ForTokensProcessor extends ForLoopProcessor {
    private ProcessorProperty 	_item ;
    private ProcessorProperty 	_delims ;
	private ReadOnlyList 		_readOnlyList = null ;

    public int doStartProcess(PageContext context) {
        _readOnlyList = ForEachSupportUtil.toForEachList(
                (String)_item.getValue(context),
                (String)_delims.getValue(context) 
        );
        return super.doStartProcess(context);
    }
    
    protected int initEndParameter(PageContext context) {
        Integer endValue = getEndParameterValue(context);
        if( endValue != null ){
            int end = endValue.intValue();
            if( end >  getDefaultEndValue() ){
                return getDefaultEndValue() ;
            }
            return end ;
        }
        return getDefaultEndValue();
    }
    
    private int getDefaultEndValue() {
        return _readOnlyList.size() - 1;
    }

    protected void setCurrentObjectToVarValue(PageContext context) {
        setVarValue(context,getCurrentObject());
	}
    
    private Object getCurrentObject() {
        return _readOnlyList.get(getIndexValue());
    }

    ////
    public void setDelims(ProcessorProperty delims) {
        _delims = delims;
    }
    public void setItem(ProcessorProperty item) {
        _item = item;
    }
    public void setReadOnlyList(ReadOnlyList readOnlyList) {
        _readOnlyList = readOnlyList;
    }
}

