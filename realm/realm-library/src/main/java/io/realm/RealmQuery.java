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

import io.realm.annotations.Required;
import io.realm.internal.Collection;
import io.realm.internal.LinkView;
import io.realm.internal.PendingRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.SortDescriptor;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;


/**
 * A RealmQuery encapsulates a query on a {@link io.realm.Realm} or a {@link io.realm.RealmResults} using the Builder
 * pattern. The query is executed using either {@link #findAll()} or {@link #findFirst()}.
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
public class RealmQuery<E extends RealmModel> {

    private final Table table;
    private final BaseRealm realm;
    private final TableQuery query;
    private final RealmObjectSchema schema;
    private Class<E> clazz;
    private String className;
    private LinkView linkView;
    private static final String TYPE_MISMATCH = "Field '%s': type mismatch - %s expected.";
    private static final String EMPTY_VALUES = "Non-empty 'values' must be provided.";
    static final String ASYNC_QUERY_WRONG_THREAD_MESSAGE = "Async query cannot be created on current thread.";

    /**
     * Creates a query for objects of a given class from a {@link Realm}.
     *
     * @param realm the realm to query within.
     * @param clazz the class to query.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */
    public static <E extends RealmModel> RealmQuery<E> createQuery(Realm realm, Class<E> clazz) {
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
    public static <E extends RealmModel> RealmQuery<E> createDynamicQuery(DynamicRealm realm, String className) {
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
    public static <E extends RealmModel> RealmQuery<E> createQueryFromResult(RealmResults<E> queryResults) {
        if (queryResults.classSpec != null) {
            return new RealmQuery<>(queryResults, queryResults.classSpec);
        } else {
            return new RealmQuery(queryResults, queryResults.className);
        }
    }

    /**
     * Creates a query from an existing {@link RealmList}.
     *
     * @param list an existing @{link io.realm.RealmList} to query against.
     * @return {@link RealmQuery} object. After building the query call one of the {@code find*} methods
     * to run it.
     */
    @SuppressWarnings("unchecked")
    public static <E extends RealmModel> RealmQuery<E> createQueryFromList(RealmList<E> list) {
        if (list.clazz != null) {
            return new RealmQuery(list.realm, list.view, list.clazz);
        } else {
            return new RealmQuery(list.realm, list.view, list.className);
        }
    }

    private RealmQuery(Realm realm, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.schema = realm.getSchema().getSchemaForClass(clazz);
        this.table = schema.getTable();
        this.linkView = null;
        this.query = table.where();
    }

    private RealmQuery(RealmResults<E> queryResults, Class<E> clazz) {
        this.realm = queryResults.realm;
        this.clazz = clazz;
        this.schema = realm.getSchema().getSchemaForClass(clazz);
        this.table = queryResults.getTable();
        this.linkView = null;
        this.query = queryResults.getCollection().where();
    }

    private RealmQuery(BaseRealm realm, LinkView linkView, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.schema = realm.getSchema().getSchemaForClass(clazz);
        this.table = schema.getTable();
        this.linkView = linkView;
        this.query = linkView.where();
    }

    private RealmQuery(BaseRealm realm, String className) {
        this.realm = realm;
        this.className = className;
        this.schema = realm.getSchema().getSchemaForClass(className);
        this.table = schema.getTable();
        this.query = table.where();
    }

    private RealmQuery(RealmResults<DynamicRealmObject> queryResults, String className) {
        this.realm = queryResults.realm;
        this.className = className;
        this.schema = realm.getSchema().getSchemaForClass(className);
        this.table = schema.getTable();
        this.query = queryResults.getCollection().where();
    }

    private RealmQuery(BaseRealm realm, LinkView linkView, String className) {
        this.realm = realm;
        this.className = className;
        this.schema = realm.getSchema().getSchemaForClass(className);
        this.table = schema.getTable();
        this.linkView = linkView;
        this.query = linkView.where();
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

        if (linkView != null) {
            return linkView.isAttached();
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

        long[] columnIndices = schema.getColumnIndices(fieldName);

        // Checks that fieldName has the correct type is done in C++.
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName);

        // Checks that fieldName has the correct type is done in C++.
        this.query.isNotNull(columnIndices);
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
    public RealmQuery<E> equalTo(String fieldName, String value) {
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
    public RealmQuery<E> equalTo(String fieldName, String value, Case casing) {
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value, casing);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, String value, Case casing) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
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
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, Byte value) {
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
    public RealmQuery<E> equalTo(String fieldName, byte[] value) {
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.BINARY);
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
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, Short value) {
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
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, Integer value) {
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
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, Long value) {
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
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, Double value) {
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
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, Float value) {
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
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, Boolean value) {
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
        realm.checkIfValid();

        return equalToWithoutThreadValidation(fieldName, value);
    }

    private RealmQuery<E> equalToWithoutThreadValidation(String fieldName, Date value) {
        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.equalTo(columnIndices, value);
        return this;
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with and it cannot be null or empty.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a String field or {@code values} is {@code null} or
     * empty.
     */
    public RealmQuery<E> in(String fieldName, String[] values) {
        return in(fieldName, values, Case.SENSITIVE);
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with and it cannot be null or empty.
     * @param casing how casing is handled. {@link Case#INSENSITIVE} works only for the Latin-1 characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a String field or {@code values} is {@code null} or
     * empty.
     */
    public RealmQuery<E> in(String fieldName, String[] values, Case casing) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(EMPTY_VALUES);
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
     * @param values array of values to compare with and it cannot be null or empty.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Byte field or {@code values} is {@code null} or
     * empty.
     */
    public RealmQuery<E> in(String fieldName, Byte[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(EMPTY_VALUES);
        }
        beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
        for (int i = 1; i < values.length; i++) {
            orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
        }
        return endGroupWithoutThreadValidation();
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with and it cannot be null or empty.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Short field or {@code values} is {@code null} or
     * empty.
     */
    public RealmQuery<E> in(String fieldName, Short[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(EMPTY_VALUES);
        }
        beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
        for (int i = 1; i < values.length; i++) {
            orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
        }
        return endGroupWithoutThreadValidation();
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with and it cannot be null or empty.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Integer field or {@code values} is {@code null}
     * or empty.
     */
    public RealmQuery<E> in(String fieldName, Integer[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(EMPTY_VALUES);
        }
        beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
        for (int i = 1; i < values.length; i++) {
            orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
        }
        return endGroupWithoutThreadValidation();
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with and it cannot be null or empty.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Long field or {@code values} is {@code null} or
     * empty.
     */
    public RealmQuery<E> in(String fieldName, Long[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(EMPTY_VALUES);
        }
        beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
        for (int i = 1; i < values.length; i++) {
            orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
        }
        return endGroupWithoutThreadValidation();
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with and it cannot be null or empty.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Double field or {@code values} is {@code null} or
     * empty.
     */
    public RealmQuery<E> in(String fieldName, Double[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(EMPTY_VALUES);
        }
        beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
        for (int i = 1; i < values.length; i++) {
            orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
        }
        return endGroupWithoutThreadValidation();
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with and it cannot be null or empty.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Float field or {@code values} is {@code null} or
     * empty.
     */
    public RealmQuery<E> in(String fieldName, Float[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(EMPTY_VALUES);
        }
        beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
        for (int i = 1; i < values.length; i++) {
            orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
        }
        return endGroupWithoutThreadValidation();
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with and it cannot be null or empty.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Boolean field or {@code values} is {@code null}
     * or empty.
     */
    public RealmQuery<E> in(String fieldName, Boolean[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(EMPTY_VALUES);
        }
        beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
        for (int i = 1; i < values.length; i++) {
            orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
        }
        return endGroupWithoutThreadValidation();
    }

    /**
     * In comparison. This allows you to test if objects match any value in an array of values.
     *
     * @param fieldName the field to compare.
     * @param values array of values to compare with and it cannot be null or empty.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if the field isn't a Date field or {@code values} is {@code null} or
     * empty.
     */
    public RealmQuery<E> in(String fieldName, Date[] values) {
        realm.checkIfValid();

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(EMPTY_VALUES);
        }
        beginGroupWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[0]);
        for (int i = 1; i < values.length; i++) {
            orWithoutThreadValidation().equalToWithoutThreadValidation(fieldName, values[i]);
        }
        return endGroupWithoutThreadValidation();
    }

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
     * @param casing how casing is handled. {@link Case#INSENSITIVE} works only for the Latin-1 characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> notEqualTo(String fieldName, String value, Case casing) {
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
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
        realm.checkIfValid();

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
    public RealmQuery<E> notEqualTo(String fieldName, byte[] value) {
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.BINARY);
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
        realm.checkIfValid();

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
        realm.checkIfValid();

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
        realm.checkIfValid();

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
        realm.checkIfValid();

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
        realm.checkIfValid();

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
        realm.checkIfValid();

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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        if (value == null) {
            this.query.isNotNull(columnIndices);
        } else {
            this.query.notEqualTo(columnIndices, value);
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
        realm.checkIfValid();

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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.greaterThanOrEqual(columnIndices, value);
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

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.lessThanOrEqual(columnIndices, value);
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

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.INTEGER);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DOUBLE);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.FLOAT);
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
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.DATE);
        this.query.between(columnIndices, from, to);
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

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
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
     * @param casing how to handle casing. Setting this to {@link Case#INSENSITIVE} only works for Latin-1 characters.
     * @return the query object
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> beginsWith(String fieldName, String value, Case casing) {
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
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
     * @param casing how to handle casing. Setting this to {@link Case#INSENSITIVE} only works for Latin-1 characters.
     * @return the query object.
     * @throws java.lang.IllegalArgumentException if one or more arguments do not match class or field type.
     */
    public RealmQuery<E> endsWith(String fieldName, String value, Case casing) {
        realm.checkIfValid();

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.endsWith(columnIndices, value, casing);
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

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.STRING);
        this.query.like(columnIndices, value, casing);
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

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.STRING, RealmFieldType.BINARY, RealmFieldType.LIST);
        this.query.isEmpty(columnIndices);
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

        long[] columnIndices = schema.getColumnIndices(fieldName, RealmFieldType.STRING, RealmFieldType.BINARY, RealmFieldType.LIST);
        this.query.isNotEmpty(columnIndices);
        return this;
    }

    /**
     * Returns a distinct set of objects of a specific class. If the result is sorted, the first
     * object will be returned in case of multiple occurrences, otherwise it is undefined which
     * object is returned.
     * <p>
     * Adding {@link io.realm.annotations.Index} to the corresponding field will make this operation much faster.
     *
     * @param fieldName the field name.
     * @return a non-null {@link RealmResults} containing the distinct objects.
     * @throws IllegalArgumentException if a field is {@code null}, does not exist, is an unsupported type, or points
     * to linked fields.
     */
    public RealmResults<E> distinct(String fieldName) {
        realm.checkIfValid();

        SortDescriptor distinctDescriptor = SortDescriptor.getInstanceForDistinct(query.getTable(), fieldName);
        return createRealmResults(query, null, distinctDescriptor, true);
    }

    /**
     * Asynchronously returns a distinct set of objects of a specific class. If the result is
     * sorted, the first object will be returned in case of multiple occurrences, otherwise it is
     * undefined which object is returned.
     * Adding {@link io.realm.annotations.Index} to the corresponding field will make this operation much faster.
     *
     * @param fieldName the field name.
     * @return immediately a {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the
     * query completes.
     * @throws IllegalArgumentException if a field is {@code null}, does not exist, is an unsupported type, or points
     * to linked fields.
     */
    public RealmResults<E> distinctAsync(String fieldName) {
        realm.checkIfValid();

        realm.sharedRealm.capabilities.checkCanDeliverNotification(ASYNC_QUERY_WRONG_THREAD_MESSAGE);
        SortDescriptor distinctDescriptor = SortDescriptor.getInstanceForDistinct(query.getTable(), fieldName);
        return createRealmResults(query, null, distinctDescriptor, false);
    }

    /**
     * Returns a distinct set of objects from a specific class. When multiple distinct fields are
     * given, all unique combinations of values in the fields will be returned. In case of multiple
     * matches, it is undefined which object is returned. Unless the result is sorted, then the
     * first object will be returned.
     *
     * @param firstFieldName first field name to use when finding distinct objects.
     * @param remainingFieldNames remaining field names when determining all unique combinations of field values.
     * @return a non-null {@link RealmResults} containing the distinct objects.
     * @throws IllegalArgumentException if field names is empty or {@code null}, does not exist,
     * is an unsupported type, or points to a linked field.
     */
    public RealmResults<E> distinct(String firstFieldName, String... remainingFieldNames) {
        realm.checkIfValid();

        String[] fieldNames = new String[1 + remainingFieldNames.length];

        fieldNames[0] = firstFieldName;
        System.arraycopy(remainingFieldNames, 0, fieldNames, 1, remainingFieldNames.length);
        SortDescriptor distinctDescriptor = SortDescriptor.getInstanceForDistinct(table, fieldNames);
        return createRealmResults(query, null, distinctDescriptor, true);
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
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
        }
    }

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
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
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
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
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
                throw new IllegalArgumentException(String.format(TYPE_MISMATCH, fieldName, "int, float or double"));
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

        return this.query.count();
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

        return createRealmResults(query, null, null, true);
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
        realm.checkIfValid();

        realm.sharedRealm.capabilities.checkCanDeliverNotification(ASYNC_QUERY_WRONG_THREAD_MESSAGE);
        return createRealmResults(query, null, null, false);
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
     * @throws java.lang.IllegalArgumentException if field name does not exist or it belongs to a child
     * {@link RealmObject} or a child {@link RealmList}.
     */
    @SuppressWarnings("unchecked")
    public RealmResults<E> findAllSorted(String fieldName, Sort sortOrder) {
        realm.checkIfValid();

        SortDescriptor sortDescriptor = SortDescriptor.getInstanceForSort(query.getTable(), fieldName, sortOrder);
        return createRealmResults(query, sortDescriptor, null, true);
    }

    /**
     * Similar to {@link #findAllSorted(String, Sort)} but runs asynchronously on a worker thread
     * (need a Realm opened from a looper thread to work).
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @throws java.lang.IllegalArgumentException if field name does not exist or it belongs to a child
     * {@link RealmObject} or a child {@link RealmList}.
     */
    public RealmResults<E> findAllSortedAsync(final String fieldName, final Sort sortOrder) {
        realm.checkIfValid();

        realm.sharedRealm.capabilities.checkCanDeliverNotification(ASYNC_QUERY_WRONG_THREAD_MESSAGE);
        SortDescriptor sortDescriptor = SortDescriptor.getInstanceForSort(query.getTable(), fieldName, sortOrder);
        return createRealmResults(query, sortDescriptor, null, false);
    }


    /**
     * Finds all objects that fulfill the query conditions and sorted by specific field name in ascending order.
     * <p>
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName the field name to sort by.
     * @return a {@link io.realm.RealmResults} containing objects. If no objects match the condition, a list with zero
     * objects is returned.
     * @throws java.lang.IllegalArgumentException if the field name does not exist or it belongs to a child
     * {@link RealmObject} or a child {@link RealmList}.
     */
    public RealmResults<E> findAllSorted(String fieldName) {
        return findAllSorted(fieldName, Sort.ASCENDING);
    }

    /**
     * Similar to {@link #findAllSorted(String)} but runs asynchronously on a worker thread.
     * This method is only available from a Looper thread.
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @throws java.lang.IllegalArgumentException if the field name does not exist or it belongs to a child
     * {@link RealmObject} or a child {@link RealmList}.
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
     * objects is returned.
     * @throws java.lang.IllegalArgumentException if one of the field names does not exist or it belongs to a child
     * {@link RealmObject} or a child {@link RealmList}.
     */
    public RealmResults<E> findAllSorted(String[] fieldNames, Sort[] sortOrders) {
        realm.checkIfValid();

        SortDescriptor sortDescriptor = SortDescriptor.getInstanceForSort(query.getTable(), fieldNames, sortOrders);
        return createRealmResults(query, sortDescriptor, null, true);
    }

    private boolean isDynamicQuery() {
        return className != null;
    }

    /**
     * Similar to {@link #findAllSorted(String[], Sort[])} but runs asynchronously.
     * from a worker thread.
     * This method is only available from a Looper thread.
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @throws java.lang.IllegalArgumentException if one of the field names does not exist or it belongs to a child
     * {@link RealmObject} or a child {@link RealmList}.
     * @see io.realm.RealmResults
     */
    public RealmResults<E> findAllSortedAsync(String[] fieldNames, final Sort[] sortOrders) {
        realm.checkIfValid();

        realm.sharedRealm.capabilities.checkCanDeliverNotification(ASYNC_QUERY_WRONG_THREAD_MESSAGE);
        SortDescriptor sortDescriptor = SortDescriptor.getInstanceForSort(query.getTable(), fieldNames, sortOrders);
        return createRealmResults(query, sortDescriptor, null, false);
    }

    /**
     * Finds all objects that fulfill the query conditions and sorted by specific field names in ascending order.
     * <p>
     * Sorting is currently limited to character sets in 'Latin Basic', 'Latin Supplement', 'Latin Extended A',
     * 'Latin Extended B' (UTF-8 range 0-591). For other character sets, sorting will have no effect.
     *
     * @param fieldName1 first field name
     * @param sortOrder1 sort order for first field
     * @param fieldName2 second field name
     * @param sortOrder2 sort order for second field
     * @return a {@link io.realm.RealmResults} containing objects. If no objects match the condition, a list with zero
     * objects is returned.
     * @throws java.lang.IllegalArgumentException if a field name does not exist or it belongs to a child
     * {@link RealmObject} or a child {@link RealmList}.
     */
    public RealmResults<E> findAllSorted(String fieldName1, Sort sortOrder1,
            String fieldName2, Sort sortOrder2) {
        return findAllSorted(new String[] {fieldName1, fieldName2}, new Sort[] {sortOrder1, sortOrder2});
    }

    /**
     * Similar to {@link #findAllSorted(String, Sort, String, Sort)} but runs asynchronously on a worker thread
     * This method is only available from a Looper thread.
     *
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query completes.
     * @throws java.lang.IllegalArgumentException if a field name does not exist or it belongs to a child
     * {@link RealmObject} or a child {@link RealmList}.
     */
    public RealmResults<E> findAllSortedAsync(String fieldName1, Sort sortOrder1,
            String fieldName2, Sort sortOrder2) {
        return findAllSortedAsync(new String[] {fieldName1, fieldName2}, new Sort[] {sortOrder1, sortOrder2});
    }

    /**
     * Finds the first object that fulfills the query conditions.
     *
     * @return the object found or {@code null} if no object matches the query conditions.
     * @see io.realm.RealmObject
     */
    public E findFirst() {
        realm.checkIfValid();

        long tableRowIndex = getSourceRowIndexForFirstObject();
        return (tableRowIndex < 0) ? null : realm.get(clazz, className, tableRowIndex);
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

        realm.sharedRealm.capabilities.checkCanDeliverNotification(ASYNC_QUERY_WRONG_THREAD_MESSAGE);
        Row row;
        if (realm.isInTransaction()) {
            // It is not possible to create async query inside a transaction. So immediately query the first object.
            // See OS Results::prepare_async()
            row = new Collection(realm.sharedRealm, query).firstUncheckedRow();
        } else {
            // prepares an empty reference of the RealmObject which is backed by a pending query,
            // then update it once the query complete in the background.

            // TODO: The performance by the pending query will be a little bit worse than directly calling core's
            // Query.find(). The overhead comes with core needs to add all the row indices to the vector. However this
            // can be optimized by adding support of limit in OS's Results which is supported by core already.
            row = new PendingRow(realm.sharedRealm, query, null, isDynamicQuery());
        }
        final E result;
        if (isDynamicQuery()) {
            //noinspection unchecked
            result = (E) new DynamicRealmObject(realm, row);
        } else {
            result = realm.getConfiguration().getSchemaMediator().newInstance(
                    clazz, realm, row, realm.getSchema().getColumnInfo(clazz),
                    false, Collections.<String>emptyList());
        }

        if (row instanceof PendingRow) {
            final RealmObjectProxy proxy = (RealmObjectProxy) result;
            ((PendingRow) row).setFrontEnd(proxy.realmGet$proxyState());
        }

        return result;
    }

    private RealmResults<E> createRealmResults(TableQuery query,
            SortDescriptor sortDescriptor,
            SortDescriptor distinctDescriptor,
            boolean loadResults) {
        RealmResults<E> results;
        Collection collection = new Collection(realm.sharedRealm, query, sortDescriptor, distinctDescriptor);
        if (isDynamicQuery()) {
            results = new RealmResults<>(realm, collection, className);
        } else {
            results = new RealmResults<>(realm, collection, clazz);
        }
        if (loadResults) {
            results.load();
        }
        return results;
    }

    private long getSourceRowIndexForFirstObject() {
        return this.query.find();
    }
}
