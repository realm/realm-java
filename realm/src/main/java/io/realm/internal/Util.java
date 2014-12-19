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

package io.realm.internal;

import android.util.Log;

import java.util.Scanner;

public class Util {

    static {
        TightDB.loadLibrary();
    }

    public static long getNativeMemUsage() {
        return nativeGetMemUsage();
    }
    static native long nativeGetMemUsage();

    // Set to level=1 to get some trace from JNI native part.
    public static void setDebugLevel(int level) {
        nativeSetDebugLevel(level);
    }
    static native void nativeSetDebugLevel(int level);

    // Called by JNI. Do not remove
    static void javaPrint(String txt) {
        System.out.print(txt);
    }

    public static void waitForEnter() {
        System.out.println("Press Enter to continue...");
        Scanner sc = new Scanner(System.in);
           while(!sc.nextLine().equals(""));
        sc.close();
    }

    // Testcases run in nativeCode
    public enum Testcase {
        Exception_ClassNotFound(0),
        Exception_NoSuchField(1),
        Exception_NoSuchMethod(2),
        Exception_IllegalArgument(3),
        Exception_IOFailed(4),
        Exception_FileNotFound(5),
        Exception_FileAccessError(6),
        Exception_IndexOutOfBounds(7),
        Exception_TableInvalid(8),
        Exception_UnsupportedOperation(9),
        //Exception_OutOfMemory(10), // will terminate with runtime.halt()
        Exception_Unspecified(11),
        Exception_RuntimeError(12),
        Exception_RowInvalid(13);

        private final int nativeTestcase;
        private Testcase(int nativeValue)
        {
            this.nativeTestcase = nativeValue;
        }

        public String expectedResult(long parm1) {
            return nativeTestcase(nativeTestcase, false, parm1);
        }
        public String execute(long parm1) {
            return nativeTestcase(nativeTestcase, true, parm1);
        }
    }

    static native String nativeTestcase(int testcase, boolean dotest, long parm1);

    public static void Terminate(String logTag, String cause) throws InterruptedException {
        Log.e(logTag, "*******************************************************************");
        Log.e(logTag, "!!!App has been halted!!!");
        Log.e(logTag, "Cause: " + cause);
        Log.e(logTag, "*******************************************************************");
        Runtime.getRuntime().halt(1);
    }

}
