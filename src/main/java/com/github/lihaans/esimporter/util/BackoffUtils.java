package com.github.lihaans.esimporter.util;

public final class BackoffUtils {
    private BackoffUtils() {
    }

    public static long exponentialBackoff(long baseMillis, int retry) {
        long multiplier = 1L << Math.max(0, retry - 1);
        long jitter = (long) (Math.random() * Math.max(100L, baseMillis));
        return baseMillis * multiplier + jitter;
    }
}
