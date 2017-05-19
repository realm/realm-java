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

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmFileException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnIndices;
import io.realm.internal.ColumnInfo;
import io.realm.internal.ObjectServerFacade;
import io.realm.internal.OsObject;
import io.realm.internal.OsSchemaInfo;
import io.realm.internal.RealmCore;
import io.realm.internal.RealmNotifier;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.SharedRealm;
import io.realm.internal.Table;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.util.Pair;
import io.realm.log.RealmLog;
import rx.Observable;


/**
 * The Realm class is the storage and transactional manager of your object persistent store. It is in charge of creating
 * instances of your RealmObjects. Objects within a Realm can be queried and read at any time. Creating, modifying, and
 * deleting objects must be done while inside a transaction. See {@link #executeTransaction(Transaction)}
 * <p>
 * The transactions ensure that multiple instances (on multiple threads) can access the same objects in a consistent
 * state with full ACID guarantees.
 * <p>
 * It is important to remember to call the {@link #close()} method when done with a Realm instance. Failing to do so can
 * lead to {@link java.lang.OutOfMemoryError} as the native resources cannot be freed.
 * <p>
 * Realm instances cannot be used across different threads. This means that you have to open an instance on each thread
 * you want to use Realm. Realm instances are cached automatically per thread using reference counting, so as long as
 * the reference count doesn't reach zero, calling {@link #getInstance(RealmConfiguration)} will just return the cached
 * Realm and should be considered a lightweight operation.
 * <p>
 * For the UI thread this means that opening and closing Realms should occur in either onCreate/onDestroy or
 * onStart/onStop.
 * <p>
 * Realm instances coordinate their state across threads using the {@link android.os.Handler} mechanism. This also means
 * that Realm instances on threads without a {@link android.os.Looper} cannot receive updates unless {@link #waitForChange()}
 * is manually called.
 * <p>
 * A standard pattern for working with Realm in Android activities can be seen below:
 * <p>
 * <pre>
 * public class RealmApplication extends Application {
 *
 *     \@Override
 *     public void onCreate() {
 *         super.onCreate();
 *
 *         // The Realm file will be located in package's "files" directory.
 *         RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).build();
 *         Realm.setDefaultConfiguration(realmConfig);
 *     }
 * }
 *
 * public class RealmActivity extends Activity {
 *
 *   private Realm realm;
 *
 *   \@Override
 *   protected void onCreate(Bundle savedInstanceState) {
 *     super.onCreate(savedInstanceState);
 *     setContentView(R.layout.layout_main);
 *     realm = Realm.getDefaultInstance();
 *   }
 *
 *   \@Override
 *   protected void onDestroy() {
 *     super.onDestroy();
 *     realm.close();
 *   }
 * }
 * </pre>
 * <p>
 * Realm supports String and byte fields containing up to 16 MB.
 * <p>
 *
 * @see <a href="http://en.wikipedia.org/wiki/ACID">ACID</a>
 * @see <a href="https://github.com/realm/realm-java/tree/master/examples">Examples using Realm</a>
 */
public class Realm extends BaseRealm {

    private static final String NULL_CONFIG_MSG = "A non-null RealmConfiguration must be provided";

    public static final String DEFAULT_REALM_NAME = RealmConfiguration.DEFAULT_REALM_NAME;

    private static RealmConfiguration defaultConfiguration;

    /**
     * The constructor is private to enforce the use of the static one.
     *
     * @param cache the {@link RealmCache} associated to this Realm instance.
     * @throws IllegalArgumentException if trying to open an encrypted Realm with the wrong key.
     */
    private Realm(RealmCache cache) {
        super(cache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Realm> asObservable() {
        return configuration.getRxFactory().from(this);
    }

    /**
     * Initializes the Realm library and creates a default configuration that is ready to use. It is required to call
     * this method before interacting with any other of the Realm API's.
     * <p>
     * A good place is in an {@link android.app.Application} subclass:
     * <pre>
     * {@code
     * public class MyApplication extends Application {
     *   \@Override
     *   public void onCreate() {
     *     super.onCreate();
     *     Realm.init(this);
     *   }
     * }
     * }
     * </pre>
     * <p>
     * Remember to register it in the {@code AndroidManifest.xml} file:
     * <pre>
     * {@code
     * <?xml version="1.0" encoding="utf-8"?>
     * <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="io.realm.example">
     * <application android:name=".MyApplication">
     *   // ...
     * </application>
     * </manifest>
     * }
     * </pre>
     *
     * @param context the Application Context.
     * @throws IllegalArgumentException if a {@code null} context is provided.
     * @throws IllegalStateException if {@link Context#getFilesDir()} could not be found.
     * @see #getDefaultInstance()
     */
    public static synchronized void init(Context context) {
        if (BaseRealm.applicationContext == null) {
            if (context == null) {
                throw new IllegalArgumentException("Non-null context required.");
            }
            checkFilesDirAvailable(context);
            RealmCore.loadLibrary(context);
            defaultConfiguration = new RealmConfiguration.Builder(context).build();
            ObjectServerFacade.getSyncFacadeIfPossible().init(context);
            BaseRealm.applicationContext = context.getApplicationContext();
            SharedRealm.initialize(new File(context.getFilesDir(), ".realm.temp"));
        }
    }

    /**
     * In some cases, Context.getFilesDir() is not available when the app launches the first time.
     * This should never happen according to the official Android documentation, but the race condition wasn't fixed
     * until Android 4.4.
     * <p>
     * This method attempts to fix that situation. If this doesn't work an {@link IllegalStateException} will be
     * thrown.
     * <p>
     * See these links for further details:
     * https://issuetracker.google.com/issues/36918154
     * https://github.com/realm/realm-java/issues/4493#issuecomment-295349044
     */
    private static void checkFilesDirAvailable(Context context) {
        File filesDir = context.getFilesDir();
        if (filesDir != null) {
            if (filesDir.exists()) {
                return; // Everything is fine. Escape as soon as possible
            } else {
                try {
                    // This was reported as working on some devices, which I really hope is just the race condition
                    // kicking in, otherwise something is seriously wrong with the permission system on those devices.
                    // We will try it anyway, since starting a loop will be slower by many magnitudes.
                    filesDir.mkdirs();
                } catch (SecurityException ignored) {
                }
            }
        }
        if (filesDir == null || !filesDir.exists()) {
            // Wait a "reasonable" amount of time before quitting.
            // In this case we define reasonable as 200 ms (~12 dropped frames) before giving up (which most likely
            // will result in the app crashing). This lag would only be seen in worst case scenarios, and then, only
            // when the app is started the first time.
            long[] timeoutsMs = new long[]{1, 2, 5, 10, 16}; // Exponential waits, capped at 16 ms;
            long maxTotalWaitMs = 200;
            long currentTotalWaitMs = 0;
            int waitIndex = -1;
            while (context.getFilesDir() == null || !context.getFilesDir().exists()) {
                long waitMs = timeoutsMs[Math.min(++waitIndex, timeoutsMs.length - 1)];
                SystemClock.sleep(waitMs);
                currentTotalWaitMs += waitMs;
                if (currentTotalWaitMs > maxTotalWaitMs) {
                    break;
                }
            }
        }

        // One final check before giving up
        if (context.getFilesDir() == null || !context.getFilesDir().exists()) {
            throw new IllegalStateException("Context.getFilesDir() returns " + context.getFilesDir() + " which is not an existing directory. See https://issuetracker.google.com/issues/36918154");
        }
    }

    /**
     * Realm static constructor that returns the Realm instance defined by the {@link io.realm.RealmConfiguration} set
     * by {@link #setDefaultConfiguration(RealmConfiguration)}
     *
     * @return an instance of the Realm class.
     * @throws java.lang.NullPointerException if no default configuration has been defined.
     * @throws RealmMigrationNeededException if no migration has been provided by the default configuration and the
     * RealmObject classes or version has has changed so a migration is required.
     * @throws RealmFileException if an error happened when accessing the underlying Realm file.
     * @throws io.realm.exceptions.DownloadingRealmInterruptedException if {@link SyncConfiguration.Builder#waitForInitialRemoteData()}
     * was set and the thread opening the Realm was interrupted while the download was in progress.
     */
    public static Realm getDefaultInstance() {
        if (defaultConfiguration == null) {
            throw new IllegalStateException("Call `Realm.init(Context)` before calling this method.");
        }
        return RealmCache.createRealmOrGetFromCache(defaultConfiguration, Realm.class);
    }

    /**
     * Realm static constructor that returns the Realm instance defined by provided {@link io.realm.RealmConfiguration}
     *
     * @param configuration {@link RealmConfiguration} used to open the Realm
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException if no migration has been provided by the configuration and the RealmObject
     * classes or version has has changed so a migration is required.
     * @throws RealmFileException if an error happened when accessing the underlying Realm file.
     * @throws IllegalArgumentException if a null {@link RealmConfiguration} is provided.
     * @throws io.realm.exceptions.DownloadingRealmInterruptedException if {@link SyncConfiguration.Builder#waitForInitialRemoteData()}
     * was set and the thread opening the Realm was interrupted while the download was in progress.
     * @see RealmConfiguration for details on how to configure a Realm.
     */
    public static Realm getInstance(RealmConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(NULL_CONFIG_MSG);
        }
        return RealmCache.createRealmOrGetFromCache(configuration, Realm.class);
    }

    /**
     * The creation of the first Realm instance per {@link RealmConfiguration} in a process can take some time as all
     * initialization code need to run at that point (setting up the Realm, validating schemas and creating initial
     * data). This method places the initialization work in a background thread and deliver the Realm instance
     * to the caller thread asynchronously after the initialization is finished.
     *
     * @param configuration {@link RealmConfiguration} used to open the Realm.
     * @param callback invoked to return the results.
     * @throws IllegalArgumentException if a null {@link RealmConfiguration} or a null {@link Callback} is provided.
     * @throws IllegalStateException if it is called from a non-Looper or {@link IntentService} thread.
     * @return a {@link RealmAsyncTask} representing a cancellable task.
     * @see Callback for more details.
     */
    public static RealmAsyncTask getInstanceAsync(RealmConfiguration configuration,
                                                  Callback callback) {
        if (configuration == null) {
            throw new IllegalArgumentException(NULL_CONFIG_MSG);
        }
        return RealmCache.createRealmOrGetFromCacheAsync(configuration, callback, Realm.class);
    }

    /**
     * Sets the {@link io.realm.RealmConfiguration} used when calling {@link #getDefaultInstance()}.
     *
     * @param configuration the {@link io.realm.RealmConfiguration} to use as the default configuration.
     * @throws IllegalArgumentException if a null {@link RealmConfiguration} is provided.
     * @see RealmConfiguration for details on how to configure a Realm.
     */
    public static void setDefaultConfiguration(RealmConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("A non-null RealmConfiguration must be provided");
        }
        defaultConfiguration = configuration;
    }

    /**
     * Removes the current default configuration (if any). Any further calls to {@link #getDefaultInstance()} will
     * fail until a new default configuration has been set using {@link #setDefaultConfiguration(RealmConfiguration)}.
     */
    public static void removeDefaultConfiguration() {
        defaultConfiguration = null;
    }

    /**
     * Creates a {@link Realm} instance without checking the existence in the {@link RealmCache}.
     *
     * @param cache the {@link RealmCache} where to create the realm in.
     * @return a {@link Realm} instance.
     */
    static Realm createInstance(RealmCache cache) {
        RealmConfiguration configuration = cache.getConfiguration();
        try {
            return createAndValidateFromCache(cache);

        } catch (RealmMigrationNeededException e) {
            if (configuration.shouldDeleteRealmIfMigrationNeeded()) {
                deleteRealm(configuration);
            } else {
                try {
                    if (configuration.getMigration() != null) {
                        migrateRealm(configuration, e);
                    }
                } catch (FileNotFoundException fileNotFoundException) {
                    // Should never happen.
                    throw new RealmFileException(RealmFileException.Kind.NOT_FOUND, fileNotFoundException);
                }
            }

            return createAndValidateFromCache(cache);
        }
    }

    private static Realm createAndValidateFromCache(RealmCache cache) {
        Realm realm = new Realm(cache);
        RealmConfiguration configuration = realm.configuration;

        final long currentVersion = realm.getVersion();
        final long requiredVersion = configuration.getSchemaVersion();

        final ColumnIndices columnIndices = RealmCache.findColumnIndices(cache.getTypedColumnIndicesArray(),
                requiredVersion);

        if (columnIndices != null) {
            // Copies global cache as a Realm local indices cache.
            realm.schema.setInitialColumnIndices(columnIndices);
        } else {
            final boolean syncingConfig = configuration.isSyncConfiguration();

            if (!syncingConfig && (currentVersion != UNVERSIONED)) {
                if (currentVersion < requiredVersion) {
                    realm.doClose();
                    throw new RealmMigrationNeededException(
                            configuration.getPath(),
                            String.format("Realm on disk need to migrate from v%s to v%s", currentVersion, requiredVersion));
                }
                if (requiredVersion < currentVersion) {
                    realm.doClose();
                    throw new IllegalArgumentException(
                            String.format("Realm on disk is newer than the one specified: v%s vs. v%s", currentVersion, requiredVersion));
                }
            }

            // Initializes Realm schema if needed.
            try {
                initializeRealm(realm);
            } catch (RuntimeException e) {
                realm.doClose();
                throw e;
            }
        }

        return realm;
    }

    private static void initializeRealm(Realm realm) {
        // Everything in this method needs to be behind a transaction lock to prevent multi-process interaction while
        // the Realm is initialized.
        boolean commitChanges = false;
        try {
            // We need to start a transaction no matter readOnly mode, because it acts as an interprocess lock.
            // TODO: For proper inter-process support we also need to move e.g copying the asset file under an
            // interprocess lock. This lock can obviously not be created by a Realm instance so we probably need
            // to implement it in Object Store. When this happens, the `beginTransaction(true)` can be removed again.
            realm.beginTransaction(true);
            RealmConfiguration configuration = realm.getConfiguration();
            long currentVersion = realm.getVersion();
            boolean unversioned = currentVersion == UNVERSIONED;
            long newVersion = configuration.getSchemaVersion();

            RealmProxyMediator mediator = configuration.getSchemaMediator();
            Set<Class<? extends RealmModel>> modelClasses = mediator.getModelClasses();

            if (configuration.isSyncConfiguration()) {
                // Update/create the schema if allowed
                if (!configuration.isReadOnly()) {
                    OsSchemaInfo schema = new OsSchemaInfo(mediator.getExpectedObjectSchemaInfoList());

                    // Object Store handles all update logic
                    realm.sharedRealm.updateSchema(schema, newVersion);
                    commitChanges = true;
                }
            } else {
                // Only allow creating the schema if not in read-only mode
                if (unversioned) {
                    if (configuration.isReadOnly()) {
                        throw new IllegalArgumentException("Cannot create the Realm schema in a read-only file.");
                    }

                    // Let Object Store initialize all tables
                    OsSchemaInfo schemaInfo = new OsSchemaInfo(mediator.getExpectedObjectSchemaInfoList());
                    realm.sharedRealm.updateSchema(schemaInfo, newVersion);
                    commitChanges = true;
                }
            }

            // Now that they have all been created, validate them.
            final Map<Pair<Class<? extends RealmModel>, String>, ColumnInfo> columnInfoMap = new HashMap<>(modelClasses.size());
            for (Class<? extends RealmModel> modelClass : modelClasses) {
                String className = Table.getClassNameForTable(mediator.getTableName(modelClass));
                Pair<Class<? extends RealmModel>, String> key = Pair.<Class<? extends RealmModel>, String>create(modelClass, className);
                // More fields in the Realm than defined is allowed for synced Realm.
                columnInfoMap.put(key, mediator.validateTable(modelClass, realm.sharedRealm,
                        configuration.isSyncConfiguration()));
            }

            realm.getSchema().setInitialColumnIndices(
                    (unversioned) ? newVersion : currentVersion,
                    columnInfoMap);

            // Finally add any initial data
            final Transaction transaction = configuration.getInitialDataTransaction();
            if (transaction != null && unversioned) {
                transaction.execute(realm);
            }
        } catch (Exception e) {
            commitChanges = false;
            throw e;
        } finally {
            if (commitChanges) {
                realm.commitTransaction();
            } else if (realm.isInTransaction()) {
                realm.cancelTransaction();
            }
        }
    }

    /**
     * Creates a Realm object for each object in a JSON array. This must be done within a transaction.
     * <p>
     * JSON properties with unknown properties will be ignored. If a {@link RealmObject} field is not present in the
     * JSON object the {@link RealmObject} field will be set to the default value for that type.
     *
     * @param clazz type of Realm objects to create.
     * @param json an array where each JSONObject must map to the specified class.
     * @throws RealmException if mapping from JSON fails.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     */
    public <E extends RealmModel> void createAllFromJson(Class<E> clazz, JSONArray json) {
        if (clazz == null || json == null) {
            return;
        }
        checkIfValid();

        for (int i = 0; i < json.length(); i++) {
            try {
                configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json.getJSONObject(i), false);
            } catch (JSONException e) {
                throw new RealmException("Could not map JSON", e);
            }
        }
    }

    /**
     * Tries to update a list of existing objects identified by their primary key with new JSON data. If an existing
     * object could not be found in the Realm, a new object will be created. This must happen within a transaction.
     * If updating a {@link RealmObject} and a field is not found in the JSON object, that field will not be updated. If
     * a new {@link RealmObject} is created and a field is not found in the JSON object, that field will be assigned the
     * default value for the field type.
     *
     * @param clazz type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json array with object data.
     * @throws IllegalArgumentException if trying to update a class without a {@link io.realm.annotations.PrimaryKey}.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     * @throws RealmException if unable to map JSON.
     * @see #createAllFromJson(Class, org.json.JSONArray)
     */
    public <E extends RealmModel> void createOrUpdateAllFromJson(Class<E> clazz, JSONArray json) {
        if (clazz == null || json == null) {
            return;
        }
        checkIfValid();
        checkHasPrimaryKey(clazz);
        for (int i = 0; i < json.length(); i++) {
            try {
                configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json.getJSONObject(i), true);
            } catch (JSONException e) {
                throw new RealmException("Could not map JSON", e);
            }
        }
    }

    /**
     * Creates a Realm object for each object in a JSON array. This must be done within a transaction.
     * JSON properties with unknown properties will be ignored. If a {@link RealmObject} field is not present in the
     * JSON object the {@link RealmObject} field will be set to the default value for that type.
     *
     * @param clazz type of Realm objects to create.
     * @param json the JSON array as a String where each object can map to the specified class.
     * @throws RealmException if mapping from JSON fails.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     */
    public <E extends RealmModel> void createAllFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return;
        }

        JSONArray arr;
        try {
            arr = new JSONArray(json);
        } catch (JSONException e) {
            throw new RealmException("Could not create JSON array from string", e);
        }

        createAllFromJson(clazz, arr);
    }

    /**
     * Tries to update a list of existing objects identified by their primary key with new JSON data. If an existing
     * object could not be found in the Realm, a new object will be created. This must happen within a transaction.
     * If updating a {@link RealmObject} and a field is not found in the JSON object, that field will not be updated.
     * If a new {@link RealmObject} is created and a field is not found in the JSON object, that field will be assigned
     * the default value for the field type.
     *
     * @param clazz type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json string with an array of JSON objects.
     * @throws IllegalArgumentException if trying to update a class without a {@link io.realm.annotations.PrimaryKey}.
     * @throws RealmException if unable to create a JSON array from the json string.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     * @see #createAllFromJson(Class, String)
     */
    public <E extends RealmModel> void createOrUpdateAllFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return;
        }
        checkIfValid();
        checkHasPrimaryKey(clazz);

        JSONArray arr;
        try {
            arr = new JSONArray(json);
        } catch (JSONException e) {
            throw new RealmException("Could not create JSON array from string", e);
        }

        createOrUpdateAllFromJson(clazz, arr);
    }

    /**
     * Creates a Realm object for each object in a JSON array. This must be done within a transaction.
     * JSON properties with unknown properties will be ignored. If a {@link RealmObject} field is not present in the
     * JSON object the {@link RealmObject} field will be set to the default value for that type.
     * <p>
     * This API is only available in API level 11 or later.
     *
     * @param clazz type of Realm objects created.
     * @param inputStream the JSON array as a InputStream. All objects in the array must be of the specified class.
     * @throws RealmException if mapping from JSON fails.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     * @throws IOException if something was wrong with the input stream.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmModel> void createAllFromJson(Class<E> clazz, InputStream inputStream) throws IOException {
        if (clazz == null || inputStream == null) {
            return;
        }
        checkIfValid();

        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                configuration.getSchemaMediator().createUsingJsonStream(clazz, this, reader);
            }
            reader.endArray();
        } finally {
            reader.close();
        }
    }

    /**
     * Tries to update a list of existing objects identified by their primary key with new JSON data. If an existing
     * object could not be found in the Realm, a new object will be created. This must happen within a transaction.
     * If updating a {@link RealmObject} and a field is not found in the JSON object, that field will not be updated.
     * If a new {@link RealmObject} is created and a field is not found in the JSON object, that field will be assigned
     * the default value for the field type.
     * <p>
     * This API is only available in API level 11 or later.
     *
     * @param clazz type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param in the InputStream with a list of object data in JSON format.
     * @throws IllegalArgumentException if trying to update a class without a {@link io.realm.annotations.PrimaryKey}.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     * @throws RealmException if unable to read JSON.
     * @see #createOrUpdateAllFromJson(Class, java.io.InputStream)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmModel> void createOrUpdateAllFromJson(Class<E> clazz, InputStream in) {
        if (clazz == null || in == null) {
            return;
        }
        checkIfValid();
        checkHasPrimaryKey(clazz);

        // As we need the primary key value we have to first parse the entire input stream as in the general
        // case that value might be the last property. :(
        Scanner scanner = null;
        try {
            scanner = getFullStringScanner(in);
            JSONArray json = new JSONArray(scanner.next());
            for (int i = 0; i < json.length(); i++) {
                configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json.getJSONObject(i), true);
            }
        } catch (JSONException e) {
            throw new RealmException("Failed to read JSON", e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /**
     * Creates a Realm object pre-filled with data from a JSON object. This must be done inside a transaction. JSON
     * properties with unknown properties will be ignored. If a {@link RealmObject} field is not present in the JSON
     * object the {@link RealmObject} field will be set to the default value for that type.
     *
     * @param clazz type of Realm object to create.
     * @param json the JSONObject with object data.
     * @return created object or {@code null} if no JSON data was provided.
     * @throws RealmException if the mapping from JSON fails.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     * @see #createOrUpdateObjectFromJson(Class, org.json.JSONObject)
     */
    public <E extends RealmModel> E createObjectFromJson(Class<E> clazz, JSONObject json) {
        if (clazz == null || json == null) {
            return null;
        }
        checkIfValid();

        try {
            return configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json, false);
        } catch (JSONException e) {
            throw new RealmException("Could not map JSON", e);
        }
    }

    /**
     * Tries to update an existing object defined by its primary key with new JSON data. If no existing object could be
     * found a new object will be saved in the Realm. This must happen within a transaction. If updating a {@link RealmObject}
     * and a field is not found in the JSON object, that field will not be updated. If a new {@link RealmObject} is
     * created and a field is not found in the JSON object, that field will be assigned the default value for the field type.
     *
     * @param clazz Type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json {@link org.json.JSONObject} with object data.
     * @return created or updated {@link io.realm.RealmObject}.
     * @throws IllegalArgumentException if trying to update a class without a {@link io.realm.annotations.PrimaryKey}.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     * @throws RealmException if JSON data cannot be mapped.
     * @see #createObjectFromJson(Class, org.json.JSONObject)
     */
    public <E extends RealmModel> E createOrUpdateObjectFromJson(Class<E> clazz, JSONObject json) {
        if (clazz == null || json == null) {
            return null;
        }
        checkIfValid();
        checkHasPrimaryKey(clazz);
        try {
            return configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json, true);
        } catch (JSONException e) {
            throw new RealmException("Could not map JSON", e);
        }
    }

    /**
     * Creates a Realm object pre-filled with data from a JSON object. This must be done inside a transaction. JSON
     * properties with unknown properties will be ignored. If a {@link RealmObject} field is not present in the JSON
     * object the {@link RealmObject} field will be set to the default value for that type.
     *
     * @param clazz type of Realm object to create.
     * @param json the JSON string with object data.
     * @return created object or {@code null} if JSON string was empty or null.
     * @throws RealmException if mapping to json failed.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     */
    public <E extends RealmModel> E createObjectFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return null;
        }

        JSONObject obj;
        try {
            obj = new JSONObject(json);
        } catch (JSONException e) {
            throw new RealmException("Could not create Json object from string", e);
        }

        return createObjectFromJson(clazz, obj);
    }

    /**
     * Tries to update an existing object defined by its primary key with new JSON data. If no existing object could be
     * found a new object will be saved in the Realm. This must happen within a transaction. If updating a
     * {@link RealmObject} and a field is not found in the JSON object, that field will not be updated. If a new
     * {@link RealmObject} is created and a field is not found in the JSON object, that field will be assigned the
     * default value for the field type.
     *
     * @param clazz type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json string with object data in JSON format.
     * @return created or updated {@link io.realm.RealmObject}.
     * @throws IllegalArgumentException if trying to update a class without a {@link io.realm.annotations.PrimaryKey}.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     * @throws RealmException if JSON object cannot be mapped from the string parameter.
     * @see #createObjectFromJson(Class, String)
     */
    public <E extends RealmModel> E createOrUpdateObjectFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return null;
        }
        checkIfValid();
        checkHasPrimaryKey(clazz);

        JSONObject obj;
        try {
            obj = new JSONObject(json);
        } catch (JSONException e) {
            throw new RealmException("Could not create Json object from string", e);
        }

        return createOrUpdateObjectFromJson(clazz, obj);
    }

    /**
     * Creates a Realm object pre-filled with data from a JSON object. This must be done inside a transaction. JSON
     * properties with unknown properties will be ignored. If a {@link RealmObject} field is not present in the JSON
     * object the {@link RealmObject} field will be set to the default value for that type.
     * <p>
     * This API is only available in API level 11 or later.
     *
     * @param clazz type of Realm object to create.
     * @param inputStream the JSON object data as a InputStream.
     * @return created object or {@code null} if JSON string was empty or null.
     * @throws RealmException if the mapping from JSON failed.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     * @throws IOException if something went wrong with the input stream.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmModel> E createObjectFromJson(Class<E> clazz, InputStream inputStream) throws IOException {
        if (clazz == null || inputStream == null) {
            return null;
        }
        checkIfValid();
        E realmObject;
        Table table = schema.getTable(clazz);
        if (table.hasPrimaryKey()) {
            // As we need the primary key value we have to first parse the entire input stream as in the general
            // case that value might be the last property. :(
            Scanner scanner = null;
            try {
                scanner = getFullStringScanner(inputStream);
                JSONObject json = new JSONObject(scanner.next());
                realmObject = configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json, false);

            } catch (JSONException e) {
                throw new RealmException("Failed to read JSON", e);
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        } else {
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            try {
                realmObject = configuration.getSchemaMediator().createUsingJsonStream(clazz, this, reader);
            } finally {
                reader.close();
            }
        }
        return realmObject;
    }

    /**
     * Tries to update an existing object defined by its primary key with new JSON data. If no existing object could be
     * found a new object will be saved in the Realm. This must happen within a transaction. If updating a
     * {@link RealmObject} and a field is not found in the JSON object, that field will not be updated. If a new
     * {@link RealmObject} is created and a field is not found in the JSON object, that field will be assigned the
     * default value for the field type.
     * <p>
     * This API is only available in API level 11 or later.
     *
     * @param clazz type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param in the {@link InputStream} with object data in JSON format.
     * @return created or updated {@link io.realm.RealmObject}.
     * @throws IllegalArgumentException if trying to update a class without a {@link io.realm.annotations.PrimaryKey}.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     * @throws RealmException if failure to read JSON.
     * @see #createObjectFromJson(Class, java.io.InputStream)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmModel> E createOrUpdateObjectFromJson(Class<E> clazz, InputStream in) {
        if (clazz == null || in == null) {
            return null;
        }
        checkIfValid();
        checkHasPrimaryKey(clazz);

        // As we need the primary key value we have to first parse the entire input stream as in the general
        // case that value might be the last property. :(
        Scanner scanner = null;
        try {
            scanner = getFullStringScanner(in);
            JSONObject json = new JSONObject(scanner.next());
            return createOrUpdateObjectFromJson(clazz, json);
        } catch (JSONException e) {
            throw new RealmException("Failed to read JSON", e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private Scanner getFullStringScanner(InputStream in) {
        return new Scanner(in, "UTF-8").useDelimiter("\\A");
    }

    /**
     * Instantiates and adds a new object to the Realm.
     * <p>
     * This method is only available for model classes with no @PrimaryKey annotation.
     * If you like to create an object that has a primary key, use {@link #createObject(Class, Object)}
     * or {@link #copyToRealm(RealmModel)} instead.
     *
     * @param clazz the Class of the object to create.
     * @return the new object.
     * @throws RealmException if the primary key is defined in the model class or an object cannot be created.
     * @see #createObject(Class, Object)
     */
    public <E extends RealmModel> E createObject(Class<E> clazz) {
        checkIfValid();
        return createObjectInternal(clazz, true, Collections.<String>emptyList());
    }

    /**
     * Same as {@link #createObject(Class)} but this does not check the thread.
     *
     * @param clazz the Class of the object to create.
     * @param acceptDefaultValue if {@code true}, default value of the object will be applied and
     * if {@code false}, it will be ignored.
     * @return the new object.
     * @throws RealmException if the primary key is defined in the model class or an object cannot be created.
     */
    // Called from proxy classes.
    <E extends RealmModel> E createObjectInternal(
            Class<E> clazz,
            boolean acceptDefaultValue,
            List<String> excludeFields) {
        Table table = schema.getTable(clazz);
        // Checks and throws the exception earlier for a better exception message.
        if (table.hasPrimaryKey()) {
            throw new RealmException(String.format("'%s' has a primary key, use" +
                    " 'createObject(Class<E>, Object)' instead.", table.getClassName()));
        }
        return configuration.getSchemaMediator().newInstance(clazz, this,
                OsObject.create(sharedRealm, table),
                schema.getColumnInfo(clazz),
                acceptDefaultValue, excludeFields);
    }

    /**
     * Instantiates and adds a new object to the Realm with the primary key value already set.
     * <p>
     * If the value violates the primary key constraint, no object will be added and a {@link RealmException} will be
     * thrown.
     * The default value for primary key provided by the model class will be ignored.
     *
     * @param clazz the Class of the object to create.
     * @param primaryKeyValue value for the primary key field.
     * @return the new object.
     * @throws RealmException if object could not be created due to the primary key being invalid.
     * @throws IllegalStateException if the model class does not have an primary key defined.
     * @throws IllegalArgumentException if the {@code primaryKeyValue} doesn't have a value that can be converted to the
     * expected value.
     */
    public <E extends RealmModel> E createObject(Class<E> clazz, Object primaryKeyValue) {
        checkIfValid();
        return createObjectInternal(clazz, primaryKeyValue, true, Collections.<String>emptyList());
    }

    /**
     * Same as {@link #createObject(Class, Object)} but this does not check the thread.
     *
     * @param clazz the Class of the object to create.
     * @param primaryKeyValue value for the primary key field.
     * @param acceptDefaultValue if {@code true}, default value of the object will be applied and
     * if {@code false}, it will be ignored.
     * @return the new object.
     * @throws RealmException if object could not be created due to the primary key being invalid.
     * @throws IllegalStateException if the model class does not have an primary key defined.
     * @throws IllegalArgumentException if the {@code primaryKeyValue} doesn't have a value that can be converted to the
     * expected value.
     */
    // Called from proxy classes.
    <E extends RealmModel> E createObjectInternal(
            Class<E> clazz,
            Object primaryKeyValue,
            boolean acceptDefaultValue,
            List<String> excludeFields) {
        Table table = schema.getTable(clazz);

        return configuration.getSchemaMediator().newInstance(clazz, this,
                OsObject.createWithPrimaryKey(sharedRealm, table, primaryKeyValue),
                schema.getColumnInfo(clazz),
                acceptDefaultValue, excludeFields);
    }

    /**
     * Copies a RealmObject to the Realm instance and returns the copy. Any further changes to the original RealmObject
     * will not be reflected in the Realm copy. This is a deep copy, so all referenced objects will be copied. Objects
     * already in this Realm will be ignored.
     * <p>
     * Please note, copying an object will copy all field values. Any unset field in this and child objects will be
     * set to their default value if not provided.
     *
     * @param object the {@link io.realm.RealmObject} to copy to the Realm.
     * @return a managed RealmObject with its properties backed by the Realm.
     * @throws java.lang.IllegalArgumentException if the object is {@code null} or it belongs to a Realm instance
     * in a different thread.
     */
    public <E extends RealmModel> E copyToRealm(E object) {
        checkNotNullObject(object);
        return copyOrUpdate(object, false, new HashMap<RealmModel, RealmObjectProxy>());
    }

    /**
     * Updates an existing RealmObject that is identified by the same {@link io.realm.annotations.PrimaryKey} or creates
     * a new copy if no existing object could be found. This is a deep copy or update i.e., all referenced objects will be
     * either copied or updated.
     * <p>
     * Please note, copying an object will copy all field values. Any unset field in the object and child objects will be
     * set to their default value if not provided.
     *
     * @param object {@link io.realm.RealmObject} to copy or update.
     * @return the new or updated RealmObject with all its properties backed by the Realm.
     * @throws java.lang.IllegalArgumentException if the object is {@code null} or doesn't have a Primary key defined
     * or it belongs to a Realm instance in a different thread.
     * @see #copyToRealm(RealmModel)
     */
    public <E extends RealmModel> E copyToRealmOrUpdate(E object) {
        checkNotNullObject(object);
        checkHasPrimaryKey(object.getClass());
        return copyOrUpdate(object, true, new HashMap<RealmModel, RealmObjectProxy>());
    }

    /**
     * Copies a collection of RealmObjects to the Realm instance and returns their copy. Any further changes to the
     * original RealmObjects will not be reflected in the Realm copies. This is a deep copy i.e., all referenced objects
     * will be copied. Objects already in this Realm will be ignored.
     * <p>
     * Please note, copying an object will copy all field values. Any unset field in the objects and child objects will be
     * set to their default value if not provided.
     *
     * @param objects the RealmObjects to copy to the Realm.
     * @return a list of the the converted RealmObjects that all has their properties managed by the Realm.
     * @throws io.realm.exceptions.RealmException if any of the objects has already been added to Realm.
     * @throws java.lang.IllegalArgumentException if any of the elements in the input collection is {@code null}.
     */
    public <E extends RealmModel> List<E> copyToRealm(Iterable<E> objects) {
        if (objects == null) {
            return new ArrayList<>();
        }
        Map<RealmModel, RealmObjectProxy> cache = new HashMap<>();
        ArrayList<E> realmObjects = new ArrayList<>();
        for (E object : objects) {
            checkNotNullObject(object);
            realmObjects.add(copyOrUpdate(object, false, cache));
        }

        return realmObjects;
    }

    /**
     * Inserts a list of an unmanaged RealmObjects. This is generally faster than {@link #copyToRealm(Iterable)} since it
     * doesn't return the inserted elements, and performs minimum allocations and checks.
     * After being inserted any changes to the original objects will not be persisted.
     * <p>
     * Please note:
     * <ul>
     * <li>
     * We don't check if the provided objects are already managed or not, so inserting a managed object might duplicate it.
     * Duplication will only happen if the object doesn't have a primary key. Objects with primary keys will never get duplicated.
     * </li>
     * <li>We don't create (nor return) a managed {@link RealmObject} for each element</li>
     * <li>Copying an object will copy all field values. Any unset field in the object and child objects will be set to their default value if not provided</li>
     * </ul>
     * <p>
     * If you want the managed {@link RealmObject} returned, use {@link #copyToRealm(Iterable)}, otherwise if
     * you have a large number of object this method is generally faster.
     *
     * @param objects RealmObjects to insert.
     * @throws IllegalStateException if the corresponding Realm is closed, called from an incorrect thread or not in a
     * transaction.
     * @see #copyToRealm(Iterable)
     */
    public void insert(Collection<? extends RealmModel> objects) {
        checkIfValidAndInTransaction();
        if (objects == null) {
            throw new IllegalArgumentException("Null objects cannot be inserted into Realm.");
        }
        if (objects.isEmpty()) {
            return;
        }
        configuration.getSchemaMediator().insert(this, objects);
    }

    /**
     * Inserts an unmanaged RealmObject. This is generally faster than {@link #copyToRealm(RealmModel)} since it
     * doesn't return the inserted elements, and performs minimum allocations and checks.
     * After being inserted any changes to the original object will not be persisted.
     * <p>
     * Please note:
     * <ul>
     * <li>
     * We don't check if the provided objects are already managed or not, so inserting a managed object might duplicate it.
     * Duplication will only happen if the object doesn't have a primary key. Objects with primary keys will never get duplicated.
     * </li>
     * <li>We don't create (nor return) a managed {@link RealmObject} for each element</li>
     * <li>Copying an object will copy all field values. Any unset field in the object and child objects will be set to their default value if not provided</li>
     * </ul>
     * <p>
     * If you want the managed {@link RealmObject} returned, use {@link #copyToRealm(RealmModel)}, otherwise if
     * you have a large number of object this method is generally faster.
     *
     * @param object RealmObjects to insert.
     * @throws IllegalStateException if the corresponding Realm is closed, called from an incorrect thread or not in a
     * transaction.
     * @throws io.realm.exceptions.RealmPrimaryKeyConstraintException if two objects with the same primary key is
     * inserted or if a primary key value already exists in the Realm.
     * @see #copyToRealm(RealmModel)
     */
    public void insert(RealmModel object) {
        checkIfValidAndInTransaction();
        if (object == null) {
            throw new IllegalArgumentException("Null object cannot be inserted into Realm.");
        }
        Map<RealmModel, Long> cache = new HashMap<>();
        configuration.getSchemaMediator().insert(this, object, cache);
    }

    /**
     * Inserts or updates a list of unmanaged RealmObjects. This is generally faster than
     * {@link #copyToRealmOrUpdate(Iterable)} since it doesn't return the inserted elements, and performs minimum
     * allocations and checks.
     * After being inserted any changes to the original objects will not be persisted.
     * <p>
     * Please note:
     * <ul>
     * <li>
     * We don't check if the provided objects are already managed or not, so inserting a managed object might duplicate it.
     * Duplication will only happen if the object doesn't have a primary key. Objects with primary keys will never get duplicated.
     * </li>
     * <li>We don't create (nor return) a managed {@link RealmObject} for each element</li>
     * <li>Copying an object will copy all field values. Any unset field in the object and child objects will be set to their default value if not provided</li>
     * </ul>
     * <p>
     * If you want the managed {@link RealmObject} returned, use {@link #copyToRealm(Iterable)}, otherwise if
     * you have a large number of object this method is generally faster.
     *
     * @param objects RealmObjects to insert.
     * @throws IllegalStateException if the corresponding Realm is closed, called from an incorrect thread or not in a
     * transaction.
     * @throws io.realm.exceptions.RealmPrimaryKeyConstraintException if two objects with the same primary key is
     * inserted or if a primary key value already exists in the Realm.
     * @see #copyToRealmOrUpdate(Iterable)
     */
    public void insertOrUpdate(Collection<? extends RealmModel> objects) {
        checkIfValidAndInTransaction();
        if (objects == null) {
            throw new IllegalArgumentException("Null objects cannot be inserted into Realm.");
        }
        if (objects.isEmpty()) {
            return;
        }
        configuration.getSchemaMediator().insertOrUpdate(this, objects);
    }

    /**
     * Inserts or updates an unmanaged RealmObject. This is generally faster than
     * {@link #copyToRealmOrUpdate(RealmModel)} since it doesn't return the inserted elements, and performs minimum
     * allocations and checks.
     * After being inserted any changes to the original object will not be persisted.
     * <p>
     * Please note:
     * <ul>
     * <li>
     * We don't check if the provided objects are already managed or not, so inserting a managed object might duplicate it.
     * Duplication will only happen if the object doesn't have a primary key. Objects with primary keys will never get duplicated.
     * </li>
     * <li>We don't create (nor return) a managed {@link RealmObject} for each element</li>
     * <li>Copying an object will copy all field values. Any unset field in the object and child objects will be set to their default value if not provided</li>
     * </ul>
     * <p>
     * If you want the managed {@link RealmObject} returned, use {@link #copyToRealm(RealmModel)}, otherwise if
     * you have a large number of object this method is generally faster.
     *
     * @param object RealmObjects to insert.
     * @throws IllegalStateException if the corresponding Realm is closed, called from an incorrect thread or not in a
     * transaction.
     * @see #copyToRealmOrUpdate(RealmModel)
     */
    public void insertOrUpdate(RealmModel object) {
        checkIfValidAndInTransaction();
        if (object == null) {
            throw new IllegalArgumentException("Null object cannot be inserted into Realm.");
        }
        Map<RealmModel, Long> cache = new HashMap<>();
        configuration.getSchemaMediator().insertOrUpdate(this, object, cache);
    }

    /**
     * Updates a list of existing RealmObjects that is identified by their {@link io.realm.annotations.PrimaryKey} or
     * creates a new copy if no existing object could be found. This is a deep copy or update i.e., all referenced
     * objects will be either copied or updated.
     * <p>
     * Please note, copying an object will copy all field values. Any unset field in the objects and child objects will be
     * set to their default value if not provided.
     *
     * @param objects a list of objects to update or copy into Realm.
     * @return a list of all the new or updated RealmObjects.
     * @throws java.lang.IllegalArgumentException if RealmObject is {@code null} or doesn't have a Primary key defined.
     * @see #copyToRealm(Iterable)
     */
    public <E extends RealmModel> List<E> copyToRealmOrUpdate(Iterable<E> objects) {
        if (objects == null) {
            return new ArrayList<>(0);
        }

        Map<RealmModel, RealmObjectProxy> cache = new HashMap<>();
        ArrayList<E> realmObjects = new ArrayList<>();
        for (E object : objects) {
            checkNotNullObject(object);
            realmObjects.add(copyOrUpdate(object, true, cache));
        }

        return realmObjects;
    }

    /**
     * Makes an unmanaged in-memory copy of already persisted RealmObjects. This is a deep copy that will copy all
     * referenced objects.
     * <p>
     * The copied objects are all detached from Realm and they will no longer be automatically updated. This means
     * that the copied objects might contain data that are no longer consistent with other managed Realm objects.
     * <p>
     * *WARNING*: Any changes to copied objects can be merged back into Realm using
     * {@link #copyToRealmOrUpdate(RealmModel)}, but all fields will be overridden, not just those that were changed.
     * This includes references to other objects, and can potentially override changes made by other threads.
     *
     * @param realmObjects RealmObjects to copy.
     * @param <E> type of object.
     * @return an in-memory detached copy of managed RealmObjects.
     * @throws IllegalArgumentException if the RealmObject is no longer accessible or it is a {@link DynamicRealmObject}.
     * @see #copyToRealmOrUpdate(Iterable)
     */
    public <E extends RealmModel> List<E> copyFromRealm(Iterable<E> realmObjects) {
        return copyFromRealm(realmObjects, Integer.MAX_VALUE);
    }

    /**
     * Makes an unmanaged in-memory copy of already persisted RealmObjects. This is a deep copy that will copy all
     * referenced objects up to the defined depth.
     * <p>
     * The copied objects are all detached from Realm and they will no longer be automatically updated. This means
     * that the copied objects might contain data that are no longer consistent with other managed Realm objects.
     * <p>
     * *WARNING*: Any changes to copied objects can be merged back into Realm using
     * {@link #copyToRealmOrUpdate(Iterable)}, but all fields will be overridden, not just those that were changed.
     * This includes references to other objects even though they might be {@code null} due to {@code maxDepth} being
     * reached. This can also potentially override changes made by other threads.
     *
     * @param realmObjects RealmObjects to copy.
     * @param maxDepth limit of the deep copy. All references after this depth will be {@code null}. Starting depth is
     * {@code 0}.
     * @param <E> type of object.
     * @return an in-memory detached copy of the RealmObjects.
     * @throws IllegalArgumentException if {@code maxDepth < 0}, the RealmObject is no longer accessible or it is a
     * {@link DynamicRealmObject}.
     * @see #copyToRealmOrUpdate(Iterable)
     */
    public <E extends RealmModel> List<E> copyFromRealm(Iterable<E> realmObjects, int maxDepth) {
        checkMaxDepth(maxDepth);
        if (realmObjects == null) {
            return new ArrayList<>(0);
        }

        ArrayList<E> unmanagedObjects = new ArrayList<>();
        Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>> listCache = new HashMap<>();
        for (E object : realmObjects) {
            checkValidObjectForDetach(object);
            unmanagedObjects.add(createDetachedCopy(object, maxDepth, listCache));
        }

        return unmanagedObjects;
    }

    /**
     * Makes an unmanaged in-memory copy of an already persisted {@link RealmObject}. This is a deep copy that will copy
     * all referenced objects.
     * <p>
     * The copied object(s) are all detached from Realm and they will no longer be automatically updated. This means
     * that the copied objects might contain data that are no longer consistent with other managed Realm objects.
     * <p>
     * *WARNING*: Any changes to copied objects can be merged back into Realm using
     * {@link #copyToRealmOrUpdate(RealmModel)}, but all fields will be overridden, not just those that were changed.
     * This includes references to other objects, and can potentially override changes made by other threads.
     *
     * @param realmObject {@link RealmObject} to copy.
     * @param <E> type of object.
     * @return an in-memory detached copy of the managed {@link RealmObject}.
     * @throws IllegalArgumentException if the RealmObject is no longer accessible or it is a {@link DynamicRealmObject}.
     * @see #copyToRealmOrUpdate(RealmModel)
     */
    public <E extends RealmModel> E copyFromRealm(E realmObject) {
        return copyFromRealm(realmObject, Integer.MAX_VALUE);
    }

    /**
     * Makes an unmanaged in-memory copy of an already persisted {@link RealmObject}. This is a deep copy that will copy
     * all referenced objects up to the defined depth.
     * <p>
     * The copied object(s) are all detached from Realm and they will no longer be automatically updated. This means
     * that the copied objects might contain data that are no longer consistent with other managed Realm objects.
     * <p>
     * *WARNING*: Any changes to copied objects can be merged back into Realm using
     * {@link #copyToRealmOrUpdate(RealmModel)}, but all fields will be overridden, not just those that were changed.
     * This includes references to other objects even though they might be {@code null} due to {@code maxDepth} being
     * reached. This can also potentially override changes made by other threads.
     *
     * @param realmObject {@link RealmObject} to copy.
     * @param maxDepth limit of the deep copy. All references after this depth will be {@code null}. Starting depth is
     * {@code 0}.
     * @param <E> type of object.
     * @return an in-memory detached copy of the managed {@link RealmObject}.
     * @throws IllegalArgumentException if {@code maxDepth < 0}, the RealmObject is no longer accessible or it is a
     * {@link DynamicRealmObject}.
     * @see #copyToRealmOrUpdate(RealmModel)
     */
    public <E extends RealmModel> E copyFromRealm(E realmObject, int maxDepth) {
        checkMaxDepth(maxDepth);
        checkValidObjectForDetach(realmObject);
        return createDetachedCopy(realmObject, maxDepth, new HashMap<RealmModel, RealmObjectProxy.CacheData<RealmModel>>());
    }

    /**
     * Returns a typed RealmQuery, which can be used to query for specific objects of this type
     *
     * @param clazz the class of the object which is to be queried for.
     * @return a typed RealmQuery, which can be used to query for specific objects of this type.
     * @see io.realm.RealmQuery
     */
    public <E extends RealmModel> RealmQuery<E> where(Class<E> clazz) {
        checkIfValid();
        return RealmQuery.createQuery(this, clazz);
    }

    /**
     * Adds a change listener to the Realm.
     * <p>
     * The listeners will be executed when changes are committed by this or another thread.
     * <p>
     * Realm instances are per thread singletons and cached, so listeners should be
     * removed manually even if calling {@link #close()}. Otherwise there is a
     * risk of memory leaks.
     *
     * @param listener the change listener.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to register a listener from a non-Looper or {@link IntentService} thread.
     * @see io.realm.RealmChangeListener
     * @see #removeChangeListener(RealmChangeListener)
     * @see #removeAllChangeListeners()
     */
    public void addChangeListener(RealmChangeListener<Realm> listener) {
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
    public void removeChangeListener(RealmChangeListener<Realm> listener) {
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
     * Executes a given transaction on the Realm. {@link #beginTransaction()} and {@link #commitTransaction()} will be
     * called automatically. If any exception is thrown during the transaction {@link #cancelTransaction()} will be
     * called instead of {@link #commitTransaction()}.
     *
     * @param transaction the {@link io.realm.Realm.Transaction} to execute.
     * @throws IllegalArgumentException if the {@code transaction} is {@code null}.
     * @throws RealmMigrationNeededException if the latest version contains incompatible schema changes.
     */
    public void executeTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction should not be null");
        }

        beginTransaction();
        try {
            transaction.execute(this);
            commitTransaction();
        } catch (Throwable e) {
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
            final Realm.Transaction.OnSuccess onSuccess,
            final Realm.Transaction.OnError onError) {
        checkIfValid();

        if (transaction == null) {
            throw new IllegalArgumentException("Transaction should not be null");
        }

        // Avoid to call canDeliverNotification() in bg thread.
        final boolean canDeliverNotification = sharedRealm.capabilities.canDeliverNotification();

        // If the user provided a Callback then we have to make sure the current Realm has an events looper to deliver
        // the results.
        if ((onSuccess != null || onError != null)) {
            sharedRealm.capabilities.checkCanDeliverNotification("Callback cannot be delivered on current thread.");
        }

        // We need to use the same configuration to open a background SharedRealm (i.e Realm)
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

                SharedRealm.VersionID versionID = null;
                Throwable exception = null;

                final Realm bgRealm = Realm.getInstance(realmConfiguration);
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
                final SharedRealm.VersionID backgroundVersionID = versionID;
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
     * Deletes all objects of the specified class from the Realm.
     *
     * @param clazz the class which objects should be removed.
     * @throws IllegalStateException if the corresponding Realm is closed or called from an incorrect thread.
     */
    public void delete(Class<? extends RealmModel> clazz) {
        checkIfValid();
        schema.getTable(clazz).clear();
    }


    @SuppressWarnings("unchecked")
    private <E extends RealmModel> E copyOrUpdate(E object, boolean update, Map<RealmModel, RealmObjectProxy> cache) {
        checkIfValid();
        return configuration.getSchemaMediator().copyOrUpdate(this, object, update, cache);
    }

    private <E extends RealmModel> E createDetachedCopy(E object, int maxDepth, Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>> cache) {
        checkIfValid();
        return configuration.getSchemaMediator().createDetachedCopy(object, maxDepth, cache);
    }

    private <E extends RealmModel> void checkNotNullObject(E object) {
        if (object == null) {
            throw new IllegalArgumentException("Null objects cannot be copied into Realm.");
        }
    }

    private void checkHasPrimaryKey(Class<? extends RealmModel> clazz) {
        if (!schema.getTable(clazz).hasPrimaryKey()) {
            throw new IllegalArgumentException("A RealmObject with no @PrimaryKey cannot be updated: " + clazz.toString());
        }
    }

    private void checkMaxDepth(int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be > 0. It was: " + maxDepth);
        }
    }

    private <E extends RealmModel> void checkValidObjectForDetach(E realmObject) {
        if (realmObject == null) {
            throw new IllegalArgumentException("Null objects cannot be copied from Realm.");
        }
        if (!(RealmObject.isManaged(realmObject) && RealmObject.isValid(realmObject))) {
            throw new IllegalArgumentException("Only valid managed objects can be copied from Realm.");
        }
        if (realmObject instanceof DynamicRealmObject) {
            throw new IllegalArgumentException("DynamicRealmObject cannot be copied from Realm.");
        }
    }

    /**
     * Manually triggers the migration associated with a given RealmConfiguration. If Realm is already at the latest
     * version, nothing will happen.
     *
     * @param configuration {@link RealmConfiguration}
     * @throws FileNotFoundException if the Realm file doesn't exist.
     */
    public static void migrateRealm(RealmConfiguration configuration) throws FileNotFoundException {
        migrateRealm(configuration, (RealmMigration) null);
    }

    /**
     * Called when migration needed in the Realm initialization.
     *
     * @param configuration {@link RealmConfiguration}
     * @param cause which triggers this migration.
     * @throws FileNotFoundException if the Realm file doesn't exist.
     */
    private static void migrateRealm(final RealmConfiguration configuration, final RealmMigrationNeededException cause)
            throws FileNotFoundException {
        BaseRealm.migrateRealm(configuration, null, new MigrationCallback() {
            @Override
            public void migrationComplete() {
            }
        }, cause);
    }

    /**
     * Manually triggers a migration on a RealmMigration.
     *
     * @param configuration the{@link RealmConfiguration}.
     * @param migration the {@link RealmMigration} to run on the Realm. This will override any migration set on the
     * configuration.
     * @throws FileNotFoundException if the Realm file doesn't exist.
     */
    public static void migrateRealm(RealmConfiguration configuration, RealmMigration migration)
            throws FileNotFoundException {
        BaseRealm.migrateRealm(configuration, migration, new MigrationCallback() {
            @Override
            public void migrationComplete() {
            }
        }, null);
    }

    /**
     * Deletes the Realm file specified by the given {@link RealmConfiguration} from the filesystem.
     * All Realm instances must be closed before calling this method.
     *
     * @param configuration a {@link RealmConfiguration}.
     * @return {@code false} if a file could not be deleted. The failing file will be logged.
     * @throws IllegalStateException if not all realm instances are closed.
     */
    public static boolean deleteRealm(RealmConfiguration configuration) {
        return BaseRealm.deleteRealm(configuration);
    }

    /**
     * Compacts a Realm file. A Realm file usually contain free/unused space.
     * This method removes this free space and the file size is thereby reduced.
     * Objects within the Realm files are untouched.
     * <p>
     * The file must be closed before this method is called, otherwise {@code false} will be returned.<br>
     * The file system should have free space for at least a copy of the Realm file.<br>
     * The Realm file is left untouched if any file operation fails.<br>
     *
     * @param configuration a {@link RealmConfiguration} pointing to a Realm file.
     * @return {@code true} if successful, {@code false} if any file operation failed.
     * @throws UnsupportedOperationException if Realm is synchronized.
     */
    public static boolean compactRealm(RealmConfiguration configuration) {
        // FIXME: remove this restriction when https://github.com/realm/realm-core/issues/2345 is resolved
        if (configuration.isSyncConfiguration()) {
            throw new UnsupportedOperationException("Compacting is not supported yet on synced Realms. See https://github.com/realm/realm-core/issues/2345");
        }
        return BaseRealm.compactRealm(configuration);
    }

    Table getTable(Class<? extends RealmModel> clazz) {
        return schema.getTable(clazz);
    }

    /**
     * Updates own schema cache.
     *
     * @param globalCacheArray global cache of column indices. If it contains an entry for current
     * schema version, this method only copies the indices information in the entry.
     * @return newly created indices information for current schema version. Or {@code null} if {@code globalCacheArray}
     * already contains the entry for current schema version.
     */
    ColumnIndices updateSchemaCache(ColumnIndices[] globalCacheArray) {
        final long currentSchemaVersion = sharedRealm.getSchemaVersion();
        final long cacheSchemaVersion = schema.getSchemaVersion();
        if (currentSchemaVersion == cacheSchemaVersion) {
            return null;
        }

        ColumnIndices createdGlobalCache = null;
        ColumnIndices cacheForCurrentVersion = RealmCache.findColumnIndices(globalCacheArray,
                currentSchemaVersion);
        if (cacheForCurrentVersion == null) {
            final RealmProxyMediator mediator = getConfiguration().getSchemaMediator();

            // Not found in global cache. create it.
            final Set<Class<? extends RealmModel>> modelClasses = mediator.getModelClasses();
            final Map<Pair<Class<? extends RealmModel>, String>, ColumnInfo> map;
            map = new HashMap<>(modelClasses.size());


            // This code may throw a RealmMigrationNeededException
            //noinspection CaughtExceptionImmediatelyRethrown
            try {
                for (Class<? extends RealmModel> clazz : modelClasses) {
                    final ColumnInfo columnInfo = mediator.validateTable(clazz, sharedRealm, true);
                    String className = Table.getClassNameForTable(mediator.getTableName(clazz));
                    Pair<Class<? extends RealmModel>, String> key = Pair.<Class<? extends RealmModel>, String>create(clazz, className);
                    map.put(key, columnInfo);
                }
            } catch (RealmMigrationNeededException e) {
                throw e;
            }

            cacheForCurrentVersion = createdGlobalCache = new ColumnIndices(currentSchemaVersion, map);
        }
        schema.updateColumnIndices(cacheForCurrentVersion);
        return createdGlobalCache;
    }

    /**
     * Returns the default Realm module. This module contains all Realm classes in the current project, but not those
     * from library or project dependencies. Realm classes in these should be exposed using their own module.
     *
     * @return the default Realm module or {@code null} if no default module exists.
     * @throws RealmException if unable to create an instance of the module.
     * @see io.realm.RealmConfiguration.Builder#modules(Object, Object...)
     */
    public static Object getDefaultModule() {
        String moduleName = "io.realm.DefaultRealmModule";
        Class<?> clazz;
        //noinspection TryWithIdenticalCatches
        try {
            clazz = Class.forName(moduleName);
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InvocationTargetException e) {
            throw new RealmException("Could not create an instance of " + moduleName, e);
        } catch (InstantiationException e) {
            throw new RealmException("Could not create an instance of " + moduleName, e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Could not create an instance of " + moduleName, e);
        }
    }

    /**
     * Returns the current number of open Realm instances across all threads that are using this configuration.
     * This includes both dynamic and normal Realms.
     *
     * @param configuration the {@link io.realm.RealmConfiguration} for the Realm.
     * @return number of open Realm instances across all threads.
     */
    public static int getGlobalInstanceCount(RealmConfiguration configuration) {
        final AtomicInteger globalCount = new AtomicInteger(0);
        RealmCache.invokeWithGlobalRefCount(configuration, new RealmCache.Callback() {
            @Override
            public void onResult(int count) {
                globalCount.set(count);
            }
        });
        return globalCount.get();
    }

    /**
     * Returns the current number of open Realm instances on the thread calling this method. This include both
     * dynamic and normal Realms.
     *
     * @param configuration the {@link io.realm.RealmConfiguration} for the Realm.
     * @return number of open Realm instances across all threads.
     */
    public static int getLocalInstanceCount(RealmConfiguration configuration) {
        return RealmCache.getLocalThreadCount(configuration);
    }

    /**
     * Encapsulates a Realm transaction.
     * <p>
     * Using this class will automatically handle {@link #beginTransaction()} and {@link #commitTransaction()}
     * If any exception is thrown during the transaction {@link #cancelTransaction()} will be called instead of
     * {@link #commitTransaction()}.
     */
    public interface Transaction {
        void execute(Realm realm);

        /**
         * Callback invoked to notify the caller thread.
         */
        class Callback {
            public void onSuccess() {}

            public void onError(Exception ignore) {}
        }

        /**
         * Callback invoked to notify the caller thread about the success of the transaction.
         */
        interface OnSuccess {
            void onSuccess();
        }

        /**
         * Callback invoked to notify the caller thread about error during the transaction.
         * The transaction will be rolled back and the background Realm will be closed before
         * invoking {@link #onError(Throwable)}.
         */
        interface OnError {
            void onError(Throwable error);
        }
    }

    /**
     * {@inheritDoc}
     */
    public static abstract class Callback extends InstanceCallback<Realm> {
        /**
         * {@inheritDoc}
         */
        @Override
        public abstract void onSuccess(Realm realm);

        /**
         * {@inheritDoc}
         */
        @Override
        public void onError(Throwable exception) {
            super.onError(exception);
        }
    }
}
