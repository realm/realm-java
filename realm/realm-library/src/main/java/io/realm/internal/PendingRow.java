package io.realm.internal;

import java.lang.ref.WeakReference;
import java.util.Date;

import io.realm.FrozenPendingRow;
import io.realm.RealmChangeListener;
import io.realm.RealmFieldType;
import io.realm.internal.core.DescriptorOrdering;


/**
 * A PendingRow is a row relies on a pending async query.
 * Before the query returns, calling any accessors will immediately throw. In this case run {@link #executeQuery()} to
 * get the queried row immediately. If the query results is empty, an {@link InvalidRow} will be returned.
 * After the query returns, {@link FrontEnd#onQueryFinished(Row)} will be called to give the front end a chance to reset
 * the row. If the async query returns an empty result, the query will be executed again later until a valid row is
 * contained by the query results.
 */
public class PendingRow implements Row {

    // Implement this interface to reset the PendingRow to a Row backed by real data when query returned.
    public interface FrontEnd {
        // When asyncQuery is true, the pending query is executed asynchronously.
        void onQueryFinished(Row row);
    }

    private static final String QUERY_NOT_RETURNED_MESSAGE =
            "The pending query has not been executed.";
    private static final String PROXY_NOT_SET_MESSAGE = "The 'frontEnd' has not been set.";
    private static final String QUERY_EXECUTED_MESSAGE =
            "The query has been executed. This 'PendingRow' is not valid anymore.";

    private OsSharedRealm sharedRealm;
    private OsResults pendingOsResults;
    private RealmChangeListener<PendingRow> listener;
    private WeakReference<FrontEnd> frontEndRef;
    private boolean returnCheckedRow;

    public PendingRow(OsSharedRealm sharedRealm, TableQuery query, DescriptorOrdering queryDescriptors,
                      final boolean returnCheckedRow) {
        this.sharedRealm = sharedRealm;
        pendingOsResults = OsResults.createFromQuery(sharedRealm, query, queryDescriptors);

        listener = new RealmChangeListener<PendingRow>() {
            @Override
            public void onChange(PendingRow pendingRow) {
                notifyFrontEnd();
            }
        };
        pendingOsResults.addListener(this, listener);
        this.returnCheckedRow = returnCheckedRow;
        sharedRealm.addPendingRow(this);
    }

    // To set the front end of this PendingRow.
    public void setFrontEnd(FrontEnd frontEnd) {
        this.frontEndRef = new WeakReference<FrontEnd>(frontEnd);
    }

    @Override
    public long getColumnCount() {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public String[] getColumnNames() {
        throw new  IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public long getColumnKey(String columnName) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public RealmFieldType getColumnType(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public Table getTable() {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public long getObjectKey() {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public long getLong(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public boolean getBoolean(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public float getFloat(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public double getDouble(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public Date getDate(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public String getString(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public byte[] getBinaryByteArray(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public long getLink(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public boolean isNullLink(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public OsList getModelList(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public OsList getValueList(long columnKey, RealmFieldType fieldType) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setLong(long columnKey, long value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setBoolean(long columnKey, boolean value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setFloat(long columnKey, float value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setDouble(long columnKey, double value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setDate(long columnKey, Date date) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setString(long columnKey, String value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setBinaryByteArray(long columnKey, byte[] data) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setLink(long columnKey, long value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void nullifyLink(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public boolean isNull(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setNull(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void checkIfAttached() {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public boolean hasColumn(String fieldName) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public Row freeze(OsSharedRealm frozenRealm) {
        return FrozenPendingRow.INSTANCE;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    private void clearPendingCollection() {
        pendingOsResults.removeListener(this, listener);
        pendingOsResults = null;
        listener = null;
        sharedRealm.removePendingRow(this);
    }

    private void notifyFrontEnd() {
        if (frontEndRef == null) {
            throw new IllegalStateException(PROXY_NOT_SET_MESSAGE);
        }
        FrontEnd frontEnd = frontEndRef.get();
        if (frontEnd == null) {
            // The front end is GCed.
            clearPendingCollection();
            return;
        }

        if (pendingOsResults.isValid()) {
            // PendingRow will always get the first Row of the query since we only support findFirst.
            UncheckedRow uncheckedRow = pendingOsResults.firstUncheckedRow();
            // Clear the pending collection immediately in case beginTransaction is called in the listener which will
            // execute the query again.
            clearPendingCollection();
            // If no rows returned by the query, notify the frontend with an invalid row.
            if (uncheckedRow != null) {
                Row row = returnCheckedRow ? CheckedRow.getFromRow(uncheckedRow) : uncheckedRow;
                // Ask the front end to reset the row and stop async query.
                frontEnd.onQueryFinished(row);
            } else {
                // No row matches the query, return a invalid row.
                frontEnd.onQueryFinished(InvalidRow.INSTANCE);
            }
        } else {
            clearPendingCollection();
        }

    }

    // Execute the query immediately and call frontend's onQueryFinished().
    public void executeQuery() {
        if (pendingOsResults == null) {
            throw new IllegalStateException(QUERY_EXECUTED_MESSAGE);
        }

        notifyFrontEnd();
    }
}
