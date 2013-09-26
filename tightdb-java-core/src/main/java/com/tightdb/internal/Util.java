package com.tightdb.internal;

import java.util.Scanner;

import com.tightdb.typed.TightDB;

public class Util {

    public static final long REQUIRED_JNI_VERSION = 19;

    static {
        TightDB.loadLibrary();
    }

    public static long getNativeMemUsage() {
        return nativeGetMemUsage();
    }
    static native long nativeGetMemUsage();

    public static boolean versionCompatible() {

        boolean compatible = (nativeGetVersion() == REQUIRED_JNI_VERSION);
        if (!compatible)
            System.err.println("Native lib is version " + nativeGetVersion()
                    + " != " +  REQUIRED_JNI_VERSION + " which is expected by the jar.");
        return compatible;
    }

    static native int nativeGetVersion();

    // Set to level=1 to get some trace from JNI native part.
    public static void setDebugLevel(int level) {
        nativeSetDebugLevel(level);
    }
    static native void nativeSetDebugLevel(int level);
    
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
        Exception_OutOfMemory(10),
        Exception_Unspecified(11),
        Exception_RuntimeError(12);
        
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
    
}
