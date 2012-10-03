package com.tightdb;


public class TableQuery {
	
	protected long nativePtr;

	public TableQuery(){
		this.nativePtr = createNativePtr();
	}

	protected native long createNativePtr();
	
	// Grouping
	
	public TableQuery group(){
		nativeGroup(nativePtr);
		return this;
	}
	protected native void nativeGroup(long nativeQueryPtr);
	
	public TableQuery endGroup(){
		nativeEndGroup(nativePtr);
		return this;
	}
	protected native void nativeEndGroup(long nativeQueryPtr);
	
	public TableQuery subTable(long columnIndex){
		nativeSubTable(nativePtr, columnIndex);
		return this;
	}
	protected native void nativeSubTable(long nativeQueryPtr, long columnIndex);

	public TableQuery endSubTable(){
		nativeParent(nativePtr);
		return this;
	}
	public native void nativeParent(long nativeQueryPtr);

	public TableQuery or(){
		nativeOr(nativePtr);
		return this;
	}
	protected native void nativeOr(long nativeQueryPtr);

	// Query for integer values.

	public TableQuery equal(long columnIndex, long value){
		nativeEqual(nativePtr, columnIndex, value);
		return this;
	}
	public TableQuery eq(long columnIndex, long value){
		nativeEqual(nativePtr, columnIndex, value);
		return this;
	}
	protected native void nativeEqual(long nativeQueryPtr, long columnIndex, long value);
	
	public TableQuery notEqual(long columnIndex, long value){
		nativeNotEqual(nativePtr, columnIndex, value);
		return this;
	}
	public TableQuery neq(long columnIndex, long value){
		nativeNotEqual(nativePtr, columnIndex, value);
		return this;
	}
	protected native void nativeNotEqual(long nativeQueryPtr, long columnIndex, long value);
		
	public TableQuery greaterThan(long columnIndex, long value){
		nativeGreater(nativePtr, columnIndex, value);
		return this;
	}
	public TableQuery gt(long columnIndex, long value){
		nativeGreater(nativePtr, columnIndex, value);
		return this;
	}
	protected native void nativeGreater(long nativeQueryPtr, long columnIndex, long value);
	
	public TableQuery greaterThanOrEqual(long columnIndex, long value){
		nativeGreaterEqual(nativePtr, columnIndex, value);
		return this;
	}
	public TableQuery gte(long columnIndex, long value){
		nativeGreaterEqual(nativePtr, columnIndex, value);
		return this;
	}
	protected native void nativeGreaterEqual(long nativeQueryPtr, long columnIndex, long value);

	public TableQuery lessThan(long columnIndex, long value){
		nativeLess(nativePtr, columnIndex, value);
		return this;
	}
	public TableQuery lt(long columnIndex, long value){
		nativeLess(nativePtr, columnIndex, value);
		return this;
	}
	protected native void nativeLess(long nativeQueryPtr, long columnIndex, long value);
	
	public TableQuery lessThanOrEqual(long columnIndex, long value){
		nativeLessEqual(nativePtr, columnIndex, value);
		return this;
	}
	public TableQuery lte(long columnIndex, long value){
		nativeLessEqual(nativePtr, columnIndex, value);
		return this;
	}
	protected native void nativeLessEqual(long nativeQueryPtr, long columnIndex, long value);
	
	public TableQuery between(long columnIndex, long value1, long value2){
		nativeBetween(nativePtr, columnIndex, value1, value2);
		return this;
	}
	protected native void nativeBetween(long nativeQueryPtr, long columnIndex, long value1, long value2);
	
	// Query for boolean values.
	
	public TableQuery equal(long columnIndex, boolean value){
		nativeEqual(nativePtr, columnIndex, value);
		return this;
	}
	public TableQuery eq(long columnIndex, boolean value){
		nativeEqual(nativePtr, columnIndex, value);
		return this;
	}
	protected native void nativeEqual(long nativeQueryPtr, long columnIndex, boolean value);
	
	// Query for String values.
	
	// Equal
	public TableQuery equal(long columnIndex, String value, boolean caseSensitive){
		nativeEqual(nativePtr, columnIndex, value, caseSensitive);
		return this;
	}
	public TableQuery eq(long columnIndex, String value, boolean caseSensitive){
		nativeEqual(nativePtr, columnIndex, value, caseSensitive);
		return this;
	}
	public TableQuery equal(long columnIndex, String value){
		nativeEqual(nativePtr, columnIndex, value, true);
		return this;
	}
	public TableQuery eq(long columnIndex, String value){
		nativeEqual(nativePtr, columnIndex, value, true);
		return this;
	}
	protected native void nativeEqual(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);
	
	// Not Equal
	public TableQuery notEqual(long columnIndex, String value, boolean caseSensitive){
		nativeNotEqual(nativePtr, columnIndex, value, caseSensitive);
		return this;
	}
	public TableQuery neq(long columnIndex, String value, boolean caseSensitive){
		nativeNotEqual(nativePtr, columnIndex, value, caseSensitive);
		return this;
	}
	public TableQuery notEqual(long columnIndex, String value){
		nativeNotEqual(nativePtr, columnIndex, value, true);
		return this;
	}
	public TableQuery neq(long columnIndex, String value){
		nativeNotEqual(nativePtr, columnIndex, value, true);
		return this;
	}
	protected native void nativeNotEqual(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);

	public TableQuery beginsWith(long columnIndex, String value, boolean caseSensitive){
		nativeBeginsWith(nativePtr, columnIndex, value, caseSensitive);
		return this;
	}
	public TableQuery beginsWith(long columnIndex, String value){
		nativeBeginsWith(nativePtr, columnIndex, value, true);
		return this;
	}
	protected native void nativeBeginsWith(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);
	
	public TableQuery endsWith(long columnIndex, String value, boolean caseSensitive){
		nativeEndsWith(nativePtr, columnIndex, value, caseSensitive);
		return this;
	}
	public TableQuery endsWith(long columnIndex, String value){
		nativeEndsWith(nativePtr, columnIndex, value, true);
		return this;
	}
	protected native void nativeEndsWith(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);
	
	public TableQuery contains(long columnIndex, String value, boolean caseSensitive){
		nativeContains(nativePtr, columnIndex, value, caseSensitive);
		return this;
	}
	public TableQuery contains(long columnIndex, String value){
		nativeContains(nativePtr, columnIndex, value, true);
		return this;
	}
	protected native void nativeContains(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);
	
	
	// Searching methods.
	
	public long findNext(TableBase table, long lastMatch){
		return nativeFindNext(nativePtr, table, table.nativePtr, lastMatch);
	}
	
	public long findNext(TableBase table){
		return nativeFindNext(nativePtr, table, table.nativePtr, util.INFINITE);
	}
	
	protected native long nativeFindNext(long nativeQueryPtr, TableBase table, long tableNativePtr, long lastMatch);
	
	public TableViewBase findAll(TableBase table, long start, long end, long limit){
		return new TableViewBase(table, nativeFindAll(nativePtr, table, table.nativePtr, start, end, limit));
	}
	
	public TableViewBase findAll(TableBase table){
		return new TableViewBase(table, nativeFindAll(nativePtr, table, table.nativePtr, 0, util.INFINITE, util.INFINITE));
	}

	protected native long nativeFindAll(long nativeQueryPtr, TableBase table, long tableNativePtr, long start, long end, long limit);
	
	
	// Aggregation methods
	
	public long sum(TableBase table, long columnIndex, long start, long end){
		return nativeSum(nativePtr, table, table.nativePtr, columnIndex, start, end, util.INFINITE);
	}

	public long sum(TableBase table, long columnIndex){
		return nativeSum(nativePtr, table, table.nativePtr, columnIndex, 0, util.INFINITE, util.INFINITE);
	}
	
	protected native long nativeSum(long nativeQueryPtr, TableBase table, long tableNativePtr, long columnIndex, long start, long end, long limit);	

	public long maximum(TableBase table, long columnIndex, long start, long end){
		return nativeMaximum(nativePtr, table, table.nativePtr, columnIndex, start, end,  util.INFINITE);
	}
	
	public long maximum(TableBase table, long columnIndex){
		return nativeMaximum(nativePtr, table, table.nativePtr, columnIndex, 0, util.INFINITE, util.INFINITE);
	}
	
	protected native long nativeMaximum(long nativeQueryPtr, TableBase table, long tableNativePtr, long columnIndex, long start, long end, long limit);
	
	public long minimum(TableBase table, long columnIndex, long start, long end){
		return nativeMinimum(nativePtr, table, table.nativePtr, columnIndex, start, end, util.INFINITE);
	}
	
	public long minimum(TableBase table, long columnIndex){
		return nativeMinimum(nativePtr, table, table.nativePtr, columnIndex, 0, util.INFINITE, util.INFINITE);
	}
	
	protected native long nativeMinimum(long nativeQueryPtr, TableBase table, long tableNativePtr, long columnIndex, long start, long end, long limit);
	
	
	public double average(TableBase table, long columnIndex, long start, long end){
		return nativeAverage(nativePtr, table, table.nativePtr, columnIndex, start, end, util.INFINITE);
	}
	
	public double average(TableBase table, long columnIndex){
		return nativeAverage(nativePtr, table, table.nativePtr, columnIndex, 0, util.INFINITE, util.INFINITE);
	}
	
	protected native double nativeAverage(long nativeQueryPtr, TableBase table, long tableNativePtr, long columnIndex, long start, long end, long limit);

	
	public long count(TableBase table, long start, long end){
		return nativeCount(nativePtr, table, table.nativePtr, start, end, util.INFINITE);
	}
	
	public long count(TableBase table){
		return nativeCount(nativePtr, table, table.nativePtr, 0, util.INFINITE, util.INFINITE);
	}
	
	protected native long nativeCount(long nativeQueryPtr, TableBase table, long tableNativePtr, long start, long end, long limit);

	
	// Deletion.
	public long remove(TableBase table, long start, long end){
		return nativeRemove(nativePtr, table, table.nativePtr, start, end, util.INFINITE);
	}
	
	public long remove(TableBase table){
		return nativeRemove(nativePtr, table, table.nativePtr, 0, util.INFINITE, util.INFINITE);
	}
	
	protected native long nativeRemove(long nativeQueryPtr, TableBase table, long tableBaseNativePtr, long start, long end, long limit);
	
	public String getErrorCode(){
		return nativeGetErrorCode(nativePtr);
	}

	protected native String nativeGetErrorCode(long nativePtr);

	public TableViewBase findAllMulti(TableBase table, long start, long end){
		return new TableViewBase(table, nativeFindAllMulti(nativePtr, table, table.nativePtr, start, end));
	}
	
	public TableViewBase findAllMulti(TableBase table){
		return new TableViewBase(table, nativeFindAllMulti(nativePtr, table, table.nativePtr, 0, util.INFINITE));
	}
	
	protected native long nativeFindAllMulti(long nativeQueryPtr, TableBase table, long tableNativePtr, long start, long end);
	
	public int setThreads(int threadCount){
		return nativeSetThreads(nativePtr, threadCount);
	}
	
	protected native int nativeSetThreads(long nativeQueryPtr, int threadCount);
}
