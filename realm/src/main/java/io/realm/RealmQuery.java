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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.internal.LinkView;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;
import io.realm.internal.TableView;
import io.realm.annotations.Required;

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

    private BaseRealm realm;
    private Class<E> clazz;
    private String className;
    private Map<String, Long> columns = new HashMap<String, Long>();
    private Table table;
    private LinkView view;
    private TableQuery query;
    private static final String TYPE_MISMATCH = "Field '%s': type mismatch - %s expected.";

    /**
     * Creates a query for objects of a given class from a {@link Realm}.
     *
     * @param realm  The realm to query within.
     * @param clazz  The class to query.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */
    public static <E extends RealmObject> RealmQuery<E> createQuery(Realm realm, Class<E> clazz) {
        return new RealmQuery<E>(realm, clazz);
    }

    /**
     * Creates a query for dynamic objects of a given type from a {@link DynamicRealm}.
     *
     * @param realm  The realm to query within.
     * @param className  The type to query.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */
    public static <E extends RealmObject> RealmQuery<E> createDynamicQuery(DynamicRealm realm, String className) {
        return new RealmQuery<E>(realm, className);
    }

    /**
     * Creates a query from a existing {@link RealmResults}.
     *
     * @param queryResults   A existing @{link io.realm.RealmResults} to query against.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */

    @SuppressWarnings("unchecked")
    public static <E extends RealmObject> RealmQuery<E> createQueryFromResult(RealmResults<E> queryResults) {
        if (queryResults.classSpec != null) {
            return new RealmQuery<E>(queryResults, queryResults.classSpec);
        } else {
            return new RealmQuery(queryResults, queryResults.className);
        }
    }

    /**
     * Creates a query from a existing {@link RealmList}.
     *
     * @param list   A existing @{link io.realm.RealmList} to query against.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */
    @SuppressWarnings("unchecked")
    public static <E extends RealmObject> RealmQuery<E> createQueryFromList(RealmList<E> list) {
        if (list.clazz != null) {
            return new RealmQuery(list.realm, list.view, list.clazz);
        } else {
            return new RealmQuery(list.realm, list.view, list.className);
        }
    }

    private RealmQuery(Realm realm, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.table = realm.getTable(clazz);
        this.view = null;
        this.query = table.where();
        this.columns = realm.columnIndices.getClassFields(clazz);
    }

    private RealmQuery(RealmResults<E> queryResults, Class<E> clazz) {
        this.realm = queryResults.realm;
        this.clazz = clazz;
        this.table = realm.getTable(clazz);
        this.view = null;
        this.query = queryResults.getTable().where();
        this.columns = realm.columnIndices.getClassFields(clazz);
    }

    private RealmQuery(BaseRealm realm, LinkView view, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.query = view.where();
        this.view = view;
        this.table = realm.getTable(clazz);
        this.columns = realm.columnIndices.getClassFields(clazz);
    }

    private RealmQuery(BaseRealm realm, String className) {
        this.realm = realm;
        this.className = className;
        this.table = realm.getTable(className);
        this.query = table.where();
        this.columns = new DynamicColumnMap(table);
    }

    private RealmQuery(RealmResults<DynamicRealmObject> queryResults, String className) {
        this.realm = queryResults.realm;
        this.className = className;
        this.table = realm.getTable(className);
        this.query = queryResults.getTable().where();
        this.columns = new DynamicColumnMap(table);
    }

    private RealmQuery(BaseRealm realm, LinkView view, String className) {
        this.realm = realm;
        this.className = className;
        this.query = view.where();
        this.view = view;
        this.table = realm.getTable(className);
        this.columns = new DynamicColumnMap(table);
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
        arr[n] = s.substring(s.lastIndexOf('.')+1);

        return arr;
    }

    // TODO: consider another caching strategy so linked classes are included in the cache.
    private long[] getColumnIndices(String fieldName, RealmFieldType fieldType) {
        Table table = this.table;
        if (containsDot(fieldName)) {
            String[] names = splitString(fieldName); //fieldName.split("\\.");
            long[] columnIndices = new long[names.length];
            for (int i = 0; i < names.length-1; i++) {
                long index = table.getColumnIndex(names[i]);
                if (index < 0) {
                    throw new IllegalArgumentException("Invalid query: " + names[i] + " does not refer to a class.");
                }
                RealmFieldType type = table.getColumnType(index);
                if (type == RealmFieldType.OBJECT || type == RealmFieldType.LIST) {
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

            RealmFieldType tableColumnType = table.getColumnType(columns.get(fieldName));
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
        return this.equalTo(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Equal-to comparison.
     * @param fieldName   the field to compare.
     * @param value       the value to compare with.
     * @param casing     How to handle casing. Setting this to {@link Case#INSENSITIVE} only works for English locale characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.

     */
    public RealmQuery<E> equalTo(String fieldName, String value, Case casing) {
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.equalTo(columnIndices, value, casing);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.BOOLEAN);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.DATE);
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
        return this.notEqualTo(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Not-equal-to comparison.
     * @param fieldName  the field to compare.
     * @param value      the value to compare with.
     * @param casing     How casing is handled. {@link Case#INSENSITIVE} works only for the English locale characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or
     * field type.
     * @throws java.lang.RuntimeException if any other error happens.
     */
    public RealmQuery<E> notEqualTo(String fieldName, String value, Case casing) {
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.STRING);
        if (columnIndices.length > 1 && !casing.getValue()) {
            throw new IllegalArgumentException("Link queries cannot be case insensitive - coming soon.");
        }
        this.query.notEqualTo(columnIndices, value, casing);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.BOOLEAN);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.DATE);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long[] columnIndices = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.DATE);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.DATE);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.DATE);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.DATE);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.DATE);
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
        return contains(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Condition that value of field contains the specified substring
     * @param fieldName  The field to compare
     * @param value      The substring
     * @param casing     How to handle casing. Setting this to {@link Case#INSENSITIVE} only works for English locale characters.
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> contains(String fieldName, String value, Case casing) {
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.contains(columnIndices, value, casing);
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
        return beginsWith(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Condition that the value of field begins with the specified substring
     * @param fieldName The field to compare
     * @param value     The substring
     * @param casing     How to handle casing. Setting this to {@link Case#INSENSITIVE} only works for English locale characters.
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> beginsWith(String fieldName, String value, Case casing) {
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.beginsWith(columnIndices, value, casing);
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
        return endsWith(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Condition that the value of field ends with the specified substring
     * @param fieldName The field to compare
     * @param value     The substring
     * @param casing     How to handle casing. Setting this to {@link Case#INSENSITIVE} only works for English locale characters.
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> endsWith(String fieldName, String value, Case casing) {
        long columnIndices[] = getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.endsWith(columnIndices, value, casing);
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
     * @see io.realm.RealmResults
     * @throws java.lang.RuntimeException Any other error
     */
    @SuppressWarnings("unchecked")
    public RealmResults<E> findAll() {
        if (isDynamicQuery()) {
            return (RealmResults<E>) RealmResults.createFromDynamicQuery(realm, query.findAll(), className);
        } else {
            return RealmResults.createFromQuery(realm, query.findAll(), clazz);
        }
    }

    /**
     * Find all objects that fulfill the query conditions and sorted by specific field name.
     *
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName the field name to sort by.
     * @param sortOrder how to sort the results.
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    @SuppressWarnings("unchecked")
    public RealmResults<E> findAllSorted(String fieldName, Sort sortOrder) {
        TableView tableView = query.findAll();
        Long columnIndex = columns.get(fieldName);
        if (columnIndex == null || columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }
        tableView.sort(columnIndex, sortOrder);

        if (isDynamicQuery()) {
            return (RealmResults<E>) RealmResults.createFromDynamicQuery(realm, tableView, className);
        } else {
            return RealmResults.createFromQuery(realm, tableView, clazz);
        }
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
    public RealmResults<E> findAllSorted(String fieldName) {
        return findAllSorted(fieldName, Sort.ASCENDING);
    }

    /**
     * Find all objects that fulfill the query conditions and sorted by specific field names.
     *
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldNames an array of field names to sort by.
     * @param sortOrders how to sort the field names.
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    @SuppressWarnings("unchecked")
    public RealmResults<E> findAllSorted(String fieldNames[], Sort sortOrders[]) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("fieldNames cannot be 'null'.");
        } else if (sortOrders == null) {
            throw new IllegalArgumentException("sortOrders cannot be 'null'.");
        } else if (fieldNames.length == 0) {
            throw new IllegalArgumentException("At least one field name must be specified.");
        } else if (fieldNames.length != sortOrders.length) {
            throw new IllegalArgumentException(String.format("Number of field names (%d) and sort orders (%d) does not match.", fieldNames.length, sortOrders.length));
        }

        if (fieldNames.length == 1 && sortOrders.length == 1) {
            return findAllSorted(fieldNames[0], sortOrders[0]);
        } else {
            TableView tableView = query.findAll();
            List<Long> columnIndices = new ArrayList<Long>();
            for (int i = 0; i < fieldNames.length; i++) {
                String fieldName = fieldNames[i];
                Long columnIndex = columns.get(fieldName);
                if (columnIndex == null || columnIndex < 0) {
                    throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
                }
                columnIndices.add(columnIndex);
            }
            tableView.sort(columnIndices, sortOrders);

            if (isDynamicQuery()) {
                return (RealmResults<E>) RealmResults.createFromDynamicQuery(realm, tableView, className);
            } else {
                return RealmResults.createFromQuery(realm, tableView, clazz);
            }
        }
    }

    private boolean isDynamicQuery() {
        return className != null;
    }

    /**
     * Find all objects that fulfill the query conditions and sorted by specific field names in
     * ascending order.
     *
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName1 first field name
     * @param sortOrder1 sort order for first field
     * @param fieldName2 second field name
     * @param sortOrder2 sort order for second field
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public RealmResults<E> findAllSorted(String fieldName1, Sort sortOrder1,
                                   String fieldName2, Sort sortOrder2) {
        return findAllSorted(new String[]{fieldName1, fieldName2}, new Sort[]{sortOrder1, sortOrder2});
    }


    /**
     * Find all objects that fulfill the query conditions and sorted by specific field names in
     * ascending order.
     *
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName1 first field name
     * @param sortOrder1 sort order for first field
     * @param fieldName2 second field name
     * @param sortOrder2 sort order for second field
     * @param fieldName3 third field names
     * @param sortOrder3 sort order for third field
     * @return A {@link io.realm.RealmResults} containing objects. If no objects match the condition,
     * a list with zero objects is returned.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public RealmResults<E> findAllSorted(String fieldName1, Sort sortOrder1,
                                   String fieldName2, Sort sortOrder2,
                                   String fieldName3, Sort sortOrder3) {
        return findAllSorted(new String[] {fieldName1, fieldName2, fieldName3},
                new Sort[] {sortOrder1, sortOrder2, sortOrder3});
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
            return realm.get(clazz, className, (view != null) ? view.getTargetRowIndex(rowIndex) : rowIndex);
        } else {
            return null;
        }
    }

    // FIXME Replace with Schema when it is available
    private static class DynamicColumnMap implements Map<String, Long> {

        private final Table table;

        public DynamicColumnMap(Table table) {
            this.table = table;
        }

        @Override
        public Long get(Object key) {
            return table.getColumnIndex((String) key);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<String, Long>> entrySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long put(String key, Long value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Long> map) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Long> values() {
            throw new UnsupportedOperationException();
        }
    }

}
