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
package org.seasar.mayaa.impl.engine.processor;

import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.script.rhino.ScriptEnvironmentImpl;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.EscapeUtil;

/**
 * autoEscape 関連のリクエスト内コンテキストを扱う。
 */
final class AutoEscapeContext {

    private static final String REQUEST_PAGE_OVERRIDE_KEY = AutoEscapeContext.class.getName()
            + "#pageAutoEscapeEnabled";
    private static final String REQUEST_CONFIG_ENABLED_KEY = AutoEscapeContext.class.getName()
            + "#configAutoEscapeEnabled";
    private static final String REQUEST_CONFIG_DETECTION_LEVEL_KEY = AutoEscapeContext.class.getName()
            + "#configEscapeDetectionLevel";
    private static final String REQUEST_SUPPRESS_DEPTH_KEY = AutoEscapeContext.class.getName()
            + "#autoEscapeSuppressDepth";

    private AutoEscapeContext() {
        // no instantiation
    }

    static boolean isAutoEscapeEnabled() {
        Boolean pageValue = getPageAutoEscapeEnabled();
        if (pageValue != null) {
            return pageValue.booleanValue();
        }
        RequestScope requestScope = getRequestScopeSafely();
        if (requestScope != null) {
            Object cached = requestScope.getAttribute(REQUEST_CONFIG_ENABLED_KEY);
            if (cached instanceof Boolean) {
                return ((Boolean) cached).booleanValue();
            }
        }
        boolean configuredValue = getConfiguredAutoEscapeEnabled();
        if (requestScope != null) {
            requestScope.setAttribute(REQUEST_CONFIG_ENABLED_KEY, Boolean.valueOf(configuredValue));
        }
        return configuredValue;
    }

    static String getEscapeDetectionLevel() {
        RequestScope requestScope = getRequestScopeSafely();
        if (requestScope != null) {
            Object cached = requestScope.getAttribute(REQUEST_CONFIG_DETECTION_LEVEL_KEY);
            if (cached instanceof String) {
                return (String) cached;
            }
        }
        String configuredValue = getConfiguredEscapeDetectionLevel();
        if (requestScope != null) {
            requestScope.setAttribute(REQUEST_CONFIG_DETECTION_LEVEL_KEY, configuredValue);
        }
        return configuredValue;
    }

    static void setPageAutoEscapeEnabled(Boolean value) {
        RequestScope requestScope = getRequestScopeSafely();
        if (requestScope == null) {
            return;
        }
        if (value == null) {
            requestScope.removeAttribute(REQUEST_PAGE_OVERRIDE_KEY);
        } else {
            requestScope.setAttribute(REQUEST_PAGE_OVERRIDE_KEY, value);
        }
    }

    static Boolean getPageAutoEscapeEnabled() {
        RequestScope requestScope = getRequestScopeSafely();
        if (requestScope == null) {
            return null;
        }
        Object value = requestScope.getAttribute(REQUEST_PAGE_OVERRIDE_KEY);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }

    static void pushSuppressAutoEscape() {
        getSuppressDepth()[0]++;
    }

    static void popSuppressAutoEscape() {
        int[] depth = getSuppressDepth();
        if (depth[0] > 0) {
            depth[0]--;
        }
    }

    static boolean isAutoEscapeSuppressed() {
        return getSuppressDepth()[0] > 0;
    }

    private static boolean getConfiguredAutoEscapeEnabled() {
        ScriptEnvironment env = ProviderUtil.getScriptEnvironment();
        if (env instanceof ScriptEnvironmentImpl) {
            return ((ScriptEnvironmentImpl) env).isAutoEscapeEnabled();
        }
        return false;
    }

    private static String getConfiguredEscapeDetectionLevel() {
        ScriptEnvironment env = ProviderUtil.getScriptEnvironment();
        if (env instanceof ScriptEnvironmentImpl) {
            return ((ScriptEnvironmentImpl) env).getEscapeDetectionLevel();
        }
        return EscapeUtil.DETECTION_LEVEL_NORMAL;
    }

    private static int[] getSuppressDepth() {
        RequestScope requestScope = getRequestScopeSafely();
        if (requestScope == null) {
            return new int[] { 0 };
        }
        Object value = requestScope.getAttribute(REQUEST_SUPPRESS_DEPTH_KEY);
        if (value instanceof int[]) {
            return (int[]) value;
        }
        int[] depth = new int[] { 0 };
        requestScope.setAttribute(REQUEST_SUPPRESS_DEPTH_KEY, depth);
        return depth;
    }

    private static RequestScope getRequestScopeSafely() {
        try {
            return CycleUtil.getRequestScope();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
