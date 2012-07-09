package com.tightdb.performance;

import com.tightdb.util;
import com.tightdb.lib.Table;
import com.tightdb.performance.generated.Test;
import com.tightdb.performance.generated.TestTable;

public class Tightdb extends PerformanceBase implements IPerformance {

    @Table
	class test
	{
		int 	indexInt;
        String 	second;
        int 	byteInt;
        int 	smallInt;
        long 	longInt;
	}
    
    private TestTable table = null;
	
    public Tightdb() {
    	table = new TestTable();	
    }
    
    public long usedNativeMemory() {
    	return util.getNativeMemUsage();
    }
    
    public void buildTable(int rows) {
		for (int i = 0; i < rows; ++i) {
		    int n = Util.getRandNumber();
		    table.add(n, Util.getNumberString(n), Performance.BYTE_TEST_VAL, Performance.SMALL_TEST_VAL, Performance.LONG_TEST_VAL);
		}
    }
    
    //--------------- small Int
    
    public void begin_findSmallInt(long value) {
    	//TestQuery q = table.smallInt.eq(value);
    }
    
    public boolean findSmallInt(long value) {
    	//Test res = q.findFirst();
    	Test res = table.smallInt.findFirst(value);	
    	return (res != null);
    }
    
    //--------------- byte Int

    public boolean findByteInt(long value) {
    	Test res = table.byteInt.findFirst(value);		
        return (res != null);
    }
    
    //--------------- long Int

    public boolean findLongInt(long value) {
    	Test res = table.longInt.findFirst(value);		
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
   
	public long findIntWithIndex(long value) 
	{
        Test res = table.indexInt.findFirst(value);
		return (res != null) ? (int)res.getPosition() : -1;
	}
}
