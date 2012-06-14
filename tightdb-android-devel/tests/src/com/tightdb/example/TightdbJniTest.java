package com.tightdb.example;

import android.test.ActivityInstrumentationTestCase;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.tightdb.example.TightdbJniTest \
 * com.tightdb.example.tests/android.test.InstrumentationTestRunner
 */
public class TightdbJniTest extends ActivityInstrumentationTestCase<TightdbJni> {

    public TightdbJniTest() {
        super("com.tightdb.example", TightdbJni.class);
    }

}
