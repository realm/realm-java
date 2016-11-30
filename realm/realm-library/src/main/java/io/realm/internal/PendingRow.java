package io.realm.internal;

import java.lang.ref.WeakReference;
import java.util.Date;

import io.realm.RealmChangeListener;
import io.realm.RealmFieldType;

/**
 * A PendingRow is a row relies on a pending async query.
 * Before the query returns, calling any accessors will immediately execute the query and call the corresponding
 * accessor on the query result. If the query results is empty, an {@link IllegalStateException} will be thrown.
 * After the query returns, {@link FrontEnd#onQueryFinished(Row, boolean)} will be called to give the front end a
 * chance to reset the row. If the async query returns an empty result, the query will be executed again later until a
 * valid row is contained by the query results.
 */
public class PendingRow implements Row {

    // Implement this interface to reset the PendingRow to a Row backed by real data when query returned.
    public interface FrontEnd {
        // When asyncQuery is true, the pending query is executed asynchronously. Otherwise the query is triggered by
        // calling any accessors before the async query returns.
        void onQueryFinished(Row row, boolean asyncQuery);
    }

    private static final String EMPTY_ROW_MESSAGE =
            "This RealmObject is empty. There isn't any objects match the query.";
    private static final String PROXY_NOT_SET_MESSAGE = "The 'frontEnd' has not been set.";
    private static final String QUERY_EXECUTED_MESSAGE =
            "The query has been executed. This 'PendingRow' is not valid anymore.";

    private Collection pendingCollection;
    private Collection.Listener listener;
    private WeakReference<FrontEnd> frontEnd;

    public PendingRow(SharedRealm sharedRealm, TableQuery query, SortDescriptor sortDescriptor) {
        pendingCollection = new Collection(sharedRealm, query, sortDescriptor);
        listener = new Collection.Listener(new RealmChangeListener<PendingRow>() {
            @Override
            public void onChange(PendingRow pendingRow) {
                if (frontEnd == null) {
                    throw new IllegalStateException(PROXY_NOT_SET_MESSAGE);
                }
                // TODO: PendingRow will always get the first Row of the query since we only support findFirst.
                Row row = pendingCollection.firstUncheckedRow();
                if (frontEnd.get() == null) {
                    // The front end is GCed.
                    clearPendingCollection();
                    return;
                }
                // If no rows returned by the query, just wait for the query updates until it returns a valid row.
                if (row != null) {
                    // Ask the front end to reset the row and stop async query.
                    frontEnd.get().onQueryFinished(row, true);
                    clearPendingCollection();
                }
            }
        }, this);
        pendingCollection.addListener(listener);
    }

    // To set the front end of this PendingRow.
    public void setFrontEnd(FrontEnd frontEnd) {
        this.frontEnd = new WeakReference<FrontEnd>(frontEnd);
    }

    @Override
    public long getColumnCount() {
        return executeQuery().getColumnCount();
    }

    @Override
    public String getColumnName(long columnIndex) {
        return executeQuery().getColumnName(columnIndex);
    }

    @Override
    public long getColumnIndex(String columnName) {
        return executeQuery().getColumnIndex(columnName);
    }

    @Override
    public RealmFieldType getColumnType(long columnIndex) {
        return executeQuery().getColumnType(columnIndex);
    }

    @Override
    public Table getTable() {
        return executeQuery().getTable();
    }

    @Override
    public long getIndex() {
        return executeQuery().getIndex();
    }

    @Override
    public long getLong(long columnIndex) {
        return executeQuery().getLong(columnIndex);
    }

    @Override
    public boolean getBoolean(long columnIndex) {
        return executeQuery().getBoolean(columnIndex);
    }

    @Override
    public float getFloat(long columnIndex) {
        return executeQuery().getFloat(columnIndex);
    }

    @Override
    public double getDouble(long columnIndex) {
        return executeQuery().getDouble(columnIndex);
    }

    @Override
    public Date getDate(long columnIndex) {
        return executeQuery().getDate(columnIndex);
    }

    @Override
    public String getString(long columnIndex) {
        return executeQuery().getString(columnIndex);
    }

    @Override
    public byte[] getBinaryByteArray(long columnIndex) {
        return executeQuery().getBinaryByteArray(columnIndex);
    }

    @Override
    public long getLink(long columnIndex) {
        return executeQuery().getLink(columnIndex);
    }

    @Override
    public boolean isNullLink(long columnIndex) {
        return executeQuery().isNullLink(columnIndex);
    }

    @Override
    public LinkView getLinkList(long columnIndex) {
        return executeQuery().getLinkList(columnIndex);
    }

    @Override
    public void setLong(long columnIndex, long value) {
        executeQuery().setLong(columnIndex, value);
    }

    @Override
    public void setBoolean(long columnIndex, boolean value) {
        executeQuery().setBoolean(columnIndex, value);
    }

    @Override
    public void setFloat(long columnIndex, float value) {
        executeQuery().setFloat(columnIndex, value);
    }

    @Override
    public void setDouble(long columnIndex, double value) {
        executeQuery().setDouble(columnIndex, value);
    }

    @Override
    public void setDate(long columnIndex, Date date) {
        executeQuery().setDate(columnIndex, date);
    }

    @Override
    public void setString(long columnIndex, String value) {
        executeQuery().setString(columnIndex, value);
    }

    @Override
    public void setBinaryByteArray(long columnIndex, byte[] data) {
        executeQuery().setBinaryByteArray(columnIndex, data);
    }

    @Override
    public void setLink(long columnIndex, long value) {
        executeQuery().setLink(columnIndex, value);
    }

    @Override
    public void nullifyLink(long columnIndex) {
        executeQuery().nullifyLink(columnIndex);
    }

    @Override
    public boolean isNull(long columnIndex) {
        return executeQuery().isNull(columnIndex);
    }

    @Override
    public void setNull(long columnIndex) {
        executeQuery().setNull(columnIndex);
    }

    @Override
    public boolean isAttached() {
        return executeQuery().isAttached();
    }

    @Override
    public boolean hasColumn(String fieldName) {
        return executeQuery().hasColumn(fieldName);
    }

    private void clearPendingCollection() {
        pendingCollection.removeListener(listener);
        pendingCollection = null;
        listener = null;
    }

    private Row executeQuery() {
        if (pendingCollection == null) {
            throw new IllegalStateException(QUERY_EXECUTED_MESSAGE);
        }
        if (frontEnd == null) {
            throw new IllegalStateException(PROXY_NOT_SET_MESSAGE);
        }
        Row row = pendingCollection.getUncheckedRow(0);
        if (row == null) {
            throw new IllegalStateException(EMPTY_ROW_MESSAGE);
        }
        if (frontEnd.get() != null) {
            frontEnd.get().onQueryFinished(pendingCollection.firstUncheckedRow(), false);
        }
        clearPendingCollection();
        return row;
    }
}
