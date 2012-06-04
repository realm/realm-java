package com.tightdb.performance;

import java.util.ArrayList;

public class JavaArrayList implements PerformanceTest {

	public static class Table
	{
		int 	indexInt;
        String 	second;
        int 	byteInt;
        int 	smallInt;

		public Table(int indexInt, String second, int byteInt, int smallInt) {
        	this.indexInt = indexInt;
        	this.second = second;
        	this.byteInt = byteInt;
        	this.smallInt = smallInt;
        }
	}
	
    private ArrayList<Table> table = null;
    private int Rows = 0;
    
    public JavaArrayList() {
    	table = new ArrayList<Table>();
    }
    
    public void buildTable(int rows) {
		for (int i = 0; i < rows; ++i) {
		    // create random string
		    int n = Util.getRandNumber();
		    String s = Util.getNumberString(n);
		    
		    table.add(new Table(n, s, Performance.BYTE_TEST_VAL, Performance.SMALL_TEST_VAL) );
		}
		this.Rows = rows;
    }
    
  //--------------- small Int
    
    public void begin_findSmallInt(int value) { }
    
    public boolean findSmallInt(int value) {
    	int index;
    	for (index = 0; index < Rows; index++) {
        	if (table.get(index).smallInt == value) {
        		break;
        	}
        }
    	return (index != Rows);	
    }
    
    public void end_findSmallInt() {}
    
    //--------------- byte Int
    
	public void begin_findByteInt(int value) {}

    public boolean findByteInt(int value) {
    	int index;
    	for (index = 0; index < Rows; index++) {
        	if (table.get(index).byteInt == value) {
        		break;
        	}
        }
    	return (index != Rows);	
    }
    
    public void end_findByteInt() {}
    
    //---------------- string
    
    public void begin_findString(String value) {}
    
    public boolean findString(String value) {
    	int index;
    	for (index = 0; index < Rows; index++) {
        	if (table.get(index).second.equalsIgnoreCase(value)) {
        		break;
        	}
        }
    	return (index != Rows);	
    }
    
    public void end_findString() {}
    
    //---------------- int with index
    
    public boolean addIndex() {
    	return false;
    }

	public void begin_findIntWithIndex() {}

	public int findIntWithIndex(int value) 
	{
		return -1;
	}
	
	public void end_findIntWithIndex() {}

	public void closeTable() {}
}
