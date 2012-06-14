package com.tightdb.performance;

import java.util.Scanner;

import com.tightdb.lib.*;

public class Util {
    
	public static int getRandNumber() {
		return (int)(Math.random() * 1000);
	}
	
	public static String getNumberString(long nlong) {
	    String ones[] = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
	                                 "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
	                                 "eighteen", "nineteen"};
	    String tens[] = {"", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
	
	    int n = (int)nlong;
	    
	    String txt = null;
	    if (n >= 1000) {
	        txt = getNumberString(n/1000) + " thousand ";
	        n %= 1000;
	    }
	    if (n >= 100) {
	        txt += ones[n/100];
	        txt += " hundred ";
	        n %= 100;
	    }
	    if (n >= 20) {
	        txt += tens[n/10];
	        n %= 10;
	    }
	    else {
	        txt += " ";
	        txt += ones[n];
	    }
	
	    return txt;
	}
	
	public void waitForEnter() {
		System.out.println("Press Enter to continue...");
		Scanner sc = new Scanner(System.in);
	       while(!sc.nextLine().equals(""));
	}
	
	// Measuring memory usage in Java is highly unreliable... 
	
	static final Runtime run = Runtime.getRuntime();
	
	private static long memUsed() {
		return run.totalMemory() - run.freeMemory();
	}

	public static long getUsedMemory() {
		long memAfterGC  = memUsed();
		long memBeforeGC = memAfterGC+1;
		while (memAfterGC < memBeforeGC) {
			memBeforeGC = memAfterGC;
			for (int i = 0; i < 5; ++i) {
				TightDB.gcGuaranteed();
				System.runFinalization();
				Thread.yield();
			}
			memAfterGC = memUsed();
		}
		return memBeforeGC;
	}

	public static void test_getMemUsed() {
		long mem[] = new long[5];
		mem[0] = Util.getUsedMemory();
		mem[1] = Util.getUsedMemory();
		mem[2] = Util.getUsedMemory();
		mem[3] = Util.getUsedMemory();
		mem[4] = Util.getUsedMemory();
		for (int i=0; i<5; ++i)
			System.out.println("Memuse " + mem[i]);
	}
}
