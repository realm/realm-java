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

import java.util.Locale;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import io.reactivex.Flowable;
import io.realm.annotations.RealmClass;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmFileException;
import io.realm.internal.CheckedRow;
import io.realm.internal.OsObject;
import io.realm.internal.OsObjectStore;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmNotifier;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.Util;
import io.realm.internal.async.RealmAsyncTaskImpl;
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

    private DynamicRealm(final RealmCache cache, OsSharedRealm.VersionID version) {
        super(cache, null, version);
        RealmCache.invokeWithGlobalRefCount(cache.getConfiguration(), new RealmCache.Callback() {
            @Override
            public void onResult(int count) {
                if (count > 0)  {
                    return;
                }
                if (cache.getConfiguration().isReadOnly()) {
                    return;
                }
                if (OsObjectStore.getSchemaVersion(sharedRealm) != OsObjectStore.SCHEMA_NOT_VERSIONED) {
                    return;
                }
                sharedRealm.beginTransaction();
                if (OsObjectStore.getSchemaVersion(sharedRealm) == OsObjectStore.SCHEMA_NOT_VERSIONED) {
                    // To initialize the meta table.
                    OsObjectStore.setSchemaVersion(sharedRealm, OsObjectStore.SCHEMA_NOT_VERSIONED);
                }
                sharedRealm.commitTransaction();
            }
        });
        this.schema = new MutableRealmSchema(this);
    }

    private DynamicRealm(OsSharedRealm sharedRealm) {
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
        String pkField = OsObjectStore.getPrimaryKeyForObject(sharedRealm, className);
        // Check and throw the exception earlier for a better exception message.
        if (pkField != null) {
            throw new RealmException(String.format(Locale.US,
                    "'%s' has a primary key field '%s', use  'createObject(String, Object)' instead.",
                    className, pkField));
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
     * Instantiates and adds a new embedded object to the Realm.
     * <p>
     * This method should only be used to create objects of types marked as embedded.
     *
     * @param className the class name of the object to create.
     * @param parentObject The parent object which should hold a reference to the embedded object.
     *                     If the parent property is a list the embedded object will be added to the
     *                     end of that list.
     * @param parentProperty the property in the parent class which holds the reference.
     * @return the newly created embedded object.
     * @throws IllegalArgumentException if {@code clazz} is not an embedded class or if the property
     * in the parent class cannot hold objects of the appropriate type.
     * @see RealmClass#embedded()
     */
    public DynamicRealmObject createEmbeddedObject(String className,
                                                   DynamicRealmObject parentObject,
                                                   String parentProperty) {
        checkIfValid();
        Util.checkNull(parentObject, "parentObject");
        Util.checkEmpty(parentProperty, "parentProperty");
        if (!RealmObject.isManaged(parentObject) || !RealmObject.isValid(parentObject)) {
            throw new IllegalArgumentException("Only valid, managed objects can be a parent to an embedded object.");
        }

        String pkField = OsObjectStore.getPrimaryKeyForObject(sharedRealm, className);
        // Check and throw the exception earlier for a better exception message.
        if (pkField != null) {
            throw new RealmException(String.format(Locale.US,
                    "'%s' has a primary key field '%s', embedded objects cannot have primary keys.",
                    className, pkField));
        }

        String parentClassName = parentObject.getType();
        RealmObjectSchema parentObjectSchema = schema.get(parentClassName);

        if (parentObjectSchema == null) {
            throw new IllegalStateException(String.format("No schema found for '%s'.", parentClassName));
        }

        Row embeddedObject = getEmbeddedObjectRow(className, parentObject, parentProperty, schema, parentObjectSchema);

        return new DynamicRealmObject(this, embeddedObject);
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
     * @see #refresh()
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
     * @throws IllegalStateException if the Realm is closed or called from an incorrect thread.
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
     * <p>
     * Calling this method from the UI thread will throw a {@link RealmException}. Doing so may result in a drop of frames
     * or even ANRs. We recommend calling this method from a non-UI thread or using
     * {@link #executeTransactionAsync(Transaction)} instead.
     *
     * @param transaction {@link io.realm.DynamicRealm.Transaction} to execute.
     * @throws IllegalArgumentException if the {@code transaction} is {@code null}.
     * @throws RealmException if called from the UI thread, unless an explicit opt-in has been declared in {@link RealmConfiguration.Builder#allowWritesOnUiThread(boolean)}.
     */
    public void executeTransaction(Transaction transaction) {
        //noinspection ConstantConditions
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction should not be null");
        }

        checkAllowWritesOnUiThread();

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
     * Similar to {@link #executeTransaction(Transaction)} but runs asynchronously on a worker thread.
     *
     * @param transaction {@link io.realm.Realm.Transaction} to execute.
     * @return a {@link RealmAsyncTask} representing a cancellable task.
     * @throws IllegalArgumentException if the {@code transaction} is {@code null}, or if the Realm is opened from
     * another thread.
     */
    public RealmAsyncTask executeTransactionAsync(final Transaction transaction) {
        return executeTransactionAsync(transaction, null, null);
    }

    /**
     * Similar to {@link #executeTransactionAsync(Transaction)}, but also accepts an OnSuccess callback.
     *
     * @param transaction {@link io.realm.Realm.Transaction} to execute.
     * @param onSuccess callback invoked when the transaction succeeds.
     * @return a {@link RealmAsyncTask} representing a cancellable task.
     * @throws IllegalArgumentException if the {@code transaction} is {@code null}, or if the realm is opened from
     * another thread.
     */
    public RealmAsyncTask executeTransactionAsync(final Transaction transaction, final Realm.Transaction.OnSuccess onSuccess) {
        //noinspection ConstantConditions
        if (onSuccess == null) {
            throw new IllegalArgumentException("onSuccess callback can't be null");
        }

        return executeTransactionAsync(transaction, onSuccess, null);
    }

    /**
     * Similar to {@link #executeTransactionAsync(Transaction)}, but also accepts an OnError callback.
     *
     * @param transaction {@link io.realm.Realm.Transaction} to execute.
     * @param onError callback invoked when the transaction fails.
     * @return a {@link RealmAsyncTask} representing a cancellable task.
     * @throws IllegalArgumentException if the {@code transaction} is {@code null}, or if the realm is opened from
     * another thread.
     */
    public RealmAsyncTask executeTransactionAsync(final Transaction transaction, final Realm.Transaction.OnError onError) {
        //noinspection ConstantConditions
        if (onError == null) {
            throw new IllegalArgumentException("onError callback can't be null");
        }

        return executeTransactionAsync(transaction, null, onError);
    }

    /**
     * Similar to {@link #executeTransactionAsync(Transaction)}, but also accepts an OnSuccess and OnError callbacks.
     *
     * @param transaction {@link io.realm.Realm.Transaction} to execute.
     * @param onSuccess callback invoked when the transaction succeeds.
     * @param onError callback invoked when the transaction fails.
     * @return a {@link RealmAsyncTask} representing a cancellable task.
     * @throws IllegalArgumentException if the {@code transaction} is {@code null}, or if the realm is opened from
     * another thread.
     */
    public RealmAsyncTask executeTransactionAsync(final Transaction transaction,
                                                  @Nullable final Realm.Transaction.OnSuccess onSuccess,
                                                  @Nullable final Realm.Transaction.OnError onError) {
        checkIfValid();

        //noinspection ConstantConditions
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction should not be null");
        }

        if (isFrozen()) {
            throw new IllegalStateException("Write transactions on a frozen Realm is not allowed.");
        }

        // Avoid to call canDeliverNotification() in bg thread.
        final boolean canDeliverNotification = sharedRealm.capabilities.canDeliverNotification();

        // If the user provided a Callback then we have to make sure the current Realm has an events looper to deliver
        // the results.
        if ((onSuccess != null || onError != null)) {
            sharedRealm.capabilities.checkCanDeliverNotification("Callback cannot be delivered on current thread.");
        }

        // We need to use the same configuration to open a background OsSharedRealm (i.e Realm)
        // to perform the transaction
        final RealmConfiguration realmConfiguration = getConfiguration();
        // We need to deliver the callback even if the Realm is closed. So acquire a reference to the notifier here.
        final RealmNotifier realmNotifier = sharedRealm.realmNotifier;

        final Future<?> pendingTransaction = asyncTaskExecutor.submitTransaction(new Runnable() {
            @Override
            public void run() {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                OsSharedRealm.VersionID versionID = null;
                Throwable exception = null;

                final DynamicRealm bgRealm = DynamicRealm.getInstance(realmConfiguration);
                bgRealm.beginTransaction();
                try {
                    transaction.execute(bgRealm);

                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    bgRealm.commitTransaction();
                    // The bgRealm needs to be closed before post event to caller's handler to avoid concurrency
                    // problem. This is currently guaranteed by posting callbacks later below.
                    versionID = bgRealm.sharedRealm.getVersionID();
                } catch (final Throwable e) {
                    exception = e;
                } finally {
                    try {
                        if (bgRealm.isInTransaction()) {
                            bgRealm.cancelTransaction();
                        }
                    } finally {
                        bgRealm.close();
                    }
                }

                final Throwable backgroundException = exception;
                final OsSharedRealm.VersionID backgroundVersionID = versionID;
                // Cannot be interrupted anymore.
                if (canDeliverNotification) {
                    if (backgroundVersionID != null && onSuccess != null) {
                        realmNotifier.post(new Runnable() {
                            @Override
                            public void run() {
                                if (isClosed()) {
                                    // The caller Realm is closed. Just call the onSuccess. Since the new created Realm
                                    // cannot be behind the background one.
                                    onSuccess.onSuccess();
                                    return;
                                }

                                if (sharedRealm.getVersionID().compareTo(backgroundVersionID) < 0) {
                                    sharedRealm.realmNotifier.addTransactionCallback(new Runnable() {
                                        @Override
                                        public void run() {
                                            onSuccess.onSuccess();
                                        }
                                    });
                                } else {
                                    onSuccess.onSuccess();
                                }
                            }
                        });
                    } else if (backgroundException != null) {
                        realmNotifier.post(new Runnable() {
                            @Override
                            public void run() {
                                if (onError != null) {
                                    onError.onError(backgroundException);
                                } else {
                                    throw new RealmException("Async transaction failed", backgroundException);
                                }
                            }
                        });
                    }
                } else {
                    if (backgroundException != null) {
                        // FIXME: ThreadPoolExecutor will never throw the exception in the background.
                        // We need a redesign of the async transaction API.
                        // Throw in the worker thread since the caller thread cannot get notifications.
                        throw new RealmException("Async transaction failed", backgroundException);
                    }
                }

            }
        });

        return new RealmAsyncTaskImpl(pendingTransaction, asyncTaskExecutor);
    }

    /**
     * Creates a {@link DynamicRealm} instance without checking the existence in the {@link RealmCache}.
     *
     * @return a {@link DynamicRealm} instance.
     */
    static DynamicRealm createInstance(RealmCache cache, OsSharedRealm.VersionID version) {
        return new DynamicRealm(cache, version);
    }

    /**
     * Creates a {@link DynamicRealm} instance with a given {@link OsSharedRealm} instance without owning it.
     * This is designed to be used in the migration block when opening a typed Realm instance.
     *
     * @param sharedRealm the existing {@link OsSharedRealm} instance.
     * @return a {@link DynamicRealm} instance.
     */
    static DynamicRealm createInstance(OsSharedRealm sharedRealm) {
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
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        checkIfValid();
        return sharedRealm.isEmpty();
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
     * {@inheritDoc}
     */
    @Override
    public DynamicRealm freeze() {
        // In some cases a Read transaction has not begun for the Realm, which means
        // we cannot read the current version. In that case, do some work that will create the
        // read transaction.
        OsSharedRealm.VersionID version;
        try {
            version = sharedRealm.getVersionID();
        } catch (IllegalStateException e) {
            getVersion();
            version = sharedRealm.getVersionID();
        }
        return RealmCache.createRealmOrGetFromCache(configuration, DynamicRealm.class, version);
    }

    /**
     * Set the schema version of this dynamic realm to the given version number. If the meta table doesn't exist, this
     * will create the meta table first.
     * <p>
     * NOTE: This API is for internal testing only. Except testing, the schema version should always be set by the
     * Object Store during schema initialization or migration.
     *
     * @param version the schema version to be set.
     */
    void setVersion(long version) {
        OsObjectStore.setSchemaVersion(sharedRealm, version);
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
    public abstract static class Callback extends InstanceCallback<DynamicRealm> {
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
