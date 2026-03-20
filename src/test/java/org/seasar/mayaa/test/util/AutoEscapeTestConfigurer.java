/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.test.util;

import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.impl.cycle.script.rhino.ScriptEnvironmentImpl;
import org.seasar.mayaa.impl.util.EscapeUtil;

/**
 * テスト実行時のみ autoEscape 関連のシステムプロパティを ScriptEnvironment に反映する。
 */
public final class AutoEscapeTestConfigurer {

    private static final String PROP_AUTO_ESCAPE_ENABLED = "mayaa.autoEscapeEnabled";
    private static final String PROP_ESCAPE_DETECTION_LEVEL = "mayaa.escapeDetectionLevel";

    private AutoEscapeTestConfigurer() {
        // utility class
    }

    public static void applyFromSystemProperties(ScriptEnvironment env) {
        if (env == null || !(env instanceof ScriptEnvironmentImpl)) {
            return;
        }
        ScriptEnvironmentImpl scriptEnvironment = (ScriptEnvironmentImpl) env;

        String autoEscapeEnabled = System.getProperty(PROP_AUTO_ESCAPE_ENABLED);
        if ("true".equalsIgnoreCase(autoEscapeEnabled)
                || "false".equalsIgnoreCase(autoEscapeEnabled)) {
            scriptEnvironment.setParameter("autoEscapeEnabled", autoEscapeEnabled);
        }

        String detectionLevel = System.getProperty(PROP_ESCAPE_DETECTION_LEVEL);
        if (EscapeUtil.DETECTION_LEVEL_NORMAL.equalsIgnoreCase(detectionLevel)
                || EscapeUtil.DETECTION_LEVEL_STRICT.equalsIgnoreCase(detectionLevel)) {
            scriptEnvironment.setParameter("escapeDetectionLevel", detectionLevel.toLowerCase());
        }
    }
}
