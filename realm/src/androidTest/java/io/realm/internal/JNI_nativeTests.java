package io.realm.internal;

import junit.framework.TestCase;

public class JNI_nativeTests extends TestCase {

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

    // Test has been disabled as it will stop the execution of the remaining tests
    public void DISABLEDtestTerminate() throws InterruptedException {
        io.realm.internal.Util.Terminate("REALM", "FooBar Test");
    }

    // Test has been disabled as it will stop the execution of the remaining tests
    public void DISABLEDtestNativeOutOfMemoryException() {
        io.realm.internal.Util.nativeTestcase(10, true, 10);
    }
}
