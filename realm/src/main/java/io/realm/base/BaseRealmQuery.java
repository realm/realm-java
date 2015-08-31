/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.base;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.ColumnType;
import io.realm.internal.LinkView;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;
import io.realm.internal.TableView;

/**
 * Base class for Realm queries.
 *
 * @see RealmQuery
 * @see io.realm.dynamic.DynamicRealmQuery
 */
public abstract class BaseRealmQuery<O extends RealmObject, Q extends BaseRealmQuery<O, Q>> {

    public static final boolean CASE_SENSITIVE = true;
    public static final boolean CASE_INSENSITIVE = false;

    protected Table table;
    protected LinkView view;
    protected TableQuery query;
    protected Map<String, Long> columns = new HashMap<String, Long>();

    private boolean containsDot(String s) {
        return s.indexOf('.') != -1;
    }

    private String[] splitString(String s) {
        int i, j, n;

        // count the number of .
        n = 0;
        for (i = 0; i < s.length(); i++)
            if (s.charAt(i) == '.')
                n++;

        // split at .
        String[] arr = new String[n+1];
        i = 0;
        n = 0;
        j = s.indexOf('.');
        while (j != -1) {
            arr[n] = s.substring(i, j);
            i = j+1;
            j = s.indexOf('.', i);
            n++;
        }
        arr[n] = s.substring(s.lastIndexOf('.')+1);

        return arr;
    }

    // TODO: consider another caching strategy so linked classes are included in the cache.
    private long[] getColumnIndices(String fieldName, ColumnType fieldType) {
        Table table = this.table;
        if (containsDot(fieldName)) {
            String[] names = splitString(fieldName); //fieldName.split("\\.");
            long[] columnIndices = new long[names.length];
            for (int i = 0; i < names.length-1; i++) {
                long index = table.getColumnIndex(names[i]);
                if (index < 0) {
                    throw new IllegalArgumentException("Invalid query: " + names[i] + " does not refer to a class.");
                }
                ColumnType type = table.getColumnType(index);
                if (type == ColumnType.LINK || type == ColumnType.LINK_LIST) {
                    table = table.getLinkTarget(index);
                    columnIndices[i] = index;
                } else {
                    throw new IllegalArgumentException("Invalid query: " + names[i] + " does not refer to a class.");
                }
            }
            columnIndices[names.length-1] = table.getColumnIndex(names[names.length-1]);
            if (fieldType != table.getColumnType(columnIndices[names.length-1])) {
                throw new IllegalArgumentException(String.format("Field '%s': type mismatch.", names[names.length-1]));
            }
            return columnIndices;
        } else {
            if (columns.get(fieldName) == null) {
                throw new IllegalArgumentException(String.format("Field '%s' does not exist.", fieldName));
            }

            ColumnType tableColumnType = table.getColumnType(columns.get(fieldName));
            if (fieldType != tableColumnType) {
                throw new IllegalArgumentException(String.format("Field '%s': type mismatch. Was %s, expected %s.",
                        fieldName, fieldType, tableColumnType
                ));
            }
            return new long[] {columns.get(fieldName)};
        }
    }

    // See https://weblogs.java.net/blog/emcmanus/archive/2010/10/25/using-builder-pattern-subclasses
    protected abstract Q getSelf();

    // Construct the query results
    protected abstract RealmResults<O> getResults(TableView queryResult);

    // Construct a RealmObject from the rowIndex
    protected abstract O getObject(long rowIndex);

    /**
     * Test if a field is null. Only works for relationships and RealmLists.
     *
     * @param fieldName - the field name
     * @return The query object
     * @throws java.lang.IllegalArgumentException if field is not a RealmObject or RealmList
     */
    public Q isNull(String fieldName) {
        // Currently we only support querying top-level
        if (containsDot(fieldName)) {
            throw new IllegalArgumentException("Checking for null in nested objects is not supported.");
        }

        // checking that fieldName has the correct type is done in C++
        this.query.isNull(columns.get(fieldName));
        return getSelf();
    }

    /**
     * Test if a field is not null. Only works for relationships and RealmLists.
     *
     * @param fieldName - the field name
     * @return The query object
     * @throws java.lang.IllegalArgumentException if field is not a RealmObject or RealmList
     */
    public Q isNotNull(String fieldName) {
        return this.beginGroup().not().isNull(fieldName).endGroup();
    }

    // Equal

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q equalTo(String fieldName, String value) {
        return this.equalTo(fieldName, value, CASE_SENSITIVE);
    }

    /**
     * Equal-to comparison
     * @param fieldName   The field to compare
     * @param value       The value to compare with
     * @param caseSensitive if true, substring matching is case sensitive. Setting this to false only works for English
     *                      locale characters.
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error

     */
    public Q equalTo(String fieldName, String value, boolean caseSensitive) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.STRING);
        this.query.equalTo(columnIndices, value, caseSensitive);
        return getSelf();
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q equalTo(String fieldName, int value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.equalTo(columnIndices, value);
        return getSelf();
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q equalTo(String fieldName, long value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.equalTo(columnIndices, value);
        return getSelf();
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q equalTo(String fieldName, double value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.equalTo(columnIndices, value);
        return getSelf();
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q equalTo(String fieldName, float value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.equalTo(columnIndices, value);
        return getSelf();
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q equalTo(String fieldName, boolean value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.BOOLEAN);
        this.query.equalTo(columnIndices, value);
        return getSelf();
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q equalTo(String fieldName, Date value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.equalTo(columnIndices, value);
        return getSelf();
    }

    // Not Equal

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q notEqualTo(String fieldName, String value) {
        return this.notEqualTo(fieldName, value, RealmQuery.CASE_SENSITIVE);
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @param caseSensitive if true, substring matching is case sensitive. Setting this to false only works for English
     *                      locale characters.
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q notEqualTo(String fieldName, String value, boolean caseSensitive) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.STRING);
        if (columnIndices.length > 1 && !caseSensitive) {
            throw new IllegalArgumentException("Link queries cannot be case insensitive - coming soon.");
        }
        this.query.notEqualTo(columnIndices, value, caseSensitive);
        return getSelf();
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q notEqualTo(String fieldName, int value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.notEqualTo(columnIndices, value);
        return getSelf();
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q notEqualTo(String fieldName, long value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.notEqualTo(columnIndices, value);
        return getSelf();
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q notEqualTo(String fieldName, double value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.notEqualTo(columnIndices, value);
        return getSelf();
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q notEqualTo(String fieldName, float value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.notEqualTo(columnIndices, value);
        return getSelf();
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q notEqualTo(String fieldName, boolean value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.BOOLEAN);
        this.query.equalTo(columnIndices, !value);
        return getSelf();
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q notEqualTo(String fieldName, Date value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.notEqualTo(columnIndices, value);
        return getSelf();
    }

    // Greater Than

    /**
     * Greater-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q greaterThan(String fieldName, int value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.greaterThan(columnIndices, value);
        return getSelf();
    }

    /**
     * Greater-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q greaterThan(String fieldName, long value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.greaterThan(columnIndices, value);
        return getSelf();
    }

    /**
     * Greater-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q greaterThan(String fieldName, double value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.greaterThan(columnIndices, value);
        return getSelf();
    }

    /**
     * Greater-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q greaterThan(String fieldName, float value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.greaterThan(columnIndices, value);
        return getSelf();
    }

    /**
     * Greater-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q greaterThan(String fieldName, Date value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.greaterThan(columnIndices, value);
        return getSelf();
    }

    /**
     * Greater-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q greaterThanOrEqualTo(String fieldName, int value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.greaterThanOrEqual(columnIndices, value);
        return getSelf();
    }

    /**
     * Greater-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q greaterThanOrEqualTo(String fieldName, long value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.greaterThanOrEqual(columnIndices, value);
        return getSelf();
    }

    /**
     * Greater-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q greaterThanOrEqualTo(String fieldName, double value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.greaterThanOrEqual(columnIndices, value);
        return getSelf();
    }

    /**
     * Greater-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q greaterThanOrEqualTo(String fieldName, float value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.greaterThanOrEqual(columnIndices, value);
        return getSelf();
    }

    /**
     * Greater-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q greaterThanOrEqualTo(String fieldName, Date value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.greaterThanOrEqual(columnIndices, value);
        return getSelf();
    }

    // Less Than

    /**
     * Less-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q lessThan(String fieldName, int value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.lessThan(columnIndices, value);
        return getSelf();
    }

    /**
     * Less-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q lessThan(String fieldName, long value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.lessThan(columnIndices, value);
        return getSelf();
    }

    /**
     * Less-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q lessThan(String fieldName, double value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.lessThan(columnIndices, value);
        return getSelf();
    }

    /**
     * Less-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q lessThan(String fieldName, float value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.lessThan(columnIndices, value);
        return getSelf();
    }

    /**
     * Less-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q lessThan(String fieldName, Date value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.lessThan(columnIndices, value);
        return getSelf();
    }

    /**
     * Less-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q lessThanOrEqualTo(String fieldName, int value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.lessThanOrEqual(columnIndices, value);
        return getSelf();
    }

    /**
     * Less-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q lessThanOrEqualTo(String fieldName, long value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.lessThanOrEqual(columnIndices, value);
        return getSelf();
    }

    /**
     * Less-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q lessThanOrEqualTo(String fieldName, double value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.lessThanOrEqual(columnIndices, value);
        return getSelf();
    }

    /**
     * Less-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q lessThanOrEqualTo(String fieldName, float value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.lessThanOrEqual(columnIndices, value);
        return getSelf();
    }

    /**
     * Less-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q lessThanOrEqualTo(String fieldName, Date value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.lessThanOrEqual(columnIndices, value);
        return getSelf();
    }

    // Between

    /**
     * Between condition
     * @param fieldName  The field to compare
     * @param from       Lowest value (inclusive)
     * @param to         Highest value (inclusive)
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q between(String fieldName, int from, int to) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.between(columnIndices, from, to);
        return getSelf();
    }

    /**
     * Between condition
     * @param fieldName  The field to compare
     * @param from       Lowest value (inclusive)
     * @param to         Highest value (inclusive)
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q between(String fieldName, long from, long to) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.between(columnIndices, from, to);
        return getSelf();
    }

    /**
     * Between condition
     * @param fieldName  The field to compare
     * @param from       Lowest value (inclusive)
     * @param to         Highest value (inclusive)
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q between(String fieldName, double from, double to) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.between(columnIndices, from, to);
        return getSelf();
    }

    /**
     * Between condition
     * @param fieldName  The field to compare
     * @param from       Lowest value (inclusive)
     * @param to         Highest value (inclusive)
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q between(String fieldName, float from, float to) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.between(columnIndices, from, to);
        return getSelf();
    }

    /**
     * Between condition
     * @param fieldName  The field to compare
     * @param from       Lowest value (inclusive)
     * @param to         Highest value (inclusive)
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q between(String fieldName, Date from, Date to) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.between(columnIndices, from, to);
        return getSelf();
    }


    // Contains

    /**
     * Condition that value of field contains the specified substring
     * @param fieldName  The field to compare
     * @param value      The substring
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q contains(String fieldName, String value) {
        return contains(fieldName, value, CASE_SENSITIVE);
    }

    /**
     * Condition that value of field contains the specified substring
     * @param fieldName  The field to compare
     * @param value      The substring
     * @param caseSensitive if true, substring matching is case sensitive. Setting this to false only works for English
     *                      locale characters.
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q contains(String fieldName, String value, boolean caseSensitive) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.STRING);
        this.query.contains(columnIndices, value, caseSensitive);
        return getSelf();
    }

    /**
     * Condition that the value of field begins with the specified string
     * @param fieldName The field to compare
     * @param value     The string
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q beginsWith(String fieldName, String value) {
        return beginsWith(fieldName, value, CASE_SENSITIVE);
    }

    /**
     * Condition that the value of field begins with the specified substring
     * @param fieldName The field to compare
     * @param value     The substring
     * @param caseSensitive if true, substring matching is case sensitive. Setting this to false only works for English
     *                      locale characters.
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q beginsWith(String fieldName, String value, boolean caseSensitive) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.STRING);
        this.query.beginsWith(columnIndices, value, caseSensitive);
        return getSelf();
    }

    /**
     * Condition that the value of field ends with the specified string
     * @param fieldName The field to compare
     * @param value     The string
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q endsWith(String fieldName, String value) {
        return endsWith(fieldName, value, CASE_SENSITIVE);
    }

    /**
     * Condition that the value of field ends with the specified substring
     * @param fieldName The field to compare
     * @param value     The substring
     * @param caseSensitive if true, substring matching is case sensitive. Setting this to false only works for English
     *                      locale characters.
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public Q endsWith(String fieldName, String value, boolean caseSensitive) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.STRING);
        this.query.endsWith(columnIndices, value, caseSensitive);
        return getSelf();
    }

    // Grouping

    /**
     * Begin grouping of conditions ("left parenthesis"). A group must be closed with a
     * call to <code>endGroup()</code>.
     * @return The query object
     * @see #endGroup()
     */
    public Q beginGroup() {
        this.query.group();
        return getSelf();
    }

    /**
     * End grouping of conditions ("right parenthesis") which was opened by a call to
     * <code>beginGroup()</code>.
     * @return The query object
     * @see #beginGroup()
     */
    public Q endGroup() {
        this.query.endGroup();
        return getSelf();
    }

    /**
     * Logical-or two conditions
     * @return The query object
     */
    public Q or() {
        this.query.or();
        return getSelf();
    }

    /**
     * Negate condition.
     * @return The query object
     */
    public Q not() {
        this.query.not();
        return getSelf();
    }

    // Aggregates

    // Sum

    /**
     * Calculate the sum of a field
     * @param fieldName The field name
     * @return The sum
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public long sumInt(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.sumInt(columnIndex);
    }

    /**
     * Calculate the sum of a field
     * @param fieldName The field name
     * @return The sum
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double sumDouble(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.sumDouble(columnIndex);
    }

    /**
     * Calculate the sum of a field
     * @param fieldName The field name
     * @return The sum
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double sumFloat(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.sumFloat(columnIndex);
    }

    // Average

    /**
     * Calculate the average of a field
     * @param fieldName The field name
     * @return The average
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double averageInt(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.averageInt(columnIndex);
    }

    /**
     * Calculate the average of a field
     * @param fieldName The field name
     * @return The average
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double averageDouble(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.averageDouble(columnIndex);
    }

    /**
     * Calculate the average of a field
     * @param fieldName The field name
     * @return The average
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double averageFloat(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.averageFloat(columnIndex);
    }

    // Min

    /**
     * Find the minimum value of a field
     * @param fieldName  The field name
     * @return The minimum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public long minimumInt(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.minimumInt(columnIndex);
    }

    /**
     * Find the minimum value of a field
     * @param fieldName  The field name
     * @return The minimum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double minimumDouble(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.minimumDouble(columnIndex);
    }

    /**
     * Find the minimum value of a field
     * @param fieldName  The field name
     * @return The minimum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public float minimumFloat(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.minimumFloat(columnIndex);
    }

    /**
     * Find the minimum value of a field
     * @param fieldName  The field name
     * @return The minimum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public Date minimumDate(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.minimumDate(columnIndex);
    }

    // Max

    /**
     * Find the maximum value of a field
     * @param fieldName  The field name
     * @return The maximum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public long maximumInt(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.maximumInt(columnIndex);
    }

    /**
     * Find the maximum value of a field
     * @param fieldName  The field name
     * @return The maximum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double maximumDouble(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.maximumDouble(columnIndex);
    }

    /**
     * Find the maximum value of a field
     * @param fieldName  The field name
     * @return The maximum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public float maximumFloat(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.maximumFloat(columnIndex);
    }

    /**
     * Find the maximum value of a field
     * @param fieldName  The field name
     * @return The maximum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public Date maximumDate(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.maximumDate(columnIndex);
    }

    /**
     * Count the number of objects that fulfill the query conditions.
     *
     * @return The number of matching objects.
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public long count() {
        return this.query.count();
    }

    /**
     * Find all objects that fulfill the query conditions.
     *
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @see io.realm.RealmResults
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmResults<O> findAll() {
        return getResults(query.findAll());
    }

    /**
     * Find all objects that fulfill the query conditions and sorted by specific field name.
     *
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName the field name to sort by.
     * @param sortAscending sort ascending if <code>SORT_ORDER_ASCENDING</code>, sort descending
     *                      if <code>SORT_ORDER_DESCENDING</code>
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public RealmResults<O> findAllSorted(String fieldName, boolean sortAscending) {
        TableView tableView = query.findAll();
        TableView.Order order = sortAscending ? TableView.Order.ascending : TableView.Order.descending;
        Long columnIndex = columns.get(fieldName);
        if (columnIndex == null || columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }
        tableView.sort(columnIndex, order);
        return getResults(tableView);
    }

    /**
     * Find all objects that fulfill the query conditions and sorted by specific field name in
     * ascending order.
     *
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName the field name to sort by.
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public RealmResults<O> findAllSorted(String fieldName) {
        return findAllSorted(fieldName, true);
    }

    /**
     * Find all objects that fulfill the query conditions and sorted by specific field names.
     *
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldNames an array of field names to sort by.
     * @param sortAscending sort ascending if <code>SORT_ORDER_ASCENDING</code>, sort descending
     *                      if <code>SORT_ORDER_DESCENDING</code>.
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public RealmResults<O> findAllSorted(String fieldNames[], boolean sortAscending[]) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("fieldNames cannot be 'null'.");
        } else if (sortAscending == null) {
            throw new IllegalArgumentException("sortAscending cannot be 'null'.");
        } else if (fieldNames.length == 0) {
            throw new IllegalArgumentException("At least one field name must be specified.");
        } else if (fieldNames.length != sortAscending.length) {
            throw new IllegalArgumentException(String.format("Number of field names (%d) and sort orders (%d) does not match.", fieldNames.length, sortAscending.length));
        }

        if (fieldNames.length == 1 && sortAscending.length == 1) {
            return findAllSorted(fieldNames[0], sortAscending[0]);
        } else {
            TableView tableView = query.findAll();
            List<Long> columnIndices = new ArrayList<Long>();
            List<TableView.Order> orders = new ArrayList<TableView.Order>();
            for (int i = 0; i < fieldNames.length; i++) {
                String fieldName = fieldNames[i];
                Long columnIndex = columns.get(fieldName);
                if (columnIndex == null || columnIndex < 0) {
                    throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
                }
                columnIndices.add(columnIndex);
            }
            for (int i = 0; i < sortAscending.length; i++) {
                orders.add(sortAscending[i] ? TableView.Order.ascending : TableView.Order.descending);
            }
            tableView.sort(columnIndices, orders);
            return getResults(tableView);
        }
    }

    /**
     * Find all objects that fulfill the query conditions and sorted by specific field names in
     * ascending order.
     *
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName1 first field name
     * @param sortAscending1 sort order for first field
     * @param fieldName2 second field name
     * @param sortAscending2 sort order for second field
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public RealmResults<O> findAllSorted(String fieldName1, boolean sortAscending1,
                                         String fieldName2, boolean sortAscending2) {
        return findAllSorted(new String[]{fieldName1, fieldName2}, new boolean[]{sortAscending1, sortAscending2});
    }

    /**
     * Find all objects that fulfill the query conditions and sorted by specific field names in
     * ascending order.
     *
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName1 first field name
     * @param sortAscending1 sort order for first field
     * @param fieldName2 second field name
     * @param sortAscending2 sort order for second field
     * @param fieldName3 third field name
     * @param sortAscending3 sort order for third field
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public RealmResults<O> findAllSorted(String fieldName1, boolean sortAscending1,
                                         String fieldName2, boolean sortAscending2,
                                         String fieldName3, boolean sortAscending3) {
        return findAllSorted(new String[]{fieldName1, fieldName2, fieldName3},
                new boolean[]{sortAscending1, sortAscending2, sortAscending3});
    }

    /**
     * Find the first object that fulfills the query conditions.
     *
     * @return The object found or null if no object matches the query conditions.
     * @see io.realm.RealmObject
     * @throws java.lang.RuntimeException Any other error.
     */
    public O findFirst() {
        long rowIndex = this.query.find();
        if (rowIndex >= 0) {
            return getObject((view != null) ? view.getTargetRowIndex(rowIndex) : rowIndex);
        } else {
            return null;
        }
    }
}
