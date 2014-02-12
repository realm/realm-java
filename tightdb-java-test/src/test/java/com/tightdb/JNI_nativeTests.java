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
    
    
    @Test
    public void jniTest(){
        Table table = new Table();
        
        long records = 1000000;
        
        long tic = System.currentTimeMillis();
        
        table.jniTest(records);
        
        System.out.println("Total time for " + records + " records: " + (System.currentTimeMillis() - tic));
    }
}
