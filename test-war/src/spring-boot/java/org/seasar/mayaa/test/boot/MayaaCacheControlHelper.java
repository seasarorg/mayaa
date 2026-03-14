package org.seasar.mayaa.test.boot;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public final class MayaaCacheControlHelper {

    private static final String CACHE_OBJECT_NAME_QUERY = "org.seasar.mayaa:type=CacheControl,name=*";

    private MayaaCacheControlHelper() {
    }

    public static String invalidateByPattern(String namePattern) {
        Pattern pattern = compileSafePattern(namePattern);
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> names = server.queryNames(new ObjectName(CACHE_OBJECT_NAME_QUERY), null);
            List<ObjectName> sorted = new ArrayList<>(names);
            Collections.sort(sorted, (a, b) -> a.toString().compareTo(b.toString()));

            int matched = 0;
            int invalidated = 0;
            for (ObjectName objectName : sorted) {
                String cacheName = objectName.getKeyProperty("name");
                if (cacheName == null || !pattern.matcher(cacheName).matches()) {
                    continue;
                }
                matched++;
                server.invoke(objectName, "invalidateAll", null, null);
                invalidated++;
            }
            return "matched=" + matched + ", invalidated=" + invalidated + ", pattern=" + pattern.pattern();
        } catch (Exception e) {
            return "cache-reset-error=" + e.getClass().getSimpleName() + ", message=" + e.getMessage();
        }
    }

    public static String listCacheNames() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> names = server.queryNames(new ObjectName(CACHE_OBJECT_NAME_QUERY), null);
            List<String> cacheNames = new ArrayList<>();
            for (ObjectName objectName : names) {
                String cacheName = objectName.getKeyProperty("name");
                if (cacheName != null) {
                    cacheNames.add(cacheName);
                }
            }
            Collections.sort(cacheNames);
            return "caches=" + cacheNames;
        } catch (Exception e) {
            return "cache-list-error=" + e.getClass().getSimpleName() + ", message=" + e.getMessage();
        }
    }

    private static Pattern compileSafePattern(String rawPattern) {
        String pattern = rawPattern;
        if (pattern == null || pattern.trim().isEmpty()) {
            pattern = ".*";
        }
        try {
            return Pattern.compile(pattern);
        } catch (PatternSyntaxException ex) {
            return Pattern.compile(Pattern.quote(pattern));
        }
    }
}
