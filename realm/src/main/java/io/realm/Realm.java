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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmIOException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnIndices;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Row;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;
import io.realm.internal.TableView;
import io.realm.internal.android.DebugAndroidLogger;
import io.realm.internal.android.ReleaseAndroidLogger;
import io.realm.internal.log.RealmLog;
import io.realm.internal.migration.SetVersionNumberMigration;
import io.realm.internal.modules.FilterableMediator;


/**
 * The Realm class is the storage and transactional manager of your object persistent store. It
 * is in charge of creating instances of your RealmObjects. Objects within a Realm can be queried
 * and read at any time. Creating, modifying, and deleting objects must be done while inside a
 * transaction. See {@link #beginTransaction()}
 * <p>
 * The transactions ensure that multiple instances (on multiple threads) can access the same
 * objects in a consistent state with full ACID guarantees.
 * <p>
 * It is important to remember to call the {@link #close()} method when done with a Realm
 * instance. Failing to do so can lead to {@link java.lang.OutOfMemoryError} as the native
 * resources cannot be freed.
 * <p>
 * Realm instances cannot be used across different threads. This means that you have to open an
 * instance on each thread you want to use Realm. Realm instances are cached automatically per
 * thread using reference counting, so as long as the reference count doesn't reach zero, calling
 * {@link #getInstance(android.content.Context)} will just return the cached Realm and should be
 * considered a lightweight operation.
 * <p>
 * For the UI thread this means that opening and closing Realms should occur in either
 * onCreate/onDestroy or onStart/onStop.
 * <p>
 * Realm instances coordinate their state across threads using the {@link android.os.Handler}
 * mechanism. This also means that Realm instances on threads without a {@link android.os.Looper}
 * cannot receive updates unless {@link #refresh()} is manually called.
 * <p>
 * A standard pattern for working with Realm in Android activities can be seen below:
 * <p>
 * <pre>
 * public class RealmActivity extends Activity {
 *
 *   private Realm realm;
 *
 *   \@Override
 *   protected void onCreate(Bundle savedInstanceState) {
 *     super.onCreate(savedInstanceState);
 *     setContentView(R.layout.layout_main);
 *     realm = Realm.getInstance(this);
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
 * @see <a href="http://en.wikipedia.org/wiki/ACID">ACID</a>
 * @see <a href="https://github.com/realm/realm-java/tree/master/examples">Examples using Realm</a>
 */
public final class Realm implements Closeable {
    public static final String DEFAULT_REALM_NAME = "default.realm";

    protected static final ThreadLocal<Map<String, Realm>> realmsCache = new ThreadLocal<Map<String, Realm>>() {
        @SuppressLint("UseSparseArrays")
        @Override
        protected Map<String, Realm> initialValue() {
            return new HashMap<String, Realm>(); // On Android we could use SparseArray<Realm> which is faster,
                                                  // but incompatible with Java
        }
    };
    private static final ThreadLocal<Map<String, Integer>> referenceCount = new ThreadLocal<Map<String,Integer>>() {
        @SuppressLint("UseSparseArrays")
        @Override
        protected Map<String, Integer> initialValue() {
            return new HashMap<String, Integer>();
        }
    };
    // Maps ids to a boolean set to true if the Realm is open. This is only needed by deleteRealmFile
    private static final Map<String, AtomicInteger> openRealms = new ConcurrentHashMap<String, AtomicInteger>();
    private static final int REALM_CHANGED = 14930352; // Hopefully it won't clash with other message IDs.
    protected static final Map<Handler, String> handlers = new ConcurrentHashMap<Handler, String>();

    private static RealmConfiguration defaultConfiguration;

    private static final String APT_NOT_EXECUTED_MESSAGE = "Annotation processor may not have been executed.";
    private static final String INCORRECT_THREAD_MESSAGE = "Realm access from incorrect thread. Realm objects can only be accessed on the thread they where created.";
    private static final String CLOSED_REALM_MESSAGE = "This Realm instance has already been closed, making it unusable.";
    private static final String DIFFERENT_KEY_MESSAGE = "Wrong key used to decrypt Realm.";

    @SuppressWarnings("UnusedDeclaration")
    private static SharedGroup.Durability defaultDurability = SharedGroup.Durability.FULL;
    private boolean autoRefresh;
    private Handler handler;

    private final byte[] key;
    private final String canonicalPath;
    private SharedGroup sharedGroup;
    private final ImplicitTransaction transaction;

    private final List<WeakReference<RealmChangeListener>> changeListeners = new ArrayList<WeakReference<RealmChangeListener>>();
    private static RealmProxyMediator proxyMediator = getDefaultMediator();

    private static final long UNVERSIONED = -1;

    final ColumnIndices columnIndices = new ColumnIndices();

    static {
        RealmLog.add(BuildConfig.DEBUG ? new DebugAndroidLogger() : new ReleaseAndroidLogger());
    }

    protected void checkIfValid() {
        // Check if the Realm instance has been closed
        if (sharedGroup == null) {
            throw new IllegalStateException(CLOSED_REALM_MESSAGE);
        }

        // Check if we are in the right thread
        Realm currentRealm = realmsCache.get().get(canonicalPath);
        if (currentRealm != this) {
            throw new IllegalStateException(INCORRECT_THREAD_MESSAGE);
        }
    }

    // The constructor in private to enforce the use of the static one
    private Realm(String canonicalPath, byte[] key, boolean autoRefresh) {
        this.sharedGroup = new SharedGroup(canonicalPath, true, key);
        this.transaction = sharedGroup.beginImplicitTransaction();
        this.canonicalPath = canonicalPath;
        this.key = key;
        setAutoRefresh(autoRefresh);
    }

    @Override
    protected void finalize() throws Throwable {
        if (sharedGroup != null) {
            RealmLog.w("Remember to call close() on all Realm instances. " +
                            "Realm " + canonicalPath + " is being finalized without being closed, " +
                            "this can lead to running out of native memory."
            );
        }
        super.finalize();
    }

    /**
     * Closes the Realm instance and all its resources.
     * <p>
     * It's important to always remember to close Realm instances when you're done with it in order
     * not to leak memory, file descriptors or grow the size of Realm file out of measure.
     */
    @Override
    public void close() {
        Map<String, Integer> localRefCount = referenceCount.get();
        Integer references = localRefCount.get(canonicalPath);
        if (references == null) {
            references = 0;
        }
        if (sharedGroup != null && references == 1) {
            realmsCache.get().remove(canonicalPath);
            sharedGroup.close();
            sharedGroup = null;
            AtomicInteger counter = openRealms.get(canonicalPath);
            if (counter.decrementAndGet() == 0) {
                openRealms.remove(canonicalPath);
            }
        }

        int refCount = references - 1;
        if (refCount < 0) {
            RealmLog.w("Calling close() on a Realm that is already closed: " + canonicalPath);
        }
        localRefCount.put(canonicalPath, Math.max(0, refCount));

        if (handler != null && refCount <= 0) {
            removeHandler(handler);
        }
    }

    private void removeHandler(Handler handler) {
        handler.removeCallbacksAndMessages(null);
        handlers.remove(handler);
    }

    private static RealmProxyMediator getDefaultMediator() {
        Class<?> clazz;
        try {
            clazz = Class.forName("io.realm.DefaultRealmModuleMediator");
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return (RealmProxyMediator) constructor.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RealmException("Could not find io.realm.DefaultRealmModuleMediator", e);
        } catch (InvocationTargetException e) {
            throw new RealmException("Could not create an instance of io.realm.DefaultRealmModuleMediator", e);
        } catch (InstantiationException e) {
            throw new RealmException("Could not create an instance of io.realm.DefaultRealmModuleMediator", e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Could not create an instance of io.realm.DefaultRealmModuleMediator", e);
        }
    }

    private class RealmCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == REALM_CHANGED) {
                transaction.advanceRead();
                sendNotifications();
            }
            return true;
        }
    }

    /**
     * Retrieve the auto-refresh status of the Realm instance.
     * @return the auto-refresh status
     */
    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    /**
     * Set the auto-refresh status of the Realm instance.
     * <p>
     * Auto-refresh is a feature that enables automatic update of the current realm instance and all its derived objects
     * (RealmResults and RealmObjects instances) when a commit is performed on a Realm acting on the same file in another thread.
     * This feature is only available if the realm instance lives is a {@link android.os.Looper} enabled thread.
     *
     * @param autoRefresh true will turn auto-refresh on, false will turn it off.
     * @throws java.lang.IllegalStateException if trying to enable auto-refresh in a thread without Looper.
     */
    public void setAutoRefresh(boolean autoRefresh) {
        if (autoRefresh && Looper.myLooper() == null) {
            throw new IllegalStateException("Cannot set auto-refresh in a Thread without a Looper");
        }

        if (autoRefresh && !this.autoRefresh) { // Switch it on
            handler = new Handler(new RealmCallback());
            handlers.put(handler, canonicalPath);
        } else if (!autoRefresh && this.autoRefresh && handler != null) { // Switch it off
            removeHandler(handler);
        }
        this.autoRefresh = autoRefresh;
    }

    // Public because of migrations
    public Table getTable(Class<? extends RealmObject> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        if (!superclass.equals(RealmObject.class)) {
            clazz = (Class<? extends RealmObject>) superclass;
        }
        return transaction.getTable(proxyMediator.getTableName(clazz));
    }

    /**
     * Realm static constructor for the default realm "default.realm".
     * {@link #close()} must be called when you are done using the Realm instance.
     * <p>
     * It sets auto-refresh on if the current thread has a Looper, off otherwise.
     *
     * @param context an Android {@link android.content.Context}
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     *                                       must be migrated
     * @throws RealmIOException              Error when accessing underlying file
     * @throws RealmException                Other errors
     */
    @Deprecated
    public static Realm getInstance(Context context) {
        return Realm.getInstance(context, DEFAULT_REALM_NAME);
    }

    /**
     * Realm static constructor.
     * {@link #close()} must be called when you are done using the Realm instance.
     * <p>
     * It sets auto-refresh on if the current thread has a Looper, off otherwise.
     *
     * @param context  an Android {@link android.content.Context}
     * @param fileName the name of the file to save the Realm to
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     *                                       must be migrated
     * @throws RealmIOException              Error when accessing underlying file
     * @throws RealmException                Other errors
     */
    @Deprecated
    public static Realm getInstance(Context context, String fileName) {
        return Realm.getInstance(context, fileName, null);
    }

    /**
     * Realm static constructor.
     * {@link #close()} must be called when you are done using the Realm instance.
     * <p>
     * It sets auto-refresh on if the current thread has a Looper, off otherwise.
     *
     * @param context an Android {@link android.content.Context}
     * @param key     a 64-byte encryption key
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     *                                       must be migrated
     * @throws RealmIOException              Error when accessing underlying file
     * @throws RealmException                Other errors
     */
    @Deprecated
    public static Realm getInstance(Context context, byte[] key) {
        return Realm.getInstance(context, DEFAULT_REALM_NAME, key);
    }

    /**
     * Realm static constructor.
     * {@link #close()} must be called when you are done using the Realm instance.
     * <p>
     * It sets auto-refresh on if the current thread has a Looper, off otherwise.
     *
     * @param context an Android {@link android.content.Context}
     * @param key     a 64-byte encryption key
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     *                                       must be migrated
     * @throws RealmIOException              Error when accessing underlying file
     * @throws RealmException                Other errors
     */
    @Deprecated
    public static Realm getInstance(Context context, String fileName, byte[] key) {
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder(context).name(fileName);
        if (key != null) {
            builder.encryptionKey(key);
        }
        return create(builder.build());
    }

    /**
     * Realm static constructor.
     * {@link #close()} must be called when you are done using the Realm instance.
     * <p>
     * It sets auto-refresh on if the current thread has a Looper, off otherwise.
     *
     * @param writeableFolder a File object representing a writeable folder
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     *                                       must be migrated
     * @throws RealmIOException              Error when accessing underlying file
     * @throws RealmException                Other errors
     */
    @Deprecated
    @SuppressWarnings("UnusedDeclaration")
    public static Realm getInstance(File writeableFolder) {
        return create(new RealmConfiguration.Builder(writeableFolder)
                        .name(DEFAULT_REALM_NAME)
                        .build()
        );
    }

    /**
     * Realm static constructor.
     * {@link #close()}
     * It sets auto-refresh on if the current thread has a Looper, off otherwise.
     *
     * @param writeableFolder a File object representing a writeable folder
     * @param fileName the name of the Realm file
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     *                                       must be migrated
     * @throws RealmIOException              Error when accessing underlying file
     * @throws RealmException                Other errors
     */
    @Deprecated
    @SuppressWarnings("UnusedDeclaration")
    public static Realm getInstance(File writeableFolder, String fileName) {
        return create(new RealmConfiguration.Builder(writeableFolder)
                        .name(fileName)
                        .build()
        );
    }

    /**
     * Realm static constructor.
     * {@link #close()} must be called when you are done using the Realm instance.
     * <p>
     * It sets auto-refresh on if the current thread has a Looper, off otherwise.
     *
     * @param writeableFolder a File object representing a writeable folder
     * @param key     a 64-byte encryption key
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     *                                       must be migrated
     * @throws RealmIOException              Error when accessing underlying file
     * @throws RealmException                Other errors
     */
    @Deprecated
    @SuppressWarnings("UnusedDeclaration")
    public static Realm getInstance(File writeableFolder, byte[] key) {
        return create(new RealmConfiguration.Builder(writeableFolder)
                        .name(DEFAULT_REALM_NAME)
                        .encryptionKey(key)
                        .build()
        );
    }

    /**
     * Realm static constructor.
     * {@link #close()} must be called when you are done using the Realm instance.
     * <p>
     * It sets auto-refresh on if the current thread has a Looper, off otherwise.
     *
     * @param writeableFolder a File object representing a writeable folder
     * @param fileName the name of the Realm file
     * @param key     a 64-byte encryption key
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     *                                       must be migrated
     * @throws RealmIOException              Error when accessing underlying file
     * @throws RealmException                Other errors
     */
    @Deprecated
    @SuppressWarnings("UnusedDeclaration")
    public static Realm getInstance(File writeableFolder, String fileName, byte[] key) {
        return create(new RealmConfiguration.Builder(writeableFolder)
                        .name(fileName)
                        .encryptionKey(key)
                        .build()
        );
    }

    /**
     * Realm static constructor that returns the Realm instance defined by the {@link io.realm.RealmConfiguration} set
     * by {@link #setDefaultConfiguration(RealmConfiguration)}
     *
     * @return an instance of the Realm class
     *
     * @throws java.lang.NullPointerException If no default configuration has been defined.
     * @throws RealmMigrationNeededException If no migration has been provided by the default configuration and the
     * model classes or version has has changed so a migration is required.
     */
    public static Realm getDefaultInstance() {
        if (defaultConfiguration == null) {
            throw new NullPointerException("No default RealmConfiguration was found. Call setDefaultConfiguration() first");
        }
        return create(defaultConfiguration);
    }

    /**
     * Realm static constructor that returns the Realm instance defined by provided {@link io.realm.RealmConfiguration}
     *
     * @return an instance of the Realm class
     *
     * @throws RealmMigrationNeededException If no migration has been provided by the configuration and the
     * model classes or version has has changed so a migration is required.
     * @see {@link io.realm.RealmConfiguration} for details on how to configure a Realm.
     */
    public static Realm getInstance(RealmConfiguration configuration) {
        if (configuration == null) {
            throw new NullPointerException("A non-null RealmConfiguration must be provided");
        }
        return create(configuration);
    }

    /**
     * Set the {@link io.realm.RealmConfiguration} used when calling {@link #getDefaultInstance()}
     *
     * @param configuration RealmConfiguration to use as the default configuration
     * @see {@link io.realm.RealmConfiguration} for details on how to configure a Realm
     */
    public static void setDefaultConfiguration(RealmConfiguration configuration) {
        if (configuration == null) {
            throw new NullPointerException("A non-null RealmConfiguration must be provided");
        }
        defaultConfiguration = configuration;
    }

    private static Realm create(RealmConfiguration config) {
        boolean autoRefresh = Looper.myLooper() != null;
        try {
            return createAndValidate(config, true, autoRefresh);
        } catch (RealmMigrationNeededException e) {
            if (config.shouldDeleteRealmIfMigrationNeeded()) {
                deleteRealm(config);
            } else {
                migrateRealm(config);
            }

            return createAndValidate(config, true, autoRefresh);
        }
    }

    private static synchronized Realm createAndValidate(RealmConfiguration config, boolean validateSchema, boolean autoRefresh) {
        setSchema(config.getSchema());
        byte[] key = config.getEncryptionKey();
        String canonicalPath = config.getPath();
        Map<String, Integer> localRefCount = referenceCount.get();
        Integer references = localRefCount.get(canonicalPath);
        if (references == null) {
            references = 0;
        }
        if (references == 0) {
            AtomicInteger counter = openRealms.get(canonicalPath);
            if (counter == null) {
                if (config.shouldDeleteRealmBeforeOpening()) {
                    deleteRealm(config);
                }
            }
        }
        Map<String, Realm> realms = realmsCache.get();
        Realm realm = realms.get(canonicalPath);
        if (realm != null) {
            if (!Arrays.equals(realm.key, key)) {
                throw new IllegalStateException(DIFFERENT_KEY_MESSAGE);
            }
            localRefCount.put(canonicalPath, references + 1);
            return realm;
        }

        // Create new Realm and cache it. All exception code paths must close the Realm otherwise we risk serving
        // faulty cache data.
        realm = new Realm(canonicalPath, key, autoRefresh);
        realms.put(canonicalPath, realm);
        realmsCache.set(realms);
        localRefCount.put(canonicalPath, references + 1);

        // Increment global reference counter
        if (references == 0) {
            AtomicInteger counter = openRealms.get(canonicalPath);
            if (counter == null) {
                openRealms.put(canonicalPath, new AtomicInteger(1));
            } else {
                counter.incrementAndGet();
            }
        }

        // Check versions of Realm
        long currentVersion = realm.getVersion();
        long requiredVersion = config.getSchemaVersion();
        if (currentVersion != UNVERSIONED && currentVersion < requiredVersion && validateSchema) {
            realm.close();
            throw new RealmMigrationNeededException(canonicalPath, String.format("Realm on disc need to migrate from v%s to v%s", currentVersion, requiredVersion));
        }
        if (currentVersion != UNVERSIONED && requiredVersion < currentVersion && validateSchema) {
            realm.close();
            throw new IllegalArgumentException(String.format("Realm on disc is newer than the one specified: v%s vs. v%s", currentVersion, requiredVersion));
		}

        // Initialize Realm schema if needed
        if (validateSchema) {
            try {
                initializeRealm(realm, config);
            } catch (RuntimeException e) {
                realm.close();
                throw e;
            }
        }

        return realm;
    }

    @SuppressWarnings("unchecked")
    private static void initializeRealm(Realm realm, RealmConfiguration config) {
        long version = realm.getVersion();
        boolean commitNeeded = false;
        try {
            realm.beginTransaction();
            if (version == UNVERSIONED) {
                commitNeeded = true;
                realm.setVersion(config.getSchemaVersion());
            }
            for (Class<? extends RealmObject> modelClass : proxyMediator.getModelClasses()) {
                // Create and validate table
                if (version == UNVERSIONED) {
                    proxyMediator.createTable(modelClass, realm.transaction);
                }
                proxyMediator.validateTable(modelClass, realm.transaction);
                realm.columnIndices.addClass(modelClass, proxyMediator.getColumnIndices(modelClass));
            }
        } finally {
            if (commitNeeded) {
                realm.commitTransaction();
            } else {
                realm.cancelTransaction();
            }
        }
    }

    /**
     * Create a Realm object for each object in a JSON array. This must be done within a transaction.
     * JSON properties with a null value will map to the default value for the data type in Realm
     * and unknown properties will be ignored.
     *
     * @param clazz Type of Realm objects to create.
     * @param json  Array where each JSONObject must map to the specified class.
     *
     * @throws RealmException if mapping from JSON fails.
     */
    public <E extends RealmObject> void createAllFromJson(Class<E> clazz, JSONArray json) {
        if (clazz == null || json == null) {
            return;
        }

        for (int i = 0; i < json.length(); i++) {
            try {
                proxyMediator.createOrUpdateUsingJsonObject(clazz, this, json.getJSONObject(i), false);
            } catch (Exception e) {
                throw new RealmException("Could not map Json", e);
            }
        }
    }

    /**
     * Tries to update a list of existing objects identified by their primary key with new JSON data. If an existing
     * object could not be found in the Realm, a new object will be created. This must happen within a transaction.
     *
     * @param clazz Type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json  Array with object data.
     *
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     * @see #createAllFromJson(Class, org.json.JSONArray)
     */
    public <E extends RealmObject> void createOrUpdateAllFromJson(Class<E> clazz, JSONArray json) {
        if (clazz == null || json == null) {
            return;
        }
        checkHasPrimaryKey(clazz);
        for (int i = 0; i < json.length(); i++) {
            try {
                proxyMediator.createOrUpdateUsingJsonObject(clazz, this, json.getJSONObject(i), true);
            } catch (Exception e) {
                throw new RealmException("Could not map Json", e);
            }
        }
    }

    /**
     * Create a Realm object for each object in a JSON array. This must be done within a transaction.
     * JSON properties with a null value will map to the default value for the data type in Realm
     * and unknown properties will be ignored.
     *
     * @param clazz Type of Realm objects to create.
     * @param json  JSON array as a String where each object can map to the specified class.
     *
     * @throws RealmException if mapping from JSON fails.
     */
    public <E extends RealmObject> void createAllFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return;
        }

        JSONArray arr;
        try {
            arr = new JSONArray(json);
        } catch (Exception e) {
            throw new RealmException("Could not create JSON array from string", e);
        }

        createAllFromJson(clazz, arr);
    }

    /**
     * Tries to update a list of existing objects identified by their primary key with new JSON data. If an existing
     * object could not be found in the Realm, a new object will be created. This must happen within a transaction.
     *
     * @param clazz Type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json  String with an array of JSON objects.
     *
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     * @see #createAllFromJson(Class, String)
     */
    public <E extends RealmObject> void createOrUpdateAllFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return;
        }
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
     * Create a Realm object for each object in a JSON array. This must be done within a transaction.
     * JSON properties with a null value will map to the default value for the data type in Realm
     * and unknown properties will be ignored.
     *
     * @param clazz         Type of Realm objects created.
     * @param inputStream   JSON array as a InputStream. All objects in the array must be of the
     *                      specified class.
     *
     * @throws RealmException if mapping from JSON fails.
     * @throws IOException if something was wrong with the input stream.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmObject> void createAllFromJson(Class<E> clazz, InputStream inputStream) throws IOException {
        if (clazz == null || inputStream == null) {
            return;
        }

        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                proxyMediator.createUsingJsonStream(clazz, this, reader);
            }
            reader.endArray();
        } finally {
            reader.close();
        }
    }

    /**
     * Tries to update a list of existing objects identified by their primary key with new JSON data. If an existing
     * object could not be found in the Realm, a new object will be created. This must happen within a transaction.
     *
     * @param clazz Type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param in    InputStream with a list of object data in JSON format.
     *
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     * @see #createOrUpdateAllFromJson(Class, java.io.InputStream)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmObject> void createOrUpdateAllFromJson(Class<E> clazz, InputStream in) throws IOException {
        if (clazz == null || in == null) {
            return;
        }
        checkHasPrimaryKey(clazz);

        // As we need the primary key value we have to first parse the entire input stream as in the general
        // case that value might be the last property :(
        Scanner scanner = null;
        try {
            scanner = getFullStringScanner(in);
            JSONArray json = new JSONArray(scanner.next());
            for (int i = 0; i < json.length(); i++) {
                proxyMediator.createOrUpdateUsingJsonObject(clazz, this, json.getJSONObject(i), true);
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
     * Create a Realm object prefilled with data from a JSON object. This must be done inside a
     * transaction. JSON properties with a null value will map to the default value for the data
     * type in Realm and unknown properties will be ignored.
     *
     * @param clazz Type of Realm object to create.
     * @param json  JSONObject with object data.
     * @return Created object or null if no json data was provided.
     *
     * @throws RealmException if the mapping from JSON fails.
     * @see #createOrUpdateObjectFromJson(Class, org.json.JSONObject)
     */
    public <E extends RealmObject> E createObjectFromJson(Class<E> clazz, JSONObject json) {
        if (clazz == null || json == null) {
            return null;
        }

        try {
            return proxyMediator.createOrUpdateUsingJsonObject(clazz, this, json, false);
        } catch (Exception e) {
            throw new RealmException("Could not map Json", e);
        }
    }

    /**
     * Tries to update an existing object defined by its primary key with new JSON data. If no existing object could be
     * found a new object will be saved in the Realm. This must happen within a transaction.
     *
     * @param clazz Type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json  {@link org.json.JSONObject} with object data.
     * @return Created or updated {@link io.realm.RealmObject}.
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     * @see #createObjectFromJson(Class, org.json.JSONObject)
     */
    public <E extends RealmObject> E createOrUpdateObjectFromJson(Class<E> clazz, JSONObject json) {
        if (clazz == null || json == null) {
            return null;
        }
        checkHasPrimaryKey(clazz);
        try {
            return proxyMediator.createOrUpdateUsingJsonObject(clazz, this, json, true);
        } catch (JSONException e) {
            throw new RealmException("Could not map Json", e);
        }
    }

    /**
     * Create a Realm object prefilled with data from a JSON object. This must be done inside a
     * transaction. JSON properties with a null value will map to the default value for the data
     * type in Realm and unknown properties will be ignored.
     *
     * @param clazz Type of Realm object to create.
     * @param json  JSON string with object data.
     * @return Created object or null if json string was empty or null.
     *
     * @throws RealmException if mapping to json failed.
     */
    public <E extends RealmObject> E createObjectFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return null;
        }

        JSONObject obj;
        try {
            obj = new JSONObject(json);
        } catch (Exception e) {
            throw new RealmException("Could not create Json object from string", e);
        }

        return createObjectFromJson(clazz, obj);
    }

    /**
     * Tries to update an existing object defined by its primary key with new JSON data. If no existing object could be
     * found a new object will be saved in the Realm. This must happen within a transaction.
     *
     * @param clazz Type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json  String with object data in JSON format.
     * @return Created or updated {@link io.realm.RealmObject}.
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     *
     * @see #createObjectFromJson(Class, String)
     */
    public <E extends RealmObject> E createOrUpdateObjectFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return null;
        }
        checkHasPrimaryKey(clazz);

        JSONObject obj;
        try {
            obj = new JSONObject(json);
        } catch (Exception e) {
            throw new RealmException("Could not create Json object from string", e);
        }

        return createOrUpdateObjectFromJson(clazz, obj);
    }

    /**
     * Create a Realm object pre-filled with data from a JSON object. This must be done inside a
     * transaction. JSON properties with a null value will map to the default value for the data
     * type in Realm and unknown properties will be ignored.
     *
     * @param clazz         Type of Realm object to create.
     * @param inputStream   JSON object data as a InputStream.
     * @return Created object or null if json string was empty or null.
     *
     * @throws RealmException if the mapping from JSON failed.
     * @throws IOException if something was wrong with the input stream.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmObject> E createObjectFromJson(Class<E> clazz, InputStream inputStream) throws IOException {
        if (clazz == null || inputStream == null) {
            return null;
        }

        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return proxyMediator.createUsingJsonStream(clazz, this, reader);
        } finally {
            reader.close();
        }
    }

    /**
     * Tries to update an existing object defined by its primary key with new JSON data. If no existing object could be
     * found a new object will be saved in the Realm. This must happen within a transaction.
     *
     * @param clazz Type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param in    Inputstream with object data in JSON format.
     * @return Created or updated {@link io.realm.RealmObject}.
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     * @see #createObjectFromJson(Class, java.io.InputStream)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmObject> E createOrUpdateObjectFromJson(Class<E> clazz, InputStream in) throws IOException {
        if (clazz == null || in == null) {
            return null;
        }
        checkHasPrimaryKey(clazz);

        // As we need the primary key value we have to first parse the entire input stream as in the general
        // case that value might be the last property :(
        Scanner scanner = null;
        try {
            scanner = getFullStringScanner(in);
            JSONObject json = new JSONObject(scanner.next());
            return proxyMediator.createOrUpdateUsingJsonObject(clazz, this, json, true);
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
     * Write a compacted copy of the Realm to the given destination File.
     * <p>
     * The destination file cannot already exist.
     * <p>
     * Note that if this is called from within a write transaction it writes the
     * current data, and not the data as it was when the last write transaction was committed.
     *
     * @param destination File to save the Realm to
     * @throws java.io.IOException if any write operation fails
     */
    public void writeCopyTo(File destination) throws IOException {
        writeEncryptedCopyTo(destination, null);
    }

    /**
     * Write a compacted and encrypted copy of the Realm to the given destination File.
     * <p>
     * The destination file cannot already exist.
     * <p>
     * Note that if this is called from within a write transaction it writes the
     * current data, and not the data as it was when the last write transaction was committed.
     * <p>
     * @param destination File to save the Realm to
     * @throws java.io.IOException if any write operation fails
     */
    public void writeEncryptedCopyTo(File destination, byte[] key) throws IOException {
        if (destination == null) {
            throw new IllegalArgumentException("The destination argument cannot be null");
        }
        checkIfValid();
        transaction.writeToFile(destination, key);
    }


    /**
     * Instantiates and adds a new object to the realm
     *
     * @param clazz The Class of the object to create
     * @return The new object
     * @throws RealmException An object could not be created
     */
    public <E extends RealmObject> E createObject(Class<E> clazz) {
        Table table = getTable(clazz);
        long rowIndex = table.addEmptyRow();
        return get(clazz, rowIndex);
    }

    /**
     * Creates a new object inside the Realm with the Primary key value initially set.
     * If the value violates the primary key constraint, no object will be added and a
     * {@link RealmException} will be thrown.
     *
     * @param clazz The Class of the object to create
     * @param primaryKeyValue Value for the primary key field.
     * @return The new object
     * @throws {@link RealmException} if object could not be created.
     */
    <E extends RealmObject> E createObject(Class<E> clazz, Object primaryKeyValue) {
        Table table = getTable(clazz);
        long rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue);
        return get(clazz, rowIndex);
    }

    void remove(Class<? extends RealmObject> clazz, long objectIndex) {
        getTable(clazz).moveLastOver(objectIndex);
    }

    <E extends RealmObject> E get(Class<E> clazz, long rowIndex) {
        Table table = getTable(clazz);
        Row row = table.getRow(rowIndex);
        E result = proxyMediator.newInstance(clazz);
        result.row = row;
        result.realm = this;
        return result;
    }

    /**
     * Copies a RealmObject to the Realm instance and returns the copy. Any further changes to the original RealmObject
     * will not be reflected in the Realm copy.
     *
     * @param object {@link io.realm.RealmObject} to copy to the Realm.
     * @return A managed RealmObject with its properties backed by the Realm.
     *
     * @throws java.lang.IllegalArgumentException if RealmObject is {@code null}.
     */
    public <E extends RealmObject> E copyToRealm(E object) {
        checkNotNullObject(object);
        return copyOrUpdate(object, false);
    }

    /**
     * Updates an existing RealmObject that is identified by the same {@link io.realm.annotations.PrimaryKey} or create
     * a new copy if no existing object could be found.
     *
     * @param object    {@link io.realm.RealmObject} to copy or update.
     * @return The new or updated RealmObject with all its properties backed by the Realm.
     *
     * @throws java.lang.IllegalArgumentException if RealmObject is {@code null} or doesn't have a Primary key defined.
     * @see #copyToRealm(RealmObject)
     */
    public <E extends RealmObject> E copyToRealmOrUpdate(E object) {
        checkNotNullObject(object);
        checkHasPrimaryKey(object.getClass());
        return copyOrUpdate(object, true);
    }

    /**
     * Copies a collection of RealmObjects to the Realm instance and returns their copy. Any further changes
     * to the original RealmObjects will not be reflected in the Realm copies.
     *
     * @param objects RealmObjects to copy to the Realm.
     * @return A list of the the converted RealmObjects that all has their properties managed by the Realm.
     *
     * @throws io.realm.exceptions.RealmException if any of the objects has already been added to Realm.
     * @throws java.lang.IllegalArgumentException if any of the elements in the input collection is {@code null}.
     */
    public <E extends RealmObject> List<E> copyToRealm(Iterable<E> objects) {
        if (objects == null) {
            return new ArrayList<E>();
        }

        ArrayList<E> realmObjects = new ArrayList<E>();
        for (E object : objects) {
            realmObjects.add(copyToRealm(object));
        }

        return realmObjects;
    }

    /**
     * Updates a list of existing RealmObjects that is identified by their {@link io.realm.annotations.PrimaryKey} or create a
     * new copy if no existing object could be found.
     *
     * @param objects   List of objects to update or copy into Realm.
     * @return A list of all the new or updated RealmObjects.
     *
     * @throws java.lang.IllegalArgumentException if RealmObject is {@code null} or doesn't have a Primary key defined.
     * @see #copyToRealm(Iterable)
     */
    public <E extends RealmObject> List<E> copyToRealmOrUpdate(Iterable<E> objects) {
        if (objects == null) {
            return new ArrayList<E>();
        }

        ArrayList<E> realmObjects = new ArrayList<E>();
        for (E object : objects) {
            realmObjects.add(copyToRealmOrUpdate(object));
        }

        return realmObjects;
    }

    private static String getProxyClassName(String simpleClassName) {
        return "io.realm." + simpleClassName + "RealmProxy";
    }

    boolean contains(Class<? extends RealmObject> clazz) {
        return proxyMediator.getModelClasses().contains(clazz);
    }

    /**
     * Returns a typed RealmQuery, which can be used to query for specific objects of this type
     *
     * @param clazz The class of the object which is to be queried for
     * @return A typed RealmQuery, which can be used to query for specific objects of this type
     * @throws java.lang.RuntimeException Any other error
     * @see io.realm.RealmQuery
     */
    public <E extends RealmObject> RealmQuery<E> where(Class<E> clazz) {
        checkIfValid();
        return new RealmQuery<E>(this, clazz);
    }

    /**
     * Get all objects of a specific Class. If no objects exist, the returned RealmResults will not
     * be null. The RealmResults.size() to check the number of objects instead.
     *
     * @param clazz the Class to get objects of
     * @return A RealmResult list containing the objects
     * @throws java.lang.RuntimeException Any other error
     * @see io.realm.RealmResults
     */
    public <E extends RealmObject> RealmResults<E> allObjects(Class<E> clazz) {
        return where(clazz).findAll();
    }

    /**
     * Get all objects of a specific Class sorted by a field.  If no objects exist, the returned
     * RealmResults will not be null. The RealmResults.size() to check the number of objects instead.
     *
     * @param clazz the Class to get objects of.
     * @param fieldName the field name to sort by.
     * @param sortAscending sort ascending if SORT_ORDER_ASCENDING, sort descending if SORT_ORDER_DESCENDING.
     * @return A sorted RealmResults containing the objects.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String fieldName,
                                                                    boolean sortAscending) {
        checkIfValid();
        Table table = getTable(clazz);
        TableView.Order order = sortAscending ? TableView.Order.ascending : TableView.Order.descending;
        long columnIndex = columnIndices.getColumnIndex(clazz, fieldName);
        if (columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }

        TableView tableView = table.getSortedView(columnIndex, order);
        return new RealmResults<E>(this, tableView, clazz);
    }


    /**
     * Get all objects of a specific class sorted by two specific field names.  If no objects exist,
     * the returned RealmResults will not be null. The RealmResults.size() to check the number of
     * objects instead.
     *
     * @param clazz the class ti get objects of.
     * @param fieldName1 first field name to sort by.
     * @param sortAscending1 sort order for first field.
     * @param fieldName2 second field name to sort by.
     * @param sortAscending2 sort order for second field.
     * @return A sorted RealmResults containing the objects.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String fieldName1,
                                                               boolean sortAscending1, String fieldName2,
                                                                    boolean sortAscending2) {
        return allObjectsSorted(clazz, new String[]{fieldName1, fieldName2}, new boolean[]{sortAscending1,
                sortAscending2});
    }

    /**
     * Get all objects of a specific class sorted by two specific field names.  If no objects exist,
     * the returned RealmResults will not be null. The RealmResults.size() to check the number of
     * objects instead.
     *
     * @param clazz the class ti get objects of.
     * @param fieldName1 first field name to sort by.
     * @param sortAscending1 sort order for first field.
     * @param fieldName2 second field name to sort by.
     * @param sortAscending2 sort order for second field.
     * @param fieldName3 third field name to sort by.
     * @param sortAscending3 sort order for third field.
     * @return A sorted RealmResults containing the objects.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String fieldName1,
                                                                    boolean sortAscending1,
                                                                    String fieldName2, boolean sortAscending2,
                                                                    String fieldName3, boolean sortAscending3) {
        return allObjectsSorted(clazz, new String[] {fieldName1, fieldName2, fieldName3},
                new boolean[] {sortAscending1, sortAscending2, sortAscending3});
    }

    /**
     * Get all objects of a specific Class sorted by multiple fields.  If no objects exist, the
     * returned RealmResults will not be null. The RealmResults.size() to check the number of
     * objects instead.
     *
     * @param clazz the Class to get objects of.
     * @param sortAscending sort ascending if SORT_ORDER_ASCENDING, sort descending if SORT_ORDER_DESCENDING.
     * @param fieldNames an array of fieldnames to sort objects by.
     *        The objects are first sorted by fieldNames[0], then by fieldNames[1] and so forth.
     * @return A sorted RealmResults containing the objects.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    @SuppressWarnings("unchecked")
    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String fieldNames[],
                                                                    boolean sortAscending[]) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("fieldNames must be provided.");
        } else if (sortAscending == null) {
            throw new IllegalArgumentException("sortAscending must be provided.");
        }

        // Convert field names to column indices
        Table table = this.getTable(clazz);
        long columnIndices[] = new long[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
            long columnIndex = table.getColumnIndex(fieldName);
            if (columnIndex == -1) {
                throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
            }
            columnIndices[i] = columnIndex;
        }

        // Perform sort
        TableView tableView = table.getSortedView(columnIndices, sortAscending);
        return new RealmResults(this, tableView, clazz);
    }

    // Notifications

    /**
     * Add a change listener to the Realm
     *
     * @param listener the change listener
     * @see io.realm.RealmChangeListener
     */
    public void addChangeListener(RealmChangeListener listener) {
        checkIfValid();
        changeListeners.add(new WeakReference<RealmChangeListener>(listener));
    }

    /**
     * Remove the specified change listener
     *
     * @param listener the change listener to be removed
     * @see io.realm.RealmChangeListener
     */
    public void removeChangeListener(RealmChangeListener listener) {
        checkIfValid();
        changeListeners.remove(new WeakReference<RealmChangeListener>(listener));
    }

    /**
     * Remove all user-defined change listeners
     *
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        checkIfValid();
        changeListeners.clear();
    }

    void sendNotifications() {
        Iterator<WeakReference<RealmChangeListener>> iterator = changeListeners.iterator();
        while (iterator.hasNext()) {
            RealmChangeListener listener = iterator.next().get();
            if (listener == null) {
                iterator.remove();
            } else {
                listener.onChange();
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    boolean hasChanged() {
        return sharedGroup.hasChanged();
    }

    /**
     * Transactions
     */

    /**
     * Refresh the Realm instance and all the RealmResults and RealmObjects instances coming from it
     */
    @SuppressWarnings("UnusedDeclaration")
    public void refresh() {
        checkIfValid();
        transaction.advanceRead();
    }

    /**
     * Starts a write transaction, this must be closed with {@link io.realm.Realm#commitTransaction()}
     * or aborted by {@link io.realm.Realm#cancelTransaction()}. Write transactions are used to
     * atomically create, update and delete objects within a realm.
     * <br>
     * Before beginning the write transaction, {@link io.realm.Realm#beginTransaction()} updates the
     * realm in the case of pending updates from other threads.
     * <br>
     * Notice: it is not possible to nest write transactions. If you start a write
     * transaction within a write transaction an exception is thrown.
     * <br>
     * @throws java.lang.IllegalStateException If already in a write transaction or incorrect thread.
     *
     */
    public void beginTransaction() {
        checkIfValid();
        transaction.promoteToWrite();
    }

    /**
     * All changes since {@link io.realm.Realm#beginTransaction()} are persisted to disk and the
     * realm reverts back to being read-only. An event is sent to notify all other realm instances
     * that a change has occurred. When the event is received, the other realms will get their
     * objects and {@link io.realm.RealmResults} updated to reflect
     * the changes from this commit.
     *
     * @throws java.lang.IllegalStateException If the write transaction is in an invalid state or incorrect thread.
     */
    public void commitTransaction() {
        checkIfValid();
        transaction.commitAndContinueAsRead();

        for (Map.Entry<Handler, String> handlerIntegerEntry : handlers.entrySet()) {
            Handler handler = handlerIntegerEntry.getKey();
            String realmPath = handlerIntegerEntry.getValue();
            if (
                    realmPath.equals(canonicalPath)                       // It's the right realm
                            && !handler.hasMessages(REALM_CHANGED)       // The right message
                            && handler.getLooper().getThread().isAlive() // The receiving thread is alive
                            && !handler.equals(this.handler)             // Don't notify yourself
                    ) {
                handler.sendEmptyMessage(REALM_CHANGED);
            }
        }
        sendNotifications();
    }

    /**
     * Revert all writes (created, updated, or deleted objects) made in the current write
     * transaction and end the transaction.
     * <br>
     * The realm reverts back to read-only.
     * <br>
     * Calling this when not in a write transaction will throw an exception.
     *
     * @throws java.lang.IllegalStateException    If the write transaction is an invalid state,
     *                                             not in a write transaction or incorrect thread.
     */
    public void cancelTransaction() {
        checkIfValid();
        transaction.rollbackAndContinueAsRead();
    }

    /**
     * Executes a given transaction on the Realm. {@link #beginTransaction()} and
     * {@link #commitTransaction()} will be called automatically. If any exception is thrown
     * during the transaction {@link #cancelTransaction()} will be called instead of {@link #commitTransaction()}.
     *
     * @param transaction {@link io.realm.Realm.Transaction} to execute.
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
     * Remove all objects of the specified class.
     *
     * @param clazz The class which objects should be removed
     * @throws java.lang.RuntimeException Any other error
     */
    public void clear(Class<? extends RealmObject> clazz) {
        getTable(clazz).clear();
    }

    // Returns the Handler for this Realm on the calling thread
    Handler getHandler() {
        for (Map.Entry<Handler, String> entry : handlers.entrySet()) {
            if (entry.getValue().equals(canonicalPath)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // package protected so unit tests can access it
    long getVersion() {
        if (!transaction.hasTable("metadata")) {
            return UNVERSIONED;
        }
        Table metadataTable = transaction.getTable("metadata");
        return metadataTable.getLong(0, 0);
    }

    // package protected so unit tests can access it
    void setVersion(long version) {
        Table metadataTable = transaction.getTable("metadata");
        if (metadataTable.getColumnCount() == 0) {
            metadataTable.addColumn(ColumnType.INTEGER, "version");
            metadataTable.addEmptyRow();
        }
        metadataTable.setLong(0, 0, version);
    }

    @SuppressWarnings("unchecked")
    private <E extends RealmObject> Class<? extends RealmObject> getRealmClassFromObject(E object) {
        if (object.realm != null) {
            // This is already a proxy object, get superclass instead
            // INVARIANT: We don't support subclasses yet so super class is always correct type
            return (Class<? extends RealmObject>) object.getClass().getSuperclass();
        } else {
            return object.getClass();
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends RealmObject> E copyOrUpdate(E object, boolean update) {
        return proxyMediator.copyOrUpdate(this, object, update, new HashMap<RealmObject, RealmObjectProxy>());
    }

    private <E extends RealmObject> void checkNotNullObject(E object) {
        if (object == null) {
            throw new IllegalArgumentException("Null objects cannot be copied into Realm.");
        }
    }

    private <E extends RealmObject> void checkHasPrimaryKey(E object) {
        Class<? extends RealmObject> objectClass = object.getClass();
        if (!getTable(objectClass).hasPrimaryKey()) {
            throw new IllegalArgumentException("RealmObject has no @PrimaryKey defined: " + objectClass.getSimpleName().toString());
        }
    }

    private void checkHasPrimaryKey(Class<? extends RealmObject> clazz) {
        if (!getTable(clazz).hasPrimaryKey()) {
            throw new IllegalArgumentException("A RealmObject with no @PrimaryKey cannot be updated: " + clazz.toString());
        }
    }

    @Deprecated
    public static void migrateRealmAtPath(String realmPath, RealmMigration migration) {
        migrateRealmAtPath(realmPath, null, migration, true);
    }

    @Deprecated
    public static void migrateRealmAtPath(String realmPath, byte[] key, RealmMigration migration) {
        migrateRealmAtPath(realmPath, key, migration, true);
    }

    @Deprecated
    public static void migrateRealmAtPath(String realmPath, RealmMigration migration, boolean autoRefresh) {
        migrateRealmAtPath(realmPath, null, migration, autoRefresh);
    }

    @Deprecated
    public static synchronized void migrateRealmAtPath(String realmPath, byte[] key, RealmMigration migration,
                                                       boolean autoUpdate) {
        File file = new File(realmPath);
        RealmConfiguration.Builder configuration = new RealmConfiguration.Builder(file.getParentFile())
                .name(file.getName())
                .migration(migration);
        if (key != null) {
            configuration.encryptionKey(key);
        }
        migrateRealm(configuration.build());
    }

    /**
     * Manually trigger the migration associated with a given RealmConfiguration. If Realm is already at the
     * latest version, nothing will happen.
     * @param configuration
     */
    public static synchronized void migrateRealm(RealmConfiguration configuration) {
        if (configuration.getMigration() == null) {
            migrateRealm(configuration, new SetVersionNumberMigration(configuration.getSchemaVersion()));
        } else {
            migrateRealm(configuration, configuration.getMigration());
        }
    }

    /**
     * Manually trigger a migration on a RealmMigration.
     *
     * @param configuration {@link RealmConfiguration}
     * @param migration {@link RealmMigration} to run on the Realm.
     */
    public static void migrateRealm(RealmConfiguration configuration, RealmMigration migration) {
        if (migration == null) {
            return;
        }

        Realm realm = Realm.createAndValidate(configuration, false, Looper.myLooper() != null);
        realm.beginTransaction();
        realm.setVersion(migration.execute(realm, realm.getVersion()));
        realm.commitTransaction();
        realm.close();

        realmsCache.remove();
    }





    /**
     * Deprecated: Use {@link #deleteRealm(RealmConfiguration)} instead.
     *
     * Delete the Realm file from the filesystem for the default Realm (named "default.realm").
     * The Realm must be unused and closed before calling this method.
     * WARNING: Your Realm must not be open (typically when your app launch).
     *
     * @param context an Android {@link android.content.Context}.
     * @return false if a file could not be deleted. The failing file will be logged.
     * @see io.realm.Realm#clear(Class)
     */
    @Deprecated
    public static boolean deleteRealmFile(Context context) {
        return deleteRealmFile(context, DEFAULT_REALM_NAME);
    }

    /**
     * Deprecated: Use {@link #deleteRealm(RealmConfiguration)} instead.
     *
     * Delete the Realm file from the filesystem for a custom named Realm.
     * The Realm must be unused and closed before calling this method.
     *
     * @param context  an Android {@link android.content.Context}.
     * @param fileName the name of the custom Realm (i.e. "myCustomRealm.realm").
     * @return false if a file could not be deleted. The failing file will be logged.
     */
    @Deprecated
    public static boolean deleteRealmFile(Context context, String fileName) {
        return deleteRealm(new RealmConfiguration.Builder(context)
                        .name(fileName)
                        .build()
        );
    }

    /**
     * Delete the Realm file specified by the given {@link RealmConfiguration} from the filesystem.
     * The realm must be unused and closed before calling this method.
     *
     * @param configuration A {@link RealmConfiguration}
     * @return false if a file could not be deleted. The failing file will be logged.
     */
    public static synchronized boolean deleteRealm(RealmConfiguration configuration) {
        boolean result = true;

        String id = configuration.getPath();
        AtomicInteger counter = openRealms.get(id);
        if (counter != null && counter.get() > 0) {
            throw new IllegalStateException("It's not allowed to delete the file associated with an open Realm. " +
                    "Remember to close() all the instances of the Realm before deleting its file.");
        }

        List<File> filesToDelete = Arrays.asList(new File(configuration.getPath()),
                new File(configuration.getRealmFolder(), configuration.getRealmFileName() + ".lock"),
                new File(configuration.getRealmFolder(), configuration.getRealmFileName() + ".lock_a"),
                new File(configuration.getRealmFolder(), configuration.getRealmFileName() + ".lock_b"),
                new File(configuration.getRealmFolder(), configuration.getRealmFileName() + ".log"));
        for (File fileToDelete : filesToDelete) {
            if (fileToDelete.exists()) {
                boolean deleteResult = fileToDelete.delete();
                if (!deleteResult) {
                    result = false;
                    RealmLog.w("Could not delete the file " + fileToDelete);
                }
            }
        }
        return result;
    }


    /**
     * Deprecated: Use {@link #compactRealm(RealmConfiguration)} instead.
      *
     * Compact a Realm file. A Realm file usually contain free/unused space.
     * This method removes this free space and the file size is thereby reduced.
     * Objects within the Realm files are untouched.
     * <p>
     * The file must be closed before this method is called.<br>
     * The file system should have free space for at least a copy of the Realm file.<br>
     * The realm file is left untouched if any file operation fails.<br>
     * Currently it is not possible to compact an encrypted Realm.<br>
     *
     * @param context an Android {@link android.content.Context}
     * @param fileName the name of the file to compact
     * @return true if successful, false if any file operation failed
     *
     * @throws java.lang.IllegalStateException if trying to compact a Realm that is already open.
     */
    @Deprecated
    public static synchronized boolean compactRealmFile(Context context, String fileName) {
        return compactRealm(new RealmConfiguration.Builder(context).name(fileName).build());
    }

    /**
     * Deprecated: Use {@link #compactRealm(RealmConfiguration)} instead.
     *
     * Compact a realm file. A realm file usually contain free/unused space.
     * This method removes this free space and the file size is thereby reduced.
     * Objects within the realm files are untouched.
     * <p>
     * The file must be closed before this method is called.<br>
     * The file system should have free space for at least a copy of the realm file.<br>
     * The realm file is left untouched if any file operation fails.<br>
     *
     * @param context an Android {@link android.content.Context}
     * @return true if successful, false if any file operation failed
     *
     * @throws java.lang.IllegalStateException if trying to compact a Realm that is already open.
     */
    @Deprecated
    public static boolean compactRealmFile(Context context) {
        return compactRealm(new RealmConfiguration.Builder(context).build());
    }

    /**
     * Compact a realm file. A realm file usually contain free/unused space.
     * This method removes this free space and the file size is thereby reduced.
     * Objects within the realm files are untouched.
     * <p>
     * The file must be closed before this method is called.<br>
     * The file system should have free space for at least a copy of the realm file.<br>
     * The realm file is left untouched if any file operation fails.<br>
     *
     * @param configuration a {@link RealmConfiguration} pointing to a Realm file.
     * @return true if successful, false if any file operation failed
     *
     * @throws java.lang.IllegalStateException if trying to compact a Realm that is already open.
     */
    public static boolean compactRealm(RealmConfiguration configuration) {
        if (configuration.getEncryptionKey() != null) {
            throw new IllegalArgumentException("Cannot currently compact an encrypted Realm.");
        }

        String canonicalPath = configuration.getPath();
        AtomicInteger openInstances = openRealms.get(canonicalPath);
        if (openInstances != null && openInstances.get() > 0) {
            throw new IllegalStateException("Cannot compact an open Realm");
        }
        SharedGroup sharedGroup = null;
        boolean result = false;
        try {
            sharedGroup = new SharedGroup(canonicalPath, false, configuration.getEncryptionKey());
            result = sharedGroup.compact();
        } finally {
            if (sharedGroup != null) {
                sharedGroup.close();
            }
        }
        return result;
    }

    /**
     * Returns the canonical path to where this Realm is persisted on disk.
     *
     * @return The canonical path to the Realm file.
     * @see File#getCanonicalPath()
     */
    public String getPath() {
        return canonicalPath;
    }

    /**
     * Override the standard behavior of all classes extended RealmObject being part of the schema.
     * Use this method to define the schema as only the classes given here.
     *
     * This class must be called before calling {@link #getInstance(android.content.Context)}
     *ø
     * If {@code null} is given as parameter, the Schema is reset to use all known classes.
     *
     */
    private static void setSchema(Collection<Class<? extends RealmObject>> classes) {
        if (classes != null && classes.size() > 0) {
            // Filter default schema
            proxyMediator = new FilterableMediator(getDefaultMediator(), classes);
        } else if (proxyMediator instanceof FilterableMediator) {
            // else reset filter if needed
            proxyMediator = ((FilterableMediator) proxyMediator).getOriginalMediator();
        }
    }

    static String getCanonicalPath(File realmFile) {
        try {
            return realmFile.getCanonicalPath();
        } catch (IOException e) {
            throw new RealmException("Could not resolve the canonical path to the Realm file: " + realmFile.getAbsolutePath());
        }
    }

    /**
     * Encapsulates a Realm transaction.
     * <p>
     * Using this class will automatically handle {@link #beginTransaction()} and {@link #commitTransaction()}
     * If any exception is thrown during the transaction {@link #cancelTransaction()} will be called
     * instead of {@link #commitTransaction()}.
     */
    public interface Transaction {
        public void execute(Realm realm);
    }
}
