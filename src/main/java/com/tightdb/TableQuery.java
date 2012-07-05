package com.tightdb;


public class TableQuery {
	public TableQuery(){
		this.nativePtr = createNativePtr();
	}
	
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

	public TableQuery parent(){
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
		return equal(columnIndex, value, true);
	}
	public TableQuery eq(long columnIndex, String value){
		return equal(columnIndex, value, true);
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
		return notEqual(columnIndex, value, true);
	}
	public TableQuery neq(long columnIndex, String value){
		return notEqual(columnIndex, value, true);
	}
	protected native void nativeNotEqual(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);

	public TableQuery beginsWith(long columnIndex, String value, boolean caseSensitive){
		nativeBeginsWith(nativePtr, columnIndex, value, caseSensitive);
		return this;
	}
	public TableQuery beginsWith(long columnIndex, String value){
		return beginsWith(columnIndex, value, true);
	}
	protected native void nativeBeginsWith(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);
	
	public TableQuery endsWith(long columnIndex, String value, boolean caseSensitive){
		nativeEndsWith(nativePtr, columnIndex, value, caseSensitive);
		return this;
	}
	public TableQuery endsWith(long columnIndex, String value){
		return endsWith(columnIndex, value, true);
	}
	protected native void nativeEndsWith(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);
	
	public TableQuery contains(long columnIndex, String value, boolean caseSensitive){
		nativeContains(nativePtr, columnIndex, value, caseSensitive);
		return this;
	}
	public TableQuery contains(long columnIndex, String value){
		return contains(columnIndex, value, true);
	}
	protected native void nativeContains(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);
	
	// Searching methods.
	
	public long findNext(TableBase table, long lastMatch){
		return nativeFindNext(nativePtr, table, table.nativePtr, lastMatch);
	}
	
	protected native long nativeFindNext(long nativeQueryPtr, TableBase table, long tableNativePtr, long lastMatch);
	
	public long findNext(TableBase table){
		return findNext(table, util.INFINITE);
	}
	
	public TableViewBase findAll(TableBase tableBase, long start, long end, long limit){
		return new TableViewBase(tableBase, nativeFindAll(nativePtr, tableBase, tableBase.nativePtr, start, end, limit));
	}
	
	protected long nativeFindAll(long nativeQueryPtr, TableBase tableBase, long start, long end, long limit){
		return nativeFindAll(nativeQueryPtr, tableBase, tableBase.nativePtr, start, end, limit);
	}

	protected native long nativeFindAll(long nativeQueryPtr, TableBase table, long tableNativePtr, long start, long end, long limit);
	
	public TableViewBase findAll(TableBase tableBase){
		return findAll(tableBase, 0L, util.INFINITE, util.INFINITE);
	}
	
	// Aggregation methods
	
	public long sum(TableBase table, long columnIndex, long start, long end, long limit){
		return nativeSum(nativePtr, table, table.nativePtr, columnIndex, start, end, limit);
	}
	
	protected native long nativeSum(long nativeQueryPtr, TableBase table, long tableNativePtr, long columnIndex, long start, long end, long limit);
	
	public long sum(TableBase tableBase, long columnIndex){
		return sum(tableBase, columnIndex, 0L, util.INFINITE, util.INFINITE);
	}
	
	public long maximum(TableBase table, long columnIndex, long start, long end, long limit){
		return nativeMaximum(nativePtr, table, table.nativePtr, columnIndex, start, end, limit);
	}
	
	protected native long nativeMaximum(long nativeQueryPtr, TableBase table, long tableNativePtr, long columnIndex, long start, long end, long limit);
	
	public long maximum(TableBase tableBase, long columnIndex){
		return maximum(tableBase, columnIndex, 0L, util.INFINITE, util.INFINITE);
	}
	
	public long minimum(TableBase table, long columnIndex, long start, long end, long limit){
		return nativeMinimum(nativePtr, table, table.nativePtr, columnIndex, start, end, limit);
	}
	
	protected native long nativeMinimum(long nativeQueryPtr, TableBase table, long tableNativePtr, long columnIndex, long start, long end, long limit);
	
	public long minimum(TableBase tableBase, long columnIndex){
		return minimum(tableBase, columnIndex, 0L, util.INFINITE, util.INFINITE);
	}
	
	public double average(TableBase table, long columnIndex, long start, long end, long limit){
		return nativeAverage(nativePtr, table, table.nativePtr, columnIndex, start, end, limit);
	}
	
	protected native double nativeAverage(long nativeQueryPtr, TableBase table, long tableNativePtr, long columnIndex, long start, long end, long limit);

	public double average(TableBase tableBase, long columnIndex){
		return average(tableBase, columnIndex, 0L, util.INFINITE, util.INFINITE);
	}
	
	public long count(TableBase table, long start, long end, long limit){
		return nativeCount(nativePtr, table, table.nativePtr, start, end, limit);
	}
	
	protected native long nativeCount(long nativeQueryPtr, TableBase table, long tableNativePtr, long start, long end, long limit);

	public long count(TableBase tableBase){
		return count(tableBase, 0L, util.INFINITE, util.INFINITE);
	}
	
	// Deletion.
	public void remove(TableBase tableBase, long start, long end, long limit){
		nativeRemove(nativePtr, tableBase, tableBase.nativePtr, start, end, limit);
	}
	
	public void remove(TableBase tableBase){
		remove(tableBase, 0L, util.INFINITE, util.INFINITE);
	}
	
	protected native void nativeRemove(long nativeQueryPtr, TableBase tableBase, long tableBaseNativePtr, long start, long end, long limit);
	
	public String getErrorCode(){
		return nativeGetErrorCode(nativePtr);
	}

	protected native String nativeGetErrorCode(long nativePtr);

	public TableViewBase findAllMulti(TableBase table, long start, long end){
		return new TableViewBase(table, nativeFindAllMulti(nativePtr, table, table.nativePtr, start, end));
	}
	
	protected native long nativeFindAllMulti(long nativeQueryPtr, TableBase tableBase, long tableNativePtr, long start, long end);
	
	public TableViewBase findAllMulti(TableBase tableBase){
		return findAllMulti(tableBase, 0L, util.INFINITE);
	}
	
	public int setThreads(int threadCount){
		return nativeSetThreads(nativePtr, threadCount);
	}
	
	protected native int nativeSetThreads(long nativeQueryPtr, int threadCount);
	
	protected native long createNativePtr();
	
	protected long nativePtr;
}
