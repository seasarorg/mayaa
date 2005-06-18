package org.seasar.maya.standard.alert;

/**
 * @author maruo_syunsuke
 */
public class NullAlertThrowner implements AlertThrower {
    public void throwAlert(String message) {
    }
    public void throwAlert(Throwable throwable) {
    }
    public void throwAlert(String message,Throwable throwable) {
    }
}
