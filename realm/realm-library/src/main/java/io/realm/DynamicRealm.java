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

import io.reactivex.Flowable;
import java.util.Locale;

import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmFileException;
import io.realm.internal.CheckedRow;
import io.realm.internal.OsObject;
import io.realm.internal.SharedRealm;
import io.realm.internal.Table;
import io.realm.log.RealmLog;

/**
 * DynamicRealm is a dynamic variant of {@link io.realm.Realm}. This means that all access to data and/or queries are
 * done using string based class names instead of class type references.
 * <p>
 * This is useful during migrations or when working with string-based data like CSV or XML files.
 * <p>
 * The same {@link io.realm.RealmConfiguration} can be used to open a Realm file in both dynamic and typed mode, but
 * modifying the schema while having both a typed and dynamic version open is highly discouraged and will most likely
 * crash the typed Realm. During migrations only a DynamicRealm will be open.
 * <p>
 * Dynamic Realms do not enforce schemas or schema versions and {@link RealmMigration} code is not used even if it has
 * been defined in the {@link RealmConfiguration}.
 * <p>
 * This means that the schema is not created or validated until a Realm has been opened in typed mode. If a Realm
 * file is opened in dynamic mode first it will not contain any information about classes and fields, and any queries
 * for classes defined by the schema will fail.
 *
 * @see Realm
 * @see RealmSchema
 */
public class DynamicRealm extends BaseRealm {

    private final RealmSchema schema;

    private DynamicRealm(RealmCache cache) {
        super(cache, null);
        this.schema = new MutableRealmSchema(this);
    }

    private DynamicRealm(SharedRealm sharedRealm) {
        super(sharedRealm);
        this.schema = new MutableRealmSchema(this);
    }

    /**
     * Realm static constructor that returns a dynamic variant of the Realm instance defined by provided
     * {@link io.realm.RealmConfiguration}. Dynamic Realms do not care about schemaVersion and schemas, so opening a
     * DynamicRealm will never trigger a migration.
     *
     * @return the DynamicRealm defined by the configuration.
     * @throws RealmFileException if an error happened when accessing the underlying Realm file.
     * @throws IllegalArgumentException if {@code configuration} argument is {@code null}.
     * @see RealmConfiguration for details on how to configure a Realm.
     */
    public static DynamicRealm getInstance(RealmConfiguration configuration) {
        //noinspection ConstantConditions
        if (configuration == null) {
            throw new IllegalArgumentException("A non-null RealmConfiguration must be provided");
        }
        return RealmCache.createRealmOrGetFromCache(configuration, DynamicRealm.class);
    }

    /**
     * The creation of the first Realm instance per {@link RealmConfiguration} in a process can take some time as all
     * initialization code need to run at that point (Setting up the Realm, validating schemas and creating initial
     * data). This method places the initialization work in a background thread and deliver the Realm instance
     * to the caller thread asynchronously after the initialization is finished.
     *
     * @param configuration {@link RealmConfiguration} used to open the Realm.
     * @param callback invoked to return the results.
     * @throws IllegalArgumentException if a null {@link RealmConfiguration} or a null {@link Callback} is provided.
     * @throws IllegalStateException if it is called from a non-Looper or {@link android.app.IntentService} thread.
     * @return a {@link RealmAsyncTask} representing a cancellable task.
     * @see Callback for more details.
     */
    public static RealmAsyncTask getInstanceAsync(RealmConfiguration configuration,
                                                  Callback callback) {
        //noinspection ConstantConditions
        if (configuration == null) {
            throw new IllegalArgumentException("A non-null RealmConfiguration must be provided");
        }
        return RealmCache.createRealmOrGetFromCacheAsync(configuration, callback, DynamicRealm.class);
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
        // Check and throw the exception earlier for a better exception message.
        if (table.hasPrimaryKey()) {
            throw new RealmException(String.format(Locale.US,
                    "'%s' has a primary key, use" +
                    " 'createObject(String, Object)' instead.", className));
        }

        return new DynamicRealmObject(this, CheckedRow.getFromRow(OsObject.create(table)));
    }

    /**
     * Creates an object with a given primary key. Classes without a primary key defined must use
     * {@link #createObject(String)}} instead.
     *
     * @return the new object. All fields will have default values for their type, except for the
     * primary key field which will have the provided value.
     * @throws RealmException if object could not be created due to the primary key being invalid.
     * @throws IllegalStateException if the model clazz does not have an primary key defined.
     * @throws IllegalArgumentException if the {@code primaryKeyValue} doesn't have a value that can be converted to the
     * expected value.
     */
    public DynamicRealmObject createObject(String className, Object primaryKeyValue) {
        Table table = schema.getTable(className);
        return new DynamicRealmObject(this,
                CheckedRow.getFromRow(OsObject.createWithPrimaryKey(table, primaryKeyValue)));
    }

    /**
     * Returns a RealmQuery, which can be used to query the provided class.
     *
     * @param className the class of the object which is to be queried.
     * @return a RealmQuery, which can be used to query for specific objects of provided type.
     * @throws IllegalArgumentException if the class doesn't exist.
     * @see io.realm.RealmQuery
     */
    public RealmQuery<DynamicRealmObject> where(String className) {
        checkIfValid();
        if (!sharedRealm.hasTable(Table.getTableNameForClass(className))) {
            throw new IllegalArgumentException("Class does not exist in the Realm and cannot be queried: " + className);
        }
        return RealmQuery.createDynamicQuery(this, className);
    }


    /**
     * Adds a change listener to the Realm.
     * <p>
     * The listeners will be executed when changes are committed by this or another thread.
     * <p>
     * Realm instances are cached per thread. For that reason it is important to
     * remember to remove listeners again either using {@link #removeChangeListener(RealmChangeListener)}
     * or {@link #removeAllChangeListeners()}. Not doing so can cause memory leaks.
     *
     * @param listener the change listener.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @see io.realm.RealmChangeListener
     * @see #removeChangeListener(RealmChangeListener)
     * @see #removeAllChangeListeners()
     * @see #waitForChange()
     */
    public void addChangeListener(RealmChangeListener<DynamicRealm> listener) {
        addListener(listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeChangeListener(RealmChangeListener<DynamicRealm> listener) {
        removeListener(listener);
    }

    /**
     * Removes all user-defined change listeners.
     *
     * @throws IllegalStateException if you try to remove listeners from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        removeAllListeners();
    }

    /**
     * Deletes all objects of the specified class from the Realm.
     *
     * @param className the class for which all objects should be removed.
     */
    public void delete(String className) {
        checkIfValid();
        checkIfInTransaction();
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
        //noinspection ConstantConditions
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
                RealmLog.warn("Could not cancel transaction, not currently in a transaction.");
            }
            throw e;
        }
    }

    /**
     * Creates a {@link DynamicRealm} instance without checking the existence in the {@link RealmCache}.
     *
     * @return a {@link DynamicRealm} instance.
     */
    static DynamicRealm createInstance(RealmCache cache) {
        return new DynamicRealm(cache);
    }

    /**
     * Creates a {@link DynamicRealm} instance with a given {@link SharedRealm} instance without owning it.
     * This is designed to be used in the migration block when opening a typed Realm instance.
     *
     * @param sharedRealm the existing {@link SharedRealm} instance.
     * @return a {@link DynamicRealm} instance.
     */
    static DynamicRealm createInstance(SharedRealm sharedRealm) {
        return new DynamicRealm(sharedRealm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flowable<DynamicRealm> asFlowable() {
        return configuration.getRxFactory().from(this);
    }

    /**
     * Returns the mutable schema for this Realm.
     *
     * @return The {@link RealmSchema} for this Realm.
     */
    @Override
    public RealmSchema getSchema() {
        return schema;
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

    /**
     * {@inheritDoc}
     */
    public static abstract class Callback extends InstanceCallback<DynamicRealm> {
        /**
         * {@inheritDoc}
         */
        @Override
        public abstract void onSuccess(DynamicRealm realm);

        /**
         * {@inheritDoc}
         */
        @Override
        public void onError(Throwable exception) {
            super.onError(exception);
        }
    }
}
