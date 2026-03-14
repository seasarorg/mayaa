package org.seasar.mayaa.test.boot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public final class PerfWorkloadHelper {

    private static final Object LOCK = new Object();
    private static volatile int configuredCacheSize = 64;
    private static volatile Map<Integer, String> payloadCache = createCache(64);
    private static final AtomicLong cacheHits = new AtomicLong();
    private static final AtomicLong cacheMisses = new AtomicLong();

    private PerfWorkloadHelper() {
    }

    public static String runWorkload(int cacheSize, int keyRange, int payloadKb, int cpuLoops) {
        int safeCacheSize = clamp(cacheSize, 8, 4096);
        int safeKeyRange = clamp(keyRange, 16, 200_000);
        int safePayloadKb = clamp(payloadKb, 8, 2048);
        int safeCpuLoops = clamp(cpuLoops, 100, 300_000);

        ensureCacheSize(safeCacheSize);

        int key = ThreadLocalRandom.current().nextInt(safeKeyRange);
        String payload = payloadCache.get(key);
        boolean hit = payload != null;
        if (!hit) {
            payload = buildPayload(key, safePayloadKb, safeCpuLoops);
            payloadCache.put(key, payload);
            cacheMisses.incrementAndGet();
        } else {
            runCpuOnly(safeCpuLoops / 2, key);
            cacheHits.incrementAndGet();
        }

        long payloadBytes = payload.length() * 2L;
        String preview = payload.substring(0, Math.min(64, payload.length()));
        return "hit=" + hit
                + ", key=" + key
                + ", payloadBytes=" + payloadBytes
                + ", preview=" + preview;
    }

    public static String stats() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        double missRate = total == 0 ? 0.0 : (double) misses * 100.0 / total;
        return "cacheSize=" + configuredCacheSize
                + ", hits=" + hits
                + ", misses=" + misses
                + ", missRate=" + String.format("%.2f", missRate) + "%"
                + ", entries=" + payloadCache.size();
    }

    private static void ensureCacheSize(int desiredSize) {
        if (configuredCacheSize == desiredSize) {
            return;
        }
        synchronized (LOCK) {
            if (configuredCacheSize == desiredSize) {
                return;
            }
            configuredCacheSize = desiredSize;
            payloadCache = createCache(desiredSize);
            cacheHits.set(0);
            cacheMisses.set(0);
        }
    }

    private static String buildPayload(int key, int payloadKb, int cpuLoops) {
        int targetChars = payloadKb * 1024;
        StringBuilder builder = new StringBuilder(targetChars + 32);
        builder.append("key=").append(key).append('|');

        long hash = 1125899906842597L;
        for (int i = 0; i < cpuLoops; i++) {
            hash = (hash * 1315423911L) ^ (key + i);
        }

        int token = (int) (hash & 0x7fffffff);
        while (builder.length() < targetChars) {
            token = token * 1103515245 + 12345;
            builder.append(Integer.toHexString(token)).append('-');
        }
        return builder.toString();
    }

    private static void runCpuOnly(int cpuLoops, int key) {
        long value = key;
        for (int i = 0; i < cpuLoops; i++) {
            value = (value * 6364136223846793005L + 1442695040888963407L) ^ i;
        }
        if (value == Long.MIN_VALUE) {
            throw new IllegalStateException("unreachable");
        }
    }

    private static Map<Integer, String> createCache(final int maxEntries) {
        return Collections.synchronizedMap(new LinkedHashMap<Integer, String>(maxEntries + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                return size() > maxEntries;
            }
        });
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
