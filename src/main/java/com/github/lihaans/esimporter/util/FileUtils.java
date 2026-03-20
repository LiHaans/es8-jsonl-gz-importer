package com.github.lihaans.esimporter.util;

public final class FileUtils {
    private FileUtils() {
    }

    public static String safeFileName(String input) {
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
