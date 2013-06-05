package com.tightdb.generator;

public class StringUtils {

    public static String join(Object[] array, String separator) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(array[i]);
        }

        return sb.toString();
    }

    public static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String escapeJava(String s) {
        return s.replaceAll("\\r\\n", "\\\\r\\\\n").replaceAll("\\t", "\\\\t")
                .replaceAll("\"", "\\\\\"");
    }

}
