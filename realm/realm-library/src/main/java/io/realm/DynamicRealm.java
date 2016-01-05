/*
 * Copyright 2015 Realm Inc.
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

import android.os.Looper;

import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmIOException;
import io.realm.internal.Table;
import io.realm.internal.TableView;
import rx.Observable;
import io.realm.internal.log.RealmLog;

/**
 * DynamicRealm is a dynamic variant of {@link io.realm.Realm}. This means that all access to data and/or queries are
 * done using string based class names instead of class type references.
 *
 * This is useful during migrations or when working with string-based data like CSV or XML files.
 *
 * The same {@link io.realm.RealmConfiguration} can be used to open a Realm file in both dynamic and typed mode, but
 * modifying the schema while having both a typed and dynamic version open is highly discouraged and will most likely
 * crash the typed Realm. During migrations only a DynamicRealm will be open.
 *
 * Dynamic Realms do not enforce schemas or schema versions and {@link RealmMigration} code is not used even if it has
 * been defined in the {@link RealmConfiguration}.
 *
 * This means that the schema is not created or validated until a Realm has been opened in typed mode, so if a Realm
 * file is opened in dynamic mode first it will not contain any information about classes and fields, and any queries
 * for classes defined by the schema will fail.
 *
 * @see Realm
 * @see RealmSchema
 */
public final class DynamicRealm extends BaseRealm {

    private DynamicRealm(RealmConfiguration configuration, boolean autoRefresh) {
        super(configuration, autoRefresh);
    }

    /**
     * Realm static constructor that returns a dynamic variant of the Realm instance defined by provided
     * {@link io.realm.RealmConfiguration}. Dynamic Realms do not care about schemaVersion and schemas, so opening a
     * DynamicRealm will never trigger a migration.
     *
     * @return the DynamicRealm defined by the configuration.
     * @see RealmConfiguration for details on how to configure a Realm.
     * @throws RealmIOException if an error happened when accessing the underlying Realm file.
     * @throws IllegalArgumentException if {@code configuration} argument is {@code null}.
     */
    public static DynamicRealm getInstance(RealmConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("A non-null RealmConfiguration must be provided");
        }
        return RealmCache.createRealmOrGetFromCache(configuration, DynamicRealm.class);
    }

    /**
     * Instantiates and adds a new object to the Realm.
     *
     * @param className the class name of the object to create.
     * @return the new object.
     * @throws RealmException if the object could not be created.
     */
    public DynamicRealmObject createObject(String className) {
        checkIfValid();
        Table table = schema.getTable(className);
        long rowIndex = table.addEmptyRow();
        DynamicRealmObject dynamicRealmObject = get(DynamicRealmObject.class, className, rowIndex);
        return dynamicRealmObject;
    }

    /**
     * Creates an object with a given primary key. Classes without a primary key defined must use
     * {@link #createObject(String)}} instead.
     *
     * @return the new object. All fields will have default values for their type, except for the
     * primary key field which will have the provided value.
     * @throws IllegalArgumentException if the primary key value is of the wrong type.
     * @throws IllegalStateException if the class doesn't have a primary key defined.
     */
    public DynamicRealmObject createObject(String className, Object primaryKeyValue) {
        Table table = schema.getTable(className);
        long index = table.addEmptyRowWithPrimaryKey(primaryKeyValue);
        DynamicRealmObject dynamicRealmObject = new DynamicRealmObject(this, table.getCheckedRow(index));
        if (handlerController != null) {
            handlerController.addToRealmObjects(dynamicRealmObject);
        }
        return dynamicRealmObject;
    }

    /**
     * Returns a RealmQuery, which can be used to query for the provided class.
     *
     * @param className The class of the object which is to be queried for.
     * @return a RealmQuery, which can be used to query for specific objects of provided type.
     * @see io.realm.RealmQuery
     * @throws IllegalArgumentException if the class doesn't exist.
     */
    public RealmQuery<DynamicRealmObject> where(String className) {
        checkIfValid();
        if (!sharedGroupManager.hasTable(Table.TABLE_PREFIX + className)) {
            throw new IllegalArgumentException("Class does not exist in the Realm so it cannot be queried: " + className);
        }
        return RealmQuery.createDynamicQuery(this, className);
    }

    /**
     * Removes all objects of the specified class.
     *
     * @param className the class for which all objects should be removed.
     */
    public void clear(String className) {
        checkIfValid();
        schema.getTable(className).clear();
    }

    /**
     * Executes a given transaction on the DynamicRealm. {@link #beginTransaction()} and
     * {@link #commitTransaction()} will be called automatically. If any exception is thrown
     * during the transaction {@link #cancelTransaction()} will be called instead of {@link #commitTransaction()}.
     *
     * @param transaction {@link io.realm.DynamicRealm.Transaction} to execute.
     * @throws IllegalArgumentException if the {@code transaction} is {@code null}.
     */
    public void executeTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction should not be null");
        }

        beginTransaction();
        try {
            transaction.execute(this);
            commitTransaction();
        } catch (RuntimeException e) {
            if (isInTransaction()) {
                cancelTransaction();
            } else {
                RealmLog.w("Could not cancel transaction, not currently in a transaction.");
            }
            throw e;
        }
    }

    /**
     * Get all objects of a specific class name.
     *
     * @param className the Class to get objects of.
     * @return a {@link RealmResults} list containing the objects. If no results where found, an empty list
     * will be returned.
     * @see io.realm.RealmResults
     */
    public RealmResults<DynamicRealmObject> allObjects(String className) {
        return where(className).findAll();
    }

    /**
     * Get all objects of a specific class name sorted by a field. If no objects exist, the returned
     * {@link RealmResults} will not be {@code null}. Use {@link RealmResults#size()} to check the number of objects instead.
     *
     * @param className the class to get all objects from.
     * @param fieldName the field name to sort by.
     * @param sortOrder how to sort the results.
     * @return a sorted {@link RealmResults} containing the objects.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public RealmResults<DynamicRealmObject> allObjectsSorted(String className, String fieldName, Sort sortOrder) {
        checkIfValid();
        Table table = schema.getTable(className);
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }

        TableView tableView = table.getSortedView(columnIndex, sortOrder);
        RealmResults<DynamicRealmObject> realmResults = RealmResults.createFromDynamicTableOrView(this, tableView, className);
        if (handlerController != null) {
            handlerController.addToRealmResults(realmResults);
        }
        return realmResults;
    }


    /**
     * Get all objects of a specific class name sorted by two specific field names.  If no objects exist,
     * the returned {@link RealmResults} will not be {@code null}. Use {@link RealmResults#size()} to check the number of
     * objects instead.
     *
     * @param className the class to get all objects from.
     * @param fieldName1 the first field name to sort by.
     * @param sortOrder1 how to sort the first field.
     * @param fieldName2 the second field name to sort by.
     * @param sortOrder2 how to sort the second field.
     * @return a sorted {@link RealmResults} containing the objects. If no results where found an empty list
     * is returned.
     * @throws java.lang.IllegalArgumentException if a field name used for sorting does not exist.
     */
    public RealmResults<DynamicRealmObject> allObjectsSorted(String className, String fieldName1,
                                                                    Sort sortOrder1, String fieldName2,
                                                                    Sort sortOrder2) {
        return allObjectsSorted(className, new String[]{fieldName1, fieldName2}, new Sort[]{sortOrder1,
                sortOrder2});
    }

    /**
     * Get all objects of a specific class name sorted by multiple fields.  If no objects exist, the
     * returned {@link RealmResults} will not be {@code null}. Use {@link RealmResults#size()} to check the number of
     * objects instead.
     *
     * @param className the class to get all objects from.
     * @param sortOrders sort ascending if SORT_ORDER_ASCENDING, sort descending if SORT_ORDER_DESCENDING.
     * @param fieldNames an array of field names to sort objects by.
     *        The objects are first sorted by fieldNames[0], then by fieldNames[1] and so forth.
     * @return A sorted {@link RealmResults} containing the objects.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    @SuppressWarnings("unchecked")
    public RealmResults<DynamicRealmObject> allObjectsSorted(String className, String fieldNames[], Sort sortOrders[]) {
        checkAllObjectsSortedParameters(fieldNames, sortOrders);
        Table table = schema.getTable(className);
        TableView tableView = doMultiFieldSort(fieldNames, sortOrders, table);

        RealmResults<DynamicRealmObject> realmResults = RealmResults.createFromDynamicTableOrView(this, tableView, className);
        if (handlerController != null) {
            handlerController.addToRealmResults(realmResults);
        }
        return realmResults;
    }

    /**
     * Creates a {@link DynamicRealm} instance without checking the existence in the {@link RealmCache}.
     *
     * @return a {@link DynamicRealm} instance.
     */
    static DynamicRealm createInstance(RealmConfiguration configuration) {
        boolean autoRefresh = Looper.myLooper() != null;
        return new DynamicRealm(configuration, autoRefresh);
    }

    /**
     * Return a distinct set of objects of a specific class. As a Realm is unordered, it is undefined which objects are
     * returned in case of multiple occurrences.
     *
     * @param className the Class to get objects of.
     * @param fieldName the field name.
     * @return A non-null {@link RealmResults} containing the distinct objects.
     * @throws IllegalArgumentException if a field name does not exist or the field is not indexed.
     */
    public RealmResults<DynamicRealmObject> distinct(String className, String fieldName) {
        checkNotNullFieldName(fieldName);
        checkIfValid();
        Table table = schema.getTable(className);
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }

        TableView tableView = table.getDistinctView(columnIndex);
        RealmResults<DynamicRealmObject> realmResults = RealmResults.createFromDynamicTableOrView(this, tableView, className);
        if (handlerController != null) {
            handlerController.addToRealmResults(realmResults);
        }
        return realmResults;
    }

    /**
     * Return a distinct set of objects of a specific class. As a Realm is unordered, it is undefined which objects are
     * returned in case of multiple occurrences.
     * This method is only available from a Looper thread.
     *
     * @param className the Class to get objects of.
     * @param fieldName the field name.
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     * {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified
     * when the query completes.
     * @throws IllegalArgumentException if a field name does not exist or the field is not indexed.
     */
    public RealmResults<DynamicRealmObject> distinctAsync(String className, String fieldName) {
        checkNotNullFieldName(fieldName);
        checkIfValid();
        Table table = schema.getTable(className);
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }

        // check if the field is indexed
        if (!table.hasSearchIndex(columnIndex)) {
            throw new IllegalArgumentException(String.format("Field name '%s' must be indexed in order to use it for distinct queries.", fieldName));
        }

        return where(className).distinctAsync(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<DynamicRealm> asObservable() {
        return configuration.getRxFactory().from(this);
    }

    /**
     * Encapsulates a Realm transaction.
     * <p>
     * Using this class will automatically handle {@link #beginTransaction()} and {@link #commitTransaction()}
     * If any exception is thrown during the transaction {@link #cancelTransaction()} will be called
     * instead of {@link #commitTransaction()}.
     */
    public interface Transaction {
        void execute(DynamicRealm realm);
    }
}

