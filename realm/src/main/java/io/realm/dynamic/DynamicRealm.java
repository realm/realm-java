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

package io.realm.dynamic;

import android.os.Looper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.RealmConfiguration;
import io.realm.base.BaseRealm;
import io.realm.exceptions.RealmException;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;

/**
 * DynamicRealm is a dynamic variant of {@link io.realm.Realm}. This means that all access to data and/or queries are
 * done using Strings instead of classes.
 *
 * The same {@link io.realm.RealmConfiguration} can be used to open a Realm file as both a dynamic Realm and the normal
 * typed one.
 *
 * Dynamic Realms do not enforce schemaVersions and doesn't trigger migrations even if they have been defined in
 * the configuration.
 *
 * Note that a DynamicRealm and a normal Realm share the same underlying resources so that also means they will
 * share transactions, i.e. it is possible to start a transaction in a normal Realm and commit it from a
 * dynamic Realm. Doing so is highly discouraged.
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

    // Caches Class objects (both model classes and proxy classes) to Realm Tables
    private final Map<String, Table> classToTable = new HashMap<String, Table>();

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
        return get(className, rowIndex);
    }

    /**
     * Returns a RealmQuery, which can be used to query for the provided class.
     *
     * @param className The class of the object which is to be queried for.
     * @return A RealmQuery, which can be used to query for specific objects of provided type.
     * @see io.realm.RealmQuery
     */
    public DynamicRealmQuery where(String className) {
        checkIfValid();
        if (!sharedGroup.hasTable(Table.TABLE_PREFIX + className)) {
            throw new IllegalArgumentException("Class does not exist in the Realm so it cannot be queried: " + className);
        }
        return new DynamicRealmQuery(this, className);
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

    protected Table getTable(String className) {
        className = Table.TABLE_PREFIX + className;
        Table table = classToTable.get(className);
        if (table == null) {
            table = sharedGroup.getTable(className);
            classToTable.put(className, table);
        }
        return table;
    }

    DynamicRealmObject get(String className, long rowIndex) {
        Table table = getTable(className);
        UncheckedRow row = table.getUncheckedRow(rowIndex);
        DynamicRealmObject result = new DynamicRealmObject(this, row);
        return result;
    }

    protected void checkIsValid() {
        super.checkIfValid();
    }

    @Override
    protected void lastLocalInstanceClosed() {
        realmsCache.get().remove(configuration);
    }

    @Override
    protected Map<RealmConfiguration, Integer> getLocalReferenceCount() {
        return referenceCount.get();
    }
}

