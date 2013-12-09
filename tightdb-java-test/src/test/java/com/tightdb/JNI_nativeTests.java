package com.tightdb;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

import com.tightdb.internal.Util;


public class JNI_nativeTests {

    @Test
    public void testNativeExceptions() {
        String expect = "";
        for (Util.Testcase test: Util.Testcase.values()) {
            expect = test.expectedResult(0);
            try {
                test.execute(0);
            } catch (Exception e) {
                assertEquals(expect, e.toString());
            } catch (Error e) {
                assertEquals(expect, e.toString());
            }

        }
    }
}
