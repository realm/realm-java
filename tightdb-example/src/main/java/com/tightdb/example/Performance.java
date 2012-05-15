package com.tightdb.example;

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
		    return (startTime - stopTime) / 1000;
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
	}

	public static void TestJava() 
	{
		Timer 		timer = new Timer();
		TestTable 	table = new TestTable();
		int 		MAX_SIZE = 2; //250000;
		
		System.out.println("Starting");
		
		
		// Build large table
		for (long i = 0; i < MAX_SIZE; ++i) {
		    // create random string
		     int n = (int) (rand() % 1000);
		     String s = number_name(n);
		
		    table.add(n, s, 100, Wed);
		}
		table.add(0, "abcde", 100, Wed);
		
//		System.out.printf("Memory usage: %lld bytes\n", (long long)GetMemUsage()); // %zu doesn't work in vc
		
		// Search small integer column
		{
		    timer.Start();
		
		    // Do a search over entire column (value not found)
		    for (long i = 0; i < 100; ++i) {
		         Test first = table.fourth.is(Wed).findFirst();
		        		 
		         if (first == null) {
		             System.out.printf("error");
		         }
		    }
		
		    long search_time = timer.GetTimeInMs();
		    System.out.printf("Search (small integer): %dms\n", search_time);
		}
		System.out.println("Finished.");
		
/*
		// Search byte-size integer column
		{
		    timer.Start();
		
		    // Do a search over entire column (value not found)
		    for (long i = 0; i < 100; ++i) {
		         long res = table.third.find_first(50);
		        if (res != -1) {
		            System.out.printf("error");
		        }
		    }
		
		     int search_time = timer.GetTimeInMs();
		    System.out.printf("Search (byte-size integer): %dms\n", search_time);
		}
		
		// Search string column
		{
		    timer.Start();
		
		    // Do a search over entire column (value not found)
		    for (long i = 0; i < 100; ++i) {
		         long res = table.second.find_first("abcde");
		        if (res != MAX_SIZE) {
		            System.out.printf("error");
		        }
		    }
		
		     int search_time = timer.GetTimeInMs();
		    System.out.printf("Search (string): %dms\n", search_time);
		}
		
		// Add index
		{
		    timer.Start();
		
		    table.setIndex(0);
		
		     int search_time = timer.GetTimeInMs();
		    System.out.printf("Add index: %dms\n", search_time);
		}
		
		//System.out.printf("Memory usage2: %lld bytes\n", (long long)GetMemUsage()); // %zu doesn't work in vc
		
		// Search with index
		{
		    timer.Start();
		
		    for (long i = 0; i < 100000; ++i) {
		         long n = rand() % 1000;
		         long res = table.first.find_first(n);
		        if (res == MAX_SIZE+2) { // to avoid above find being optimized away
		            System.out.printf("error");
		        }
		    }
		
		     int search_time = timer.GetTimeInMs();
		    System.out.printf("Search index: %dms\n", search_time);
		}	
*/
	}
}
