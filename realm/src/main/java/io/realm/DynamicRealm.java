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
 *
 */

package io.realm;

import android.os.Looper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.exceptions.RealmException;
import io.realm.internal.Table;
import io.realm.internal.TableView;

/**
 * DynamicRealm is a dynamic variant of {@link io.realm.Realm}. This means that all access to data and/or queries are
 * done using Strings instead of classes.
 *
 * The same {@link io.realm.RealmConfiguration} can be used to open a Realm file in both dynamic and typed mode, but
 * modifying the schema while doing so is highly discouraged and will most likely crash the typed Realm.
 *
 * Dynamic Realms do not enforce schemas and schema versions and doesn't trigger migrations even if they have been
 * defined in the configuration.
 *
 * @see io.realm.Realm
 */
public class DynamicRealm extends BaseRealm {

    private static final ThreadLocal<Map<RealmConfiguration, DynamicRealm>> realmsCache =
            new ThreadLocal<Map<RealmConfiguration, DynamicRealm>>() {
                @Override
                protected Map<RealmConfiguration, DynamicRealm> initialValue() {
                    return new HashMap<RealmConfiguration, DynamicRealm>();
                }
            };

    private static final ThreadLocal<Map<RealmConfiguration, Integer>> referenceCount =
            new ThreadLocal<Map<RealmConfiguration,Integer>>() {
                @Override
                protected Map<RealmConfiguration, Integer> initialValue() {
                    return new HashMap<RealmConfiguration, Integer>();
                }
            };

    private DynamicRealm(RealmConfiguration configuration, boolean autoRefresh) {
        super(configuration, autoRefresh);
    }

    /**
     * Realm static constructor that returns a dynamic variant of the Realm instance defined by provided
     * {@link io.realm.RealmConfiguration}. Dynamic Realms do not care about schemaVersion and schemas, so opening a
     * DynamicRealm will never trigger a migration
     *
     * @return The DynamicRealm defined by the configuration.
     * @see RealmConfiguration for details on how to configure a Realm.
     */
    public static DynamicRealm getInstance(RealmConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("A non-null RealmConfiguration must be provided");
        }
        return create(configuration);
    }

    /**
     * Instantiates and adds a new object to the Realm.
     *
     * @param className The class of the object to create.
     * @return The new object.
     * @throws RealmException if the object could not be created.
     */
    public DynamicRealmObject createObject(String className) {
        Table table = getTable(className);
        long rowIndex = table.addEmptyRow();
        return get(DynamicRealmObject.class, className, rowIndex);
    }

    /**
     * Returns a RealmQuery, which can be used to query for the provided class.
     *
     * @param className The class of the object which is to be queried for.
     * @return A RealmQuery, which can be used to query for specific objects of provided type.
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
     * @param className The class for which all objects should be removed.
     */
    public void clear(String className) {
        getTable(className).clear();
    }

    /**
     * Executes a given transaction on the DynamicRealm. {@link #beginTransaction()} and
     * {@link #commitTransaction()} will be called automatically. If any exception is thrown
     * during the transaction {@link #cancelTransaction()} will be called instead of {@link #commitTransaction()}.
     *
     * @param transaction {@link io.realm.DynamicRealm.Transaction} to execute.
     * @throws RealmException if any error happened during the transaction.
     */
    public void executeTransaction(Transaction transaction) {
        if (transaction == null)
            return;
        beginTransaction();
        try {
            transaction.execute(this);
            commitTransaction();
        } catch (RuntimeException e) {
            cancelTransaction();
            throw new RealmException("Error during transaction.", e);
        } catch (Error e) {
            cancelTransaction();
            throw e;
        }
    }

    /**
     * Get all objects of a specific class name.
     *
     * @param className the Class to get objects of
     * @return A RealmResult list containing the objects. If no results where found, an empty list
     * will be returned.
     * @see io.realm.RealmResults
     */
    public RealmResults<DynamicRealmObject> allObjects(String className) {
        return where(className).findAll();
    }

    /**
     * Get all objects of a specific class name sorted by a field.  If no objects exist, the returned
     * RealmResults will not be null. The RealmResults.size() to check the number of objects instead.
     *
     * @param className The class to get all objects from.
     * @param fieldName the field name to sort by.
     * @param sortOrder how to sort the results.
     * @return A sorted RealmResults containing the objects.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public RealmResults<DynamicRealmObject> allObjectsSorted(String className, String fieldName, Sort sortOrder) {
        checkIfValid();
        Table table = getTable(className);
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }

        TableView tableView = table.getSortedView(columnIndex, sortOrder);
        return RealmResults.createFromDynamicQuery(this, tableView, className);
    }


    /**
     * Get all objects of a specific class name sorted by two specific field names.  If no objects exist,
     * the returned RealmResults will not be null. The RealmResults.size() to check the number of
     * objects instead.

     * @param className The class to get all objects from.
     * @param fieldName1 The first field name to sort by.
     * @param sortOrder1 How to sort the first field.
     * @param fieldName2 The second field name to sort by.
     * @param sortOrder2 How to sort the second field.
     * @return A sorted RealmResults containing the objects. If no results where found an empty list
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
     * returned RealmResults will not be null. The RealmResults.size() to check the number of
     * objects instead.
     *
     * @param className The class to get all objects from.
     * @param sortOrders sort ascending if SORT_ORDER_ASCENDING, sort descending if SORT_ORDER_DESCENDING.
     * @param fieldNames an array of field names to sort objects by.
     *        The objects are first sorted by fieldNames[0], then by fieldNames[1] and so forth.
     * @return A sorted RealmResults containing the objects.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    @SuppressWarnings("unchecked")
    public RealmResults<DynamicRealmObject> allObjectsSorted(String className, String fieldNames[],
                                                                    Sort sortOrders[]) {
        checkAllObjectsSortedParameters(fieldNames, sortOrders);
        Table table = this.getTable(className);
        TableView tableView = doMultiFieldSort(fieldNames, sortOrders, table);

        return RealmResults.createFromDynamicQuery(this, tableView, className);
    }

    private static synchronized DynamicRealm create(RealmConfiguration configuration) {

        // Check if a cached instance already exists for this thread
        String canonicalPath = configuration.getPath();
        Map<RealmConfiguration, Integer> localRefCount = referenceCount.get();
        Integer references = localRefCount.get(configuration);
        if (references == null) {
            references = 0;
        }
        Map<RealmConfiguration, DynamicRealm> realms = realmsCache.get();
        DynamicRealm realm = realms.get(configuration);
        if (realm != null) {
            localRefCount.put(configuration, references + 1);
            return realm;
        }

        // Create new Realm and cache it. All exception code paths must close the Realm otherwise we risk serving
        // faulty cache data.
        validateAgainstExistingConfigurations(configuration);
        boolean autoRefresh = Looper.myLooper() != null;
        realm = new DynamicRealm(configuration, autoRefresh);
        List<RealmConfiguration> pathConfigurationCache = globalPathConfigurationCache.get(canonicalPath);
        if (pathConfigurationCache == null) {
            pathConfigurationCache = new CopyOnWriteArrayList<RealmConfiguration>();
            globalPathConfigurationCache.put(canonicalPath, pathConfigurationCache);
        }
        pathConfigurationCache.add(configuration);
        realms.put(configuration, realm);
        localRefCount.put(configuration, references + 1);

        // Increment global reference counter
        realm.acquireFileReference(configuration);

        return realm;
    }

    @Override
    protected void lastLocalInstanceClosed() {
        realmsCache.get().remove(configuration);
    }

    @Override
    protected Map<RealmConfiguration, Integer> getLocalReferenceCount() {
        return referenceCount.get();
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

