/*
 * Copyright 2015 FasterXML
 * Copyright 2015 Realm Inc.
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

import android.test.AndroidTestCase;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import io.realm.exceptions.RealmException;

public class JsonUtilsTest extends AndroidTestCase {

    public void testParseNullAndEmptyDateIsNull() {
        Date output = JsonUtils.stringToDate(null);
        assertNull("Null input should output a null date object", output);

        output = JsonUtils.stringToDate("");
        assertNull("Empty string input should output a null date object", output);
    }

    public void testParseMillisToDate() {
        Date originalDate = Calendar.getInstance().getTime();
        long dateTimeInMillis = originalDate.getTime();
        Date output = JsonUtils.stringToDate(String.valueOf(dateTimeInMillis));

        assertTrue("Dates should match", output.equals(originalDate));
    }

    public void testParseJsonDateToDate() {
        String jsonDate = "/Date(1198908717056)/"; // 2007-12-27T23:11:57.056
        Date output = JsonUtils.stringToDate(jsonDate);

        assertEquals(1198908717056L, output.getTime());
    }

    public void testNegativeLongDate() {
        long timeInMillis = -631152000L; // Jan 1, 1950
        Date output = JsonUtils.stringToDate(String.valueOf(timeInMillis));

        assertEquals("Should be Jan 1, 1950 in millis", timeInMillis, output.getTime());
    }

    public void testParseInvalidDateShouldThrowRealmException() {
        String invalidLongDate = "123abc";
        try {
            Date d = JsonUtils.stringToDate(invalidLongDate);
            fail("Should fail with a RealmException.");
        } catch (RealmException e) {
            assertNotNull(e);
            assertTrue(e.getCause() instanceof ParseException);
        }
    }

    public void testParseInvalidNumericDateShouldThrowRealmException() {
        String invalidLongDate = "2342347289374398342759873495743"; // not a date.
        try {
            Date d = JsonUtils.stringToDate(invalidLongDate);
            fail("Should fail with a RealmException.");
        } catch (RealmException e) {
            assertNotNull(e);
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    public void testParseISO8601Dates() throws ParseException {
        Calendar cal = new GregorianCalendar(2007, 8 - 1, 13, 19, 51, 23);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.MILLISECOND, 789);
        Date date = cal.getTime();
        cal.set(Calendar.MILLISECOND, 0);
        Date dateZeroMillis = cal.getTime();
        cal.set(Calendar.SECOND, 0);

        // Parses date with short time and decimal second.
        Date d = JsonUtils.stringToDate("2007-08-13T195123.789Z");
        assertEquals(date, d);

        // Short time without decimal second.
        d = JsonUtils.stringToDate("2007-08-13T195123Z");
        assertEquals(dateZeroMillis, d);

        // GMT+2 with decimal second.
        d = JsonUtils.stringToDate("2007-08-13T215123.789+02:00");
        assertEquals(date, d);

        // Tests without time.
        cal = new GregorianCalendar(2007, 8 - 1, 13, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateWithoutTime = cal.getTime();

        // Date only with hyphens.
        d = JsonUtils.stringToDate("2007-08-13Z");
        assertEquals(dateWithoutTime, d);

        // Date, no hyphens.
        d = JsonUtils.stringToDate("20070813Z");
        assertEquals(dateWithoutTime, d);

        // Hyphenated Date with empty time.
        d = JsonUtils.stringToDate("2007-08-13+00:00");
        assertEquals(dateWithoutTime, d);

        // Non-hyphenated date with empty time.
        d = JsonUtils.stringToDate("20070813+00:00");
        assertEquals(dateWithoutTime, d);

        // Please see the ISO8601UtilsTest.java file for a full suite of ISO8601 tests.
    }
}
