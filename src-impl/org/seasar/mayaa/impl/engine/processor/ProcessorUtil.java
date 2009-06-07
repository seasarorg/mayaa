package org.seasar.mayaa.impl.engine.processor;

import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.impl.util.ObjectUtil;

/**
 * プロセッサ用ユーティリティクラス
 * @author Koji Suga (Gluegent Inc.)
 */
public class ProcessorUtil {

    /**
     * プロパティがリテラルのとき、booleanと見なせるかを判定する。
     * @param property 判定するプロパティ。
     * @throws IllegalArgumentException プロパティがbooleanと見なせない場合発生する。
     * @see ObjectUtil#canBooleanConvert(Object)
     */
    public static void checkBoolableProperty(ProcessorProperty property) {
        if (property == null || property.getValue() == null) {
            throw new IllegalArgumentException("needs expression.");
        }
        if (property.getValue().isLiteral()) {
            if (ObjectUtil.canBooleanConvert(property.getValue().getScriptText()) == false) {
                throw new IllegalArgumentException("needs expression.");
            }
        }
    }

    /**
     * プロパティをbooleanにして返す。nullやbooleanと見なせない場合はfalseを返す。
     * @param property 判定するプロパティ。
     * @see ObjectUtil#booleanValue(Object, boolean)
     */
    public static boolean toBoolean(ProcessorProperty property) {
        return property != null && ObjectUtil.booleanValue(
                property.getValue().execute(null), false);
    }

}
