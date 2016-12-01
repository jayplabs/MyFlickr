package com.chandra.myflickr.utils;

import java.util.Locale;

public class StringUtils {

    private StringUtils() {}

    /**
     * check string value not null
     */
    public static boolean isNotNull(String string) {
        if (string == null || string.trim().isEmpty()
                || "null".equals(string.toLowerCase(Locale.US))) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * check string value null
     */
    public static boolean isNull(String string) {
        if (string == null || string.trim().isEmpty()
                || "null".equals(string.toLowerCase(Locale.US))) {
            return true;
        } else {
            return false;
        }
    }
}
