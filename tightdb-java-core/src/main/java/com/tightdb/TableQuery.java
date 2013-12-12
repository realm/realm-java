package com.tightdb;

import java.util.Date;

public class TableQuery {
    protected boolean DEBUG = false;

    protected long nativePtr;
    protected boolean immutable = false;
    private Context context = null;

    private boolean queryValidated = true;

    // TODO: Can we protect this?
    public TableQuery(Context context, long nativeQueryPtr, boolean immutable){
        if (DEBUG)
            System.err.println("++++++ new TableQuery, ptr= " + nativeQueryPtr);
        this.context = context;
        this.immutable = immutable;
        this.nativePtr = nativeQueryPtr;
    }

    protected void finalize() {
        if (nativePtr != 0)
            context.asyncDisposeQuery(nativePtr);
    }

    public void close() {
        if (DEBUG)
            System.err.println("++++ Query CLOSE, ptr= " + nativePtr);
        if (nativePtr == 0) {
            return;
        }
        nativeClose(nativePtr);
        nativePtr = 0;
    }
    protected static native void nativeClose(long nativeQueryPtr);

    /**
     * Checks in core if query syntax is valid. Throws exception, if not.
     */
    private void validateQuery(){
        if (! queryValidated) { // If not yet validated, check if syntax is valid
            String invalidMessage = nativeValidateQuery(nativePtr);
            if (invalidMessage.equals(""))
                queryValidated = true; // If empty string error message, query is valid
            else
                throw new UnsupportedOperationException(invalidMessage);
        }
    }

    protected native String nativeValidateQuery(long nativeQueryPtr);

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
        queryValidated = false;
        return this;
    }
    protected native void nativeEndGroup(long nativeQueryPtr);

    public TableQuery subTable(long columnIndex){
        throw new RuntimeException("Query.subTable(), Not implemented yet!");
        //nativeSubTable(nativePtr, columnIndex);
        //return this;
    }
    protected native void nativeSubTable(long nativeQueryPtr, long columnIndex);

    public TableQuery endSubTable(){
        nativeParent(nativePtr);
        queryValidated = false;
        return this;
    }
    protected native void nativeParent(long nativeQueryPtr);

    public TableQuery or(){
        nativeOr(nativePtr);
        queryValidated = false;
        return this;
    }
    protected native void nativeOr(long nativeQueryPtr);

    // Query for integer values.

    public TableQuery equalTo(long columnIndex, long value){
        nativeEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeEqual(long nativeQueryPtr, long columnIndex, long value);

    public TableQuery notEqualTo(long columnIndex, long value){
        nativeNotEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeNotEqual(long nativeQueryPtr, long columnIndex, long value);

    public TableQuery greaterThan(long columnIndex, long value){
        nativeGreater(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeGreater(long nativeQueryPtr, long columnIndex, long value);

    public TableQuery greaterThanOrEqual(long columnIndex, long value){
        nativeGreaterEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeGreaterEqual(long nativeQueryPtr, long columnIndex, long value);

    public TableQuery lessThan(long columnIndex, long value){
        nativeLess(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeLess(long nativeQueryPtr, long columnIndex, long value);

    public TableQuery lessThanOrEqual(long columnIndex, long value){
        nativeLessEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeLessEqual(long nativeQueryPtr, long columnIndex, long value);

    public TableQuery between(long columnIndex, long value1, long value2){
        nativeBetween(nativePtr, columnIndex, value1, value2);
        queryValidated = false;
        return this;
    }
    protected native void nativeBetween(long nativeQueryPtr, long columnIndex, long value1, long value2);


    // Query for float values.

    public TableQuery equalTo(long columnIndex, float value){
        nativeEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeEqual(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery notEqualTo(long columnIndex, float value){
        nativeNotEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeNotEqual(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery greaterThan(long columnIndex, float value){
        nativeGreater(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeGreater(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery greaterThanOrEqual(long columnIndex, float value){
        nativeGreaterEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeGreaterEqual(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery lessThan(long columnIndex, float value){
        nativeLess(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeLess(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery lessThanOrEqual(long columnIndex, float value){
        nativeLessEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeLessEqual(long nativeQueryPtr, long columnIndex, float value);

    public TableQuery between(long columnIndex, float value1, float value2){
        nativeBetween(nativePtr, columnIndex, value1, value2);
        queryValidated = false;
        return this;
    }
    protected native void nativeBetween(long nativeQueryPtr, long columnIndex, float value1, float value2);


    // Query for double values.

    public TableQuery equalTo(long columnIndex, double value){
        nativeEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeEqual(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery notEqualTo(long columnIndex, double value){
        nativeNotEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeNotEqual(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery greaterThan(long columnIndex, double value){
        nativeGreater(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeGreater(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery greaterThanOrEqual(long columnIndex, double value){
        nativeGreaterEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeGreaterEqual(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery lessThan(long columnIndex, double value){
        nativeLess(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeLess(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery lessThanOrEqual(long columnIndex, double value){
        nativeLessEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeLessEqual(long nativeQueryPtr, long columnIndex, double value);

    public TableQuery between(long columnIndex, double value1, double value2){
        nativeBetween(nativePtr, columnIndex, value1, value2);
        queryValidated = false;
        return this;
    }
    protected native void nativeBetween(long nativeQueryPtr, long columnIndex, double value1, double value2);


    // Query for boolean values.

    public TableQuery equalTo(long columnIndex, boolean value){
        nativeEqual(nativePtr, columnIndex, value);
        queryValidated = false;
        return this;
    }
    protected native void nativeEqual(long nativeQueryPtr, long columnIndex, boolean value);

    // Query for Date values

    public TableQuery equalTo(long columnIndex, Date value){
        nativeEqualDateTime(nativePtr, columnIndex, value.getTime()/1000);
        queryValidated = false;
        return this;
    }
    protected native void nativeEqualDateTime(long nativeQueryPtr, long columnIndex, long value);

    public TableQuery notEqualTo(long columnIndex, Date value){
        nativeNotEqualDateTime(nativePtr, columnIndex, value.getTime()/1000);
        queryValidated = false;
        return this;
    }
    protected native void nativeNotEqualDateTime(long nativeQueryPtr, long columnIndex, long value);

    public TableQuery greaterThan(long columnIndex, Date value){
        nativeGreaterDateTime(nativePtr, columnIndex, value.getTime()/1000);
        queryValidated = false;
        return this;
    }

    protected native void nativeGreaterDateTime(long nativeQueryPtr, long columnIndex, long value);


    public TableQuery greaterThanOrEqual(long columnIndex, Date value){
        nativeGreaterEqualDateTime(nativePtr, columnIndex, value.getTime()/1000);
        queryValidated = false;
        return this;
    }

    protected native void nativeGreaterEqualDateTime(long nativeQueryPtr, long columnIndex, long value);

    public TableQuery lessThan(long columnIndex, Date value){
        nativeLessDateTime(nativePtr, columnIndex, value.getTime()/1000);
        queryValidated = false;
        return this;
    }

    protected native void nativeLessDateTime(long nativeQueryPtr, long columnIndex, long value);


    public TableQuery lessThanOrEqual(long columnIndex, Date value){
        nativeLessEqualDateTime(nativePtr, columnIndex, value.getTime()/1000);
        queryValidated = false;
        return this;
    }

    protected native void nativeLessEqualDateTime(long nativeQueryPtr, long columnIndex, long value);

    public TableQuery between(long columnIndex, Date value1, Date value2){
        nativeBetweenDateTime(nativePtr, columnIndex, value1.getTime()/1000, value2.getTime()/1000);
        queryValidated = false;
        return this;
    }
    protected native void nativeBetweenDateTime(long nativeQueryPtr, long columnIndex, long value1, long value2);

    // Query for String values.

    // Equal
    public TableQuery equalTo(long columnIndex, String value, boolean caseSensitive){
        nativeEqual(nativePtr, columnIndex, value, caseSensitive);
        queryValidated = false;
        return this;
    }
    public TableQuery equalTo(long columnIndex, String value){
        nativeEqual(nativePtr, columnIndex, value, true);
        queryValidated = false;
        return this;
    }
    protected native void nativeEqual(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);

    // Not Equal
    public TableQuery notEqualTo(long columnIndex, String value, boolean caseSensitive){
        nativeNotEqual(nativePtr, columnIndex, value, caseSensitive);
        queryValidated = false;
        return this;
    }
    public TableQuery notEqualTo(long columnIndex, String value){
        nativeNotEqual(nativePtr, columnIndex, value, true);
        queryValidated = false;
        return this;
    }
    protected native void nativeNotEqual(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);

    public TableQuery beginsWith(long columnIndex, String value, boolean caseSensitive){
        nativeBeginsWith(nativePtr, columnIndex, value, caseSensitive);
        queryValidated = false;
        return this;
    }
    public TableQuery beginsWith(long columnIndex, String value){
        nativeBeginsWith(nativePtr, columnIndex, value, true);
        queryValidated = false;
        return this;
    }
    protected native void nativeBeginsWith(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);

    public TableQuery endsWith(long columnIndex, String value, boolean caseSensitive){
        nativeEndsWith(nativePtr, columnIndex, value, caseSensitive);
        queryValidated = false;
        return this;
    }
    public TableQuery endsWith(long columnIndex, String value){
        nativeEndsWith(nativePtr, columnIndex, value, true);
        queryValidated = false;
        return this;
    }
    protected native void nativeEndsWith(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);

    public TableQuery contains(long columnIndex, String value, boolean caseSensitive){
        nativeContains(nativePtr, columnIndex, value, caseSensitive);
        queryValidated = false;
        return this;
    }
    public TableQuery contains(long columnIndex, String value){
        nativeContains(nativePtr, columnIndex, value, true);
        queryValidated = false;
        return this;
    }
    protected native void nativeContains(long nativeQueryPtr, long columnIndex, String value, boolean caseSensitive);


    // Searching methods.

    public long find(long fromTableRow){
        validateQuery();
        return nativeFind(nativePtr, fromTableRow);
    }

    public long find(){
        validateQuery();
        return nativeFind(nativePtr, 0);
    }

    protected native long nativeFind(long nativeQueryPtr, long fromTableRow);

    public TableView findAll(long start, long end, long limit){
        validateQuery();
        context.executeDelayedDisposal();

        long nativeViewPtr = nativeFindAll(nativePtr, start, end, limit);
        return new TableView(this.context, nativeViewPtr, immutable);
    }

    public TableView findAll(){
        validateQuery();
        context.executeDelayedDisposal();
        long nativeViewPtr = nativeFindAll(nativePtr, 0, Table.INFINITE, Table.INFINITE);

        return new TableView(this.context, nativeViewPtr, immutable);
    }

    protected native long nativeFindAll(long nativeQueryPtr, long start, long end, long limit);

    //
    // Aggregation methods
    //

    // Integer aggregation

    public long sumInt(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeSumInt(nativePtr, columnIndex, start, end, limit);
    }
    public long sumInt(long columnIndex){
        validateQuery();
        return nativeSumInt(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native long nativeSumInt(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public long maximumInt(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeMaximumInt(nativePtr, columnIndex, start, end, limit);
    }
    public long maximumInt(long columnIndex){
        validateQuery();
        return nativeMaximumInt(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native long nativeMaximumInt(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public long minimumInt(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeMinimumInt(nativePtr, columnIndex, start, end, limit);
    }
    public long minimumInt(long columnIndex){
        validateQuery();
        return nativeMinimumInt(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native long nativeMinimumInt(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public double averageInt(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeAverageInt(nativePtr, columnIndex, start, end, limit);
    }
    public double averageInt(long columnIndex){
        validateQuery();
        return nativeAverageInt(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeAverageInt(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    // float aggregation

    public double sumFloat(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeSumFloat(nativePtr, columnIndex, start, end, limit);
    }
    public double sumFloat(long columnIndex){
        validateQuery();
        return nativeSumFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeSumFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public float maximumFloat(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeMaximumFloat(nativePtr, columnIndex, start, end, limit);
    }
    public float maximumFloat(long columnIndex){
        validateQuery();
        return nativeMaximumFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native float nativeMaximumFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public float minimumFloat(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeMinimumFloat(nativePtr, columnIndex, start, end, limit);
    }
    public float minimumFloat(long columnIndex){
        validateQuery();
        return nativeMinimumFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native float nativeMinimumFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public double averageFloat(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeAverageFloat(nativePtr, columnIndex, start, end, limit);
    }
    public double averageFloat(long columnIndex){
        validateQuery();
        return nativeAverageFloat(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeAverageFloat(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    // double aggregation

    public double sumDouble(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeSumDouble(nativePtr, columnIndex, start, end, limit);
    }
    public double sumDouble(long columnIndex){
        validateQuery();
        return nativeSumDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeSumDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public double maximumDouble(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeMaximumDouble(nativePtr, columnIndex, start, end, limit);
    }
    public double maximumDouble(long columnIndex){
        validateQuery();
        return nativeMaximumDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeMaximumDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public double minimumDouble(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeMinimumDouble(nativePtr, columnIndex, start, end, limit);
    }
    public double minimumDouble(long columnIndex){
        validateQuery();
        return nativeMinimumDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeMinimumDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);


    public double averageDouble(long columnIndex, long start, long end, long limit){
        validateQuery();
        return nativeAverageDouble(nativePtr, columnIndex, start, end, limit);
    }
    public double averageDouble(long columnIndex){
        validateQuery();
        return nativeAverageDouble(nativePtr, columnIndex, 0, Table.INFINITE, Table.INFINITE);
    }
    protected native double nativeAverageDouble(long nativeQueryPtr, long columnIndex, long start, long end, long limit);

    // count

    // TODO: Rename all start, end parameter names to firstRow, lastRow
    public long count(long start, long end, long limit){
        validateQuery();
        return nativeCount(nativePtr, start, end, limit);
    }

    public long count(){
        validateQuery();
        return nativeCount(nativePtr, 0, Table.INFINITE, Table.INFINITE);
    }

    protected native long nativeCount(long nativeQueryPtr, long start, long end, long limit);


    // Deletion.
    public long remove(long start, long end){
        validateQuery();
        if (immutable) throwImmutable();
        return nativeRemove(nativePtr, start, end, Table.INFINITE);
    }

    public long remove(){
        validateQuery();
        if (immutable) throwImmutable();
        return nativeRemove(nativePtr, 0, Table.INFINITE, Table.INFINITE);
    }

    protected native long nativeRemove(long nativeQueryPtr, long start, long end, long limit);

    private void throwImmutable(){
        throw new IllegalStateException("Mutable method call during read transaction.");
    }
}
