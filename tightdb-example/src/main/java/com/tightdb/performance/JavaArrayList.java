package com.tightdb.performance;

import java.util.ArrayList;

public class JavaArrayList extends PerformanceBase implements IPerformance {

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
    
    public long usedNativeMemory() {
    	return 0;
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
    
    public boolean findSmallInt(int value) {
    	int index;
    	for (index = 0; index < Rows; index++) {
        	if (table.get(index).smallInt == value) {
        		break;
        	}
        }
    	return (index != Rows);	
    }
    
    //--------------- byte Int
 
    public boolean findByteInt(int value) {
    	int index;
    	for (index = 0; index < Rows; index++) {
        	if (table.get(index).byteInt == value) {
        		break;
        	}
        }
    	return (index != Rows);	
    }
    
    //---------------- string
    
    public boolean findString(String value) {
    	int index;
    	for (index = 0; index < Rows; index++) {
        	if (table.get(index).second.equalsIgnoreCase(value)) {
        		break;
        	}
        }
    	return (index != Rows);	
    }
    
    //---------------- int with index
    
    public boolean addIndex() {
    	return false;
    }

	public int findIntWithIndex(int value) 
	{
		return -1;
	}
}
