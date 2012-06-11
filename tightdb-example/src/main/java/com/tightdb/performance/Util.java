package com.tightdb.performance;

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
	
	
	// ---Hmm... Measuring memory usage in Java is highly unreliable... 

	public static long getUsedMemory3() {
	      gc3();
	      long totalMemory = Runtime.getRuntime().totalMemory();
	      gc3();
	      long freeMemory = Runtime.getRuntime().freeMemory();
	      return totalMemory - freeMemory;
	}
	private static void gc3() {
		TightDB.gcGuaranteed();
	}
	private static void gc2() {
	      try {
	         System.gc();
	         Thread.currentThread().sleep(100);
	         System.runFinalization();
	         Thread.currentThread().sleep(100);
	         System.gc();
	         Thread.currentThread().sleep(100);
	         System.runFinalization();
	         Thread.currentThread().sleep(100);
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	}
	
	// ---- ver 2
	
	private static final int GC_TIMES = 5;
	private static Runtime _runtime = Runtime.getRuntime();

	private static long internalUsedMemory() {
		return _runtime.totalMemory() - _runtime.freeMemory();
	}

	public static long getUsedMemory() {
		long usedMemoryBeforeGC = internalUsedMemory();
		while(true){
			for (int i = 0; i < GC_TIMES; ++i) {
				//System.gc();
				gc3();
				System.runFinalization();
				//Thread.yield();
				try {
					Thread.currentThread().sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			long usedMemoryAfterGC = internalUsedMemory();
			if(usedMemoryAfterGC >= usedMemoryBeforeGC){
				return usedMemoryBeforeGC;
			}
			usedMemoryBeforeGC = usedMemoryAfterGC;
		}
	}

}
