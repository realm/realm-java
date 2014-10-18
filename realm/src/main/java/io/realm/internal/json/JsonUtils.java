package io.realm.internal.json;

import android.util.Base64;
import java.util.Date;

/**
 * Created by Christian Melchior on 17/10/14.
 */
public class JsonUtils {

    /**
     * Converts a Json string to a Java Date object. Currently supports 2 types:
     * - "<long>"
     * - "/Date(<long>)/"
     * - TODO ISO 8601 String
     *
     * @param str   String input of the supported types.
     * @return Date object or null if invalid input.
     *
     * @throws NumberFormatException If timestamp is not a proper long
     * @throws IndexOutOfBoundsException if dates of type /Date(x)/ does not have a proper format.
     */
    public static Date stringToDate(String str) {
        if (str == null || str.length() == 0) return null;
        if (str.startsWith("/Date")) {
            return new Date(Long.parseLong(str.substring(6, str.length() - 2)));
        } else {
            return new Date(Long.parseLong(str));
        }
    }

    /**
     * Converts a Json string to byte[]. String must be Base64 encoded.
     *
     * @param str   Base 64 encoded bytes.
     * @return Byte array or empty byte array
     */
    public static byte[] stringToBytes(String str) {
        if (str == null || str.length() == 0) return new byte[0];
        return Base64.decode(str, Base64.DEFAULT);
    }
}
