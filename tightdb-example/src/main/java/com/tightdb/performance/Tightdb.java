package com.tightdb.performance;

import com.tightdb.util;
import com.tightdb.generated.Test;
import com.tightdb.generated.TestTable;
import com.tightdb.lib.Table;

public class Tightdb extends PerformanceBase implements IPerformance {

    @Table
	class test
	{
		int 	indexInt;
        String 	second;
        int 	byteInt;
        int 	smallInt;
	}
    
    private TestTable table = null;
	
    public Tightdb() {
    	table = new TestTable();	
    }
    
    public long usedNativeMemory() {
    	return 0; //util.getNativeMemUsage();
    }
    
    public void buildTable(int rows) {
		for (int i = 0; i < rows; ++i) {
		    // create random string
		    int n = Util.getRandNumber();
		    String s = Util.getNumberString(n);
		    
		    table.add(n, s, Performance.BYTE_TEST_VAL, Performance.SMALL_TEST_VAL);
		}
		//table.add(0, "abcde", 123, Thu);
    }
    
    //--------------- small Int
    
    public void begin_findSmallInt(int value) {
    	//TestQuery q = table.smallInt.eq(value);
    }
    
    public boolean findSmallInt(int value) {
    	//Test res = q.findFirst();
    	Test res = table.smallInt.findFirst(value);	
    	return (res != null);
    }
    
     //--------------- byte Int

    public boolean findByteInt(int value) {
    	Test res = table.byteInt.eq(value).findFirst();	
        return (res != null);
    }
    
    //---------------- string
    
    public boolean findString(String value) {
    	Test res = table.second.eq(value).findFirst();	
        return (res != null);
    }
    
    //---------------- int with index
    
    public boolean addIndex() {
    	return false;
    }
   
	public int findIntWithIndex(int value) 
	{
        Test res = table.indexInt.eq(value).findFirst();
		return (res != null) ? (int)res.getPosition() : -1;
	}
}
