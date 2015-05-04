/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications:
 * -Imported from AOSP frameworks/base/core/java/com/android/internal/content
 * -Changed package name
 */

package com.example.android.common.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper for building selection clauses for {@link SQLiteDatabase}.
 *
 * <p>This class provides a convenient frontend for working with SQL. Instead of composing statements
 * manually using string concatenation, method calls are used to construct the statement one
 * clause at a time. These methods can be chained together.
 *
 * <p>If multiple where() statements are provided, they're combined using {@code AND}.
 *
 * <p>Example:
 *
 * <pre>
 *     SelectionBuilder builder = new SelectionBuilder();
 *     Cursor c = builder.table(FeedContract.Entry.TABLE_NAME)       // String TABLE_NAME = "entry"
 *                       .where(FeedContract.Entry._ID + "=?", id);  // String _ID = "_ID"
 *                       .query(db, projection, sortOrder)
 *
 * </pre>
 *
 * <p>In this example, the table name and filters ({@code WHERE} clauses) are both explicitly
 * specified via method call. SelectionBuilder takes care of issuing a "query" command to the
 * database, and returns the resulting {@link Cursor} object.
 *
 * <p>Inner {@code JOIN}s can be accomplished using the mapToTable() function. The map() function
 * can be used to create new columns based on arbitrary (SQL-based) criteria. In advanced usage,
 * entire subqueries can be passed into the map() function.
 *
 * <p>Advanced example:
 *
 * <pre>
 *     // String SESSIONS_JOIN_BLOCKS_ROOMS = "sessions "
 *     //        + "LEFT OUTER JOIN blocks ON sessions.block_id=blocks.block_id "
 *     //        + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id";
 *
 *     // String Subquery.BLOCK_NUM_STARRED_SESSIONS =
 *     //       "(SELECT COUNT(1) FROM "
 *     //        + Tables.SESSIONS + " WHERE " + Qualified.SESSIONS_BLOCK_ID + "="
 *     //        + Qualified.BLOCKS_BLOCK_ID + " AND " + Qualified.SESSIONS_STARRED + "=1)";
 *
 *     String Subqery.BLOCK_SESSIONS_COUNT =
 *     Cursor c = builder.table(Tables.SESSIONS_JOIN_BLOCKS_ROOMS)
 *               .map(Blocks.NUM_STARRED_SESSIONS, Subquery.BLOCK_NUM_STARRED_SESSIONS)
 *               .mapToTable(Sessions._ID, Tables.SESSIONS)
 *               .mapToTable(Sessions.SESSION_ID, Tables.SESSIONS)
 *               .mapToTable(Sessions.BLOCK_ID, Tables.SESSIONS)
 *               .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
 *               .where(Qualified.SESSIONS_BLOCK_ID + "=?", blockId);
 * </pre>
 *
 * <p>In this example, we have two different types of {@code JOIN}s: a left outer join using a
 * modified table name (since this class doesn't directly support these), and an inner join using
 * the mapToTable() function. The map() function is used to insert a count based on specific
 * criteria, executed as a sub-query.
 *
 * This class is <em>not</em> thread safe.
 */
public class SelectionBuilder {
    private static final String TAG = "basicsyncadapter";

    private String mTable = null;
    private Map<String, String> mProjectionMap = new HashMap<String, String>();
    private StringBuilder mSelection = new StringBuilder();
    private ArrayList<String> mSelectionArgs = new ArrayList<String>();

    /**
     * Reset any internal state, allowing this builder to be recycled.
     *
     * <p>Calling this method is more efficient than creating a new SelectionBuilder object.
     *
     * @return Fluent interface
     */
    public SelectionBuilder reset() {
        mTable = null;
        mSelection.setLength(0);
        mSelectionArgs.clear();
        return this;
    }

    /**
     * Append the given selection clause to the internal state. Each clause is
     * surrounded with parenthesis and combined using {@code AND}.
     *
     * <p>In the most basic usage, simply provide a selection in SQL {@code WHERE} statement format.
     *
     * <p>Example:
     *
     * <pre>
     *     .where("blog_posts.category = 'PROGRAMMING');
     * </pre>
     *
     * <p>User input should never be directly supplied as as part of the selection statement.
     * Instead, use positional parameters in your selection statement, then pass the user input
     * in via the selectionArgs parameter. This prevents SQL escape characters in user input from
     * causing unwanted side effects. (Failure to follow this convention may have security
     * implications.)
     *
     * <p>Positional parameters are specified using the '?' character.
     *
     * <p>Example:
     * <pre>
     *     .where("blog_posts.title contains ?, userSearchString);
     * </pre>
     *
     * @param selection SQL where statement
     * @param selectionArgs Values to substitute for positional parameters ('?' characters in
     *                      {@code selection} statement. Will be automatically escaped.
     * @return Fluent interface
     */
    public SelectionBuilder where(String selection, String... selectionArgs) {
        if (TextUtils.isEmpty(selection)) {
            if (selectionArgs != null && selectionArgs.length > 0) {
                throw new IllegalArgumentException(
                        "Valid selection required when including arguments=");
            }

            // Shortcut when clause is empty
            return this;
        }

        if (mSelection.length() > 0) {
            mSelection.append(" AND ");
        }

        mSelection.append("(").append(selection).append(")");
        if (selectionArgs != null) {
            Collections.addAll(mSelectionArgs, selectionArgs);
        }

        return this;
    }

    /**
     * Table name to use for SQL {@code FROM} statement.
     *
     * <p>This method may only be called once. If multiple tables are required, concatenate them
     * in SQL-format (typically comma-separated).
     *
     * <p>If you need to do advanced {@code JOIN}s, they can also be specified here.
     *
     * See also: mapToTable()
     *
     * @param table Table name
     * @return Fluent interface
     */
    public SelectionBuilder table(String table) {
        mTable = table;
        return this;
    }

    /**
     * Verify that a table name has been supplied using table().
     *
     * @throws IllegalStateException if table not set
     */
    private void assertTable() {
        if (mTable == null) {
            throw new IllegalStateException("Table not specified");
        }
    }

    /**
     * Perform an inner join.
     *
     * <p>Map columns from a secondary table onto the current result set. References to the column
     * specified in {@code column} will be replaced with {@code table.column} in the SQL {@code
     * SELECT} clause.
     *
     * @param column Column name to join on. Must be the same in both tables.
     * @param table Secondary table to join.
     * @return Fluent interface
     */
    public SelectionBuilder mapToTable(String column, String table) {
        mProjectionMap.put(column, table + "." + column);
        return this;
    }

    /**
     * Create a new column based on custom criteria (such as aggregate functions).
     *
     * <p>This adds a new column to the result set, based upon custom criteria in SQL format. This
     * is equivalent to the SQL statement: {@code SELECT toClause AS fromColumn}
     *
     * <p>This method is useful for executing SQL sub-queries.
     *
     * @param fromColumn Name of column for mapping
     * @param toClause SQL string representing data to be mapped
     * @return Fluent interface
     */
    public SelectionBuilder map(String fromColumn, String toClause) {
        mProjectionMap.put(fromColumn, toClause + " AS " + fromColumn);
        return this;
    }

    /**
     * Return selection string based on current internal state.
     *
     * @return Current selection as a SQL statement
     * @see #getSelectionArgs()
     */
    public String getSelection() {
        return mSelection.toString();

    }

    /**
     * Return selection arguments based on current internal state.
     *
     * @see #getSelection()
     */
    public String[] getSelectionArgs() {
        return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
    }

    /**
     * Process user-supplied projection (column list).
     *
     * <p>In cases where a column is mapped to another data source (either another table, or an
     * SQL sub-query), the column name will be replaced with a more specific, SQL-compatible
     * representation.
     *
     * Assumes that incoming columns are non-null.
     *
     * <p>See also: map(), mapToTable()
     *
     * @param columns User supplied projection (column list).
     */
    private void mapColumns(String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            final String target = mProjectionMap.get(columns[i]);
            if (target != null) {
                columns[i] = target;
            }
        }
    }

    /**
     * Return a description of this builder's state. Does NOT output SQL.
     *
     * @return Human-readable internal state
     */
    @Override
    public String toString() {
        return "SelectionBuilder[table=" + mTable + ", selection=" + getSelection()
                + ", selectionArgs=" + Arrays.toString(getSelectionArgs()) + "]";
    }

    /**
     * Execute query (SQL {@code SELECT}) against specified database.
     *
     * <p>Using a null projection (column list) is not supported.
     *
     * @param db Database to query.
     * @param columns Database projection (column list) to return, must be non-NULL.
     * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause (excluding the
     *                ORDER BY itself). Passing null will use the default sort order, which may be
     *                unordered.
     * @return A {@link Cursor} object, which is positioned before the first entry. Note that
     *         {@link Cursor}s are not synchronized, see the documentation for more details.
     */
    public Cursor query(SQLiteDatabase db, String[] columns, String orderBy) {
        return query(db, columns, null, null, orderBy, null);
    }

    /**
     * Execute query ({@code SELECT}) against database.
     *
     * <p>Using a null projection (column list) is not supported.
     *
     * @param db Database to query.
     * @param columns Database projection (column list) to return, must be non-null.
     * @param groupBy A filter declaring how to group rows, formatted as an SQL GROUP BY clause
     *                (excluding the GROUP BY itself). Passing null will cause the rows to not be
     *                grouped.
     * @param having A filter declare which row groups to include in the cursor, if row grouping is
     *               being used, formatted as an SQL HAVING clause (excluding the HAVING itself).
     *               Passing null will cause all row groups to be included, and is required when
     *               row grouping is not being used.
     * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause (excluding the
     *                ORDER BY itself). Passing null will use the default sort order, which may be
     *                unordered.
     * @param limit Limits the number of rows returned by the query, formatted as LIMIT clause.
     *              Passing null denotes no LIMIT clause.
     * @return A {@link Cursor} object, which is positioned before the first entry. Note that
     *         {@link Cursor}s are not synchronized, see the documentation for more details.
     */
    public Cursor query(SQLiteDatabase db, String[] columns, String groupBy,
                        String having, String orderBy, String limit) {
        assertTable();
        if (columns != null) mapColumns(columns);
        Log.v(TAG, "query(columns=" + Arrays.toString(columns) + ") " + this);
        return db.query(mTable, columns, getSelection(), getSelectionArgs(), groupBy, having,
                orderBy, limit);
    }

    /**
     * Execute an {@code UPDATE} against database.
     *
     * @param db Database to query.
     * @param values A map from column names to new column values. null is a valid value that will
     *               be translated to NULL
     * @return The number of rows affected.
     */
    public int update(SQLiteDatabase db, ContentValues values) {
        assertTable();
        Log.v(TAG, "update() " + this);
        return db.update(mTable, values, getSelection(), getSelectionArgs());
    }

    /**
     * Execute {@code DELETE} against database.
     *
     * @param db Database to query.
     * @return The number of rows affected.
     */
    public int delete(SQLiteDatabase db) {
        assertTable();
        Log.v(TAG, "delete() " + this);
        return db.delete(mTable, getSelection(), getSelectionArgs());
    }
}
