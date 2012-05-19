package com.tightdb.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.math.*;

import com.tightdb.generated.*;

import com.tightdb.lib.AbstractColumn;
import com.tightdb.lib.NestedTable;
import com.tightdb.lib.Table;
import com.tightdb.lib.TightDB;


public class Performance {
	
	static class Timer {

		static long startTime;
	    static String timerDesc;
	
		public void start(String desc)
		{
			timerDesc = desc;
		    startTime = System.nanoTime();
		}
		public void stop() 
		{
		    long lStop = System.nanoTime();
		    System.out.println(timerDesc + "=" + (lStop - startTime)/1000 + " ms");
		}
		
		public void Start() 
		{
			startTime = System.nanoTime();
		}
		
		public long GetTimeInMs() 
		{
		    long stopTime = System.nanoTime();
		    return (stopTime - startTime) / 1000000;
		}	
	}	

	public static String number_name(int n)
	{
	    String ones[] = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
	                                 "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
	                                 "eighteen", "nineteen"};
	    String tens[] = {"", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
	
	    String txt = null;
	    if (n >= 1000) {
	        txt = number_name(n/1000) + " thousand ";
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
	
    static long rand() 
	{
		return (long)(Math.random() * 1000);
	}
	
    static int Mon = 0;
    static int Tue = 1;
    static int Wed = 2;
    static int Thu = 3;
    static int Fri = 4;
    static int Sat = 5;
    static int Sun = 6;
	
    @Table
	class test
	{
		int first;
        String second;
        int third;
        int fourth; // enum Days
	}

	public static void TestTightdb(int totalRows) 
	{
		Timer 		timer = new Timer();
		TestTable 	table = new TestTable();
		long 		dummy = 0;
		
		System.out.println("\nTest TightDB: -------------------------");
		
		{
			// Build large table
			for (int i = 0; i < totalRows; ++i) {
			    // create random string
			     int n = (int) (rand() % 1000);
			     String s = number_name(n);
			
			    table.add(n, s, 100, Wed);
			}
			table.add(0, "abcde", 123, Thu);
			
			System.out.printf("Added %d rows.\n", totalRows); 
			System.out.printf("Memory usage (tigthdb): 10176 bytes\n"); // in Tightdb without Java

			//for (long i = 0; i < Math.min(100, table.size()); ++i)
			//	TightDB.print("row: ", table.at(i));
		}
		
		// Search small integer column
		{
		    timer.Start();
		
		    // Do a search over entire column (value not found)
		    TestQuery q = table.fourth.is(Tue);
		    for (int i = 0; i < 100; ++i) {
		         Test res = q.findFirst();
		         if (res != null) {
		             System.out.printf("error !! %d", res.getPosition());
		             break;
		         }
		    }
		
		    long search_time = timer.GetTimeInMs();
		    System.out.printf("Search (small integer): %d ms\n", search_time);
		}

		// Search byte-size integer column
		{
		    timer.Start();
		
		    // Do a search over entire column (value not found)
		    for (int i = 0; i < 100; ++i) {
		        Test res = table.third.is(50).findFirst();
		        if (res != null) {
		            System.out.printf("error");
		            break;
		        }
		    }
		
		     long search_time = timer.GetTimeInMs();
		    System.out.printf("Search (byte-size integer): %d ms\n", search_time);
		}
		
		// Search string column
		{
		    timer.Start();
		    
		    // Do a search over entire column (value not found)
		    for (int i = 0; i < 100; ++i) {
		        Test res = table.second.is("abcde").findFirst();
		        
		        //TightDB.print("row: ", res);
		        long row = res.getPosition();
		        dummy += row;
		        if (row != totalRows) {
		        	//  System.out.printf("error %d. ", res.getPosition());
		          //  break;
		        }
		    }
		
		    long search_time = timer.GetTimeInMs();
		    System.out.printf("Search (string): %d ms.\n", search_time);
		}
/*		
		// Add index
		{
		    timer.Start();
		
		  // ??? missing currently:  table.setIndex(0);
		
		    long search_time = timer.GetTimeInMs();
		    System.out.printf("Add index: %dms\n", search_time);
		}
		
		//System.out.printf("Memory usage2: %d bytes\n", (long long)GetMemUsage());
		
		// Search with index
		{
			long dummy = 0;
		    timer.Start();
		
		    for (int i = 0; i < 100000; ++i) {
		        long n = rand() % 1000;
		        Test res = table.first.is(n).findFirst();
		        long row = res.getPosition();
		        dummy += row;
		    }
		
		    long search_time = timer.GetTimeInMs();
		    System.out.printf("Search index: %d ms.  %d\n", search_time, dummy);
		}
*/
	}

	
	public static class test2
	{
		int first;
        String second;
        int third;
        int fourth; // enum Days

		public test2(int first, String second, int third, int fourth) {
        	this.first = first;
        	this.second = second;
        	this.third = third;
        	this.fourth = fourth;
        }
	}
	
	
	public static void TestJavaArray(int totalRows) 
	{
		Timer 				timer = new Timer();
		ArrayList<test2> 	table = new ArrayList<test2>();
	
		System.out.println("\nTest Java ArrayList: -------------------------");
		
		{
			// Build large table
			for (int i = 0; i < totalRows; ++i) {
			    // create random string
			    int n = (int) (rand() % 1000);
			    String s = number_name(n);
			
			    table.add(new test2(n, s, 100, Wed));
			}
			table.add(new test2(0, "abcde", 100, Wed));
			
			System.out.printf("Added %d rows.\n", totalRows); 
			System.out.printf("Memory usage (Java): ??? bytes\n");

		}
		
        int index = 0;
        
		// Search small integer column
		{
		    timer.Start();
		
		    // Do a search over entire column (value not found)
		    for (int i = 0; i < 100; ++i) {
		    	for (index = 0; index < totalRows; index++) {
		        	if (table.get(index).fourth == Tue) {
		        		break;
		        	}
		        }
		    	if (index != totalRows) {
		            System.out.printf("error");
		            break;
		        }
		    }
		
		    long search_time = timer.GetTimeInMs();
		    System.out.printf("Search (small integer): %d ms\n", search_time);
		}

		// Search byte-size integer column
		{
		    timer.Start();
		
		    // Do a search over entire column (value not found)
		    for (int i = 0; i < 100; ++i) {
		    	for (index = 0; index < totalRows; index++) {
		        	if (table.get(index).third == 50) {
		        		break;
		        	}
		        }
		    	if (index != totalRows) {
		            System.out.printf("error");
		            break;
		        }
		    }
		
		     long search_time = timer.GetTimeInMs();
		    System.out.printf("Search (byte-size integer): %d ms\n", search_time);
		}
		
		// Search string column
		{
		    timer.Start();
		
		    // Do a search over entire column (value not found)
		    for (int i = 0; i < 100; ++i) {
		    	for (index = 0; index < totalRows; index++) {
		        	if (table.get(index).second.equalsIgnoreCase("abcde")) {
		        		break;
		        	}
		        }
		    	if (index != totalRows - 1) {
		            System.out.printf("error %d != %d", index, totalRows);
		            break;
		        }
		        /*
		        TightDB.print("row: ", res);
		        if (res.getPosition() != totalRows) {
		            System.out.printf("error %d. ", res.getPosition());
		            break;
		        }
		        */
		    }
		    long search_time = timer.GetTimeInMs();
		    System.out.printf("Search (string): %d ms\n", search_time);
		}
	}
}
