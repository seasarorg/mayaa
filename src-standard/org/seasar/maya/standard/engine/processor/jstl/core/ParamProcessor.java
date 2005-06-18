/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.standard.alert.AlertThrower;
import org.seasar.maya.standard.alert.AlertThrowerFactory;
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;
import org.seasar.maya.standard.engine.processor.ChildParamReciever;

/**
 * @author maruo_syunsuke
 */
public class ParamProcessor extends AbstractBodyProcessor {

    private String				_name ;

    protected int process(PageContext context, Object obj) {
        String paramValue = getParamValue(obj);
        setParamValueToParentProcessor(paramValue);
        return Tag.EVAL_PAGE;
    }
    private void setParamValueToParentProcessor(String paramValue) {
        if( getParentProcessor() instanceof ChildParamReciever ){
            ChildParamReciever reciever = (ChildParamReciever)getParentProcessor() ;
            reciever.addChildParam(_name,paramValue);
        }else{
            AlertThrower alertThrower = AlertThrowerFactory.getAlertThrower();
            alertThrower.throwAlert("parent tag is not ChildParamReciever.");
        }
    }
    private String getParamValue(Object obj) {
        if( obj == null ) return "" ;
        return obj.toString();
    }
    
    ////
    public void setName(String name) {
        _name = name ;
    }
}