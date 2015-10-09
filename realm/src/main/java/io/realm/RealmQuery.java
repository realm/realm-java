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

package io.realm;


import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import io.realm.annotations.Required;
import io.realm.internal.ColumnType;
import io.realm.internal.LinkView;
import io.realm.internal.Row;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;
import io.realm.internal.TableView;
import io.realm.internal.async.ArgumentsHolder;
import io.realm.internal.async.QueryUpdateTask;
import io.realm.internal.log.RealmLog;

/**
 * A RealmQuery encapsulates a query on a {@link io.realm.Realm} or a {@link io.realm.RealmResults}
 * using the Builder pattern. The query is executed using either {@link #findAll()} or
 * {@link #findFirst()}
 * <p>
 * The input to many of the query functions take a field name as String. Note that this is not
 * type safe. If a model class is refactored care has to be taken to not break any queries.
 * <p>
 * A {@link io.realm.Realm} is unordered, which means that there is no guarantee that querying a
 * Realm will return the objects in the order they where inserted. Use
 * {@link #findAllSorted(String)} and similar methods if a specific order is required.
 * <p>
 * A RealmQuery cannot be passed between different threads.
 *
 * @param <E> The class of the objects to be queried.
 * @see <a href="http://en.wikipedia.org/wiki/Builder_pattern">Builder pattern</a>
 * @see Realm#where(Class)
 * @see RealmResults#where()
 */
public class RealmQuery<E extends RealmObject> {

    private final Realm realm;
    private final Table table;
    private final LinkView view;
    private final TableQuery query;
    private final Map<String, Long> columns;
    private final Class<E> clazz;

    private static final String LINK_NOT_SUPPORTED_METHOD = "'%s' is not supported for link queries";
    private static final String TYPE_MISMATCH = "Field '%s': type mismatch - %s expected.";

    public static final boolean CASE_SENSITIVE = true;
    public static final boolean CASE_INSENSITIVE = false;

    private final static Long INVALID_NATIVE_POINTER = 0L;
    private ArgumentsHolder argumentsHolder;

    /**
     * Creating a RealmQuery instance.
     *
     * @param realm  The realm to query within.
     * @param clazz  The class to query.
     * @throws java.lang.RuntimeException Any other error.
     */
    public RealmQuery(Realm realm, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.table = realm.getTable(clazz);
        this.view = null;
        this.query = table.where();
        this.columns = realm.columnIndices.getClassFields(clazz);
    }

    /**
     * Create a RealmQuery instance from a @{link io.realm.RealmResults}.
     *
     * @param realmList   The @{link io.realm.RealmResults} to query
     * @param clazz       The class to query
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery(RealmResults realmList, Class<E> clazz) {
        this.realm = realmList.getRealm();
        this.clazz = clazz;
        this.table = realm.getTable(clazz);
        this.view = null;
        this.query = realmList.getTable().where();
        this.columns = realm.columnIndices.getClassFields(clazz);
    }

    RealmQuery(Realm realm, LinkView view, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.query = view.where();
        this.view = view;
        this.table = realm.getTable(clazz);
        this.columns = realm.columnIndices.getClassFields(clazz);
    }

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
        arr[n] = s.substring(s.lastIndexOf('.') + 1);

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
            columnIndices[names.length - 1] = table.getColumnIndex(names[names.length - 1]);
            if (fieldType != null && fieldType != table.getColumnType(columnIndices[names.length - 1])) {
                throw new IllegalArgumentException(String.format("Field '%s': type mismatch.", names[names.length - 1]));
            }
            return columnIndices;
        } else {
            if (columns.get(fieldName) == null) {
                throw new IllegalArgumentException(String.format("Field '%s' does not exist.", fieldName));
            }

            ColumnType tableColumnType = table.getColumnType(columns.get(fieldName));
            if (fieldType != null && fieldType != tableColumnType) {
                throw new IllegalArgumentException(String.format("Field '%s': type mismatch. Was %s, expected %s.",
                        fieldName, fieldType, tableColumnType
                ));
            }
            return new long[] {columns.get(fieldName)};
        }
    }

    /**
     * Test if a field is {@code null}. Only works for nullable fields.
     *
     * @param fieldName the field name.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field is not nullable.
     * @see Required for further infomation.
     */
    public RealmQuery<E> isNull(String fieldName) {
        long columnIndices[] = getColumnIndices(fieldName, null);

        // checking that fieldName has the correct type is done in C++
        this.query.isNull(columnIndices);
        return this;
    }

    /**
     * Test if a field is not {@code null}. Only works for nullable fields.
     *
     * @param fieldName the field name.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field is not nullable.
     * @see Required for further infomation.
     */
    public RealmQuery<E> isNotNull(String fieldName) {
        long columnIndices[] = getColumnIndices(fieldName, null);

        // checking that fieldName has the correct type is done in C++
        this.query.isNotNull(columnIndices);
        return this;
    }

    // Equal

    /**
     * Equal-to comparison.
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> equalTo(String fieldName, String value) {
        return this.equalTo(fieldName, value, CASE_SENSITIVE);
    }

    /**
     * Equal-to comparison.
     * @param fieldName   the field to compare.
     * @param value       the value to compare with.
     * @param caseSensitive if true, substring matching is case sensitive. Setting this to false only works for English
     *                      locale characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.

     */
    public RealmQuery<E> equalTo(String fieldName, String value, boolean caseSensitive) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.STRING);
        this.query.equalTo(columnIndices, value, caseSensitive);
        return this;
    }

    /**
     * Equal-to comparison.
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> equalTo(String fieldName, Byte value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        if (value == null) {
            this.query.isNull(columnIndices);
        } else {
            this.query.equalTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Equal-to comparison.
     * @param fieldName  the field to compare.
     * @param value      the value to compare with
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> equalTo(String fieldName, Short value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        if (value == null) {
            this.query.isNull(columnIndices);
        } else {
            this.query.equalTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Equal-to comparison.
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> equalTo(String fieldName, Integer value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        if (value == null) {
            this.query.isNull(columnIndices);
        } else {
            this.query.equalTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Equal-to comparison
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> equalTo(String fieldName, Long value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        if (value == null) {
            this.query.isNull(columnIndices);
        } else {
            this.query.equalTo(columnIndices, value);
        }
        return this;
    }
    /**
     * Equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value     the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     *                                            field type.
     * @throws java.lang.RuntimeException         if any other error happens.
     */
    public RealmQuery<E> equalTo(String fieldName, Double value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.DOUBLE);
        if (value == null) {
            this.query.isNull(columnIndices);
        } else {
            this.query.equalTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value     the value to compare with.
     * @return The query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     *                                            field type.
     * @throws java.lang.RuntimeException         if any other error happens.
     */
    public RealmQuery<E> equalTo(String fieldName, Float value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.FLOAT);
        if (value == null) {
            this.query.isNull(columnIndices);
        } else {
            this.query.equalTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Equal-to comparison.
     *
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> equalTo(String fieldName, Boolean value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.BOOLEAN);
        if (value == null) {
            this.query.isNull(columnIndices);
        } else {
            this.query.equalTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Equal-to comparison.
     *
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> equalTo(String fieldName, Date value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.equalTo(columnIndices, value);
        return this;
    }

    // Not Equal

    /**
     * Not-equal-to comparison.
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> notEqualTo(String fieldName, String value) {
        return this.notEqualTo(fieldName, value, RealmQuery.CASE_SENSITIVE);
    }

    /**
     * Not-equal-to comparison.
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @param caseSensitive if true, substring matching is case sensitive. Setting this to false only works for English
     *                      locale characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> notEqualTo(String fieldName, String value, boolean caseSensitive) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.STRING);
        if (columnIndices.length > 1 && !caseSensitive) {
            throw new IllegalArgumentException("Link queries cannot be case insensitive - coming soon.");
        }
        this.query.notEqualTo(columnIndices, value, caseSensitive);
        return this;
    }

    /**
     * Not-equal-to comparison.
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Byte value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        if (value == null) {
            this.query.isNotNull(columnIndices);
        } else {
            this.query.notEqualTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Not-equal-to comparison.
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Short value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        if (value == null) {
            this.query.isNotNull(columnIndices);
        } else {
            this.query.notEqualTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Not-equal-to comparison.
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Integer value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        if (value == null) {
            this.query.isNotNull(columnIndices);
        } else {
            this.query.notEqualTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Not-equal-to comparison.
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Long value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        if (value == null) {
            this.query.isNotNull(columnIndices);
        } else {
            this.query.notEqualTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Not-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value     the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     *                                            field type.
     * @throws java.lang.RuntimeException         if any other error happens.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Double value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.DOUBLE);
        if (value == null) {
            this.query.isNotNull(columnIndices);
        } else {
            this.query.notEqualTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Not-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value     the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     *                                            field type.
     * @throws java.lang.RuntimeException         if any other error happens.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Float value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.FLOAT);
        if (value == null) {
            this.query.isNotNull(columnIndices);
        } else {
            this.query.notEqualTo(columnIndices, value);
        }
        return this;
    }

    /**
     * Not-equal-to comparison.
     *
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Boolean value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.BOOLEAN);
        if (value == null) {
            this.query.isNotNull(columnIndices);
        } else {
            this.query.equalTo(columnIndices, !value);
        }
        return this;
    }

    /**
     * Not-equal-to comparison.
     *
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Date value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.DATE);
        if (value == null) {
            this.query.isNotNull(columnIndices);
        } else {
            this.query.notEqualTo(columnIndices, value);
        }
        return this;
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
    public RealmQuery<E> greaterThan(String fieldName, int value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.greaterThan(columnIndices, value);
        return this;
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
    public RealmQuery<E> greaterThan(String fieldName, long value) {
        long[] columnIndices = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.greaterThan(columnIndices, value);
        return this;
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
    public RealmQuery<E> greaterThan(String fieldName, double value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.greaterThan(columnIndices, value);
        return this;
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
    public RealmQuery<E> greaterThan(String fieldName, float value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.greaterThan(columnIndices, value);
        return this;
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
    public RealmQuery<E> greaterThan(String fieldName, Date value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.greaterThan(columnIndices, value);
        return this;
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
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, int value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.greaterThanOrEqual(columnIndices, value);
        return this;
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
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, long value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.greaterThanOrEqual(columnIndices, value);
        return this;
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
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, double value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.greaterThanOrEqual(columnIndices, value);
        return this;
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
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, float value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.greaterThanOrEqual(columnIndices, value);
        return this;
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
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, Date value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.greaterThanOrEqual(columnIndices, value);
        return this;
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
    public RealmQuery<E> lessThan(String fieldName, int value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.lessThan(columnIndices, value);
        return this;
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
    public RealmQuery<E> lessThan(String fieldName, long value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.lessThan(columnIndices, value);
        return this;
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
    public RealmQuery<E> lessThan(String fieldName, double value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.lessThan(columnIndices, value);
        return this;
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
    public RealmQuery<E> lessThan(String fieldName, float value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.lessThan(columnIndices, value);
        return this;
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
    public RealmQuery<E> lessThan(String fieldName, Date value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.lessThan(columnIndices, value);
        return this;
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
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, int value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.lessThanOrEqual(columnIndices, value);
        return this;
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
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, long value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.lessThanOrEqual(columnIndices, value);
        return this;
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
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, double value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.lessThanOrEqual(columnIndices, value);
        return this;
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
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, float value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.lessThanOrEqual(columnIndices, value);
        return this;
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
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, Date value) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.lessThanOrEqual(columnIndices, value);
        return this;
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
    public RealmQuery<E> between(String fieldName, int from, int to) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.between(columnIndices, from, to);
        return this;
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
    public RealmQuery<E> between(String fieldName, long from, long to) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.INTEGER);
        this.query.between(columnIndices, from, to);
        return this;
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
    public RealmQuery<E> between(String fieldName, double from, double to) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DOUBLE);
        this.query.between(columnIndices, from, to);
        return this;
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
    public RealmQuery<E> between(String fieldName, float from, float to) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.FLOAT);
        this.query.between(columnIndices, from, to);
        return this;
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
    public RealmQuery<E> between(String fieldName, Date from, Date to) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.DATE);
        this.query.between(columnIndices, from, to);
        return this;
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
    public RealmQuery<E> contains(String fieldName, String value) {
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
    public RealmQuery<E> contains(String fieldName, String value, boolean caseSensitive) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.STRING);
        this.query.contains(columnIndices, value, caseSensitive);
        return this;
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
    public RealmQuery<E> beginsWith(String fieldName, String value) {
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
    public RealmQuery<E> beginsWith(String fieldName, String value, boolean caseSensitive) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.STRING);
        this.query.beginsWith(columnIndices, value, caseSensitive);
        return this;
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
    public RealmQuery<E> endsWith(String fieldName, String value) {
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
    public RealmQuery<E> endsWith(String fieldName, String value, boolean caseSensitive) {
        long columnIndices[] = getColumnIndices(fieldName, ColumnType.STRING);
        this.query.endsWith(columnIndices, value, caseSensitive);
        return this;
    }

    // Grouping

    /**
     * Begin grouping of conditions ("left parenthesis"). A group must be closed with a
     * call to <code>endGroup()</code>.
     * @return The query object
     * @see #endGroup()
     */
    public RealmQuery<E> beginGroup() {
        this.query.group();
        return this;
    }

    /**
     * End grouping of conditions ("right parenthesis") which was opened by a call to
     * <code>beginGroup()</code>.
     * @return The query object
     * @see #beginGroup()
     */
    public RealmQuery<E> endGroup() {
        this.query.endGroup();
        return this;
    }

    /**
     * Logical-or two conditions
     * @return The query object
     */
    public RealmQuery<E> or() {
        this.query.or();
        return this;
    }

    /**
     * Negate condition.
     * @return The query object
     */
    public RealmQuery<E> not() {
        this.query.not();
        return this;
    }

    // Aggregates

    // Sum

    /**
     * Calculate the sum of a given field.
     *
     * @param fieldName   the field to sum. Only number fields are supported.
     * @return            the sum. If no objects exist or they all have {@code null} as the value for the given field,
     *                    {@code 0} will be returned. When computing the sum, objects with {@code null} values are ignored.
     * @throws            java.lang.IllegalArgumentException if the field is not a number type.
     */
    public Number sum(String fieldName) {
        long columnIndex = columns.get(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return query.sumInt(columnIndex);
            case FLOAT:
                return query.sumFloat(columnIndex);
            case DOUBLE:
                return query.sumDouble(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
        }
    }

    /**
     * Calculate the sum of a field
     * @param fieldName The field name
     * @return The sum
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     * @deprecated Please use {@link #sum(String)} instead.
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
     * @deprecated Please use {@link #sum(String)} instead.
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
     * @deprecated Please use {@link #sum(String)} instead.
     */
    public double sumFloat(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.sumFloat(columnIndex);
    }

    // Average

    /**
     * Returns the average of a given field.
     *
     * @param fieldName  the field to calculate average on. Only number fields are supported.
     * @return           The average for the given field amongst objects in query results. This
     *                   will be of type double for all types of number fields. If no objects exist or
     *                   they all have {@code null} as the value for the given field, {@code 0} will be returned.
     *                   When computing the average, objects with {@code null} values are ignored.
     * @throws           java.lang.IllegalArgumentException if the field is not a number type.
     */
    public double average(String fieldName) {
        long columnIndex = columns.get(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return query.averageInt(columnIndex);
            case DOUBLE:
                return query.averageDouble(columnIndex);
            case FLOAT:
                return query.averageFloat(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
        }
    }

    /**
     * Calculate the average of a field
     * @param fieldName The field name
     * @return The average
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     * @deprecated Please use {@link #average(String)} instead.
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
     * @deprecated Please use {@link #average(String)} instead.
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
     * @deprecated Please use {@link #average(String)} instead.
     */
    public double averageFloat(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.averageFloat(columnIndex);
    }

    // Min

    /**
     * Find the minimum value of a field.
     *
     * @param fieldName   the field to look for a minimum on. Only number fields are supported.
     * @return            if no objects exist or they all have {@code null} as the value for the given
     *                    field, {@code null} will be returned. Otherwise the minimum value is returned.
     *                    When determining the minimum value, objects with {@code null} values are ignored.
     * @throws            java.lang.IllegalArgumentException if the field is not a number type.
     */
    public Number min(String fieldName) {
        realm.checkIfValid();
        long columnIndex = table.getColumnIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return this.query.minimumInt(columnIndex);
            case FLOAT:
                return this.query.minimumFloat(columnIndex);
            case DOUBLE:
                return this.query.minimumDouble(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
        }
    }

    /**
     * Find the minimum value of a field
     * @param fieldName  The field name
     * @return The minimum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated Please use {@link #min(String)} instead.
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
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated Please use {@link #min(String)} instead.
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
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated Please use {@link #min(String)} instead.
     */
    public float minimumFloat(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.minimumFloat(columnIndex);
    }

    /**
     * Find the minimum value of a field
     * @param fieldName  The field name
     * @return           If no objects exist or they all have {@code null} as the value for the given
     *                   date field, {@code null} will be returned. Otherwise the minimum date is returned.
     *                   When determining the minimum date, objects with {@code null} values are ignored.
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public Date minimumDate(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.minimumDate(columnIndex);
    }

    // Max

    /**
     * Find the maximum value of a field.
     *
     * @param fieldName   the field to look for a maximum on. Only number fields are supported.
     * @return            if no objects exist or they all have {@code null} as the value for the given
     *                    field, {@code null} will be returned. Otherwise the maximum value is returned.
     *                    When determining the maximum value, objects with {@code null} values are ignored.
     * @throws            java.lang.IllegalArgumentException if the field is not a number type.
     */
    public Number max(String fieldName) {
        realm.checkIfValid();
        long columnIndex = table.getColumnIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return this.query.maximumInt(columnIndex);
            case FLOAT:
                return this.query.maximumFloat(columnIndex);
            case DOUBLE:
                return this.query.maximumDouble(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
        }
    }

    /**
     * Find the maximum value of a field
     * @param fieldName  The field name
     * @return The maximum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated Please use {@link #max(String)} instead.
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
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated Please use {@link #max(String)} instead.
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
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated Please use {@link #max(String)} instead.
     */
    public float maximumFloat(String fieldName) {
        long columnIndex = columns.get(fieldName);
        return this.query.maximumFloat(columnIndex);
    }

    /**
     * Find the maximum value of a field.
     * @param fieldName  the field name.
     * @return           if no objects exist or they all have {@code null} as the value for the given
     *                   date field, {@code null} will be returned. Otherwise the maximum date is returned.
     *                   When determining the maximum date, objects with {@code null} values are ignored.
     * @throws java.lang.UnsupportedOperationException the query is not valid ("syntax error").
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

    // Execute

    /**
     * Find all objects that fulfill the query conditions.
     *
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmResults<E> findAll() {
        return new RealmResults<E>(realm, query.findAll(), clazz);
    }
    
    /**
     * Similar to {@link #findAll()} but runs asynchronously from a worker thread.
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified
     * when the query completes.
     * @throws java.lang.RuntimeException Any other error
     * @see io.realm.RealmResults
     */
    public RealmResults<E> findAllAsync() {
        checkQueryIsNotReused();
        final WeakReference<Handler> weakHandler = getWeakReferenceHandler();

        // handover the query (to be used by a worker thread)
        final long handoverQueryPointer = query.handoverQuery(realm.sharedGroupManager.getNativePointer());

        // save query arguments (for future update)
        argumentsHolder =  new ArgumentsHolder(ArgumentsHolder.TYPE_FIND_ALL);

        // we need to use the same configuration to open a background SharedGroup (i.e Realm)
        // to perform the query
        final RealmConfiguration realmConfiguration = realm.getConfiguration();

        // prepare an empty reference of the RealmResults, so we can return it immediately (promise)
        // then update it once the query completes in the background.
        RealmResults<E> realmResults = new RealmResults<E>(realm, query, clazz);
        final WeakReference<RealmResults<?>> weakRealmResults = new WeakReference<RealmResults<?>>(realmResults, realm.referenceQueue);
        realm.asyncRealmResults.put(weakRealmResults, this);

        final Future<Long> pendingQuery = Realm.asyncQueryExecutor.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                if (!Thread.currentThread().isInterrupted()) {
                    SharedGroup sharedGroup = null;

                    try {
                        sharedGroup = new SharedGroup(realmConfiguration.getPath(),
                                SharedGroup.IMPLICIT_TRANSACTION,
                                realmConfiguration.getDurability(),
                                realmConfiguration.getEncryptionKey());

                        // Run the query & handover the table view for the caller thread
                        // Note: the handoverQueryPointer contains the versionID needed by the SG in order
                        // to import it.
                        long handoverTableViewPointer = query.findAllWithHandover(sharedGroup.getNativePointer(), sharedGroup.getNativeReplicationPointer(), handoverQueryPointer);

                        QueryUpdateTask.Result result = QueryUpdateTask.Result.newRealmResultsResponse();
                        result.updatedTableViews.put(weakRealmResults, handoverTableViewPointer);
                        result.versionID = sharedGroup.getVersion();

                        // notify the caller thread
                        Handler handler = weakHandler.get();
                        if (handler != null && handler.getLooper().getThread().isAlive()) {
                            handler.obtainMessage(Realm.REALM_COMPLETED_ASYNC_QUERY, result).sendToTarget();
                        }

                        return handoverTableViewPointer;

                    } catch (Exception e) {
                        RealmLog.e(e.getMessage());

                    } finally {
                        if (null != sharedGroup) {
                            sharedGroup.close();
                        }
                    }
                } else {
                    TableQuery.nativeCloseQueryHandover(handoverQueryPointer);
                }

                return INVALID_NATIVE_POINTER;
            }
        });

        realmResults.setPendingQuery(pendingQuery);
        return realmResults;
    }

    /**
     * Find all objects that fulfill the query conditions and sorted by specific field name.
     * <p>
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
    public RealmResults<E> findAllSorted(String fieldName, boolean sortAscending) {
        TableView tableView = query.findAll();
        TableView.Order order = sortAscending ? TableView.Order.ascending : TableView.Order.descending;
        Long columnIndex = columns.get(fieldName);
        if (columnIndex == null || columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }
        tableView.sort(columnIndex, order);
        return new RealmResults<E>(realm, tableView, clazz);
    }

    /**
     * Similar to {@link #findAllSorted(String, boolean)} but runs asynchronously from a worker thread
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified
     * when the query completes.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public RealmResults<E> findAllSortedAsync(String fieldName, boolean sortAscending) {
        checkQueryIsNotReused();
        final TableView.Order order = sortAscending ? TableView.Order.ascending : TableView.Order.descending;
        final Long columnIndex = columns.get(fieldName);
        if (columnIndex == null || columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }

        // capture the query arguments for future retries & update
        argumentsHolder =  new ArgumentsHolder(ArgumentsHolder.TYPE_FIND_ALL_SORTED);
        argumentsHolder.ascending = sortAscending;
        argumentsHolder.columnIndex = columnIndex;

        final WeakReference<Handler> weakHandler = getWeakReferenceHandler();

        // handover the query (to be used by a worker thread)
        final long handoverQueryPointer = query.handoverQuery(realm.sharedGroupManager.getNativePointer());

        // we need to use the same configuration to open a background SharedGroup to perform the query
        final RealmConfiguration realmConfiguration = realm.getConfiguration();

        RealmResults<E> realmResults = new RealmResults<E>(realm, query, clazz);
        final WeakReference<RealmResults<?>> weakRealmResults = new WeakReference<RealmResults<?>>(realmResults, realm.referenceQueue);
        realm.asyncRealmResults.put(weakRealmResults, this);

        final Future<Long> pendingQuery = Realm.asyncQueryExecutor.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                if (!Thread.currentThread().isInterrupted()) {
                    SharedGroup sharedGroup = null;

                    try {
                        sharedGroup = new SharedGroup(realmConfiguration.getPath(),
                                SharedGroup.IMPLICIT_TRANSACTION,
                                realmConfiguration.getDurability(),
                                realmConfiguration.getEncryptionKey());

                        // run the query & handover the table view for the caller thread
                        long handoverTableViewPointer = query.findAllSortedWithHandover(sharedGroup.getNativePointer(), sharedGroup.getNativeReplicationPointer(), handoverQueryPointer, columnIndex, (order == TableView.Order.ascending));

                        QueryUpdateTask.Result result = QueryUpdateTask.Result.newRealmResultsResponse();
                        result.updatedTableViews.put(weakRealmResults, handoverTableViewPointer);
                        result.versionID = sharedGroup.getVersion();

                        Handler handler = weakHandler.get();
                        if (handler != null && handler.getLooper().getThread().isAlive()) {
                            handler.obtainMessage(Realm.REALM_COMPLETED_ASYNC_QUERY, result).sendToTarget();
                        }

                        return handoverTableViewPointer;

                    } catch (Exception e) {
                        RealmLog.e(e.getMessage());

                    } finally {
                        if (sharedGroup != null) {
                            sharedGroup.close();
                        }
                    }
                } else {
                    TableQuery.nativeCloseQueryHandover(handoverQueryPointer);
                }

                return INVALID_NATIVE_POINTER;
            }
        });
        realmResults.setPendingQuery(pendingQuery);
        return realmResults;
    }


    /**
     * Find all objects that fulfill the query conditions and sorted by specific field name in
     * ascending order.
     * <p>
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName the field name to sort by.
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public RealmResults<E> findAllSorted(String fieldName) {
        return findAllSorted(fieldName, true);
    }

    /**
     * Similar to {@link #findAllSorted(String)} but runs asynchronously from a worker thread
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified
     * when the query completes.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public RealmResults<E> findAllSortedAsync(String fieldName) {
        return findAllSortedAsync(fieldName, true);
    }

    /**
     * Find all objects that fulfill the query conditions and sorted by specific field names.
     * <p>
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
    public RealmResults<E> findAllSorted(String fieldNames[], boolean sortAscending[]) {
        checkSortParameters (fieldNames, sortAscending);

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
            return new RealmResults<E>(realm, tableView, clazz);
        }
    }

    /**
     * Similar to {@link #findAllSorted(String[], boolean[])} but runs asynchronously
     * from a worker thread
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified
     * when the query completes.
     * @throws java.lang.RuntimeException Any other error
     * @see io.realm.RealmResults
     */
    public RealmResults<E> findAllSortedAsync(String fieldNames[], final boolean [] sortAscending) {
        checkQueryIsNotReused();
        checkSortParameters (fieldNames, sortAscending);

        if (fieldNames.length == 1 && sortAscending.length == 1) {
            return findAllSortedAsync(fieldNames[0], sortAscending[0]);

        } else {
            final WeakReference<Handler> weakHandler = getWeakReferenceHandler();

            // Handover the query (to be used by a worker thread)
            final long handoverQueryPointer = query.handoverQuery(realm.sharedGroupManager.getNativePointer());

            // We need to use the same configuration to open a background SharedGroup to perform the query
            final RealmConfiguration realmConfiguration = realm.getConfiguration();

            final long indices[] = new long[fieldNames.length];
            for (int i = 0; i < fieldNames.length; i++) {
                String fieldName = fieldNames[i];
                Long columnIndex = columns.get(fieldName);
                if (columnIndex == null || columnIndex < 0) {
                    throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
                }
                indices[i] = columnIndex;
            }

            // capture the query arguments for future retries & update
            argumentsHolder = new ArgumentsHolder(ArgumentsHolder.TYPE_FIND_ALL_MULTI_SORTED);
            argumentsHolder.ascendings = sortAscending;
            argumentsHolder.columnIndices = indices;

            // prepare the promise result
            RealmResults<E> realmResults = new RealmResults<E>(realm, query, clazz);
            final WeakReference<RealmResults<?>> weakRealmResults = new WeakReference<RealmResults<?>>(realmResults, realm.referenceQueue);
            realm.asyncRealmResults.put(weakRealmResults, this);

            final Future<Long> pendingQuery = Realm.asyncQueryExecutor.submit(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    if (!Thread.currentThread().isInterrupted()) {
                        SharedGroup sharedGroup = null;

                        try {
                            sharedGroup = new SharedGroup(realmConfiguration.getPath(),
                                    SharedGroup.IMPLICIT_TRANSACTION,
                                    realmConfiguration.getDurability(),
                                    realmConfiguration.getEncryptionKey());

                            // run the query & handover the table view for the caller thread
                            long handoverTableViewPointer = query.findAllMultiSortedWithHandover(sharedGroup.getNativePointer(), sharedGroup.getNativeReplicationPointer(), handoverQueryPointer, indices, sortAscending);

                            QueryUpdateTask.Result result = QueryUpdateTask.Result.newRealmResultsResponse();
                            result.updatedTableViews.put(weakRealmResults, handoverTableViewPointer);
                            result.versionID = sharedGroup.getVersion();

                            Handler handler = weakHandler.get();
                            if (handler!= null && handler.getLooper().getThread().isAlive()) {
                                handler.obtainMessage(Realm.REALM_COMPLETED_ASYNC_QUERY, result).sendToTarget();
                            }

                            return handoverTableViewPointer;

                        } catch (Exception e) {
                            RealmLog.e(e.getMessage());

                        } finally {
                            if (sharedGroup != null) {
                                sharedGroup.close();
                            }
                        }
                    } else {
                        TableQuery.nativeCloseQueryHandover(handoverQueryPointer);
                    }

                    return INVALID_NATIVE_POINTER;
                }
            });

            realmResults.setPendingQuery(pendingQuery);
            return realmResults;
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
    public RealmResults<E> findAllSorted(String fieldName1, boolean sortAscending1,
                                   String fieldName2, boolean sortAscending2) {
        return findAllSorted(new String[]{fieldName1, fieldName2}, new boolean[]{sortAscending1, sortAscending2});
    }

    /**
     * Similar to {@link #findAllSorted(String, boolean, String, boolean)} but runs asynchronously from a worker thread
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified
     * when the query completes.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public RealmResults<E> findAllSortedAsync(String fieldName1, boolean sortAscending1,
                                         String fieldName2, boolean sortAscending2) {
        return findAllSortedAsync(new String[]{fieldName1, fieldName2}, new boolean[]{sortAscending1, sortAscending2});
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
    public RealmResults<E> findAllSorted(String fieldName1, boolean sortAscending1,
                                   String fieldName2, boolean sortAscending2,
                                   String fieldName3, boolean sortAscending3) {
        return findAllSorted(new String[]{fieldName1, fieldName2, fieldName3},
                new boolean[]{sortAscending1, sortAscending2, sortAscending3});
    }

    /**
     * Similar to {@link #findAllSorted(String, boolean, String, boolean, String, boolean)} but
     * runs asynchronously from a worker thread
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified
     * when the query completes.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public RealmResults<E> findAllSortedAsync(String fieldName1, boolean sortAscending1,
                                              String fieldName2, boolean sortAscending2,
                                              String fieldName3, boolean sortAscending3) {
        return findAllSortedAsync(new String[]{fieldName1, fieldName2, fieldName3},
                new boolean[]{sortAscending1, sortAscending2, sortAscending3});
    }

    /**
     * Find the first object that fulfills the query conditions.
     *
     * @return The object found or {@code null} if no object matches the query conditions.
     * @see io.realm.RealmObject
     * @throws java.lang.RuntimeException Any other error.
     */
    public E findFirst() {
        long rowIndex = this.query.find();
        if (rowIndex >= 0) {
            return realm.get(clazz, (view != null) ? view.getTargetRowIndex(rowIndex) : rowIndex);
        } else {
            return null;
        }
    }

    /**
     * Similar to {@link #findFirst()} but runs asynchronously from a worker thread
     *
     * @return immediately an empty {@link RealmObject}. Users need to register a listener
     * {@link io.realm.RealmObject#addChangeListener} to be notified when the query completes.
     */
    public E findFirstAsync () {
        checkQueryIsNotReused();
        final WeakReference<Handler> weakHandler = getWeakReferenceHandler();

        // handover the query (to be used by a worker thread)
        final long handoverQueryPointer = query.handoverQuery(realm.sharedGroupManager.getNativePointer());

        // save query arguments (for future update)
        argumentsHolder =  new ArgumentsHolder(ArgumentsHolder.TYPE_FIND_FIRST);

        final RealmConfiguration realmConfiguration = realm.getConfiguration();

        // prepare an empty reference of the RealmObject, so we can return it immediately (promise)
        // then update it once the query complete in the background.
        final E result = realm.getConfiguration().getSchemaMediator().newInstance(clazz);
        final WeakReference<RealmObject> realmObjectWeakReference = new WeakReference<RealmObject>(result);
        realm.asyncRealmObjects.put(realmObjectWeakReference, this);
        result.realm = realm;
        result.row = Row.EMPTY_ROW;

        final Future<Long> pendingQuery = Realm.asyncQueryExecutor.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                if (!Thread.currentThread().isInterrupted()) {
                    SharedGroup sharedGroup = null;

                    try {
                        sharedGroup = new SharedGroup(realmConfiguration.getPath(),
                                SharedGroup.IMPLICIT_TRANSACTION,
                                realmConfiguration.getDurability(),
                                realmConfiguration.getEncryptionKey());

                        long handoverTableViewPointer =query.findWithHandover(sharedGroup.getNativePointer(),
                                sharedGroup.getNativeReplicationPointer(), handoverQueryPointer);

                        QueryUpdateTask.Result result = QueryUpdateTask.Result.newRealmObjectResponse();
                        result.updatedRow.put(realmObjectWeakReference, handoverTableViewPointer);
                        result.versionID = sharedGroup.getVersion();

                        Handler handler = weakHandler.get();
                        if (handler != null && handler.getLooper().getThread().isAlive()) {
                            handler.obtainMessage(Realm.REALM_COMPLETED_ASYNC_FIND_FIRST, result).sendToTarget();
                        }
                        return handoverTableViewPointer;

                    }  catch (Exception e) {
                        RealmLog.e(e.getMessage());

                    } finally {
                        if (null != sharedGroup) {
                            sharedGroup.close();
                        }
                    }
                } else {
                    TableQuery.nativeCloseQueryHandover(handoverQueryPointer);
                }

                return INVALID_NATIVE_POINTER;
            }
        });
        result.setPendingQuery(pendingQuery);

        return result;
    }

    private void checkSortParameters (String fieldNames[], final boolean [] sortAscendings) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("fieldNames cannot be 'null'.");
        } else if (sortAscendings == null) {
            throw new IllegalArgumentException("sortAscending cannot be 'null'.");
        } else if (fieldNames.length == 0) {
            throw new IllegalArgumentException("At least one field name must be specified.");
        } else if (fieldNames.length != sortAscendings.length) {
            throw new IllegalArgumentException(String.format("Number of field names (%d) and sort orders (%d) does not match.", fieldNames.length, sortAscendings.length));
        }
    }

    private WeakReference<Handler> getWeakReferenceHandler() {
        if (realm.handler == null) {
            throw new IllegalStateException("Your Realm is opened from a thread without a Looper." +
                    " Async queries need a Handler to send results of your query");
        }
        return new WeakReference<Handler>(realm.handler); // use caller Realm's Looper
    }

    // We need to prevent the user from using the query again (mostly for async)
    // Ex: if the first query fail with findFirstAsync, if the user reuse the same RealmQuery
    //     with findAllSorted, argumentsHolder of the first query will be overridden,
    //     which cause any retry to use the findAllSorted argumentsHolder.
    private void checkQueryIsNotReused () {
        if (argumentsHolder != null) {
            throw new IllegalStateException("This RealmQuery is already used by a find* query, please create a new query");
        }
    }

    public ArgumentsHolder getArgument () {
        return argumentsHolder;
    }

    /**
     * export & handover the query to be used by a worker thread
     * @return the exported handover pointer for this RealmQuery
     */
    long handoverQueryPointer () {
        return query.handoverQuery(realm.sharedGroupManager.getNativePointer());
    }
}
