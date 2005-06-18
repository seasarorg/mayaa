package org.seasar.maya.standard.alert;

/**
 * @author maruo_syunsuke
 */
public class DebugAlertThrowner implements AlertThrower{
    public void throwAlert(String message) {
        throw new RuntimeException(message);
    }
    public void throwAlert(Throwable throwable) {
        throw new RuntimeException(throwable);
    }
    public void throwAlert(String message,Throwable throwable) {
        throw new RuntimeException(message,throwable);
    }
}
