package org.seasar.maya.standard.alert;


/**
 * @author maruo_syunsuke
 */
public class AlertThrowerFactory{
    private static AlertThrower _alertThrower = new DebugAlertThrowner();
    
    // to be injection model.
    public static AlertThrower getAlertThrower(){
        return _alertThrower ;
    }
}
