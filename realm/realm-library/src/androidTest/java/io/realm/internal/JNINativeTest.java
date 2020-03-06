/*
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

package io.realm.internal;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class JNINativeTest {

    @Test
    public void nativeExceptions() {
        long maxExceptionNumber = TestUtil.getMaxExceptionNumber();
        for (long i = 0; i < maxExceptionNumber; i++) {
            String expect = TestUtil.getExpectedMessage(i);
            try {
                TestUtil.testThrowExceptions(i);
            } catch (Throwable throwable) {
                assertEquals("Exception kind: " + i, expect, throwable.toString());
            }
        }
    }

    @Test
    public void clampMaxTimestamps() {
        assertEquals(Long.MAX_VALUE, TestUtil.getDateFromTimestamp(Long.MAX_VALUE, 999999999));
        assertEquals(Long.MAX_VALUE, TestUtil.getDateFromTimestamp((Long.MAX_VALUE / 1000) + 1, 0)); // 1 second above MAX in milliseconds
    }

    @Test
    public void clampMinTimestamps() {
        assertEquals(Long.MIN_VALUE, TestUtil.getDateFromTimestamp(Long.MIN_VALUE, -999999999));
        assertEquals(Long.MIN_VALUE, TestUtil.getDateFromTimestamp((Long.MIN_VALUE / 1000) - 1, 0)); // 1 second below MIN in milliseconds
    }
}
