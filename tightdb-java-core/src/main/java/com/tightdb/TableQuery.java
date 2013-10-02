package com.tightdb;

public class TableQuery {
    protected boolean DEBUG = false;

    protected long nativePtr;
    protected boolean immutable = false;

// TODO: Can we protect this?
    public TableQuery(long nativeQueryPtr, boolean immutable){
        if (DEBUG)
        	System.err.println("++++++ new TableQuery, ptr= " + nativeQueryPtr);
        this.immutable = immutable;
        this.nativePtr = nativeQueryPtr;
    }

    @Override
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    private synchronized void close() {
        if (DEBUG)
        	System.err.println("++++ Query CLOSE, ptr= " + nativePtr);
        if (nativePtr == 0) {
            return;
        }
        nativeClose(nativePtr);
        nativePtr = 0;
    }
    protected native void nativeClose(long nativeQueryPtr);

    // Query TableView
    public TableQuery tableview(TableView tv){
        nativeTableview(nativePtr, tv.nativePtr);
        return this;
    }
    protected native void nativeTableview(long nativeQueryPtr, long nativeTableViewPtr);

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
    protected native void nativeParent(long nativeQueryPtr);

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


    // Query for float values.

    public TableQuery equal(long columnIndex, float value){
        nativeEqual(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery eq(long columnIndex, float value){
        nativeEqual(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeEqual(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery notEqual(long columnIndex, float value){
        nativeNotEqual(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery neq(long columnIndex, float value){
        nativeNotEqual(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeNotEqual(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery greaterThan(long columnIndex, float value){
        nativeGreater(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery gt(long columnIndex, float value){
        nativeGreater(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeGreater(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery greaterThanOrEqual(long columnIndex, float value){
        nativeGreaterEqual(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery gte(long columnIndex, float value){
        nativeGreaterEqual(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeGreaterEqual(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery lessThan(long columnIndex, float value){
        nativeLess(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery lt(long columnIndex, float value){
        nativeLess(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeLess(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery lessThanOrEqual(long columnIndex, float value){
        nativeLessEqual(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery lte(long columnIndex, float value){
        nativeLessEqual(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeLessEqual(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery between(long columnIndex, float value1, float value2){
        nativeBetween(nativePtr, columnIndex, value1, value2);
        return this;
    }
    protected native void nativeBetween(long nativeQueryPtr, long columnIndex, float value1, float value2);


    // Query for double values.

    public TableQuery equal(long columnIndex, double value){
        nativeEqual(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery eq(long columnIndex, double value){
        nativeEqual(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeEqual(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery notEqual(long columnIndex, double value){
        nativeNotEqual(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery neq(long columnIndex, double value){
        nativeNotEqual(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeNotEqual(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery greaterThan(long columnIndex, double value){
        nativeGreater(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery gt(long columnIndex, double value){
        nativeGreater(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeGreater(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery greaterThanOrEqual(long columnIndex, double value){
        nativeGreaterEqual(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery gte(long columnIndex, double value){
        nativeGreaterEqual(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeGreaterEqual(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery lessThan(long columnIndex, double value){
        nativeLess(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery lt(long columnIndex, double value){
        nativeLess(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeLess(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery lessThanOrEqual(long columnIndex, double value){
        nativeLessEqual(nativePtr, columnIndex, value);
        return this;
    }
    public TableQuery lte(long columnIndex, double value){
        nativeLessEqual(nativePtr, columnIndex, value);
        return this;
    }
    protected native void nativeLessEqual(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery between(long columnIndex, double value1, double value2){
        nativeBetween(nativePtr, columnIndex, value1, value2);
        return this;
    }
    protected native void nativeBetween(long nativeQueryPtr, long columnIndex, double value1, double value2);


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

    public long find(long fromTableRow){
        return nativeFind(nativePtr, fromTableRow);
    }

    public long find(){
        return nativeFind(nativePtr, 0);
    }

    protected native long nativeFind(long nativeQueryPtr, long fromTableRow);

    public TableView findAll(long start, long end, long limit){
        return new TableView(nativeFindAll(nativePtr, start, end, limit), immutable);
    }

    public TableView findAll(){
        return new TableView(nativeFindAll(nativePtr, 0, Table.INFINITE, Table.INFINITE), immutable);
    }

    protected native long nativeFindAll(long nativeQueryPtr, long start, long end, long limit);

    //
    // Aggregation methods
    //

    // Integer aggregation

    public long sum(long columnIndex, long start, long end){
        return nativeSum(nativePtr, columnIndex, start, end, Table.INFINITE);
    }
    public long sum(long columnIndex){
        return nativeSum(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native long nativeSum(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public long maximum(long columnIndex, long start, long end){
        return nativeMaximum(nativePtr, columnIndex, start, end,  Table.INFINITE);
    }
    public long maximum(long columnIndex){
        return nativeMaximum(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native long nativeMaximum(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public long minimum(long columnIndex, long start, long end){
        return nativeMinimum(nativePtr, columnIndex, start, end, Table.INFINITE);
    }
    public long minimum(long columnIndex){
        return nativeMinimum(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native long nativeMinimum(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public double average(long columnIndex, long start, long end){
        return nativeAverage(nativePtr, columnIndex, start, end, Table.INFINITE);
    }
    public double average(long columnIndex){
        return nativeAverage(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeAverage(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    // float aggregation

    public double sumFloat(long columnIndex, long start, long end){
        return nativeSumFloat(nativePtr, columnIndex, start, end, Table.INFINITE);
    }
    public double sumFloat(long columnIndex){
        return nativeSumFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeSumFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public float maximumFloat(long columnIndex, long start, long end){
        return nativeMaximumFloat(nativePtr, columnIndex, start, end,  Table.INFINITE);
    }
    public float maximumFloat(long columnIndex){
        return nativeMaximumFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native float nativeMaximumFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public float minimumFloat(long columnIndex, long start, long end){
        return nativeMinimumFloat(nativePtr, columnIndex, start, end, Table.INFINITE);
    }
    public float minimumFloat(long columnIndex){
        return nativeMinimumFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native float nativeMinimumFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public double averageFloat(long columnIndex, long start, long end){
        return nativeAverageFloat(nativePtr, columnIndex, start, end, Table.INFINITE);
    }
    public double averageFloat(long columnIndex){
        return nativeAverageFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeAverageFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    // double aggregation

    public double sumDouble(long columnIndex, long start, long end){
        return nativeSumDouble(nativePtr, columnIndex, start, end, Table.INFINITE);
    }
    public double sumDouble(long columnIndex){
        return nativeSumDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeSumDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public double maximumDouble(long columnIndex, long start, long end){
        return nativeMaximumDouble(nativePtr, columnIndex, start, end,  Table.INFINITE);
    }
    public double maximumDouble(long columnIndex){
        return nativeMaximumDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeMaximumDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public double minimumDouble(long columnIndex, long start, long end){
        return nativeMinimumDouble(nativePtr, columnIndex, start, end, Table.INFINITE);
    }
    public double minimumDouble(long columnIndex){
        return nativeMinimumDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeMinimumDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public double averageDouble(long columnIndex, long start, long end){
        return nativeAverageDouble(nativePtr, columnIndex, start, end, Table.INFINITE);
    }
    public double averageDouble(long columnIndex){
        return nativeAverageDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeAverageDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    // count

    // TODO: Rename all start, end parameter names to firstRow, lastRow
    public long count(long start, long end){
        return nativeCount(nativePtr, start, end, Table.INFINITE);
    }
    public long count(){
        return nativeCount(nativePtr, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native long nativeCount(long nativeQueryPtr, long start, long end, long limit);


    // Deletion.
    public long remove(long start, long end){
        if (immutable) throwImmutable();
        return nativeRemove(nativePtr, start, end, Table.INFINITE);
    }

    public long remove(){
        if (immutable) throwImmutable();
        return nativeRemove(nativePtr, 0, Table.INFINITE, Table.INFINITE);
    }

    protected native long nativeRemove(long nativeQueryPtr, long start, long end, long limit);

    private void throwImmutable()
    {
        throw new IllegalStateException("Mutable method call during read transaction.");
    }
}
