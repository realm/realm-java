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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import io.realm.annotations.Required;
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
 * A RealmQuery encapsulates a query on a {@link io.realm.Realm} or a {@link io.realm.RealmResults} using the Builder
 * pattern. The query is executed using either {@link #findAll()} or {@link #findFirst()}
 * <p>
 * The input to many of the query functions take a field name as String. Note that this is not type safe. If a 
 * RealmObject class is refactored care has to be taken to not break any queries.
 * <p>
 * A {@link io.realm.Realm} is unordered, which means that there is no guarantee that querying a Realm will return the
 * objects in the order they where inserted. Use {@link #findAllSorted(String)} and similar methods if a specific order
 * is required.
 * <p>
 * A RealmQuery cannot be passed between different threads.
 *
 * @param <E> the class of the objects to be queried.
 * @see <a href="http://en.wikipedia.org/wiki/Builder_pattern">Builder pattern</a>
 * @see Realm#where(Class)
 * @see RealmResults#where()
 */
public class RealmQuery<E extends RealmObject> {

    private BaseRealm realm;
    private Class<E> clazz;
    private String className;
    private Table table;
    private RealmObjectSchema schema;
    private LinkView view;
    private TableQuery query;
    private static final String TYPE_MISMATCH = "Field '%s': type mismatch - %s expected.";

    private final static Long INVALID_NATIVE_POINTER = 0L;
    private ArgumentsHolder argumentsHolder;

    /**
     * Creates a query for objects of a given class from a {@link Realm}.
     *
     * @param realm  the realm to query within.
     * @param clazz  the class to query.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */
    public static <E extends RealmObject> RealmQuery<E> createQuery(Realm realm, Class<E> clazz) {
        return new RealmQuery<E>(realm, clazz);
    }

    /**
     * Creates a query for dynamic objects of a given type from a {@link DynamicRealm}.
     *
     * @param realm  the realm to query within.
     * @param className  the type to query.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */
    public static <E extends RealmObject> RealmQuery<E> createDynamicQuery(DynamicRealm realm, String className) {
        return new RealmQuery<E>(realm, className);
    }

    /**
     * Creates a query from a existing {@link RealmResults}.
     *
     * @param queryResults   a existing @{link io.realm.RealmResults} to query against.
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
     * @param list   a existing @{link io.realm.RealmList} to query against.
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
        this.schema = realm.schema.getSchemaForClass(clazz);
        this.table = schema.table;
        this.view = null;
        this.query = table.where();
    }

    private RealmQuery(RealmResults<E> queryResults, Class<E> clazz) {
        this.realm = queryResults.realm;
        this.clazz = clazz;
        this.schema = realm.schema.getSchemaForClass(clazz);
        this.table = schema.table;
        this.view = null;
        this.query = queryResults.getTable().where();
    }

    private RealmQuery(BaseRealm realm, LinkView view, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.query = view.where();
        this.view = view;
        this.schema = realm.schema.getSchemaForClass(clazz);
        this.table = schema.table;
    }

    private RealmQuery(BaseRealm realm, String className) {
        this.realm = realm;
        this.className = className;
        this.schema = realm.schema.getSchemaForClass(className);
        this.table = schema.table;
        this.query = table.where();
    }

    private RealmQuery(RealmResults<DynamicRealmObject> queryResults, String className) {
        this.realm = queryResults.realm;
        this.className = className;
        this.schema = realm.schema.getSchemaForClass(className);
        this.table = schema.table;
        this.query = queryResults.getTable().where();
    }

    private RealmQuery(BaseRealm realm, LinkView view, String className) {
        this.realm = realm;
        this.className = className;
        this.query = view.where();
        this.view = view;
        this.schema = realm.schema.getSchemaForClass(className);
        this.table = schema.table;
    }

    /**
     * Checks if {@link io.realm.RealmQuery} is still valid to use i.e. the {@link io.realm.Realm} instance hasn't been
     * closed and any parent {@link io.realm.RealmResults} is still valid.
     *
     * @return {@code true} if still valid to use, {@code false} otherwise.
     */
    public boolean isValid() {
        if (realm == null || realm.isClosed()) {
            return false;
        }

        if (view != null) {
            return view.isAttached();
        }
        return table != null && table.isValid();
    }

    /**
     * Tests if a field is {@code null}. Only works for nullable fields.
     *
     * For link queries, if any part of the link path is {@code null} the whole path is considered to be {@code null}
     * e.g. {@code isNull("linkField.stringField")} will be considered to be {@code null} if either {@code linkField} or
     * {@code linkField.stringField} is {@code null}.
     *
     * @param fieldName the field name.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field is not nullable.
     * @see Required for further infomation.
     */
    public RealmQuery<E> isNull(String fieldName) {
        long columnIndices[] = schema.getColumnIndices(fieldName);

        // checking that fieldName has the correct type is done in C++
        this.query.isNull(columnIndices);
        return this;
    }

    /**
     * Tests if a field is not {@code null}. Only works for nullable fields.
     *
     * @param fieldName the field name.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field is not nullable.
     * @see Required for further infomation.
     */
    public RealmQuery<E> isNotNull(String fieldName) {
        long columnIndices[] = schema.getColumnIndices(fieldName);

        // checking that fieldName has the correct type is done in C++
        this.query.isNotNull(columnIndices);
        return this;
    }

    // Equal

    /**
     * Equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> equalTo(String fieldName, String value) {
        return this.equalTo(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @param casing     How to handle casing. Setting this to {@link Case#INSENSITIVE} only works for English locale characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> equalTo(String fieldName, String value, Case casing) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.equalTo(columnIndices, value, casing);
        return this;
    }

    /**
     * Equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> equalTo(String fieldName, Byte value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> equalTo(String fieldName, Short value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> equalTo(String fieldName, Integer value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> equalTo(String fieldName, Long value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> equalTo(String fieldName, Double value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
     * @param value the value to compare with.
     * @return The query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> equalTo(String fieldName, Float value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> equalTo(String fieldName, Boolean value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.BOOLEAN);
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
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> equalTo(String fieldName, Date value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.equalTo(columnIndices, value);
        return this;
    }

    // Not Equal

    /**
     * Not-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, String value) {
        return this.notEqualTo(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Not-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @param casing     How casing is handled. {@link Case#INSENSITIVE} works only for the English locale characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, String value, Case casing) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        if (columnIndices.length > 1 && !casing.getValue()) {
            throw new IllegalArgumentException("Link queries cannot be case insensitive - coming soon.");
        }
        this.query.notEqualTo(columnIndices, value, casing);
        return this;
    }

    /**
     * Not-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Byte value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Short value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Integer value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
     * @param value the value to compare with.
     * @return the query object
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Long value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Double value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Float value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Boolean value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.BOOLEAN);
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
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, Date value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        if (value == null) {
            this.query.isNotNull(columnIndices);
        } else {
            this.query.notEqualTo(columnIndices, value);
        }
        return this;
    }

    // Greater Than

    /**
     * Greater-than comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> greaterThan(String fieldName, int value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.greaterThan(columnIndices, value);
        return this;
    }

    /**
     * Greater-than comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> greaterThan(String fieldName, long value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.greaterThan(columnIndices, value);
        return this;
    }

    /**
     * Greater-than comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> greaterThan(String fieldName, double value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        this.query.greaterThan(columnIndices, value);
        return this;
    }

    /**
     * Greater-than comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> greaterThan(String fieldName, float value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        this.query.greaterThan(columnIndices, value);
        return this;
    }

    /**
     * Greater-than comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> greaterThan(String fieldName, Date value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.greaterThan(columnIndices, value);
        return this;
    }

    /**
     * Greater-than-or-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, int value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.greaterThanOrEqual(columnIndices, value);
        return this;
    }

    /**
     * Greater-than-or-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, long value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.greaterThanOrEqual(columnIndices, value);
        return this;
    }

    /**
     * Greater-than-or-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, double value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        this.query.greaterThanOrEqual(columnIndices, value);
        return this;
    }

    /**
     * Greater-than-or-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type
     */
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, float value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        this.query.greaterThanOrEqual(columnIndices, value);
        return this;
    }

    /**
     * Greater-than-or-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, Date value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.greaterThanOrEqual(columnIndices, value);
        return this;
    }

    // Less Than

    /**
     * Less-than comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> lessThan(String fieldName, int value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.lessThan(columnIndices, value);
        return this;
    }

    /**
     * Less-than comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> lessThan(String fieldName, long value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.lessThan(columnIndices, value);
        return this;
    }

    /**
     * Less-than comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> lessThan(String fieldName, double value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        this.query.lessThan(columnIndices, value);
        return this;
    }

    /**
     * Less-than comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> lessThan(String fieldName, float value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        this.query.lessThan(columnIndices, value);
        return this;
    }

    /**
     * Less-than comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> lessThan(String fieldName, Date value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.lessThan(columnIndices, value);
        return this;
    }

    /**
     * Less-than-or-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, int value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.lessThanOrEqual(columnIndices, value);
        return this;
    }

    /**
     * Less-than-or-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, long value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.lessThanOrEqual(columnIndices, value);
        return this;
    }

    /**
     * Less-than-or-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, double value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        this.query.lessThanOrEqual(columnIndices, value);
        return this;
    }

    /**
     * Less-than-or-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, float value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        this.query.lessThanOrEqual(columnIndices, value);
        return this;
    }

    /**
     * Less-than-or-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, Date value) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.lessThanOrEqual(columnIndices, value);
        return this;
    }

    // Between

    /**
     * Between condition.
     *
     * @param fieldName the field to compare.
     * @param from lowest value (inclusive).
     * @param to highest value (inclusive).
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> between(String fieldName, int from, int to) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.between(columnIndices, from, to);
        return this;
    }

    /**
     * Between condition.
     *
     * @param fieldName the field to compare.
     * @param from lowest value (inclusive).
     * @param to highest value (inclusive).
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> between(String fieldName, long from, long to) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.between(columnIndices, from, to);
        return this;
    }

    /**
     * Between condition.
     *
     * @param fieldName the field to compare.
     * @param from lowest value (inclusive).
     * @param to highest value (inclusive).
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> between(String fieldName, double from, double to) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        this.query.between(columnIndices, from, to);
        return this;
    }

    /**
     * Between condition.
     *
     * @param fieldName the field to compare.
     * @param from lowest value (inclusive).
     * @param to highest value (inclusive).
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> between(String fieldName, float from, float to) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        this.query.between(columnIndices, from, to);
        return this;
    }

    /**
     * Between condition.
     *
     * @param fieldName the field to compare.
     * @param from lowest value (inclusive).
     * @param to highest value (inclusive).
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> between(String fieldName, Date from, Date to) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.between(columnIndices, from, to);
        return this;
    }


    // Contains

    /**
     * Condition that value of field contains the specified substring.
     *
     * @param fieldName the field to compare.
     * @param value the substring.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> contains(String fieldName, String value) {
        return contains(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Condition that value of field contains the specified substring.
     *
     * @param fieldName the field to compare.
     * @param value the substring.
     * @param casing     How to handle casing. Setting this to {@link Case#INSENSITIVE} only works for English locale characters.
     * @return The query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> contains(String fieldName, String value, Case casing) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.contains(columnIndices, value, casing);
        return this;
    }

    /**
     * Condition that the value of field begins with the specified string.
     *
     * @param fieldName the field to compare.
     * @param value the string.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> beginsWith(String fieldName, String value) {
        return beginsWith(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Condition that the value of field begins with the specified substring.
     *
     * @param fieldName the field to compare.
     * @param value the substring.
     * @param casing     How to handle casing. Setting this to {@link Case#INSENSITIVE} only works for English locale characters.
     * @return the query object
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> beginsWith(String fieldName, String value, Case casing) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.beginsWith(columnIndices, value, casing);
        return this;
    }

    /**
     * Condition that the value of field ends with the specified string.
     *
     * @param fieldName the field to compare.
     * @param value the string.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> endsWith(String fieldName, String value) {
        return endsWith(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Condition that the value of field ends with the specified substring.
     *
     * @param fieldName the field to compare.
     * @param value the substring.
     * @param casing     How to handle casing. Setting this to {@link Case#INSENSITIVE} only works for English locale characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or field type.
     */
    public RealmQuery<E> endsWith(String fieldName, String value, Case casing) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.endsWith(columnIndices, value, casing);
        return this;
    }

    // Grouping

    /**
     * Begin grouping of conditions ("left parenthesis"). A group must be closed with a call to {@code endGroup()}.
     *
     * @return the query object.
     * @see #endGroup()
     */
    public RealmQuery<E> beginGroup() {
        this.query.group();
        return this;
    }

    /**
     * End grouping of conditions ("right parenthesis") which was opened by a call to {@code beginGroup()}.
     *
     * @return the query object.
     * @see #beginGroup()
     */
    public RealmQuery<E> endGroup() {
        this.query.endGroup();
        return this;
    }

    /**
     * Logical-or two conditions.
     *
     * @return the query object.
     */
    public RealmQuery<E> or() {
        this.query.or();
        return this;
    }

    /**
     * Negate condition.
     *
     * @return the query object.
     */
    public RealmQuery<E> not() {
        this.query.not();
        return this;
    }

    /**
     * Condition that find values that are considered "empty", i.e. an empty list, the 0-length string or byte array.
     *
     * @param fieldName the field to compare.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field name isn't valid or its type isn't either a RealmList,
     * String or byte array.
     */
    public RealmQuery<E> isEmpty(String fieldName) {
        long columnIndices[] = schema.getColumnIndices(fieldName, RealmFieldType.STRING, RealmFieldType.BINARY, RealmFieldType.LIST);
        this.query.isEmpty(columnIndices);
        return this;
    }

    // Aggregates

    // Sum

    /**
     * Calculates the sum of a given field.
     *
     * @param fieldName the field to sum. Only number fields are supported.
     * @return the sum if no objects exist or they all have {@code null} as the value for the given field, {@code 0}
     * will be returned. When computing the sum, objects with {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     */
    public Number sum(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
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
     * Calculates the sum of a field.
     *
     * @param fieldName the field name.
     * @return the sum.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @deprecated please use {@link #sum(String)} instead.
     */
    public long sumInt(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.sumInt(columnIndex);
    }

    /**
     * Calculates the sum of a field.
     *
     * @param fieldName the field name.
     * @return the sum.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @deprecated please use {@link #sum(String)} instead.
     */
    public double sumDouble(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.sumDouble(columnIndex);
    }

    /**
     * Calculates the sum of a field.
     *
     * @param fieldName the field name.
     * @return the sum.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @deprecated please use {@link #sum(String)} instead.
     */
    public double sumFloat(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.sumFloat(columnIndex);
    }

    // Average

    /**
     * Returns the average of a given field.
     *
     * @param fieldName the field to calculate average on. Only number fields are supported.
     * @return the average for the given field amongst objects in query results. This will be of type double for all
     * types of number fields. If no objects exist or they all have {@code null} as the value for the given field,
     * {@code 0} will be returned. When computing the average, objects with {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     */
    public double average(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
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
     * Calculates the average of a field.
     *
     * @param fieldName the field name.
     * @return the average.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @deprecated please use {@link #average(String)} instead.
     */
    public double averageInt(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.averageInt(columnIndex);
    }

    /**
     * Calculate the average of a field.
     *
     * @param fieldName the field name.
     * @return the average.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @deprecated please use {@link #average(String)} instead.
     */
    public double averageDouble(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.averageDouble(columnIndex);
    }

    /**
     * Calculates the average of a field.
     *
     * @param fieldName the field name.
     * @return the average.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @deprecated please use {@link #average(String)} instead.
     */
    public double averageFloat(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.averageFloat(columnIndex);
    }

    // Min

    /**
     * Finds the minimum value of a field.
     *
     * @param fieldName the field to look for a minimum on. Only number fields are supported.
     * @return if no objects exist or they all have {@code null} as the value for the given field, {@code null} will be
     * returned. Otherwise the minimum value is returned. When determining the minimum value, objects with {@code null}
     * values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
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
     * Finds the minimum value of a field.
     *
     * @param fieldName the field name.
     * @return the minimum value.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated please use {@link #min(String)} instead.
     */
    public long minimumInt(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.minimumInt(columnIndex);
    }

    /**
     * Finds the minimum value of a field.
     *
     * @param fieldName the field name.
     * @return the minimum value.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated please use {@link #min(String)} instead.
     */
    public double minimumDouble(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.minimumDouble(columnIndex);
    }

    /**
     * Finds the minimum value of a field.
     *
     * @param fieldName the field name.
     * @return the minimum value.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated please use {@link #min(String)} instead.
     */
    public float minimumFloat(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.minimumFloat(columnIndex);
    }

    /**
     * Finds the minimum value of a field.
     *
     * @param fieldName the field name
     * @return if no objects exist or they all have {@code null} as the value for the given date field, {@code null}
     * will be returned. Otherwise the minimum date is returned. When determining the minimum date, objects with
     * {@code null} values are ignored.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     */
    public Date minimumDate(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.minimumDate(columnIndex);
    }

    // Max

    /**
     * Finds the maximum value of a field.
     *
     * @param fieldName the field to look for a maximum on. Only number fields are supported.
     * @return  if no objects exist or they all have {@code null} as the value for the given field, {@code null} will be
     * returned. Otherwise the maximum value is returned. When determining the maximum value, objects with {@code null}
     * values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
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
     * Finds the maximum value of a field.
     *
     * @param fieldName the field name.
     * @return the maximum value.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated please use {@link #max(String)} instead.
     */
    public long maximumInt(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.maximumInt(columnIndex);
    }

    /**
     * Find the maximum value of a field.
     *
     * @param fieldName the field name.
     * @return the maximum value.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated please use {@link #max(String)} instead.
     */
    public double maximumDouble(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.maximumDouble(columnIndex);
    }

    /**
     * Finds the maximum value of a field.
     *
     * @param fieldName the field name.
     * @return the maximum value.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     * @throws java.lang.NullPointerException if no objects exist or they all have {@code null} as the value for the
     * given field.
     * @deprecated please use {@link #max(String)} instead.
     */
    public float maximumFloat(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.maximumFloat(columnIndex);
    }

    /**
     * Finds the maximum value of a field.
     *
     * @param fieldName the field name.
     * @return if no objects exist or they all have {@code null} as the value for the given date field, {@code null}
     * will be returned. Otherwise the maximum date is returned. When determining the maximum date, objects with
     * {@code null} values are ignored.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     */
    public Date maximumDate(String fieldName) {
        long columnIndex = schema.getFieldIndex(fieldName);
        return this.query.maximumDate(columnIndex);
    }

    /**
     * Counts the number of objects that fulfill the query conditions.
     *
     * @return the number of matching objects.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     */
    public long count() {
        return this.query.count();
    }

    RealmResults<E> distinctAsync(final long columnIndex) {
        checkQueryIsNotReused();
        final WeakReference<Handler> weakHandler = getWeakReferenceHandler();

        // handover the query (to be used by a worker thread)
        final long handoverQueryPointer = query.handoverQuery(realm.sharedGroupManager.getNativePointer());

        // save query arguments (for future update)
        argumentsHolder = new ArgumentsHolder(ArgumentsHolder.TYPE_DISTINCT);
        argumentsHolder.columnIndex = columnIndex;

        // we need to use the same configuration to open a background SharedGroup (i.e Realm)
        // to perform the query
        final RealmConfiguration realmConfiguration = realm.getConfiguration();

        // prepare an empty reference of the RealmResults, so we can return it immediately (promise)
        // then update it once the query completes in the background.
        RealmResults<E> realmResults = new RealmResults<E>(realm, query, clazz);
        final WeakReference<RealmResults<?>> weakRealmResults = new WeakReference<RealmResults<?>>(realmResults, realm.getReferenceQueue());
        realm.addAsyncRealmResults(weakRealmResults, this);

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

                        long handoverTableViewPointer = query.
                                findDistinctWithHandover(sharedGroup.getNativePointer(),
                                        sharedGroup.getNativeReplicationPointer(),
                                        handoverQueryPointer,
                                        columnIndex);

                        QueryUpdateTask.Result result = QueryUpdateTask.Result.newRealmResultsResponse();
                        result.updatedTableViews.put(weakRealmResults, handoverTableViewPointer);
                        result.versionID = sharedGroup.getVersion();
                        sendMessageToHandler(weakHandler, HandlerController.REALM_COMPLETED_ASYNC_QUERY, result);

                        return handoverTableViewPointer;
                    } catch (Exception e) {
                        RealmLog.e(e.getMessage());
                        sendMessageToHandler(weakHandler, HandlerController.REALM_ASYNC_BACKGROUND_EXCEPTION, new Error(e));

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
     * Finds all objects that fulfill the query conditions.
     *
     * @return a {@link io.realm.RealmResults} containing objects. If no objects match the condition, a list with zero
     * objects is returned.
     * @see io.realm.RealmResults
     */
    @SuppressWarnings("unchecked")
    public RealmResults<E> findAll() {
        checkQueryIsNotReused();
        if (isDynamicQuery()) {
            return (RealmResults<E>) RealmResults.createFromDynamicTableOrView(realm, query.findAll(), className);
        } else {
            return RealmResults.createFromTableOrView(realm, query.findAll(), clazz);
        }
    }

    /**
     * Finds all objects that fulfill the query conditions and sorted by specific field name.
     * This method is only available from a Looper thread.
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @see io.realm.RealmResults
     */
    public RealmResults<E> findAllAsync() {
        checkQueryIsNotReused();
        final WeakReference<Handler> weakHandler = getWeakReferenceHandler();

        // handover the query (to be used by a worker thread)
        final long handoverQueryPointer = query.handoverQuery(realm.sharedGroupManager.getNativePointer());

        // save query arguments (for future update)
        argumentsHolder = new ArgumentsHolder(ArgumentsHolder.TYPE_FIND_ALL);

        // we need to use the same configuration to open a background SharedGroup (i.e Realm)
        // to perform the query
        final RealmConfiguration realmConfiguration = realm.getConfiguration();

        // prepare an empty reference of the RealmResults, so we can return it immediately (promise)
        // then update it once the query completes in the background.
        RealmResults<E> realmResults = new RealmResults<E>(realm, query, clazz);
        final WeakReference<RealmResults<?>> weakRealmResults = new WeakReference<RealmResults<?>>(realmResults, realm.getReferenceQueue());
        realm.addAsyncRealmResults(weakRealmResults, this);

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
                        sendMessageToHandler(weakHandler, HandlerController.REALM_COMPLETED_ASYNC_QUERY, result);

                        return handoverTableViewPointer;

                    } catch (Exception e) {
                        RealmLog.e(e.getMessage());
                        sendMessageToHandler(weakHandler, HandlerController.REALM_ASYNC_BACKGROUND_EXCEPTION, new Error(e));

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
     * Finds all objects that fulfill the query conditions and sorted by specific field name.
     * <p>
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName the field name to sort by.
     * @param sortOrder how to sort the results.
     * @return a {@link io.realm.RealmResults} containing objects. If no objects match the condition, a list with zero
     * objects is returned.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    @SuppressWarnings("unchecked")
    public RealmResults<E> findAllSorted(String fieldName, Sort sortOrder) {
        checkQueryIsNotReused();
        TableView tableView = query.findAll();
        Long columnIndex = schema.getFieldIndex(fieldName);
        if (columnIndex == null || columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }
        tableView.sort(columnIndex, sortOrder);

        if (isDynamicQuery()) {
            return (RealmResults<E>) RealmResults.createFromDynamicTableOrView(realm, tableView, className);
        } else {
            return RealmResults.createFromTableOrView(realm, tableView, clazz);
        }
    }

    /**
     * Similar to {@link #findAllSorted(String, Sort)} but runs asynchronously on a worker thread
     * (Need a Realm opened from a looper thread to work).
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     *         {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public RealmResults<E> findAllSortedAsync(String fieldName, final Sort sortOrder) {
        checkQueryIsNotReused();
        final Long columnIndex = schema.getFieldIndex(fieldName);
        if (columnIndex == null || columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }

        // capture the query arguments for future retries & update
        argumentsHolder = new ArgumentsHolder(ArgumentsHolder.TYPE_FIND_ALL_SORTED);
        argumentsHolder.sortOrder = sortOrder;
        argumentsHolder.columnIndex = columnIndex;

        final WeakReference<Handler> weakHandler = getWeakReferenceHandler();

        // handover the query (to be used by a worker thread)
        final long handoverQueryPointer = query.handoverQuery(realm.sharedGroupManager.getNativePointer());

        // we need to use the same configuration to open a background SharedGroup to perform the query
        final RealmConfiguration realmConfiguration = realm.getConfiguration();

        RealmResults<E> realmResults = new RealmResults<E>(realm, query, clazz);
        final WeakReference<RealmResults<?>> weakRealmResults = new WeakReference<RealmResults<?>>(realmResults, realm.getReferenceQueue());
        realm.addAsyncRealmResults(weakRealmResults, this);

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
                        long handoverTableViewPointer = query.findAllSortedWithHandover(sharedGroup.getNativePointer(),
                                sharedGroup.getNativeReplicationPointer(), handoverQueryPointer, columnIndex, sortOrder);

                        QueryUpdateTask.Result result = QueryUpdateTask.Result.newRealmResultsResponse();
                        result.updatedTableViews.put(weakRealmResults, handoverTableViewPointer);
                        result.versionID = sharedGroup.getVersion();
                        sendMessageToHandler(weakHandler, HandlerController.REALM_COMPLETED_ASYNC_QUERY, result);

                        return handoverTableViewPointer;
                    } catch (Exception e) {
                        RealmLog.e(e.getMessage());
                        sendMessageToHandler(weakHandler, HandlerController.REALM_ASYNC_BACKGROUND_EXCEPTION, new Error(e));

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
     * Finds all objects that fulfill the query conditions and sorted by specific field name in ascending order.
     *
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName the field name to sort by.
     * @return a {@link io.realm.RealmResults} containing objects. If no objects match the condition, a list with zero
     * objects is returned.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public RealmResults<E> findAllSorted(String fieldName) {
        return findAllSorted(fieldName, Sort.ASCENDING);
    }

    /**
     * Similar to {@link #findAllSorted(String)} but runs asynchronously on a worker thread
     * This method is only available from a Looper thread.
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public RealmResults<E> findAllSortedAsync(String fieldName) {
        return findAllSortedAsync(fieldName, Sort.ASCENDING);
    }

    /**
     * Finds all objects that fulfill the query conditions and sorted by specific field names.
     * <p>
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldNames an array of field names to sort by.
     * @param sortOrders how to sort the field names.
     * @return a {@link io.realm.RealmResults} containing objects. If no objects match the condition, a list with zero 
     *         objects is returned.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    @SuppressWarnings("unchecked")
    public RealmResults<E> findAllSorted(String fieldNames[], Sort sortOrders[]) {
        checkSortParameters(fieldNames, sortOrders);

        if (fieldNames.length == 1 && sortOrders.length == 1) {
            return findAllSorted(fieldNames[0], sortOrders[0]);
        } else {
            TableView tableView = query.findAll();
            List<Long> columnIndices = new ArrayList<Long>();
            for (int i = 0; i < fieldNames.length; i++) {
                String fieldName = fieldNames[i];
                Long columnIndex = schema.getFieldIndex(fieldName);
                if (columnIndex == null || columnIndex < 0) {
                    throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
                }
                columnIndices.add(columnIndex);
            }
            tableView.sort(columnIndices, sortOrders);

            if (isDynamicQuery()) {
                return (RealmResults<E>) RealmResults.createFromDynamicTableOrView(realm, tableView, className);
            } else {
                return RealmResults.createFromTableOrView(realm, tableView, clazz);
            }
        }
    }

    private boolean isDynamicQuery() {
        return className != null;
    }

    /**
     * Similar to {@link #findAllSorted(String[], Sort[])} but runs asynchronously
     * from a worker thread.
     * This method is only available from a Looper thread.
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @see io.realm.RealmResults
     */
    public RealmResults<E> findAllSortedAsync(String fieldNames[], final Sort[] sortOrders) {
        checkQueryIsNotReused();
        checkSortParameters(fieldNames, sortOrders);

        if (fieldNames.length == 1 && sortOrders.length == 1) {
            return findAllSortedAsync(fieldNames[0], sortOrders[0]);

        } else {
            final WeakReference<Handler> weakHandler = getWeakReferenceHandler();

            // Handover the query (to be used by a worker thread)
            final long handoverQueryPointer = query.handoverQuery(realm.sharedGroupManager.getNativePointer());

            // We need to use the same configuration to open a background SharedGroup to perform the query
            final RealmConfiguration realmConfiguration = realm.getConfiguration();

            final long indices[] = new long[fieldNames.length];
            for (int i = 0; i < fieldNames.length; i++) {
                String fieldName = fieldNames[i];
                Long columnIndex = schema.getFieldIndex(fieldName);
                if (columnIndex == null || columnIndex < 0) {
                    throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
                }
                indices[i] = columnIndex;
            }

            // capture the query arguments for future retries & update
            argumentsHolder = new ArgumentsHolder(ArgumentsHolder.TYPE_FIND_ALL_MULTI_SORTED);
            argumentsHolder.sortOrders = sortOrders;
            argumentsHolder.columnIndices = indices;

            // prepare the promise result
            RealmResults<E> realmResults = new RealmResults<E>(realm, query, clazz);
            final WeakReference<RealmResults<?>> weakRealmResults = new WeakReference<RealmResults<?>>(realmResults, realm.getReferenceQueue());
            realm.addAsyncRealmResults(weakRealmResults, this);

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
                            long handoverTableViewPointer = query.findAllMultiSortedWithHandover(sharedGroup.getNativePointer(),
                                    sharedGroup.getNativeReplicationPointer(), handoverQueryPointer, indices, sortOrders);

                            QueryUpdateTask.Result result = QueryUpdateTask.Result.newRealmResultsResponse();
                            result.updatedTableViews.put(weakRealmResults, handoverTableViewPointer);
                            result.versionID = sharedGroup.getVersion();
                            sendMessageToHandler(weakHandler, HandlerController.REALM_COMPLETED_ASYNC_QUERY, result);

                            return handoverTableViewPointer;
                        } catch (Exception e) {
                            RealmLog.e(e.getMessage());
                            sendMessageToHandler(weakHandler, HandlerController.REALM_ASYNC_BACKGROUND_EXCEPTION, new Error(e));

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
     * Finds all objects that fulfill the query conditions and sorted by specific field names in ascending order.
     *
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName1 first field name
     * @param sortOrder1 sort order for first field
     * @param fieldName2 second field name
     * @param sortOrder2 sort order for second field
     * @return a {@link io.realm.RealmResults} containing objects. If no objects match the condition, a list with zero
     * objects is returned.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public RealmResults<E> findAllSorted(String fieldName1, Sort sortOrder1,
                                   String fieldName2, Sort sortOrder2) {
        return findAllSorted(new String[]{fieldName1, fieldName2}, new Sort[]{sortOrder1, sortOrder2});
    }

    /**
     * Similar to {@link #findAllSorted(String, Sort, String, Sort)} but runs asynchronously on a worker thread
     * This method is only available from a Looper thread.
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public RealmResults<E> findAllSortedAsync(String fieldName1, Sort sortOrder1,
                                              String fieldName2, Sort sortOrder2) {
        return findAllSortedAsync(new String[]{fieldName1, fieldName2}, new Sort[]{sortOrder1, sortOrder2});
    }

    /**
     * Finds all objects that fulfill the query conditions and sorted by specific field names in
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
     * @return a {@link io.realm.RealmResults} containing objects. If no objects match the condition, a list with zero
     * objects is returned.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public RealmResults<E> findAllSorted(String fieldName1, Sort sortOrder1,
                                   String fieldName2, Sort sortOrder2,
                                   String fieldName3, Sort sortOrder3) {
        return findAllSorted(new String[]{fieldName1, fieldName2, fieldName3},
                new Sort[]{sortOrder1, sortOrder2, sortOrder3});
    }

    /**
     * Similar to {@link #findAllSorted(String, Sort, String, Sort, String, Sort)} but
     * runs asynchronously on a worker thread.
     * This method is only available from a Looper thread.
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public RealmResults<E> findAllSortedAsync(String fieldName1, Sort sortOrder1,
                                              String fieldName2, Sort sortOrder2,
                                              String fieldName3, Sort sortOrder3) {
        return findAllSortedAsync(new String[]{fieldName1, fieldName2, fieldName3},
                new Sort[]{sortOrder1, sortOrder2, sortOrder3});
    }

    /**
     * Finds the first object that fulfills the query conditions.
     *
     * @return the object found or {@code null} if no object matches the query conditions.
     * @see io.realm.RealmObject
     */
    public E findFirst() {
        checkQueryIsNotReused();
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

    /**
     * Similar to {@link #findFirst()} but runs asynchronously on a worker thread
     * This method is only available from a Looper thread.
     *
     * @return immediately an empty {@link RealmObject}. Users need to register a listener
     * {@link io.realm.RealmObject#addChangeListener} to be notified when the query completes.
     */
    public E findFirstAsync() {
        checkQueryIsNotReused();
        final WeakReference<Handler> weakHandler = getWeakReferenceHandler();

        // handover the query (to be used by a worker thread)
        final long handoverQueryPointer = query.handoverQuery(realm.sharedGroupManager.getNativePointer());

        // save query arguments (for future update)
        argumentsHolder = new ArgumentsHolder(ArgumentsHolder.TYPE_FIND_FIRST);

        final RealmConfiguration realmConfiguration = realm.getConfiguration();

        // prepare an empty reference of the RealmObject, so we can return it immediately (promise)
        // then update it once the query complete in the background.
        final E result = realm.getConfiguration().getSchemaMediator().newInstance(clazz, realm.schema.getColumnInfo(clazz));
        final WeakReference<RealmObject> realmObjectWeakReference = new WeakReference<RealmObject>(result);
        realm.addAsyncRealmObject(realmObjectWeakReference, this);
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

                        long handoverTableViewPointer = query.findWithHandover(sharedGroup.getNativePointer(),
                                sharedGroup.getNativeReplicationPointer(), handoverQueryPointer);

                        QueryUpdateTask.Result result = QueryUpdateTask.Result.newRealmObjectResponse();
                        result.updatedRow.put(realmObjectWeakReference, handoverTableViewPointer);
                        result.versionID = sharedGroup.getVersion();
                        sendMessageToHandler(weakHandler, HandlerController.REALM_COMPLETED_ASYNC_FIND_FIRST, result);

                        return handoverTableViewPointer;

                    } catch (Exception e) {
                        RealmLog.e(e.getMessage());
                        // handler can't throw a checked exception need to wrap it into unchecked Exception
                        sendMessageToHandler(weakHandler, HandlerController.REALM_COMPLETED_ASYNC_FIND_FIRST, new Error(e));

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

    private void checkSortParameters(String fieldNames[], final Sort[] sortOrders) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("fieldNames cannot be 'null'.");
        } else if (sortOrders == null) {
            throw new IllegalArgumentException("sortOrders cannot be 'null'.");
        } else if (fieldNames.length == 0) {
            throw new IllegalArgumentException("At least one field name must be specified.");
        } else if (fieldNames.length != sortOrders.length) {
            throw new IllegalArgumentException(String.format("Number of field names (%d) and sort orders (%d) does not match.", fieldNames.length, sortOrders.length));
        }
    }

    private WeakReference<Handler> getWeakReferenceHandler() {
        if (realm.handler == null) {
            throw new IllegalStateException("Your Realm is opened from a thread without a Looper." +
                    " Async queries need a Handler to send results of your query");
        }
        return new WeakReference<Handler>(realm.handler); // use caller Realm's Looper
    }

    private void sendMessageToHandler(WeakReference<Handler> weakHandler, int what, Object obj) {
        Handler handler = weakHandler.get();
        if (handler != null && handler.getLooper().getThread().isAlive()) {
            handler.obtainMessage(what, obj).sendToTarget();
        }
    }

    // We need to prevent the user from using the query again (mostly for async)
    // Ex: if the first query fail with findFirstAsync, if the user reuse the same RealmQuery
    //     with findAllSorted, argumentsHolder of the first query will be overridden,
    //     which cause any retry to use the findAllSorted argumentsHolder.
    private void checkQueryIsNotReused() {
        if (argumentsHolder != null) {
            throw new IllegalStateException("This RealmQuery is already used by a find* query, please create a new query");
        }
    }

    public ArgumentsHolder getArgument() {
        return argumentsHolder;
    }

    /**
     * Exports & handovers the query to be used by a worker thread.
     *
     * @return the exported handover pointer for this RealmQuery.
     */
    long handoverQueryPointer() {
        return query.handoverQuery(realm.sharedGroupManager.getNativePointer());
    }
}
