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


import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

import io.realm.annotations.Beta;
import io.realm.annotations.Required;
import io.realm.internal.OsList;
import io.realm.internal.OsResults;
import io.realm.internal.PendingRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.SortDescriptor;
import io.realm.internal.SubscriptionAwareOsResults;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;
import io.realm.internal.Util;
import io.realm.internal.fields.FieldDescriptor;
import io.realm.internal.sync.SubscriptionAction;


/**
 * A RealmQuery encapsulates a query on a {@link io.realm.Realm} or a {@link io.realm.RealmResults} using the Builder
 * pattern. The query is executed using either {@link #findAll()} or {@link #findFirst()}.
 * <p>
 * The input to many of the query functions take a field name as String. Note that this is not type safe. If a
 * RealmObject class is refactored care has to be taken to not break any queries.
 * <p>
 * A {@link io.realm.Realm} is unordered, which means that there is no guarantee that querying a Realm will return the
 * objects in the order they where inserted. Use {@link #sort(String)} (String)} and similar methods if a specific order
 * is required.
 * <p>
 * A RealmQuery cannot be passed between different threads.
 *
 * @param <E> the class of the objects to be queried.
 * @see <a href="http://en.wikipedia.org/wiki/Builder_pattern">Builder pattern</a>
 * @see Realm#where(Class)
 * @see RealmResults#where()
 */
public class RealmQuery<E> {

    private final Table table;
    private final BaseRealm realm;
    private final TableQuery query;
    private final RealmObjectSchema schema;
    private Class<E> clazz;
    private String className;
    private final boolean forValues;
    private final OsList osList;
    private SortDescriptor sortDescriptor;
    private SortDescriptor distinctDescriptor;

    private static final String TYPE_MISMATCH = "Field '%s': type mismatch - %s expected.";
    private static final String EMPTY_VALUES = "Non-empty 'values' must be provided.";
    private static final String ASYNC_QUERY_WRONG_THREAD_MESSAGE = "Async query cannot be created on current thread.";

    /**
     * Creates a query for objects of a given class from a {@link Realm}.
     *
     * @param realm the realm to query within.
     * @param clazz the class to query.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */
    static <E extends RealmModel> RealmQuery<E> createQuery(Realm realm, Class<E> clazz) {
        return new RealmQuery<>(realm, clazz);
    }

    /**
     * Creates a query for dynamic objects of a given type from a {@link DynamicRealm}.
     *
     * @param realm the realm to query within.
     * @param className the type to query.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */
    static <E extends RealmModel> RealmQuery<E> createDynamicQuery(DynamicRealm realm, String className) {
        return new RealmQuery<>(realm, className);
    }

    /**
     * Creates a query from an existing {@link RealmResults}.
     *
     * @param queryResults an existing @{link io.realm.RealmResults} to query against.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */
    @SuppressWarnings("unchecked")
    static <E> RealmQuery<E> createQueryFromResult(RealmResults<E> queryResults) {
        //noinspection ConstantConditions
        return (queryResults.classSpec == null)
                ? new RealmQuery(queryResults, queryResults.className)
                : new RealmQuery<>(queryResults, queryResults.classSpec);
    }

    /**
     * Creates a query from an existing {@link RealmList}.
     *
     * @param list an existing @{link io.realm.RealmList} to query against.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */
    @SuppressWarnings("unchecked")
    static <E> RealmQuery<E> createQueryFromList(RealmList<E> list) {
        //noinspection ConstantConditions
        return (list.clazz == null)
                ? new RealmQuery(list.realm, list.getOsList(), list.className)
                : new RealmQuery(list.realm, list.getOsList(), list.clazz);
    }

    private static boolean isClassForRealmModel(Class<?> clazz) {
        return RealmModel.class.isAssignableFrom(clazz);
    }

    private RealmQuery(Realm realm, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.forValues = !isClassForRealmModel(clazz);
        if (forValues) {
            // TODO implement this
            this.schema = null;
            this.table = null;
            this.osList = null;
            this.query = null;
        } else {
            //noinspection unchecked
            this.schema = realm.getSchema().getSchemaForClass((Class<? extends RealmModel>) clazz);
            this.table = schema.getTable();
            this.osList = null;
            this.query = table.where();
        }
    }

    private RealmQuery(RealmResults<E> queryResults, Class<E> clazz) {
        this.realm = queryResults.realm;
        this.clazz = clazz;
        this.forValues = !isClassForRealmModel(clazz);
        if (forValues) {
            // TODO implement this
            this.schema = null;
            this.table = null;
            this.osList = null;
            this.query = null;
        } else {
            //noinspection unchecked
            this.schema = realm.getSchema().getSchemaForClass((Class<? extends RealmModel>) clazz);
            this.table = queryResults.getTable();
            this.osList = null;
            this.query = queryResults.getOsResults().where();
        }
    }

    private RealmQuery(BaseRealm realm, OsList osList, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.forValues = !isClassForRealmModel(clazz);
        if (forValues) {
            // TODO implement this
            this.schema = null;
            this.table = null;
            this.osList = null;
            this.query = null;
        } else {
            //noinspection unchecked
            this.schema = realm.getSchema().getSchemaForClass((Class<? extends RealmModel>) clazz);
            this.table = schema.getTable();
            this.osList = osList;
            this.query = osList.getQuery();
        }
    }

    private RealmQuery(BaseRealm realm, String className) {
        this.realm = realm;
        this.className = className;
        this.forValues = false;
        this.schema = realm.getSchema().getSchemaForClass(className);
        this.table = schema.getTable();
        this.query = table.where();
        this.osList = null;
    }

    private RealmQuery(RealmResults<DynamicRealmObject> queryResults, String className) {
        this.realm = queryResults.realm;
        this.className = className;
        this.forValues = false;
        this.schema = realm.getSchema().getSchemaForClass(className);
        this.table = schema.getTable();
        this.query = queryResults.getOsResults().where();
        this.osList = null;
    }

    private RealmQuery(BaseRealm realm, OsList osList, String className) {
        this.realm = realm;
        this.className = className;
        this.forValues = false;
        this.schema = realm.getSchema().getSchemaForClass(className);
        this.table = schema.getTable();
        this.query = osList.getQuery();
        this.osList = osList;
    }

    /**
     * Checks if {@link io.realm.RealmQuery} is still valid to use i.e., the {@link io.realm.Realm} instance hasn't been
     * closed and any parent {@link io.realm.RealmResults} is still valid.
     *
     * @return {@code true} if still valid to use, {@code false} otherwise.
     */
    public boolean isValid() {
        if (realm == null || realm.isClosed() /* this includes thread checking */) {
            return false;
        }

        if (osList != null) {
            return osList.isValid();
        }
        return table != null && table.isValid();
    }

    /**
     * Tests if a field is {@code null}. Only works for nullable fields.
     * <p>
     * For link queries, if any part of the link path is {@code null} the whole path is considered to be {@code null}
     * e.g., {@code isNull("linkField.stringField")} will be considered to be {@code null} if either {@code linkField} or
     * {@code linkField.stringField} is {@code null}.
     *
     * @param fieldName the field name.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field is not nullable.
     * @see Required for further infomation.
     */
    public RealmQuery<E> isNull(String fieldName) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName);

        // Checks that fieldName has the correct type is done in C++.
        this.query.isNull(fd.getColumnIndices(), fd.getNativeTablePointers());
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName);

        // Checks that fieldName has the correct type is done in C++.
        this.query.isNotNull(fd.getColumnIndices(), fd.getNativeTablePointers());
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
    public RealmQuery<E> equalTo(String fieldName, @Nullable String value) {
        return this.equalTo(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @param casing how to handle casing. Setting this to {@link Case#INSENSITIVE} only works for Latin-1 characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> equalTo(String fieldName, @Nullable String value, Case casing) {
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value, casing);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, @Nullable String value, Case casing) {
        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.equalTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value, casing);
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
    public RealmQuery<E> equalTo(String fieldName, @Nullable Byte value) {
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, @Nullable Byte value) {
        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        if (value == null) {
            this.query.isNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.equalTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> equalTo(String fieldName, @Nullable byte[] value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.BINARY);
        if (value == null) {
            this.query.isNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.equalTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> equalTo(String fieldName, @Nullable Short value) {
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, @Nullable Short value) {
        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        if (value == null) {
            this.query.isNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.equalTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> equalTo(String fieldName, @Nullable Integer value) {
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, @Nullable Integer value) {
        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        if (value == null) {
            this.query.isNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.equalTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> equalTo(String fieldName, @Nullable Long value) {
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, @Nullable Long value) {
        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        if (value == null) {
            this.query.isNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.equalTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> equalTo(String fieldName, @Nullable Double value) {
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, @Nullable Double value) {
        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        if (value == null) {
            this.query.isNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.equalTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> equalTo(String fieldName, @Nullable Float value) {
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, @Nullable Float value) {
        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        if (value == null) {
            this.query.isNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.equalTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> equalTo(String fieldName, @Nullable Boolean value) {
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, @Nullable Boolean value) {
        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.BOOLEAN);
        if (value == null) {
            this.query.isNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.equalTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> equalTo(String fieldName, @Nullable Date value) {
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, @Nullable Date value) {
        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.equalTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
        return this;
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with. If {@code null} or the empty array is provided the query will never
     *               match any results.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a String field.
     */
    public RealmQuery<E> in(String fieldName, @Nullable String[] values) {
        return in(fieldName, values, Case.SENSITIVE);
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with. If {@code null} or the empty array is provided the query will never
     *               match any results.
     * @param casing how casing is handled. {@link Case#INSENSITIVE} works only for the Latin-1 characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a String field.
     */
    public RealmQuery<E> in(String fieldName, @Nullable String[] values, Case casing) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            alwaysFalse();
            return this;
        }
        beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0], casing);
        for (int i = 1; i < values.length; i++) {
            orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i], casing);
        }
        return endGroupWithoutThreadValidation();
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with. If {@code null} or the empty array is provided the query will never
     *               match any results.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Byte field.
     */
    public RealmQuery<E> in(String fieldName, @Nullable Byte[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            alwaysFalse();
            return this;
        } else {
            beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
            for (int i = 1; i < values.length; i++) {
                orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
            }
            return endGroupWithoutThreadValidation();
        }
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with. If {@code null} or the empty array is provided the query will never
     *               match any results.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Short field.
     */
    public RealmQuery<E> in(String fieldName, @Nullable Short[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            alwaysFalse();
            return this;
        } else {
            beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
            for (int i = 1; i < values.length; i++) {
                orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
            }
            return endGroupWithoutThreadValidation();
        }
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with. If {@code null} or the empty array is provided the query will never
     *               match any results.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Integer field.
     */
    public RealmQuery<E> in(String fieldName, @Nullable Integer[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            alwaysFalse();
            return this;
        } else {
            beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
            for (int i = 1; i < values.length; i++) {
                orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
            }
            return endGroupWithoutThreadValidation();
        }
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with. If {@code null} or the empty array is provided the query will never
     *               match any results.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Long field.
     * empty.
     */
    public RealmQuery<E> in(String fieldName, @Nullable Long[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            alwaysFalse();
            return this;
        } else {
            beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
            for (int i = 1; i < values.length; i++) {
                orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
            }
            return endGroupWithoutThreadValidation();
        }
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with. If {@code null} or the empty array is provided the query will never
     *               match any results.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Double field.
     * empty.
     */
    public RealmQuery<E> in(String fieldName, @Nullable Double[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            alwaysFalse();
            return this;
        } else {
            beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
            for (int i = 1; i < values.length; i++) {
                orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
            }
            return endGroupWithoutThreadValidation();
        }
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with. If {@code null} or the empty array is provided the query will never
     *               match any results.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Float field.
     */
    public RealmQuery<E> in(String fieldName, @Nullable Float[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            alwaysFalse();
            return this;
        } else {
            beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
            for (int i = 1; i < values.length; i++) {
                orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
            }
            return endGroupWithoutThreadValidation();
        }
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with. If {@code null} or the empty array is provided the query will never
     *               match any results.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Boolean.
     * or empty.
     */
    public RealmQuery<E> in(String fieldName, @Nullable Boolean[] values) {
        realm.checkIfValid();

        //noinspection ConstantConditions
        if (values == null || values.length == 0) {
            alwaysFalse();
            return this;
        } else {
            beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
            for (int i = 1; i < values.length; i++) {
                orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
            }
            return endGroupWithoutThreadValidation();
        }
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with. If {@code null} or the empty array is provided the query will never
     *               match any results.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Date field.
     */
    public RealmQuery<E> in(String fieldName, @Nullable Date[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            alwaysFalse();
            return this;
        } else {
            beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
            for (int i = 1; i < values.length; i++) {
                orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
            }
            return endGroupWithoutThreadValidation();
        }
    }

    /**
     * Not-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, @Nullable String value) {
        return this.notEqualTo(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Not-equal-to comparison.
     *
     * @param fieldName the field to compare.
     * @param value the value to compare with.
     * @param casing how casing is handled. {@link Case#INSENSITIVE} works only for the Latin-1 characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, @Nullable String value, Case casing) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        if (fd.length() > 1 && !casing.getValue()) {
            throw new IllegalArgumentException("Link queries cannot be case insensitive - coming soon.");
        }
        this.query.notEqualTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value, casing);
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
    public RealmQuery<E> notEqualTo(String fieldName, @Nullable Byte value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        if (value == null) {
            this.query.isNotNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.notEqualTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> notEqualTo(String fieldName, @Nullable byte[] value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.BINARY);
        if (value == null) {
            this.query.isNotNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.notEqualTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> notEqualTo(String fieldName, @Nullable Short value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        if (value == null) {
            this.query.isNotNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.notEqualTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> notEqualTo(String fieldName, @Nullable Integer value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        if (value == null) {
            this.query.isNotNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.notEqualTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> notEqualTo(String fieldName, @Nullable Long value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        if (value == null) {
            this.query.isNotNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.notEqualTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> notEqualTo(String fieldName, @Nullable Double value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        if (value == null) {
            this.query.isNotNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.notEqualTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> notEqualTo(String fieldName, @Nullable Float value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        if (value == null) {
            this.query.isNotNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.notEqualTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> notEqualTo(String fieldName, @Nullable Boolean value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.BOOLEAN);
        if (value == null) {
            this.query.isNotNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.equalTo(fd.getColumnIndices(), fd.getNativeTablePointers(), !value);
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
    public RealmQuery<E> notEqualTo(String fieldName, @Nullable Date value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        if (value == null) {
            this.query.isNotNull(fd.getColumnIndices(), fd.getNativeTablePointers());
        } else {
            this.query.notEqualTo(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
        }
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
    public RealmQuery<E> greaterThan(String fieldName, int value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.greaterThan(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.greaterThan(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        this.query.greaterThan(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        this.query.greaterThan(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.greaterThan(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.greaterThanOrEqual(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.greaterThanOrEqual(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        this.query.greaterThanOrEqual(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        this.query.greaterThanOrEqual(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.greaterThanOrEqual(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> lessThan(String fieldName, int value) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.lessThan(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.lessThan(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        this.query.lessThan(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        this.query.lessThan(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.lessThan(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.lessThanOrEqual(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.lessThanOrEqual(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        this.query.lessThanOrEqual(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        this.query.lessThanOrEqual(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.lessThanOrEqual(fd.getColumnIndices(), fd.getNativeTablePointers(), value);
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
    public RealmQuery<E> between(String fieldName, int from, int to) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.between(fd.getColumnIndices(), from, to);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
        this.query.between(fd.getColumnIndices(), from, to);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
        this.query.between(fd.getColumnIndices(), from, to);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
        this.query.between(fd.getColumnIndices(), from, to);
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
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.between(fd.getColumnIndices(), from, to);
        return this;
    }


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
     * @param casing how to handle casing. Setting this to {@link Case#INSENSITIVE} only works for Latin-1 characters.
     * @return The query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> contains(String fieldName, String value, Case casing) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.contains(fd.getColumnIndices(), fd.getNativeTablePointers(), value, casing);
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
     * @param casing how to handle casing. Setting this to {@link Case#INSENSITIVE} only works for Latin-1 characters.
     * @return the query object
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> beginsWith(String fieldName, String value, Case casing) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.beginsWith(fd.getColumnIndices(), fd.getNativeTablePointers(), value, casing);
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
     * @param casing how to handle casing. Setting this to {@link Case#INSENSITIVE} only works for Latin-1 characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> endsWith(String fieldName, String value, Case casing) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.endsWith(fd.getColumnIndices(), fd.getNativeTablePointers(), value, casing);
        return this;
    }

    /**
     * Condition that the value of field matches with the specified substring, with wildcards:
     * <ul>
     * <li>'*' matches [0, n] unicode chars</li>
     * <li>'?' matches a single unicode char.</li>
     * </ul>
     *
     * @param fieldName the field to compare.
     * @param value the wildcard string.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> like(String fieldName, String value) {
        return like(fieldName, value, Case.SENSITIVE);
    }

    /**
     * Condition that the value of field matches with the specified substring, with wildcards:
     * <ul>
     * <li>'*' matches [0, n] unicode chars</li>
     * <li>'?' matches a single unicode char.</li>
     * </ul>
     *
     * @param fieldName the field to compare.
     * @param value the wildcard string.
     * @param casing how to handle casing. Setting this to {@link Case#INSENSITIVE} only works for Latin-1 characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> like(String fieldName, String value, Case casing) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.like(fd.getColumnIndices(), fd.getNativeTablePointers(), value, casing);
        return this;
    }


    /**
     * Begin grouping of conditions ("left parenthesis"). A group must be closed with a call to {@code endGroup()}.
     *
     * @return the query object.
     * @see #endGroup()
     */
    public RealmQuery<E> beginGroup() {
        realm.checkIfValid();

        return beginGroupWithoutThreadValidation();
    }

    private RealmQuery<E> beginGroupWithoutThreadValidation() {
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
        realm.checkIfValid();

        return endGroupWithoutThreadValidation();
    }

    private RealmQuery<E> endGroupWithoutThreadValidation() {
        this.query.endGroup();
        return this;
    }

    /**
     * Logical-or two conditions.
     *
     * @return the query object.
     */
    public RealmQuery<E> or() {
        realm.checkIfValid();

        return orWithoutThreadValidation();
    }

    private RealmQuery<E> orWithoutThreadValidation() {
        this.query.or();
        return this;
    }

    /**
     * Logical-and two conditions
     * Realm automatically applies logical-and between all query statements, so this is intended only as a mean to increase readability.
     *
     * @return the query object
     */
    public RealmQuery<E> and() {
        realm.checkIfValid();
        return this;
    }

    /**
     * Negate condition.
     *
     * @return the query object.
     */
    public RealmQuery<E> not() {
        realm.checkIfValid();

        this.query.not();
        return this;
    }

    /**
     * Condition that finds values that are considered "empty" i.e., an empty list, the 0-length string or byte array.
     *
     * @param fieldName the field to compare.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field name isn't valid or its type isn't either a RealmList,
     * String or byte array.
     */
    public RealmQuery<E> isEmpty(String fieldName) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.STRING, RealmFieldType.BINARY, RealmFieldType.LIST, RealmFieldType.LINKING_OBJECTS);
        this.query.isEmpty(fd.getColumnIndices(), fd.getNativeTablePointers());

        return this;
    }

    /**
     * Condition that finds values that are considered "Not-empty" i.e., a list, a string or a byte array with not-empty values.
     *
     * @param fieldName the field to compare.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field name isn't valid or its type isn't either a RealmList,
     * String or byte array.
     */
    public RealmQuery<E> isNotEmpty(String fieldName) {
        realm.checkIfValid();

        FieldDescriptor fd = schema.getColumnIndices(fieldName, RealmFieldType.STRING, RealmFieldType.BINARY, RealmFieldType.LIST, RealmFieldType.LINKING_OBJECTS);
        this.query.isNotEmpty(fd.getColumnIndices(), fd.getNativeTablePointers());

        return this;
    }

    /**
     * Calculates the sum of a given field.
     *
     * @param fieldName the field to sum. Only number fields are supported.
     * @return the sum of fields of the matching objects. If no objects exist or they all have {@code null} as the value
     * for the given field, {@code 0} will be returned. When computing the sum, objects with {@code null} values
     * are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     */
    public Number sum(String fieldName) {
        realm.checkIfValid();

        long columnIndex = schema.getAndCheckFieldIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return query.sumInt(columnIndex);
            case FLOAT:
                return query.sumFloat(columnIndex);
            case DOUBLE:
                return query.sumDouble(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(Locale.US,
                        TYPE_MISMATCH, fieldName, "int, float or double"));
        }
    }

    /**
     * Returns the average of a given field.
     * Does not support dotted field notation.
     *
     * @param fieldName the field to calculate average on. Only number fields are supported.
     * @return the average for the given field amongst objects in query results. This will be of type double for all
     * types of number fields. If no objects exist or they all have {@code null} as the value for the given field,
     * {@code 0} will be returned. When computing the average, objects with {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     */
    public double average(String fieldName) {
        realm.checkIfValid();

        long columnIndex = schema.getAndCheckFieldIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return query.averageInt(columnIndex);
            case DOUBLE:
                return query.averageDouble(columnIndex);
            case FLOAT:
                return query.averageFloat(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(Locale.US,
                        TYPE_MISMATCH, fieldName, "int, float or double"));
        }
    }

    /**
     * Finds the minimum value of a field.
     *
     * @param fieldName the field to look for a minimum on. Only number fields are supported.
     * @return if no objects exist or they all have {@code null} as the value for the given field, {@code null} will be
     * returned. Otherwise the minimum value is returned. When determining the minimum value, objects with {@code null}
     * values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     */
    @Nullable
    public Number min(String fieldName) {
        realm.checkIfValid();

        long columnIndex = schema.getAndCheckFieldIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return this.query.minimumInt(columnIndex);
            case FLOAT:
                return this.query.minimumFloat(columnIndex);
            case DOUBLE:
                return this.query.minimumDouble(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(Locale.US,
                        TYPE_MISMATCH, fieldName, "int, float or double"));
        }
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
    @Nullable
    public Date minimumDate(String fieldName) {
        realm.checkIfValid();

        long columnIndex = schema.getAndCheckFieldIndex(fieldName);
        return this.query.minimumDate(columnIndex);
    }

    /**
     * Finds the maximum value of a field.
     *
     * @param fieldName the field to look for a maximum on. Only number fields are supported.
     * @return if no objects exist or they all have {@code null} as the value for the given field, {@code null} will be
     * returned. Otherwise the maximum value is returned. When determining the maximum value, objects with {@code null}
     * values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     */
    @Nullable
    public Number max(String fieldName) {
        realm.checkIfValid();

        long columnIndex = schema.getAndCheckFieldIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return this.query.maximumInt(columnIndex);
            case FLOAT:
                return this.query.maximumFloat(columnIndex);
            case DOUBLE:
                return this.query.maximumDouble(columnIndex);
            default:
                throw new IllegalArgumentException(String.format(Locale.US,
                        TYPE_MISMATCH, fieldName, "int, float or double"));
        }
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
    @Nullable
    public Date maximumDate(String fieldName) {
        realm.checkIfValid();

        long columnIndex = schema.getAndCheckFieldIndex(fieldName);
        return this.query.maximumDate(columnIndex);
    }

    /**
     * Counts the number of objects that fulfill the query conditions.
     *
     * @return the number of matching objects.
     * @throws java.lang.UnsupportedOperationException if the query is not valid ("syntax error").
     */
    public long count() {
        realm.checkIfValid();
        // The fastest way of doing `count()` is going through `TableQuery.count()`. Unfortunately
        // doing this does not correctly apply all side effects of queries (like subscriptions). Also
        // some queries constructs, like doing distinct is not easily supported this way.
        // In order to get the best of both worlds we thus need to create a Java RealmResults object
        // and then directly access the `Results` class from Object Store.
        return lazyFindAll().size();
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
        realm.checkIfValid();
        return createRealmResults(query, sortDescriptor, distinctDescriptor, true, SubscriptionAction.NO_SUBSCRIPTION);
    }

    /**
     * The same as {@link #findAll()} expect the RealmResult is not forcefully evaluated. This
     * means this method will return a more "pure" wrapper around the Object Store Results class.
     *
     * This can be useful for internal usage where we still want to take advantage of optimizations
     * and additional functionality provided by Object Store, but do not wish to trigger the query
     * unless needed.
     */
    private OsResults lazyFindAll() {
        realm.checkIfValid();
        return createRealmResults(
                query,
                sortDescriptor,
                distinctDescriptor,
                false,
                SubscriptionAction.NO_SUBSCRIPTION).osResults;
    }

    /**
     * Finds all objects that fulfill the query conditions. This method is only available from a Looper thread.
     * <p>
     * If the Realm is a Query-based synchronized Realms, this method will also create an anonymous subscription
     * that will download all server data matching the query.
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @see io.realm.RealmResults
     */
    public RealmResults<E> findAllAsync() {
        realm.checkIfValid();

        realm.sharedRealm.capabilities.checkCanDeliverNotification(ASYNC_QUERY_WRONG_THREAD_MESSAGE);
        SubscriptionAction subscriptionAction;
        
        // Don't create subscriptions for list queries as they are always part of an object covered by another query.
        if (realm.sharedRealm.isPartial() && osList == null) {
            subscriptionAction = SubscriptionAction.ANONYMOUS_SUBSCRIPTION;
        }  else {
            subscriptionAction = SubscriptionAction.NO_SUBSCRIPTION;
        }
        return createRealmResults(query, sortDescriptor, distinctDescriptor, false, subscriptionAction);
    }

    /**
     * Finds all objects that fulfill the query condition(s). This method is only available from a Looper thread.
     * <p>
     * This method is only available on query-based synchronized Realms and will also create a named subscription
     * that will synchronize all server data matching the query. Named subscriptions can be removed again by
     * calling {@code Realm.unsubscribe(subscriptionName}.
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @see io.realm.RealmResults
     * @throws IllegalStateException If the Realm is a not a query-based synchronized Realm.
     */
    public RealmResults<E> findAllAsync(String subscriptionName) {
        realm.checkIfValid();
        realm.checkIfPartialRealm();
        if (osList != null) {
            throw new IllegalStateException("Cannot create subscriptions for queries based on a 'RealmList'");
        }
        if (Util.isEmptyString(subscriptionName)) {
            throw new IllegalArgumentException("Non-empty 'subscriptionName' required.");
        }

        realm.sharedRealm.capabilities.checkCanDeliverNotification(ASYNC_QUERY_WRONG_THREAD_MESSAGE);
        return createRealmResults(query, sortDescriptor, distinctDescriptor, false, SubscriptionAction.create(subscriptionName));
    }

    /**
     * Sorts the query result by the specific field name in ascending order.
     * <p>
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName the field name to sort by.
     * @throws IllegalArgumentException if the field name does not exist.
     * @throws IllegalStateException if a sorting order was already defined.
     */
    public RealmQuery<E> sort(String fieldName) {
        realm.checkIfValid();
        return sort(fieldName, Sort.ASCENDING);
    }

    /**
     * Sorts the query result by the specified field name and order.
     * <p>
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName the field name to sort by.
     * @param sortOrder how to sort the results.
     * @throws IllegalArgumentException if the field name does not exist.
     * @throws IllegalStateException if a sorting order was already defined.
     */
    public RealmQuery<E> sort(String fieldName, Sort sortOrder) {
        realm.checkIfValid();
        return sort(new String[] { fieldName}, new Sort[] { sortOrder});
    }

    /**
     * Sorts the query result by the specific field names in the provided orders. {@code fieldName2} is only used
     * in case of equal values in {@code fieldName1}.
     * <p>
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName1 first field name
     * @param sortOrder1 sort order for first field
     * @param fieldName2 second field name
     * @param sortOrder2 sort order for second field
     * @throws IllegalArgumentException if the field name does not exist.
     * @throws IllegalStateException if a sorting order was already defined.
     */
    public RealmQuery<E> sort(String fieldName1, Sort sortOrder1, String fieldName2, Sort sortOrder2) {
        realm.checkIfValid();
        return sort(new String[] { fieldName1, fieldName2 }, new Sort[] { sortOrder1, sortOrder2 });
    }

    /**
     * Sorts the query result by the specific field names in the provided orders. Later fields will only be used
     * if the previous field values are equal.
     * <p>
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldNames an array of field names to sort by.
     * @param sortOrders how to sort the field names.
     * @throws IllegalArgumentException if the field name does not exist.
     * @throws IllegalStateException if a sorting order was already defined.
     */
    public RealmQuery<E> sort(String[] fieldNames, Sort[] sortOrders) {
        realm.checkIfValid();
        if (sortDescriptor != null) {
            throw new IllegalStateException("A sorting order was already defined.");
        }
        sortDescriptor = SortDescriptor.getInstanceForSort(getSchemaConnector(), query.getTable(), fieldNames, sortOrders);
        return this;
    }

    /**
     * Selects a distinct set of objects of a specific class. If the result is sorted, the first object will be
     * returned in case of multiple occurrences, otherwise it is undefined which object is returned.
     * <p>
     * Adding {@link io.realm.annotations.Index} to the corresponding field will make this operation much faster.
     *
     * @param fieldName the field name.
     * @throws IllegalArgumentException if a field is {@code null}, does not exist, is an unsupported type, or points
     * to linked fields.
     * @throws IllegalStateException if distinct field names were already defined.
     */
    @Beta
    public RealmQuery<E> distinct(String fieldName) {
        return distinct(fieldName, new String[]{});
    }

    /**
     * Selects a distinct set of objects of a specific class. When multiple distinct fields are
     * given, all unique combinations of values in the fields will be returned. In case of multiple
     * matches, it is undefined which object is returned. Unless the result is sorted, then the
     * first object will be returned.
     *
     * @param firstFieldName first field name to use when finding distinct objects.
     * @param remainingFieldNames remaining field names when determining all unique combinations of field values.
     * @throws IllegalArgumentException if field names is empty or {@code null}, does not exist,
     * is an unsupported type, or points to a linked field.
     * @throws IllegalStateException if distinct field names were already defined.
     */
    @Beta
    public RealmQuery<E> distinct(String firstFieldName, String... remainingFieldNames) {
        realm.checkIfValid();
        if (distinctDescriptor != null) {
            throw new IllegalStateException("Distinct fields have already been defined.");
        }
        if (remainingFieldNames.length == 0) {
            distinctDescriptor = SortDescriptor.getInstanceForDistinct(getSchemaConnector(), table, firstFieldName);
        } else {
            String[] fieldNames = new String[1 + remainingFieldNames.length];
            fieldNames[0] = firstFieldName;
            System.arraycopy(remainingFieldNames, 0, fieldNames, 1, remainingFieldNames.length);
            distinctDescriptor = SortDescriptor.getInstanceForDistinct(getSchemaConnector(), table, fieldNames);
        }
        return this;
    }

    /**
     * This predicate will always match.
     */
    public RealmQuery<E> alwaysTrue() {
        realm.checkIfValid();
        query.alwaysTrue();
        return this;
    }

    /**
     * This predicate will never match, resulting in the query always returning 0 results.
     */
    public RealmQuery<E> alwaysFalse() {
        realm.checkIfValid();
        query.alwaysFalse();
        return this;
    }

    /**
     * Returns the {@link Realm} instance to which this query belongs.
     * <p>
     * Calling {@link Realm#close()} on the returned instance is discouraged as it is the same as
     * calling it on the original Realm instance which may cause the Realm to fully close invalidating the
     * query.
     *
     * @return {@link Realm} instance this query belongs to.
     * @throws IllegalStateException if the Realm is an instance of {@link DynamicRealm} or the
     * {@link Realm} was already closed.
     */
    public Realm getRealm() {
        if (realm == null) {
            return null;
        }
        realm.checkIfValid();
        if (!(realm instanceof Realm)) {
            throw new IllegalStateException("This method is only available for typed Realms");
        }
        return (Realm) realm;
    }

    private boolean isDynamicQuery() {
        return className != null;
    }

    /**
     * Finds the first object that fulfills the query conditions.
     *
     * @return the object found or {@code null} if no object matches the query conditions.
     * @see io.realm.RealmObject
     */
    @Nullable
    public E findFirst() {
        realm.checkIfValid();

        if (forValues) {
            // TODO implement this;
            return null;
        }

        long tableRowIndex = getSourceRowIndexForFirstObject();
        //noinspection unchecked
        return (tableRowIndex < 0) ? null : (E) realm.get((Class<? extends RealmModel>) clazz, className, tableRowIndex);
    }

    /**
     * Similar to {@link #findFirst()} but runs asynchronously on a worker thread. An listener should be registered to
     * the returned {@link RealmObject} to get the notification when query completes. The registered listener will also
     * be triggered if there are changes made to the queried {@link RealmObject}. If the {@link RealmObject} is deleted,
     * the listener will be called one last time and then stop. The query will not be re-run.
     *
     * @return immediately an empty {@link RealmObject} with {@code isLoaded() == false}. Trying to access any field on
     * the returned object before it is loaded will throw an {@code IllegalStateException}.
     * @throws IllegalStateException if this is called on a non-looper thread.
     */
    public E findFirstAsync() {
        realm.checkIfValid();

        if (forValues) {
            throw new UnsupportedOperationException("findFirstAsync() available only when type parameter 'E' is implementing RealmModel.");
        }

        realm.sharedRealm.capabilities.checkCanDeliverNotification(ASYNC_QUERY_WRONG_THREAD_MESSAGE);
        Row row;
        if (realm.isInTransaction()) {
            // It is not possible to create async query inside a transaction. So immediately query the first object.
            // See OS Results::prepare_async()
            row = OsResults.createFromQuery(realm.sharedRealm, query).firstUncheckedRow();
        } else {
            // prepares an empty reference of the RealmObject which is backed by a pending query,
            // then update it once the query complete in the background.

            // TODO: The performance by the pending query will be a little bit worse than directly calling core's
            // Query.find(). The overhead comes with core needs to add all the row indices to the vector. However this
            // can be optimized by adding support of limit in OS's Results which is supported by core already.
            row = new PendingRow(realm.sharedRealm, query, sortDescriptor, isDynamicQuery());
        }
        final E result;
        if (isDynamicQuery()) {
            //noinspection unchecked
            result = (E) new DynamicRealmObject(realm, row);
        } else {
            //noinspection unchecked
            final Class<? extends RealmModel> modelClass = (Class<? extends RealmModel>) clazz;
            //noinspection unchecked
            result = (E) realm.getConfiguration().getSchemaMediator().newInstance(
                    modelClass, realm, row, realm.getSchema().getColumnInfo(modelClass),
                    false, Collections.<String>emptyList());
        }

        if (row instanceof PendingRow) {
            final RealmObjectProxy proxy = (RealmObjectProxy) result;
            ((PendingRow) row).setFrontEnd(proxy.realmGet$proxyState());
        }

        return result;
    }


    private RealmResults<E> createRealmResults(TableQuery query,
                                               @Nullable SortDescriptor sortDescriptor,
                                               @Nullable SortDescriptor distinctDescriptor,
                                               boolean loadResults,
                                               SubscriptionAction subscriptionAction) {
        RealmResults<E> results;
        OsResults osResults;
        if (subscriptionAction.shouldCreateSubscriptions()) {
            osResults = SubscriptionAwareOsResults.createFromQuery(realm.sharedRealm, query, sortDescriptor, distinctDescriptor, subscriptionAction.getName());
        } else {
            osResults = OsResults.createFromQuery(realm.sharedRealm, query, sortDescriptor, distinctDescriptor);
        }

        if (isDynamicQuery()) {
            results = new RealmResults<>(realm, osResults, className);
        } else {
            results = new RealmResults<>(realm, osResults, clazz);
        }
        if (loadResults) {
            results.load();
        }

        return results;
    }

    private long getSourceRowIndexForFirstObject() {
        if (sortDescriptor != null || distinctDescriptor != null) {
            RealmObjectProxy obj = (RealmObjectProxy) findAll().first(null);
            if (obj != null) {
                return obj.realmGet$proxyState().getRow$realm().getIndex();
            } else {
                return -1;
            }
        } else {
            return this.query.find();
        }
    }

    private SchemaConnector getSchemaConnector() {
        return new SchemaConnector(realm.getSchema());
    }
}
