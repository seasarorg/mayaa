/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.sample;

import java.util.Date;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TimeModel {
    
    private Date _time;

    public TimeModel() {
        _time = new Date(0);
        System.out.println("TemplateModel() set " + _time.toString());
    }
    
    public void beforeTime() {
        _time = new Date();
        System.out.println("beforeTime() set " + _time.toString());
    }
    
    public String getTime() {
        System.out.println("getTime() get " + _time.toString());
        return _time.toString();
    }

    public void afterTime() {
        System.out.println("afterTime()");
    }
    
}
