package com.tightdb;

import java.util.Scanner;

public class util {
	
	// Add version check
	// TODO???
	
	public static long getNativeMemUsage() {
		return nativeGetMemUsage();
	}
	static native long nativeGetMemUsage();
	
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
	}
	
}
