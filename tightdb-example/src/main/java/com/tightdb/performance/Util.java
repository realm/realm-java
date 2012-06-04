package com.tightdb.performance;

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
	
}
