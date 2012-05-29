package com.tightdb.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.math.*;

import com.tightdb.generated.Test;
import com.tightdb.generated.TestQuery;
import com.tightdb.generated.TestTable;
import com.tightdb.lib.Table;
import com.tightdb.lib.TightDB;

public class Performance {
	
	public static void main(String[] args) {
		// Compare Tightdb performance against a Java ArrayList
		Performance.TestTightdb(250000);
		Performance.TestJavaArray(250000);
	}

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

	public static String number_name(long nlong)
	{
	    String ones[] = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
	                                 "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
	                                 "eighteen", "nineteen"};
	    String tens[] = {"", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
	
	    int n = (int)nlong;
	    
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
    /* 
	enum Days {
	Mon,
	Tue,
	Wed,
	Thu,
	Fri,
	Sat,
	Sun
	};
*/
	
    @Table
	class test
	{
		int first;
        String second;
        int third;
        int fourth; // enum Days
        test(int first, String second, int third, int fourth) {
        	this.first = first;
        	this.second = second;
        	this.third = third;
        	this.fourth = fourth;
        }
	}

	public static void TestTightdb(int totalRows) 
	{
		Timer 		timer = new Timer();
		TestTable 	table = new TestTable();
		int 		MAX_SIZE = totalRows;
		
		System.out.println("\nTest TightDB: -------------------------");
		
		{
			timer.Start();
			
			// Build large table
			for (int i = 0; i < MAX_SIZE; ++i) {
			    // create random string
			     int n = (int) (rand() % 1000);
			     String s = number_name(n);
			
			    table.add(n, s, 100, Wed);
			}
			table.add(0, "abcde", 123, Thu);
			
			long add_time = timer.GetTimeInMs();
			System.out.printf("Added %d rows in %d ms.\n", MAX_SIZE, add_time); 
			System.out.printf("Memory usage: %d bytes\n", 0); //, (long long)GetMemUsage()); // %zu doesn't work in vc
			
			if (false) {
				for (long i = 0; i < Math.min(100, table.size()); ++i) {
					TightDB.print("row: ", table.at(i));
				}
			}
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
		    long dummy = 0;
		    
		    // Do a search over entire column (value not found)
		    for (int i = 0; i < 100; ++i) {
		        Test res = table.second.is("abcde").findFirst();
		        
		        //TightDB.print("row: ", res);
		        long row = res.getPosition();
		        dummy += row;
		        if (row != MAX_SIZE) {
		        	//  System.out.printf("error %d. ", res.getPosition());
		          //  break;
		        }
		    }
		
		    long search_time = timer.GetTimeInMs();
		    System.out.printf("Search (string): %d ms.   %d\n", search_time, dummy);
		}
		
		// Add index
		{
		    timer.Start();
		
		  // ??? missing currently:  table.setIndex(0);
		
		    long search_time = timer.GetTimeInMs();
		    System.out.printf("Add index: %dms\n", search_time);
		}
		
		//System.out.printf("Memory usage2: %lld bytes\n", (long long)GetMemUsage()); // %zu doesn't work in vc
		
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
		int 				MAX_SIZE = totalRows;
		
		System.out.println("\nTest Java ArrayList: -------------------------");
		
		{
			timer.Start();
			
			// Build large table
			for (int i = 0; i < MAX_SIZE; ++i) {
			    // create random string
			    int n = (int) (rand() % 1000);
			    String s = number_name(n);
			
			    table.add(new test2(n, s, 100, Wed));
			}
			table.add(new test2(0, "abcde", 100, Wed));
			
			long add_time = timer.GetTimeInMs();
			System.out.printf("Added %d rows in %d ms.\n", MAX_SIZE, add_time); 
			System.out.printf("Memory usage: %d bytes\n", 0); //, (long long)GetMemUsage()); // %zu doesn't work in vc

		}
		
		int totalElements = table.size();
        int index = 0;
        
		// Search small integer column
		{
		    timer.Start();
		
		    // Do a search over entire column (value not found)
		    for (int i = 0; i < 100; ++i) {
		    	for (index = 0; index < totalElements; index++) {
		        	if (table.get(index).fourth == Tue) {
		        		break;
		        	}
		        }
		    	if (index != totalElements) {
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
		    	for (index = 0; index < totalElements; index++) {
		        	if (table.get(index).third == 50) {
		        		break;
		        	}
		        }
		    	if (index != totalElements) {
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
		    	for (index = 0; index < totalElements; index++) {
		        	if (table.get(index).second.equalsIgnoreCase("abcde")) {
		        		break;
		        	}
		        }
		    	if (index != totalElements - 1) {
		            System.out.printf("error %d != %d", index, totalElements);
		            break;
		        }
		        /*
		        TightDB.print("row: ", res);
		        if (res.getPosition() != MAX_SIZE) {
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
