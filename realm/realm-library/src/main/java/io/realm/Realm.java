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
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import io.reactivex.Flowable;
import io.realm.annotations.Beta;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmFileException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnIndices;
import io.realm.internal.NativeObject;
import io.realm.internal.ObjectServerFacade;
import io.realm.internal.OsObject;
import io.realm.internal.OsObjectSchemaInfo;
import io.realm.internal.OsObjectStore;
import io.realm.internal.OsResults;
import io.realm.internal.OsSchemaInfo;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmCore;
import io.realm.internal.RealmNotifier;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;
import io.realm.internal.TableQuery;
import io.realm.internal.Util;
import io.realm.internal.annotations.ObjectServer;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.log.RealmLog;
import io.realm.sync.permissions.ClassPermissions;
import io.realm.sync.permissions.ClassPrivileges;
import io.realm.sync.permissions.RealmPermissions;
import io.realm.sync.permissions.Role;

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

    private static final Object defaultConfigurationLock = new Object();
    // guarded by `defaultConfigurationLock`
    private static RealmConfiguration defaultConfiguration;
    private final RealmSchema schema;

    /**
     * The constructor is private to enforce the use of the static one.
     *
     * @param cache the {@link RealmCache} associated to this Realm instance.
     * @throws IllegalArgumentException if trying to open an encrypted Realm with the wrong key.
     */
    private Realm(RealmCache cache) {
        super(cache, createExpectedSchemaInfo(cache.getConfiguration().getSchemaMediator()));
        schema = new ImmutableRealmSchema(this,
                new ColumnIndices(configuration.getSchemaMediator(), sharedRealm.getSchemaInfo()));
        // FIXME: This is to work around the different behaviour between the read only Realms in the Object Store and
        // in current java implementation. Opening a read only Realm with some missing schemas is allowed by Object
        // Store and realm-cocoa. In that case, any query based on the missing schema should just return an empty
        // results. Fix this together with https://github.com/realm/realm-java/issues/2953
        if (configuration.isReadOnly()) {
            RealmProxyMediator mediator = configuration.getSchemaMediator();
            Set<Class<? extends RealmModel>> classes = mediator.getModelClasses();
            for (Class<? extends RealmModel> clazz  : classes) {
                String tableName = Table.getTableNameForClass(mediator.getSimpleClassName(clazz));
                if (!sharedRealm.hasTable(tableName)) {
                    sharedRealm.close();
                    throw new RealmMigrationNeededException(configuration.getPath(),
                            String.format(Locale.US, "Cannot open the read only Realm. '%s' is missing.",
                                    Table.getClassNameForTable(tableName)));
                }
            }
        }
    }

    private Realm(OsSharedRealm sharedRealm) {
        super(sharedRealm);
        schema = new ImmutableRealmSchema(this,
                new ColumnIndices(configuration.getSchemaMediator(), sharedRealm.getSchemaInfo()));
    }

    private static OsSchemaInfo createExpectedSchemaInfo(RealmProxyMediator mediator) {
        return new OsSchemaInfo(mediator.getExpectedObjectSchemaInfoMap().values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flowable<Realm> asFlowable() {
        return configuration.getRxFactory().from(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        checkIfValid();
        for (RealmObjectSchema clazz : schema.getAll()) {
            if (!clazz.getClassName().startsWith("__") && clazz.getTable().size() > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the schema for this Realm. The schema is immutable.
     * Any attempt to modify it will result in an {@link UnsupportedOperationException}.
     * <p>
     * The schema can only be modified using {@link DynamicRealm#getSchema()} or through an migration.
     *
     * @return The {@link RealmSchema} for this Realm.
     */
    @Override
    public RealmSchema getSchema() {
        return schema;
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
            //noinspection ConstantConditions
            if (context == null) {
                throw new IllegalArgumentException("Non-null context required.");
            }
            checkFilesDirAvailable(context);
            RealmCore.loadLibrary(context);
            setDefaultConfiguration(new RealmConfiguration.Builder(context).build());
            ObjectServerFacade.getSyncFacadeIfPossible().init(context);
            if (context.getApplicationContext() != null) {
                BaseRealm.applicationContext = context.getApplicationContext();
            } else {
                BaseRealm.applicationContext = context;
            }
            OsSharedRealm.initialize(new File(context.getFilesDir(), ".realm.temp"));
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
        RealmConfiguration configuration = getDefaultConfiguration();
        if (configuration == null) {
            if (BaseRealm.applicationContext == null) {
                throw new IllegalStateException("Call `Realm.init(Context)` before calling this method.");
            } else {
                throw new IllegalStateException("Set default configuration by using `Realm.setDefaultConfiguration(RealmConfiguration)`.");
            }
        }
        return RealmCache.createRealmOrGetFromCache(configuration, Realm.class);
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
        //noinspection ConstantConditions
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
        //noinspection ConstantConditions
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
        //noinspection ConstantConditions
        if (configuration == null) {
            throw new IllegalArgumentException("A non-null RealmConfiguration must be provided");
        }
        synchronized (defaultConfigurationLock) {
            defaultConfiguration = configuration;
        }
    }

    /**
     * Returns the default configuration for {@link #getDefaultInstance()}.
     *
     * @return default configuration object or {@code null} if no default configuration is specified.
     */
    @Nullable
    public static RealmConfiguration getDefaultConfiguration() {
        synchronized (defaultConfigurationLock) {
            return defaultConfiguration;
        }
    }

    /**
     * Removes the current default configuration (if any). Any further calls to {@link #getDefaultInstance()} will
     * fail until a new default configuration has been set using {@link #setDefaultConfiguration(RealmConfiguration)}.
     */
    public static void removeDefaultConfiguration() {
        synchronized (defaultConfigurationLock) {
            defaultConfiguration = null;
        }
    }

    /**
     * Creates a {@link Realm} instance without checking the existence in the {@link RealmCache}.
     *
     * @param cache the {@link RealmCache} where to create the realm in.
     * @return a {@link Realm} instance.
     */
    static Realm createInstance(RealmCache cache) {
        return new Realm(cache);
    }

    /**
     * Creates a {@code Realm} instance directly from a {@link OsSharedRealm}. This {@code Realm} doesn't need to be
     * closed.
     */
    static Realm createInstance(OsSharedRealm sharedRealm) {
        return new Realm(sharedRealm);
    }

    /**
     * Creates a Realm object for each object in a JSON array. This must be done within a transaction.
     * <p>
     * JSON properties with unknown properties will be ignored. If a {@link RealmObject} field is not present in the
     * JSON object the {@link RealmObject} field will be set to the default value for that type.
     *
     * <p>
     * This method currently does not support value list field.
     *
     * @param clazz type of Realm objects to create.
     * @param json an array where each JSONObject must map to the specified class.
     * @throws RealmException if mapping from JSON fails.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     */
    public <E extends RealmModel> void createAllFromJson(Class<E> clazz, JSONArray json) {
        //noinspection ConstantConditions
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
     * <p>
     * This method currently does not support value list field.
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
        //noinspection ConstantConditions
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
     * <p>
     * This method currently does not support value list field.
     *
     * @param clazz type of Realm objects to create.
     * @param json the JSON array as a String where each object can map to the specified class.
     * @throws RealmException if mapping from JSON fails.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     */
    public <E extends RealmModel> void createAllFromJson(Class<E> clazz, String json) {
        //noinspection ConstantConditions
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
     * <p>
     * This method currently does not support value list field.
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
        //noinspection ConstantConditions
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
     * <p>
     * This method currently does not support value list field.
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
        //noinspection ConstantConditions
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
     * <p>
     * This method currently does not support value list field.
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
        //noinspection ConstantConditions
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
     * <p>
     * This method currently does not support value list field.
     *
     * @param clazz type of Realm object to create.
     * @param json the JSONObject with object data.
     * @return created object or {@code null} if no JSON data was provided.
     * @throws RealmException if the mapping from JSON fails.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     * @see #createOrUpdateObjectFromJson(Class, org.json.JSONObject)
     */
    @Nullable
    public <E extends RealmModel> E createObjectFromJson(Class<E> clazz, JSONObject json) {
        //noinspection ConstantConditions
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
     * <p>
     * This method currently does not support value list field.
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
        //noinspection ConstantConditions
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
     * <p>
     * This method currently does not support value list field.
     *
     * @param clazz type of Realm object to create.
     * @param json the JSON string with object data.
     * @return created object or {@code null} if JSON string was empty or null.
     * @throws RealmException if mapping to json failed.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     */
    @Nullable
    public <E extends RealmModel> E createObjectFromJson(Class<E> clazz, String json) {
        //noinspection ConstantConditions
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
     * <p>
     * This method currently does not support value list field.
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
        //noinspection ConstantConditions
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
     * <p>
     * This method currently does not support value list field.
     *
     * @param clazz type of Realm object to create.
     * @param inputStream the JSON object data as a InputStream.
     * @return created object or {@code null} if JSON string was empty or null.
     * @throws RealmException if the mapping from JSON failed.
     * @throws IllegalArgumentException if the JSON object doesn't have a primary key property but the corresponding
     * {@link RealmObjectSchema} has a {@link io.realm.annotations.PrimaryKey} defined.
     * @throws IOException if something went wrong with the input stream.
     */
    @Nullable
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmModel> E createObjectFromJson(Class<E> clazz, InputStream inputStream) throws IOException {
        //noinspection ConstantConditions
        if (clazz == null || inputStream == null) {
            return null;
        }
        checkIfValid();
        E realmObject;

        if (OsObjectStore.getPrimaryKeyForObject(
                sharedRealm, configuration.getSchemaMediator().getSimpleClassName(clazz)) != null) {
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
     * <p>
     * This method currently does not support value list field.
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
        //noinspection ConstantConditions
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
        if (OsObjectStore.getPrimaryKeyForObject(
                sharedRealm, configuration.getSchemaMediator().getSimpleClassName(clazz)) != null) {
            throw new RealmException(String.format(Locale.US, "'%s' has a primary key, use" +
                    " 'createObject(Class<E>, Object)' instead.", table.getClassName()));
        }
        return configuration.getSchemaMediator().newInstance(clazz, this,
                OsObject.create(table),
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
    public <E extends RealmModel> E createObject(Class<E> clazz, @Nullable Object primaryKeyValue) {
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
            @Nullable Object primaryKeyValue,
            boolean acceptDefaultValue,
            List<String> excludeFields) {
        Table table = schema.getTable(clazz);

        return configuration.getSchemaMediator().newInstance(clazz, this,
                OsObject.createWithPrimaryKey(table, primaryKeyValue),
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
        //noinspection ConstantConditions
        if (objects == null) {
            return new ArrayList<>();
        }
        ArrayList realmObjects;
        if (objects instanceof Collection) {
            realmObjects = new ArrayList<>(((Collection) objects).size());
        } else {
            realmObjects = new ArrayList<>();
        }
        Map<RealmModel, RealmObjectProxy> cache = new HashMap<>();
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
        //noinspection ConstantConditions
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
        //noinspection ConstantConditions
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
        //noinspection ConstantConditions
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
        //noinspection ConstantConditions
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
        //noinspection ConstantConditions
        if (objects == null) {
            return new ArrayList<>(0);
        }

        ArrayList realmObjects;
        if (objects instanceof Collection) {
            realmObjects = new ArrayList<>(((Collection) objects).size());
        } else {
            realmObjects = new ArrayList<>();
        }
        Map<RealmModel, RealmObjectProxy> cache = new HashMap<>();
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
        //noinspection ConstantConditions
        if (realmObjects == null) {
            return new ArrayList<>(0);
        }

        ArrayList unmanagedObjects;
        if (realmObjects instanceof Collection) {
            unmanagedObjects = new ArrayList<>(((Collection) realmObjects).size());
        } else {
            unmanagedObjects = new ArrayList<>();
        }
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
        //noinspection ConstantConditions
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
     * Deletes all objects of the specified class from the Realm.
     *
     * @param clazz the class which objects should be removed.
     * @throws IllegalStateException if the corresponding Realm is a query-based synchronized Realm, is
     * closed or called from an incorrect thread.
     */
    public void delete(Class<? extends RealmModel> clazz) {
        checkIfValid();
        if (sharedRealm.isPartial()) {
            throw new IllegalStateException(DELETE_NOT_SUPPORTED_UNDER_PARTIAL_SYNC);
        }
        schema.getTable(clazz).clear(sharedRealm.isPartial());
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
        //noinspection ConstantConditions
        if (object == null) {
            throw new IllegalArgumentException("Null objects cannot be copied into Realm.");
        }
    }

    private void checkHasPrimaryKey(Class<? extends RealmModel> clazz) {
        String className = configuration.getSchemaMediator().getSimpleClassName(clazz);
        OsObjectSchemaInfo objectSchemaInfo = sharedRealm.getSchemaInfo().getObjectSchemaInfo(className);

        if (objectSchemaInfo.getPrimaryKeyProperty() == null) {
            throw new IllegalArgumentException("A RealmObject with no @PrimaryKey cannot be updated: " + clazz.toString());
        }
    }

    private void checkMaxDepth(int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be > 0. It was: " + maxDepth);
        }
    }

    private <E extends RealmModel> void checkValidObjectForDetach(E realmObject) {
        //noinspection ConstantConditions
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
        migrateRealm(configuration, null);
    }

    /**
     * Manually triggers a migration on a RealmMigration.
     *
     * @param configuration the{@link RealmConfiguration}.
     * @param migration the {@link RealmMigration} to run on the Realm. This will override any migration set on the
     * configuration.
     * @throws FileNotFoundException if the Realm file doesn't exist.
     */
    public static void migrateRealm(RealmConfiguration configuration, @Nullable RealmMigration migration)
            throws FileNotFoundException {
        BaseRealm.migrateRealm(configuration, migration);
    }

    /**
     * Deletes the Realm file along with the related temporary files specified by the given {@link RealmConfiguration}
     * from the filesystem. Temporary file with ".lock" extension won't be deleted.
     * <p>
     * All Realm instances must be closed before calling this method.
     * <p>
     * WARNING: For synchronized Realm, there is a chance that an internal Realm instance on the background thread is
     * not closed even all the user controlled Realm instances are closed. This will result an
     * {@code IllegalStateException}. See issue https://github.com/realm/realm-java/issues/5416 .
     *
     * @param configuration a {@link RealmConfiguration}.
     * @return {@code false} if the Realm file could not be deleted. Temporary files deletion failure won't impact
     * the return value. All of the failing file deletions will be logged.
     * @throws IllegalStateException if there are Realm instances opened on other threads or other processes.
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
     */
    public static boolean compactRealm(RealmConfiguration configuration) {
        return BaseRealm.compactRealm(configuration);
    }

    /**
     * Cancel a named subscription that was created by calling {@link RealmQuery#findAllAsync(String)}.
     * If after this, some objects are no longer part of any active subscription they will be removed
     * locally from the device (but not on the server).
     *
     * The effect of unsubscribing is not immediate. The local Realm must coordinate with the Object
     * Server before this can happen. A successful callback just indicate that the request was
     * succesfully enqueued and any data will be removed as soon as possible. When the data is
     * actually removed locally, a standard change notification will be triggered and from the
     * perspective of the device it will look like the data was deleted.
     *
     * @param subscriptionName name of the subscription to remove
     * @param callback callback reporting back if the intent to unsubscribe was enqueued successfully or failed.
     * @return a {@link RealmAsyncTask} representing a cancellable task.
     * @throws IllegalArgumentException if no {@code subscriptionName} or {@code callback} was provided.
     * @throws IllegalStateException if called on a non-looper thread.
     * @throws UnsupportedOperationException if the Realm is not a query-based synchronized Realm.
     */
    @Beta
    public RealmAsyncTask unsubscribeAsync(String subscriptionName, Realm.UnsubscribeCallback callback) {
        if (Util.isEmptyString(subscriptionName)) {
            throw new IllegalArgumentException("Non-empty 'subscriptionName' required.");
        }
        //noinspection ConstantConditions
        if (callback == null) {
            throw new IllegalArgumentException("'callback' required.");
        }
        sharedRealm.capabilities.checkCanDeliverNotification("This method is only available from a Looper thread.");
        if (!ObjectServerFacade.getSyncFacadeIfPossible().isPartialRealm(configuration)) {
            throw new UnsupportedOperationException("Realm is fully synchronized Realm. This method is only available when using query-based synchronization: " + configuration.getPath());
        }

        return executeTransactionAsync(new Transaction() {
            @Override
            public void execute(Realm realm) {

                // Need to manually run a dynamic query here.
                // TODO Add support for DynamicRealm.executeTransactionAsync()
                Table table = realm.sharedRealm.getTable("class___ResultSets");
                TableQuery query = table.where()
                        .equalTo(new long[]{table.getColumnIndex("name")}, new long[]{NativeObject.NULLPTR}, subscriptionName);

                OsResults result = OsResults.createFromQuery(realm.sharedRealm, query);
                long count = result.size();
                if (count == 0) {
                    throw new IllegalArgumentException("No active subscription named '"+ subscriptionName +"' exists.");
                }
                if (count > 1) {
                    RealmLog.warn("Multiple subscriptions named '" + subscriptionName +  "' exists. This should not be possible. They will all be deleted");
                }
                result.clear();
            }
        }, new Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                callback.onSuccess(subscriptionName);
            }
        }, new Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                callback.onError(subscriptionName, error);
            }
        });
    }

    /**
     * Returns all permissions associated with the current Realm. Attach a change listener
     * using {@link RealmPermissions#addChangeListener(RealmChangeListener)} to be notified about
     * any future changes.
     *
     * @return all permissions for the current Realm.
     */
    @Beta
    @ObjectServer
    public RealmPermissions getPermissions() {
        checkIfValid();
        return where(RealmPermissions.class).findFirst();
    }

    /**
     * Returns all {@link Role} objects available in this Realm. Attach a change listener
     * using {@link Role#addChangeListener(RealmChangeListener)} to be notified about
     * any future changes.
     *
     * @return all roles available in the current Realm.
     */
    @Beta
    @ObjectServer
    public RealmResults<Role> getRoles() {
        checkIfValid();
        return where(Role.class).sort("name").findAll();
    }

    /**
     * Returns the privileges granted the current user for the given class.
     *
     * @param clazz class to get privileges for.
     * @return the privileges granted the current user for the given class.
     */
    @Beta
    @ObjectServer
    public ClassPrivileges getPrivileges(Class<? extends RealmModel> clazz) {
        checkIfValid();
        //noinspection ConstantConditions
        if (clazz == null) {
            throw new IllegalArgumentException("Non-null 'clazz' required.");
        }
        String className = configuration.getSchemaMediator().getSimpleClassName(clazz);
        return new ClassPrivileges(sharedRealm.getClassPrivileges(className));
    }

    /**
     * Returns all permissions associated with the given class. Attach a change listener
     * using {@link ClassPermissions#addChangeListener(RealmChangeListener)} to be notified about
     * any future changes.
     *
     * @param clazz class to receive permissions for.
     * @return the permissions for the given class or {@code null} if no permissions where found.
     * @throws RealmException if the class is not part of this Realms schema.
     */
    @Beta
    @ObjectServer
    public ClassPermissions getPermissions(Class<? extends RealmModel> clazz) {
        checkIfValid();
        //noinspection ConstantConditions
        if (clazz == null) {
            throw new IllegalArgumentException("Non-null 'clazz' required.");
        }
        return where(ClassPermissions.class)
                .equalTo("name", configuration.getSchemaMediator().getSimpleClassName(clazz))
                .findFirst();
    }

    Table getTable(Class<? extends RealmModel> clazz) {
        return schema.getTable(clazz);
    }

    /**
     * Returns the default Realm module. This module contains all Realm classes in the current project, but not those
     * from library or project dependencies. Realm classes in these should be exposed using their own module.
     *
     * @return the default Realm module or {@code null} if no default module exists.
     * @throws RealmException if unable to create an instance of the module.
     * @see io.realm.RealmConfiguration.Builder#modules(Object, Object...)
     */
    @Nullable
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
     * Returns the current number of open Realm instances across all threads in current process that are using this
     * configuration. This includes both dynamic and normal Realms.
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
     * @return number of open Realm instances on the caller thread.
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
     * Interface used when canceling query-based sync subscriptions.
     *
     * @see #unsubscribeAsync(String, UnsubscribeCallback)
     */
    public interface UnsubscribeCallback {
        /**
         * Callback invoked when the request to unsubscribe was succesfully enqueued.
         *
         * @param subscriptionName subscription that was canceled.
         */
        void onSuccess(String subscriptionName);

        /**
         * Callback invoked if an error happened while trying to unsubscribe.
         *
         * @param subscriptionName subscription on which the error occurred.
         * @param error cause of error.
         */
        void onError(String subscriptionName, Throwable error);
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
