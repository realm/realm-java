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

package io.realm.internal.test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class ExtraTests {

    public static void assertArrayEquals(Object[] expecteds, Object[] actuals) {
        new ExactComparisonCriteria().arrayEquals(null, expecteds, actuals);
    }

    public static void assertArrayEquals(byte[] expecteds, byte[] actuals) {
        new ExactComparisonCriteria().arrayEquals(null, expecteds, actuals);
    }

    public static void assertDateArrayEquals(Object[] expecteds, Date[] actuals)
    {
        int i=0;
        for (Date date : actuals) {
            Date expect = (Date)expecteds[i];
            assertEquals(expect.getTime()/1000, date.getTime()/1000);
            i++;
        }
    }

    private static class ExactComparisonCriteria extends ComparisonCriteria {
        @Override
        protected void assertElementsEqual(Object expected, Object actual)
        {
            assertEquals(expected, actual);
        }
    }

    private static abstract class ComparisonCriteria {
        public void arrayEquals(String message, Object expecteds, Object actuals) throws ArrayComparisonFailure
        {
            if (expecteds == actuals) return;
            String header= message == null ? "" : message + ": ";

            int expectedsLength= assertArraysAreSameLength(expecteds, actuals, header);

            for (int i= 0; i < expectedsLength; ++i) {
                Object expected= Array.get(expecteds, i);
                Object actual= Array.get(actuals, i);

                if (isArray(expected) && isArray(actual)) {
                    try {
                        arrayEquals(message, expected, actual);
                    }
                    catch (ArrayComparisonFailure e) {
                        e.addDimension(i);
                        throw e;
                    }
                }
                else
                    try {
                        assertElementsEqual(expected, actual);
                    }
                    catch (AssertionError e) {
                        throw new ArrayComparisonFailure(header, e, i);
                    }
            }
        }

        private boolean isArray(Object expected)
        {
            return expected != null && expected.getClass().isArray();
        }

        private int assertArraysAreSameLength(Object expecteds, Object actuals, String header)
        {
            if (expecteds == null)
                fail(header + "expected array was null");
            if (actuals == null)
                fail(header + "actual array was null");
            int actualsLength= Array.getLength(actuals);
            int expectedsLength= Array.getLength(expecteds);
            if (actualsLength != expectedsLength)
                fail(header + "array lengths differed, expected.length="
                                 + expectedsLength + " actual.length=" + actualsLength);
            return expectedsLength;
        }

        protected abstract void assertElementsEqual(Object expected, Object actual);
    }

    private static class ArrayComparisonFailure extends AssertionError {

        private static final long serialVersionUID= 1L;

        private List<Integer> fIndices= new ArrayList<Integer>();
        private final String fMessage;
        private final AssertionError fCause;

        public ArrayComparisonFailure(String message, AssertionError cause, int index) {
            fMessage= message;
            fCause= cause;
            addDimension(index);
        }

        public void addDimension(int index) {
            fIndices.add(0, index);
        }

        @Override
        public String getMessage() {
            StringBuilder builder= new StringBuilder();
            if (fMessage != null)
                builder.append(fMessage);
            builder.append("arrays first differed at element ");
            for (int each : fIndices) {
                builder.append("[");
                builder.append(each);
                builder.append("]");
            }
            builder.append("; ");
            builder.append(fCause.getMessage());
            return builder.toString();
        }

        @Override public String toString() {
            return getMessage();
        }
    }
}
