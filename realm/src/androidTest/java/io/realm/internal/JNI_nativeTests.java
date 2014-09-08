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
}
