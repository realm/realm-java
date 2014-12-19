/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.internal.android;

import android.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtils {

    private static Pattern jsonDate = Pattern.compile("/Date\\((\\d*)\\)/");

    /**
     * Converts a Json string to a Java Date object. Currently supports 2 types:
     * - "<long>"
     * - "/Date(<long>)/"
     *
     * @param date   String input of date of the the supported types.
     * @return Date object or null if invalid input.
     *
     * @throws NumberFormatException If date is not a proper long or has an illegal format.
     */
    public static Date stringToDate(String date) {
        if (date == null || date.length() == 0) return null;
        Matcher matcher = jsonDate.matcher(date);
        if (matcher.matches()) {
            return new Date(Long.parseLong(matcher.group(1)));
        } else {
            return new Date(Long.parseLong(date));
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
