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
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Future;

import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmIOException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnIndices;
import io.realm.internal.ColumnInfo;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;
import io.realm.internal.TableView;
import io.realm.internal.Util;
import io.realm.internal.log.RealmLog;
import rx.Observable;

/**
 * The Realm class is the storage and transactional manager of your object persistent store. It is in charge of creating
 * instances of your RealmObjects. Objects within a Realm can be queried and read at any time. Creating, modifying, and
 * deleting objects must be done while inside a transaction. See {@link #beginTransaction()}
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
 *  onStart/onStop.
 * <p>
 * Realm instances coordinate their state across threads using the {@link android.os.Handler} mechanism. This also means
 * that Realm instances on threads without a {@link android.os.Looper} cannot receive updates unless {@link #refresh()}
 * is manually called.
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
 *
 * @see <a href="http://en.wikipedia.org/wiki/ACID">ACID</a>
 * @see <a href="https://github.com/realm/realm-java/tree/master/examples">Examples using Realm</a>
 */
public final class Realm extends BaseRealm {

    public static final String DEFAULT_REALM_NAME = RealmConfiguration.DEFAULT_REALM_NAME;

    // Caches Class objects (both model classes and proxy classes) to Realm Tables
    private final Map<Class<? extends RealmObject>, Table> classToTable =
            new HashMap<Class<? extends RealmObject>, Table>();

    private static RealmConfiguration defaultConfiguration;

    /**
     * The constructor is private to enforce the use of the static one.
     *
     * @param configuration the {@link RealmConfiguration} used to open the Realm.
     * @param autoRefresh {@code true} if Realm should auto-refresh. {@code false} otherwise.
     * @throws IllegalArgumentException if trying to open an encrypted Realm with the wrong key.
     */
    Realm(RealmConfiguration configuration, boolean autoRefresh) {
        super(configuration, autoRefresh);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Realm> asObservable() {
        return configuration.getRxFactory().from(this);
    }

    @Override
    protected void finalize() throws Throwable {
        if (sharedGroupManager != null && sharedGroupManager.isOpen()) {
            RealmLog.w("Remember to call close() on all Realm instances. " +
                            "Realm " + configuration.getPath() + " is being finalized without being closed, " +
                            "this can lead to running out of native memory."
            );
        }
        super.finalize();
    }

    /**
     * Realm static constructor for the default Realm file {@value io.realm.RealmConfiguration#DEFAULT_REALM_NAME}.
     * This is equivalent to calling {@code Realm.getInstance(new RealmConfiguration(getContext()).build())}.
     *
     * This constructor is only provided for convenience. It is recommended to use
     * {@link #getInstance(RealmConfiguration)} or {@link #getDefaultInstance()}.
     *
     * @param context a non-null Android {@link android.content.Context}
     * @return an instance of the Realm class.
     * @throws java.lang.IllegalArgumentException if no {@link Context} is provided.
     * @throws RealmMigrationNeededException if the RealmObject classes no longer match the underlying Realm and it must be
     * migrated.
     * @throws RealmIOException if an error happened when accessing the underlying Realm file.
     */
    public static Realm getInstance(Context context) {
        return Realm.getInstance(new RealmConfiguration.Builder(context)
                .name(DEFAULT_REALM_NAME)
                .build());
    }

    /**
     * Realm static constructor that returns the Realm instance defined by the {@link io.realm.RealmConfiguration} set
     * by {@link #setDefaultConfiguration(RealmConfiguration)}
     *
     * @return an instance of the Realm class.
     * @throws java.lang.NullPointerException if no default configuration has been defined.
     * @throws RealmMigrationNeededException if no migration has been provided by the default configuration and the
     * RealmObject classes or version has has changed so a migration is required.
     * @throws RealmIOException if an error happened when accessing the underlying Realm file.
     */
    public static Realm getDefaultInstance() {
        if (defaultConfiguration == null) {
            throw new NullPointerException("No default RealmConfiguration was found. Call setDefaultConfiguration() first");
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
     * @throws RealmIOException if an error happened when accessing the underlying Realm file.
     * @throws IllegalArgumentException if a null {@link RealmConfiguration} is provided.
     * @see RealmConfiguration for details on how to configure a Realm.
     */
    public static Realm getInstance(RealmConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("A non-null RealmConfiguration must be provided");
        }
        return RealmCache.createRealmOrGetFromCache(configuration, Realm.class);
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
     * @param configuration {@link RealmConfiguration} used to create the Realm.
     * @param columnIndices if this is not  {@code null} value, the {@link BaseRealm#schema#columnIndices} will be initialized
     *                      to it. Otherwise, {@link BaseRealm#schema#columnIndices} will be populated from the Realm file.
     * @return a {@link Realm} instance.
     */
    static Realm createInstance(RealmConfiguration configuration, ColumnIndices columnIndices) {
        try {
            return createAndValidate(configuration, columnIndices);

        } catch (RealmMigrationNeededException e) {
            if (configuration.shouldDeleteRealmIfMigrationNeeded()) {
                deleteRealm(configuration);
            } else {
                migrateRealm(configuration);
            }

            return createAndValidate(configuration, columnIndices);
        }
    }

    static Realm createAndValidate(RealmConfiguration configuration, ColumnIndices columnIndices) {
        boolean autoRefresh = Looper.myLooper() != null;
        Realm realm = new Realm(configuration, autoRefresh);
        long currentVersion = realm.getVersion();
        long requiredVersion = configuration.getSchemaVersion();
        if (currentVersion != UNVERSIONED && currentVersion < requiredVersion && columnIndices == null) {
            realm.doClose();
            throw new RealmMigrationNeededException(configuration.getPath(), String.format("Realm on disk need to migrate from v%s to v%s", currentVersion, requiredVersion));
        }
        if (currentVersion != UNVERSIONED && requiredVersion < currentVersion && columnIndices == null) {
            realm.doClose();
            throw new IllegalArgumentException(String.format("Realm on disk is newer than the one specified: v%s vs. v%s", currentVersion, requiredVersion));
        }

        // Initialize Realm schema if needed
        if (columnIndices == null) {
            try {
                initializeRealm(realm);
            } catch (RuntimeException e) {
                realm.doClose();
                throw e;
            }
        } else {
            realm.schema.columnIndices = columnIndices;
        }

        return realm;
    }

    @SuppressWarnings("unchecked")
    private static void initializeRealm(Realm realm) {
        long version = realm.getVersion();
        boolean commitNeeded = false;
        try {
            realm.beginTransaction();
            if (version == UNVERSIONED) {
                commitNeeded = true;
                realm.setVersion(realm.configuration.getSchemaVersion());
            }

            RealmProxyMediator mediator = realm.configuration.getSchemaMediator();
            final Set<Class<? extends RealmObject>> modelClasses = mediator.getModelClasses();
            final Map<Class<? extends RealmObject>, ColumnInfo> columnInfoMap;
            columnInfoMap = new HashMap<Class<? extends RealmObject>, ColumnInfo>(modelClasses.size());
            for (Class<? extends RealmObject> modelClass : modelClasses) {
                // Create and validate table
                if (version == UNVERSIONED) {
                    mediator.createTable(modelClass, realm.sharedGroupManager.getTransaction());
                }
                columnInfoMap.put(modelClass, mediator.validateTable(modelClass, realm.sharedGroupManager.getTransaction()));
            }
            realm.schema.columnIndices = new ColumnIndices(columnInfoMap);
        } finally {
            if (commitNeeded) {
                realm.commitTransaction();
            } else {
                realm.cancelTransaction();
            }
        }
    }

    /**
     * Creates a Realm object for each object in a JSON array. This must be done within a transaction.
     * JSON properties with a null value will map to the default value for the data type in Realm and unknown properties
     * will be ignored.
     *
     * @param clazz type of Realm objects to create.
     * @param json an array where each JSONObject must map to the specified class.
     * @throws RealmException if mapping from JSON fails.
     */
    public <E extends RealmObject> void createAllFromJson(Class<E> clazz, JSONArray json) {
        if (clazz == null || json == null) {
            return;
        }

        for (int i = 0; i < json.length(); i++) {
            try {
                configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json.getJSONObject(i), false);
            } catch (Exception e) {
                throw new RealmException("Could not map Json", e);
            }
        }
    }

    /**
     * Tries to update a list of existing objects identified by their primary key with new JSON data. If an existing
     * object could not be found in the Realm, a new object will be created. This must happen within a transaction.
     *
     * @param clazz type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json array with object data.
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     * @throws RealmException if unable to map JSON.
     * @see #createAllFromJson(Class, org.json.JSONArray)
     */
    public <E extends RealmObject> void createOrUpdateAllFromJson(Class<E> clazz, JSONArray json) {
        if (clazz == null || json == null) {
            return;
        }
        checkHasPrimaryKey(clazz);
        for (int i = 0; i < json.length(); i++) {
            try {
                configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json.getJSONObject(i), true);
            } catch (Exception e) {
                throw new RealmException("Could not map Json", e);
            }
        }
    }

    /**
     * Creates a Realm object for each object in a JSON array. This must be done within a transaction.
     * JSON properties with a null value will map to the default value for the data type in Realm and unknown properties
     * will be ignored.
     *
     * @param clazz type of Realm objects to create.
     * @param json the JSON array as a String where each object can map to the specified class.
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
     * @param clazz type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json string with an array of JSON objects.
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     * @throws RealmException if unable to create a JSON array from the json string.
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
     * Creates a Realm object for each object in a JSON array. This must be done within a transaction.
     * JSON properties with a null value will map to the default value for the data type in Realm and unknown properties
     * will be ignored.
     *
     * @param clazz type of Realm objects created.
     * @param inputStream the JSON array as a InputStream. All objects in the array must be of the specified class.
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
     *
     * @param clazz type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param in the InputStream with a list of object data in JSON format.
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     * @throws RealmException if unable to read JSON.
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
     * properties with a null value will map to the default value for the data type in Realm and unknown properties will
     * be ignored.
     *
     * @param clazz type of Realm object to create.
     * @param json the JSONObject with object data.
     * @return created object or null if no json data was provided.
     * @throws RealmException if the mapping from JSON fails.
     * @see #createOrUpdateObjectFromJson(Class, org.json.JSONObject)
     */
    public <E extends RealmObject> E createObjectFromJson(Class<E> clazz, JSONObject json) {
        if (clazz == null || json == null) {
            return null;
        }

        try {
            E realmObject = configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json, false);
            return realmObject;
        } catch (Exception e) {
            throw new RealmException("Could not map Json", e);
        }
    }

    /**
     * Tries to update an existing object defined by its primary key with new JSON data. If no existing object could be
     * found a new object will be saved in the Realm. This must happen within a transaction.
     *
     * @param clazz Type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json {@link org.json.JSONObject} with object data.
     * @return created or updated {@link io.realm.RealmObject}.
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     * @throws RealmException if JSON data cannot be mapped.
     * @see #createObjectFromJson(Class, org.json.JSONObject)
     */
    public <E extends RealmObject> E createOrUpdateObjectFromJson(Class<E> clazz, JSONObject json) {
        if (clazz == null || json == null) {
            return null;
        }
        checkHasPrimaryKey(clazz);
        try {
            E realmObject = configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json, true);
            if (handlerController != null) {
                handlerController.addToRealmObjects(realmObject);
            }
            return realmObject;
        } catch (JSONException e) {
            throw new RealmException("Could not map Json", e);
        }
    }

    /**
     * Creates a Realm object pre-filled with data from a JSON object. This must be done inside a transaction. JSON
     * properties with a null value will map to the default value for the data type in Realm and unknown properties will
     * be ignored.
     *
     * @param clazz type of Realm object to create.
     * @param json the JSON string with object data.
     * @return created object or null if json string was empty or null.
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
     * @param clazz type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param json string with object data in JSON format.
     * @return created or updated {@link io.realm.RealmObject}.
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     * @throws RealmException if JSON object cannot be mapped from the string parameter.
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
     * Creates a Realm object pre-filled with data from a JSON object. This must be done inside a transaction. JSON
     * properties with a null value will map to the default value for the data type in Realm and unknown properties will
     * be ignored.
     *
     * @param clazz type of Realm object to create.
     * @param inputStream the JSON object data as a InputStream.
     * @return created object or null if json string was empty or null.
     * @throws RealmException if the mapping from JSON failed.
     * @throws IOException if something was wrong with the input stream.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmObject> E createObjectFromJson(Class<E> clazz, InputStream inputStream) throws IOException {
        if (clazz == null || inputStream == null) {
            return null;
        }
        E realmObject;
        Table table = getTable(clazz);
        if (table.hasPrimaryKey()) {
            // As we need the primary key value we have to first parse the entire input stream as in the general
            // case that value might be the last property :(
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
     * found a new object will be saved in the Realm. This must happen within a transaction.
     *
     * @param clazz type of {@link io.realm.RealmObject} to create or update. It must have a primary key defined.
     * @param in the {@link InputStream} with object data in JSON format.
     * @return created or updated {@link io.realm.RealmObject}.
     * @throws java.lang.IllegalArgumentException if trying to update a class without a
     * {@link io.realm.annotations.PrimaryKey}.
     * @throws RealmException if failure to read JSON.
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
     *
     * @param clazz the Class of the object to create
     * @return the new object
     * @throws RealmException if an object could not be created
     */
    public <E extends RealmObject> E createObject(Class<E> clazz) {
        checkIfValid();
        Table table = getTable(clazz);
        long rowIndex = table.addEmptyRow();
        E object = get(clazz, rowIndex);
        return object;
    }

    /**
     * Creates a new object inside the Realm with the Primary key value initially set.
     * If the value violates the primary key constraint, no object will be added and a {@link RealmException} will be
     * thrown.
     *
     * @param clazz the Class of the object to create.
     * @param primaryKeyValue value for the primary key field.
     * @return the new object.
     * @throws RealmException if object could not be created.
     */
    <E extends RealmObject> E createObject(Class<E> clazz, Object primaryKeyValue) {
        Table table = getTable(clazz);
        long rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue);
        return get(clazz, rowIndex);
    }

    void remove(Class<? extends RealmObject> clazz, long objectIndex) {
        getTable(clazz).moveLastOver(objectIndex);
    }

    /**
     * Copies a RealmObject to the Realm instance and returns the copy. Any further changes to the original RealmObject
     * will not be reflected in the Realm copy. This is a deep copy, so all referenced objects will be copied. Objects
     * already in this Realm will be ignored.
     *
     * @param object the {@link io.realm.RealmObject} to copy to the Realm.
     * @return a managed RealmObject with its properties backed by the Realm.
     * @throws java.lang.IllegalArgumentException if RealmObject is {@code null}.
     */
    public <E extends RealmObject> E copyToRealm(E object) {
        checkNotNullObject(object);
        E realmObject = copyOrUpdate(object, false);
        return realmObject;
    }

    /**
     * Updates an existing RealmObject that is identified by the same {@link io.realm.annotations.PrimaryKey} or creates
     * a new copy if no existing object could be found. This is a deep copy or update, so all referenced objects will be
     * either copied or updated.
     *
     * @param object {@link io.realm.RealmObject} to copy or update.
     * @return the new or updated RealmObject with all its properties backed by the Realm.
     * @throws java.lang.IllegalArgumentException if RealmObject is {@code null} or doesn't have a Primary key defined.
     * @see #copyToRealm(RealmObject)
     */
    public <E extends RealmObject> E copyToRealmOrUpdate(E object) {
        checkNotNullObject(object);
        checkHasPrimaryKey(object.getClass());
        E realmObject = copyOrUpdate(object, true);
        return realmObject;
    }

    /**
     * Copies a collection of RealmObjects to the Realm instance and returns their copy. Any further changes to the
     * original RealmObjects will not be reflected in the Realm copies. This is a deep copy, so all referenced objects
     * will be copied. Objects already in this Realm will be ignored.
     *
     * @param objects the RealmObjects to copy to the Realm.
     * @return a list of the the converted RealmObjects that all has their properties managed by the Realm.
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
     * Updates a list of existing RealmObjects that is identified by their {@link io.realm.annotations.PrimaryKey} or
     * creates a new copy if no existing object could be found. This is a deep copy or update, so all referenced objects
     * will be either copied or updated.
     *
     * @param objects a list of objects to update or copy into Realm.
     * @return a list of all the new or updated RealmObjects.
     * @throws java.lang.IllegalArgumentException if RealmObject is {@code null} or doesn't have a Primary key defined.
     * @see #copyToRealm(Iterable)
     */
    public <E extends RealmObject> List<E> copyToRealmOrUpdate(Iterable<E> objects) {
        if (objects == null) {
            return new ArrayList<E>(0);
        }

        ArrayList<E> realmObjects = new ArrayList<E>();
        for (E object : objects) {
            realmObjects.add(copyToRealmOrUpdate(object));
        }

        return realmObjects;
    }

    /**
     * Makes a standalone in-memory copy of already persisted RealmObjects. This is a deep copy that will copy all
     * referenced objects.
     *
     * The copied objects are all detached from Realm so they will no longer be automatically updated. This means
     * that the copied objects might contain data that are no longer consistent with other managed Realm objects.
     *
     * *WARNING*: Any changes to copied objects can be merged back into Realm using {@link #copyToRealmOrUpdate(RealmObject)},
     * but all fields will be overridden, not just those that were changed. This includes references to other objects,
     * and can potentially override changes made by other threads.
     *
     * @param realmObjects RealmObjects to copy
     * @param <E> type of object.
     * @return an in-memory detached copy of managed RealmObjects.
     * @throws IllegalArgumentException if the RealmObjects are not accessible.
     * @see #copyToRealmOrUpdate(Iterable)
     */
    public <E extends RealmObject> List<E> copyFromRealm(Iterable<E> realmObjects) {
        return copyFromRealm(realmObjects, Integer.MAX_VALUE);
    }

    /**
     * Makes a standalone in-memory copy of already persisted RealmObjects. This is a deep copy that will copy all
     * referenced objects up to the defined depth.
     *
     * The copied objects are all detached from Realm so they will no longer be automatically updated. This means
     * that the copied objects might contain data that are no longer consistent with other managed Realm objects.
     *
     * *WARNING*: Any changes to copied objects can be merged back into Realm using {@link #copyToRealmOrUpdate(Iterable)},
     * but all fields will be overridden, not just those that were changed. This includes references to other objects
     * even though they might be {@code null} due to {@code maxDepth} being reached. This can also potentially override
     * changes made by other threads.
     *
     * @param realmObjects RealmObjects to copy.
     * @param maxDepth limit of the deep copy. All references after this depth will be {@code null}. Starting depth is {@code 0}.
     * @param <E> type of object.
     * @return an in-memory detached copy of the RealmObjects.
     * @throws IllegalArgumentException if {@code maxDepth < 0} or the RealmObjects aren't accessible.
     * @see #copyToRealmOrUpdate(Iterable)
     */
    public <E extends RealmObject> List<E> copyFromRealm(Iterable<E> realmObjects, int maxDepth) {
        checkMaxDepth(maxDepth);
        if (realmObjects == null) {
            return new ArrayList<E>(0);
        }

        ArrayList<E> standaloneObjects = new ArrayList<E>();
        Map<RealmObject, RealmObjectProxy.CacheData<RealmObject>> listCache = new HashMap<RealmObject, RealmObjectProxy.CacheData<RealmObject>>();
        for (E object : realmObjects) {
            checkValidObjectForDetach(object);
            standaloneObjects.add(createDetachedCopy(object, maxDepth, listCache));
        }

        return standaloneObjects;
    }

    /**
     * Makes a standalone in-memory copy of an already persisted {@link RealmObject}. This is a deep copy that will copy
     * all referenced objects.
     *
     * The copied object(s) are all detached from Realm so they will no longer be automatically updated. This means
     * that the copied objects might contain data that are no longer consistent with other managed Realm objects.
     *
     * *WARNING*: Any changes to copied objects can be merged back into Realm using {@link #copyToRealmOrUpdate(RealmObject)},
     * but all fields will be overridden, not just those that were changed. This includes references to other objects,
     * and can potentially override changes made by other threads.
     *
     * @param realmObject {@link RealmObject} to copy
     * @param <E> type of object.
     * @return an in-memory detached copy of the managed {@link RealmObject}.
     * @throws IllegalArgumentException if the RealmObject is no longer accessible.
     * @see #copyToRealmOrUpdate(RealmObject)
     */
    public <E extends RealmObject> E copyFromRealm(E realmObject) {
        return copyFromRealm(realmObject, Integer.MAX_VALUE);
    }

    /**
     * Makes a standalone in-memory copy of an already persisted {@link RealmObject}. This is a deep copy that will copy
     * all referenced objects up to the defined depth.
     *
     * The copied object(s) are all detached from Realm so they will no longer be automatically updated. This means
     * that the copied objects might contain data that are no longer consistent with other managed Realm objects.
     *
     * *WARNING*: Any changes to copied objects can be merged back into Realm using {@link #copyToRealmOrUpdate(RealmObject)},
     * but all fields will be overridden, not just those that were changed. This includes references to other objects
     * even though they might be {@code null} due to {@code maxDepth} being reached. This can also potentially override
     * changes made by other threads.
     *
     * @param realmObject {@link RealmObject} to copy
     * @param maxDepth limit of the deep copy. All references after this depth will be {@code null}. Starting depth is {@code 0}.
     * @param <E> type of object.
     * @return an in-memory detached copy of the managed {@link RealmObject}.
     * @throws IllegalArgumentException if {@code maxDepth < 0} or the RealmObject is no longer accessible.
     * @see #copyToRealmOrUpdate(RealmObject)
     */
    public <E extends RealmObject> E copyFromRealm(E realmObject, int maxDepth) {
        checkMaxDepth(maxDepth);
        checkValidObjectForDetach(realmObject);
        return createDetachedCopy(realmObject, maxDepth, new HashMap<RealmObject, RealmObjectProxy.CacheData<RealmObject>>());
    }

    boolean contains(Class<? extends RealmObject> clazz) {
        return configuration.getSchemaMediator().getModelClasses().contains(clazz);
    }

    /**
     * Returns a typed RealmQuery, which can be used to query for specific objects of this type
     *
     * @param clazz the class of the object which is to be queried for.
     * @return a typed RealmQuery, which can be used to query for specific objects of this type.
     * @see io.realm.RealmQuery
     */
    public <E extends RealmObject> RealmQuery<E> where(Class<E> clazz) {
        checkIfValid();
        return RealmQuery.createQuery(this, clazz);
    }

    /**
     * Gets all objects of a specific Class. If no objects exist, the returned RealmResults will not be {@code null}.
     * The RealmResults.size() to check the number of objects instead.
     *
     * @param clazz the Class to get objects of.
     * @return a RealmResult list containing the objects.
     * @see io.realm.RealmResults
     */
    public <E extends RealmObject> RealmResults<E> allObjects(Class<E> clazz) {
        return where(clazz).findAll();
    }

    /**
     * Get all objects of a specific Class sorted by a field. If no objects exist, the returned {@link RealmResults}
     * will not be {@code null}. The RealmResults.size() to check the number of objects instead.
     *
     * @param clazz the Class to get objects of.
     * @param fieldName the field name to sort by.
     * @param sortOrder how to sort the results.
     * @return a sorted RealmResults containing the objects.
     * @throws java.lang.IllegalArgumentException if field name does not exist.
     */
    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String fieldName,
                                                                    Sort sortOrder) {
        checkIfValid();
        Table table = getTable(clazz);
        long columnIndex = schema.columnIndices.getColumnIndex(clazz, fieldName);
        if (columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }

        TableView tableView = table.getSortedView(columnIndex, sortOrder);
        RealmResults<E> realmResults = RealmResults.createFromTableOrView(this, tableView, clazz);
        if (handlerController != null) {
            handlerController.addToRealmResults(realmResults);
        }
        return realmResults;
    }


    /**
     * Gets all objects of a specific class sorted by two specific field names.  If no objects exist, the returned
     * {@link RealmResults} will not be {@code null}. The RealmResults.size() to check the number of objects instead.
     *
     * @param clazz the class ti get objects of.
     * @param fieldName1 first field name to sort by.
     * @param sortOrder1 sort order for first field.
     * @param fieldName2 second field name to sort by.
     * @param sortOrder2 sort order for second field.
     * @return a sorted RealmResults containing the objects.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String fieldName1,
                                                                    Sort sortOrder1, String fieldName2,
                                                                    Sort sortOrder2) {
        return allObjectsSorted(clazz, new String[]{fieldName1, fieldName2}, new Sort[]{sortOrder1,
                sortOrder2});
    }

    /**
     * Gets all objects of a specific class sorted by two specific field names.  If no objects exist, the returned
     * {@link RealmResults} will not be {@code null}. The RealmResults.size() to check the number of objects instead.
     *
     * @param clazz the class ti get objects of.
     * @param fieldName1 first field name to sort by.
     * @param sortOrder1 sort order for first field.
     * @param fieldName2 second field name to sort by.
     * @param sortOrder2 sort order for second field.
     * @param fieldName3 third field name to sort by.
     * @param sortOrder3 sort order for third field.
     * @return a sorted RealmResults containing the objects.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String fieldName1,
                                                                    Sort sortOrder1,
                                                                    String fieldName2, Sort sortOrder2,
                                                                    String fieldName3, Sort sortOrder3) {
        return allObjectsSorted(clazz, new String[]{fieldName1, fieldName2, fieldName3},
                new Sort[]{sortOrder1, sortOrder2, sortOrder3});
    }

    /**
     * Gets all objects of a specific Class sorted by multiple fields. If no objects exist, the returned
     * {@link RealmResults} will not be null. The RealmResults.size() to check the number of objects instead.
     *
     * @param clazz the Class to get objects of.
     * @param sortOrders sort ascending if SORT_ORDER_ASCENDING, sort descending if SORT_ORDER_DESCENDING.
     * @param fieldNames an array of field names to sort objects by. The objects are first sorted by fieldNames[0], then
     *                   by fieldNames[1] and so forth.
     * @return a sorted RealmResults containing the objects.
     * @throws java.lang.IllegalArgumentException if a field name does not exist.
     */
    @SuppressWarnings("unchecked")
    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String fieldNames[],
                                                                    Sort sortOrders[]) {
        checkAllObjectsSortedParameters(fieldNames, sortOrders);
        Table table = this.getTable(clazz);
        TableView tableView = doMultiFieldSort(fieldNames, sortOrders, table);

        RealmResults<E> realmResults = RealmResults.createFromTableOrView(this, tableView, clazz);
        if (handlerController != null) {
            handlerController.addToRealmResults(realmResults);
        }
        return realmResults;
    }

    /**
     * Returns a distinct set of objects of a specific class. As a Realm is unordered, it is undefined which objects are
     * returned in case of multiple occurrences.
     *
     * @param clazz the Class to get objects of.
     * @param fieldName the field name.
     * @return a non-null {@link RealmResults} containing the distinct objects.
     * @throws IllegalArgumentException if a field name does not exist or the field is not indexed.
     */
    public <E extends RealmObject> RealmResults<E> distinct(Class<E> clazz, String fieldName) {
        checkNotNullFieldName(fieldName);
        checkIfValid();
        Table table = this.getTable(clazz);
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }

        TableView tableView = table.getDistinctView(columnIndex);
        RealmResults<E> realmResults = RealmResults.createFromTableOrView(this, tableView, clazz);
        if (handlerController != null) {
            handlerController.addToRealmResults(realmResults);
        }
        return realmResults;
    }

    /**
     * Returns a distinct set of objects of a specific class. As a Realm is unordered, it is undefined which objects are
     * returned in case of multiple occurrences.
     * This method is only available from a Looper thread.
     *
     * @param clazz the Class to get objects of.
     * @param fieldName the field name.
     * @return immediately an empty {@link RealmResults}. Users need to register a listener
     *      {@link io.realm.RealmResults#addChangeListener(RealmChangeListener)} to be notified when the query
     *      completes.
     * @throws IllegalArgumentException if a field name does not exist or the field is not indexed.
     */
    public <E extends RealmObject> RealmResults<E> distinctAsync(Class<E> clazz, String fieldName) {
        checkNotNullFieldName(fieldName);
        Table table = this.getTable(clazz);
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }

        // check if the field is indexed
        if (!table.hasSearchIndex(columnIndex)) {
            throw new IllegalArgumentException(String.format("Field name '%s' must be indexed in order to use it for distinct queries.", fieldName));
        }

        return where(clazz).distinctAsync(columnIndex);
    }

    /**
     * Executes a given transaction on the Realm. {@link #beginTransaction()} and {@link #commitTransaction()} will be
     * called automatically. If any exception is thrown during the transaction {@link #cancelTransaction()} will be
     * called instead of {@link #commitTransaction()}.
     *
     * @param transaction the {@link io.realm.Realm.Transaction} to execute.
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
        } catch (Throwable e) {
            if (isInTransaction()) {
                cancelTransaction();
            } else {
                RealmLog.w("Could not cancel transaction, not currently in a transaction.");
            }
            throw e;
        }
    }

    /**
     * Similar to {@link #executeTransaction(Transaction)} but runs asynchronously from a worker thread.
     *
     * @param transaction {@link io.realm.Realm.Transaction} to execute.
     * @param callback optional, to receive the result of this query.
     * @return a {@link RealmAsyncTask} representing a cancellable task.
     * @throws IllegalArgumentException if the {@code transaction} is {@code null}, or if the realm is opened from another thread.
     */
    public RealmAsyncTask executeTransaction(final Transaction transaction, final Transaction.Callback callback) {
        if (transaction == null)
            throw new IllegalArgumentException("Transaction should not be null");

        // If the user provided a Callback then we make sure, the current Realm has a Handler
        // we can use to deliver the result
        if (callback != null && handler == null) {
            throw new IllegalStateException("Your Realm is opened from a thread without a Looper" +
                    " and you provided a callback, we need a Handler to invoke your callback");
        }

        // We need to use the same configuration to open a background SharedGroup (i.e Realm)
        // to perform the transaction
        final RealmConfiguration realmConfiguration = getConfiguration();

        final Future<?> pendingQuery = asyncQueryExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (!Thread.currentThread().isInterrupted()) {
                    Realm bgRealm = Realm.getInstance(realmConfiguration);
                    bgRealm.beginTransaction();
                    try {
                        transaction.execute(bgRealm);

                        if (!Thread.currentThread().isInterrupted()) {
                            bgRealm.commitTransaction();
                            if (callback != null
                                    && handler != null
                                    && !Thread.currentThread().isInterrupted()
                                    && handler.getLooper().getThread().isAlive()) {
                                // The bgRealm needs to be closed before post event to caller's handler to avoid concurrency problem
                                // eg.: User wants to delete Realm in the callbacks.
                                bgRealm.close();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onSuccess();
                                    }
                                });
                            }
                        } else {
                            if (bgRealm.isInTransaction()) {
                                bgRealm.cancelTransaction();
                            } else {
                                RealmLog.w("Thread is interrupted. Could not cancel transaction, not currently in a transaction.");
                            }
                        }

                    } catch (final Exception e) {
                        if (bgRealm.isInTransaction()) {
                            bgRealm.cancelTransaction();
                        } else {
                            RealmLog.w("Could not cancel transaction, not currently in a transaction.");
                        }
                        if (callback != null
                                && handler != null
                                && !Thread.currentThread().isInterrupted()
                                && handler.getLooper().getThread().isAlive()) {
                            bgRealm.close();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(e);
                                }
                            });
                        }
                    } finally {
                        if (!bgRealm.isClosed()) {
                            bgRealm.close();
                        }
                    }
                }
            }
        });

        return new RealmAsyncTask(pendingQuery);
    }

    /**
     * Removes all objects of the specified class.
     *
     * @param clazz the class which objects should be removed.
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     */
    public void clear(Class<? extends RealmObject> clazz) {
        checkIfValid();
        getTable(clazz).clear();
    }

    @SuppressWarnings("unchecked")
    private <E extends RealmObject> E copyOrUpdate(E object, boolean update) {
        checkIfValid();
        return configuration.getSchemaMediator().copyOrUpdate(this, object, update, new HashMap<RealmObject, RealmObjectProxy>());
    }

    private <E extends RealmObject> E createDetachedCopy(E object, int maxDepth, Map<RealmObject, RealmObjectProxy.CacheData<RealmObject>> cache) {
        checkIfValid();
        return configuration.getSchemaMediator().createDetachedCopy(object, maxDepth, cache);
    }

    private <E extends RealmObject> void checkNotNullObject(E object) {
        if (object == null) {
            throw new IllegalArgumentException("Null objects cannot be copied into Realm.");
        }
    }

    private void checkHasPrimaryKey(Class<? extends RealmObject> clazz) {
        if (!getTable(clazz).hasPrimaryKey()) {
            throw new IllegalArgumentException("A RealmObject with no @PrimaryKey cannot be updated: " + clazz.toString());
        }
    }

    private void checkMaxDepth(int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be > 0. It was: " + maxDepth);
        }
    }

    private <E extends RealmObject> void checkValidObjectForDetach(E realmObject) {
        if (realmObject == null) {
            throw new IllegalArgumentException("Null objects cannot be copied from Realm.");
        }
        if (!realmObject.isValid()) {
            throw new IllegalArgumentException("RealmObject is not valid, so it cannot be copied.");
        }
    }

    /**
     * Manually trigger the migration associated with a given RealmConfiguration. If Realm is already at the latest
     * version, nothing will happen.
     *
     * @param configuration {@link RealmConfiguration}
     */
    public static void migrateRealm(RealmConfiguration configuration) {
        migrateRealm(configuration, null);
    }

    /**
     * Manually trigger a migration on a RealmMigration.
     *
     * @param configuration the{@link RealmConfiguration}.
     * @param migration the {@link RealmMigration} to run on the Realm. This will override any migration set on the
     *                  configuration.
     */
    public static void migrateRealm(RealmConfiguration configuration, RealmMigration migration) {
        BaseRealm.migrateRealm(configuration, migration, new MigrationCallback() {
            @Override
            public void migrationComplete() {
            }
        });
    }

    /**
     * Deletes the Realm file specified by the given {@link RealmConfiguration} from the filesystem.
     * The Realm must be unused and closed before calling this method.
     *
     * @param configuration a {@link RealmConfiguration}.
     * @return {@code false} if a file could not be deleted. The failing file will be logged.
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
     * @throws IllegalArgumentException if the realm file is encrypted. Compacting an encrypted Realm file is not
     * supported yet.
     */
    public static boolean compactRealm(RealmConfiguration configuration) {
        return BaseRealm.compactRealm(configuration);
    }

    // Get the canonical path for a given file
    static String getCanonicalPath(File realmFile) {
        try {
            return realmFile.getCanonicalPath();
        } catch (IOException e) {
            throw new RealmException("Could not resolve the canonical path to the Realm file: " + realmFile.getAbsolutePath());
        }
    }

    // Return all handlers registered for this Realm
    static Map<Handler, String> getHandlers() {
        return handlers;
    }

    // Public because of migrations
    public Table getTable(Class<? extends RealmObject> clazz) {
        Table table = classToTable.get(clazz);
        if (table == null) {
            clazz = Util.getOriginalModelClass(clazz);
            table = sharedGroupManager.getTable(configuration.getSchemaMediator().getTableName(clazz));
            classToTable.put(clazz, table);
        }
        return table;
    }

    /**
     * Returns the default Realm module. This module contains all Realm classes in the current project, but not those
     * from library or project dependencies. Realm classes in these should be exposed using their own module.
     *
     * @return the default Realm module or null if no default module exists.
     * @throws RealmException if unable to create an instance of the module.
     * @see io.realm.RealmConfiguration.Builder#setModules(Object, Object...)
     */
    public static Object getDefaultModule() {
        String moduleName = "io.realm.DefaultRealmModule";
        Class<?> clazz;
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
            public void onError(Exception e) {}
        }
    }
}
