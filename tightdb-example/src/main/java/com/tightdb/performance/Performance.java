package com.tightdb.performance;


public class Performance {
	
	final public static int SMALL_TEST_VAL = 2;
	final public static int BYTE_TEST_VAL = 100;
	
	final static int REPEAT_SEARCH 	= 100;		// Number of times to repeat the search to get a measurable number
	final static int TESTS 			= 3;
	
	public static void main(String[] args) {
		int numOfValues = 250000;
		
		System.out.println("Performance tests with " + numOfValues + " rows. Search repeated " 
				+ REPEAT_SEARCH + " times.");
		
		System.out.print("Performance testing TightDB: ");
		long time_Tightdb[] = TestPerformance(new Tightdb(), numOfValues);
		
		System.out.print("\nPerformance testing Java ArrayList: ");
		long time_Array[] = TestPerformance(new JavaArrayList(), numOfValues);
		
		System.out.print("\nPerformance testing SQLite: ");
		long time_Sqlite[] = TestPerformance(new SQLite(), numOfValues);
	
		System.out.println("\n\nRESULTS:");
		String[] testText = {
				"Search for small integer:\t",
				"Search for byte sized integer:\t",
				"Search for string:\t\t",
				"Add Index:\t\t\t",
				"Search for byte (indexed):\t"
		};
		System.out.println("\t\t\t\t   Tightdb\tArrayList\tSQLite");
		for (int test = 0; test < TESTS; ++test) {
			System.out.print( testText[test] );
			printTime(time_Tightdb[test], " ms (x1)", "\t");
			
			printTime(time_Array[test], " ms ", "");
			if (time_Tightdb[test] > 0)
				System.out.print( "(x" + time_Array[test] / time_Tightdb[test] + ")\t");
			else
				System.out.print("\t");
			
			printTime(time_Sqlite[test], " ms ", "");
			if (time_Tightdb[test] > 0)
				System.out.print( "(x" + time_Sqlite[test] / time_Tightdb[test] + ")\t");
			else
				System.out.print("\t");
			
			System.out.println();
		}
	}

	static void printTime(long time, String str, String tab) {
		if (time > 0)
			System.out.printf("%5d%s%s", time, str, tab);
		else
			System.out.print("   -- \t" + tab);
	}
	
   	public static long[] TestPerformance(PerformanceTest test, int rows) 
	{
   		Timer 		timer = new Timer();
		long[]		durations = new long[TESTS];
		int			testNo = 0;
		
		test.buildTable(rows);
		// System.out.printf("Memory usage: %d bytes\n", 0); //, (long long)GetMemUsage()); 
		
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
		    durations[testNo++] = timer.GetTimeInMs();
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
		    durations[testNo++] = timer.GetTimeInMs();
		    test.end_findByteInt();
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
		    durations[testNo++] = timer.GetTimeInMs();
		    test.end_findString();
		    System.out.printf("*");
		}
/*
		// Add index, and search
		{
		    timer.Start();

		    boolean indexSupported = test.addIndex();
		    
		    if (indexSupported) {
		    	durations[testNo++] = timer.GetTimeInMs();
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
			    durations[testNo++] = timer.GetTimeInMs();
			    test.end_findIntWithIndex();
			    System.out.printf("*");
			}
		}
*/		
		test.closeTable();
		
		return durations;
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

