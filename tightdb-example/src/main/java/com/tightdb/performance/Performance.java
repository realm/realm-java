package com.tightdb.performance;

import com.tightdb.util;
import com.tightdb.lib.TightDB;


public class Performance {
	final public static int SMALL_TEST_VAL = 2;
	final public static int BYTE_TEST_VAL = 214;
	final public static long LONG_TEST_VAL = 0x1234567890ABCDEFL;
	
	final static boolean pause		= false;
	final static int REPEAT_SEARCH 	= 100;		// Number of times to repeat the search to get a measurable number
	final static int TESTS 			= 4;
	
	static class TestResult {
		long testTime[];
		long javaDBMemUsed;
		long nativeDBMemUsed;
		TestResult() { testTime = new long[5]; }
	}
	
	public static void main(String[] args) {
		TightDB.addNativeLibraryPath("lib-sqlite");
		
		// Measuring memory is not very reliable in Java...
		// Util.test_getMemUsed();
		
		int numOfValues = 250000;
		
		System.out.println("Performance tests with " + numOfValues + " rows. Search repeated " 
				+ REPEAT_SEARCH + " times.");
		
		System.out.print("Performance testing TightDB: ");
		
		TestResult tightdb = TestPerformance(new Tightdb(), numOfValues);
		
		System.out.print("\nPerformance testing Java ArrayList: ");
		TestResult javaArray = TestPerformance(new JavaArrayList(), numOfValues);
		
		System.out.print("\nPerformance testing SQLite: ");
		TestResult sqlite = TestPerformance(new SQLiteTest(), numOfValues);
	
		System.out.println("\n\nRESULTS:");
		String[] testText = {
				"Search for small integer:\t",
				"Search for byte sized integer:\t",
				"Search for long sized integer:\t",
				"Search for string:\t\t",
				"Add Index:\t\t\t",
				"Search for byte (indexed):\t"
		};
		System.out.println("\t\t\t\t   Tightdb\tArrayList\tSQLite");
		for (int test = 0; test < TESTS; ++test) {
			System.out.print( testText[test] );
			printTime(tightdb.testTime[test], " ms (x1)", "\t");
			
			printTime(javaArray.testTime[test], " ms ", "");
			if (tightdb.testTime[test] > 0)
				System.out.print( "(x" + javaArray.testTime[test] / tightdb.testTime[test] + ")\t");
			else
				System.out.print("\t");
			
			printTime(sqlite.testTime[test], " ms ", "");
			if (tightdb.testTime[test] > 0)
				System.out.print( "(x" + sqlite.testTime[test] / tightdb.testTime[test] + ")\t");
			else
				System.out.print("\t");
			 
			System.out.println();
		}
		long tightTotal  = tightdb.javaDBMemUsed + tightdb.nativeDBMemUsed;
		long javaTotal   = javaArray.javaDBMemUsed + javaArray.nativeDBMemUsed;
		long sqliteTotal = sqlite.javaDBMemUsed + sqlite.nativeDBMemUsed;
		System.out.printf("Memory use (java+native):\t%5d KB (x1)\t%5d KB (x%d)\t%5d KB (x%x)\n",
				toKB(tightTotal),
				toKB(javaTotal), javaTotal/tightTotal,
				toKB(sqliteTotal), sqliteTotal/tightTotal);
		
		System.out.println("\nDONE.");
		if (pause) util.waitForEnter();
	}

	static void printTime(long time, String str, String tab) {
		if (time > 0)
			System.out.printf("%5d%s%s", time, str, tab);
		else
			System.out.print("   -- \t" + tab);
	}
	
	static long toKB(long val) {
		return val/1024; 
	}
	
   	public static TestResult TestPerformance(IPerformance test, int rows) 
	{
   		TestResult	result = new TestResult();
   		Timer 		timer = new Timer();
		int			testNo = 0;
		
		if (pause) util.waitForEnter();
		long memBefore = Util.getUsedMemory(); memBefore = Util.getUsedMemory();
		
		// Build the test database
		test.buildTable(rows);
		
		result.javaDBMemUsed = Math.max(Util.getUsedMemory() - memBefore, 1);
		result.nativeDBMemUsed = test.usedNativeMemory();
		
		// Search small integer column
		{
		    // Do a search over entire column (value not found)
		    test.begin_findSmallInt(SMALL_TEST_VAL + 1);
		    timer.Start();
		    for (int i = 0; i < REPEAT_SEARCH; ++i) {
		    	if (test.findSmallInt(SMALL_TEST_VAL + 1)) {
		    		System.out.println("Error - found value.");
		    		break;
		    	}
		    }
		    result.testTime[testNo++] = timer.GetTimeInMs();
		    test.end_findSmallInt();
		    System.out.printf("*");
		}

		// Search byte-size integer column
		{
			test.begin_findByteInt(BYTE_TEST_VAL + 1);
		    timer.Start();
		    // Do a search over entire column (value not found)
		    for (int i = 0; i < REPEAT_SEARCH; ++i) {
		        if (test.findByteInt(BYTE_TEST_VAL + 1)) {
		            System.out.printf("Error - found value.");
		            break;
		        }
		    }
		    result.testTime[testNo++] = timer.GetTimeInMs();
		    test.end_findByteInt();
		    System.out.printf("*");
		}
		
		// Search long-size integer column
		{
			test.begin_findLongInt(LONG_TEST_VAL + 1);
		    timer.Start();
		    // Do a search over entire column (value not found)
		    for (int i = 0; i < REPEAT_SEARCH; ++i) {
		        if (test.findLongInt(LONG_TEST_VAL + 1)) {
		            System.out.printf("Error - found value.");
		            break;
		        }
		    }
		    result.testTime[testNo++] = timer.GetTimeInMs();
		    test.end_findLongInt();
		    System.out.printf("*");
		}
		
		// Search string column
		{
			test.begin_findString("abcde");
		    timer.Start();
		    // Do a search over entire column (value not found)
		    for (int i = 0; i < REPEAT_SEARCH; ++i) {
		        if (test.findString("abcde")) {
		           	  System.out.printf("error - found value.");
		              break;
		        }
		    }
		    result.testTime[testNo++] = timer.GetTimeInMs();
		    test.end_findString();
		    System.out.printf("*");
		}
/*
		// Add index, and search
		{
		    timer.Start();

		    boolean indexSupported = test.addIndex();
		    
		    if (indexSupported) {
		    	result.testTime[testNo++] = timer.GetTimeInMs();
		    	System.out.printf("*");
			    //System.out.printf("Memory usage2: %lld bytes\n", (long long)GetMemUsage());
			
				// Search with index
				test.begin_findIntWithIndex();
			    timer.Start();
			    for (int i = 0; i < REPEAT_SEARCH; ++i) {
			        int n = Util.getRandNumber();
			        if (test.findIntWithIndex(n) != n) {
			        	System.out.printf("error - didn't find value.");
			            break;
			        }
			    }
			    result.testTime[testNo++] = timer.GetTimeInMs();
			    test.end_findIntWithIndex();
			    System.out.printf("*");
			}
		}
*/		
		if (pause) util.waitForEnter();
		test.closeTable();
		
		return result;
	}
		
}

class Timer {
	static long startTime;
    
	public void Start()	{
		startTime = System.nanoTime();
	}
	
	public long GetTimeInMs() {
	    long stopTime = System.nanoTime();
	    return (stopTime - startTime) / 1000000;
	}	
}	

