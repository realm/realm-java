package com.tightdb.performance;

public abstract interface IPerformance {

	public long usedNativeMemory();
	public void buildTable(int rows);
	
	public void begin_findSmallInt(int value);
	public boolean findSmallInt(int value);
	public void end_findSmallInt();
	
	public void begin_findByteInt(int value);
	public boolean findByteInt(int value);
	public void end_findByteInt();
	
	public void begin_findString(String value);
	public boolean findString(String value);
	public void end_findString();
	
	public boolean addIndex();
	
	public void begin_findIntWithIndex();
	public int findIntWithIndex(int value);
	public void end_findIntWithIndex();

	public void closeTable();
}

