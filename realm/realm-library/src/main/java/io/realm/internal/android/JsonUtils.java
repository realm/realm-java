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

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import io.realm.exceptions.RealmException;


public class JsonUtils {

    private static Pattern jsonDate = Pattern.compile("/Date\\((\\d*)(?:[+-]\\d*)?\\)/");
    private static Pattern numericOnly = Pattern.compile("-?\\d+");
    private static ParsePosition parsePosition = new ParsePosition(0);

    /**
     * Converts a Json string to a Java Date object. Currently supports 2 types:
     * - "<long>"
     * - "/Date(<long>[+-Zone])/"
     *
     * @param date the String input of date of the the supported types.
     * @return the Date object or {@code null} if invalid input.
     * @throws NumberFormatException if date is not a proper long or has an illegal format.
     */
    @Nullable
    public static Date stringToDate(String date) {
        if (date == null || date.length() == 0) { return null; }

        // Checks for JSON date.
        Matcher matcher = jsonDate.matcher(date);
        if (matcher.find()) {
            String dateMatch = matcher.group(1);
            return new Date(Long.parseLong(dateMatch));
        }

        // Checks for millisecond based date.
        if (numericOnly.matcher(date).matches()) {
            try {
                return new Date(Long.parseLong(date));
            } catch (NumberFormatException e) {
                throw new RealmException(e.getMessage(), e);
            }
        }

        // Tries for ISO8601 date.
        try {
            parsePosition.setIndex(0); // Resets the position each time.
            return ISO8601Utils.parse(date, parsePosition);
        } catch (ParseException e) {
            throw new RealmException(e.getMessage(), e);
        }
    }

    /**
     * Converts a Json string to byte[]. String must be Base64 encoded.
     *
     * @param str the base 64 encoded bytes.
     * @return the Byte array or empty byte array.
     */
    public static byte[] stringToBytes(String str) {
        if (str == null || str.length() == 0) { return new byte[0]; }
        return Base64.decode(str, Base64.DEFAULT);
    }
}
