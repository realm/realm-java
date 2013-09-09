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

    static void javaPrint(String txt) {
        System.out.print(txt);
    }

    public static void waitForEnter() {
        System.out.println("Press Enter to continue...");
        Scanner sc = new Scanner(System.in);
           while(!sc.nextLine().equals(""));
        sc.close();
    }
}
