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
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmIOException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.Row;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;
import io.realm.internal.android.LooperThread;


/**
 * The Realm class is the storage and transactional manager of your object persistent store. Objects
 * are created. Objects within a Realm can be queried and read at any time. Creating,
 * modifying, and deleting objects must be done through transactions.
 *
 * The transactions ensure that multiple instances (on multiple threads) can access the objects
 * in a consistent state with full ACID guaranties.
 *
 * The instances of a Realm will be automatically updated when one instance commits a
 * change (create, modify or delete an object).
 */
public class Realm {
    public static final String DEFAULT_REALM_NAME = "default.realm";
    private static final Map<String, ThreadRealm> realms = new HashMap<String, ThreadRealm>();

    private static final String TAG = "REALM";
    private static final String TABLE_PREFIX = "class_";

    private static SharedGroup.Durability defaultDurability = SharedGroup.Durability.FULL;
    private static boolean autoRefresh = true;

    private final int id;
    private final LooperThread looperThread = LooperThread.getInstance();
    private final SharedGroup sharedGroup;
    private final ImplicitTransaction transaction;
    private final Map<Class<?>, String> simpleClassNames = new HashMap<Class<?>, String>(); // Map between original class and their class name
    private final Map<String, Class<?>> generatedClasses = new HashMap<String, Class<?>>(); // Map between generated class names and their implementation
    private final Map<Class<?>, Constructor> constructors = new HashMap<Class<?>, Constructor>();
    private final Map<Class<?>, Method> initTableMethods = new HashMap<Class<?>, Method>();
    private final Map<Class<?>, Constructor> generatedConstructors = new HashMap<Class<?>, Constructor>();
    private final List<RealmChangeListener> changeListeners = new ArrayList<RealmChangeListener>();
    private final Map<Class<?>, Table> tables = new HashMap<Class<?>, Table>();
    private static final long UNVERSIONED = -1;

    private Handler handler;

    // Package protected to be reachable by proxy classes
    static final Map<String, Map<String, Long>> columnIndices = new HashMap<String, Map<String, Long>>();

    // The constructor in private to enforce the use of the static one
    private Realm(String absolutePath, byte[] key) {
        this.sharedGroup = new SharedGroup(absolutePath, true, key);
        this.transaction = sharedGroup.beginImplicitTransaction();
        this.id = absolutePath.hashCode();
        if (!looperThread.isAlive()) {
            looperThread.start();
        }

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.what == LooperThread.REALM_CHANGED) {
                    if (autoRefresh) {
                        transaction.advanceRead();
                    }
                    sendNotifications();
                }
            }
        };
        if (Looper.myLooper() == null) {
            Looper.loop();
        }
        LooperThread.handlers.put(handler, id);
    }

    @Override
    protected void finalize() throws Throwable {
        transaction.endRead();
        super.finalize();
    }

//    public static void setDefaultDurability(SharedGroup.Durability durability) {
//        defaultDurability = durability;
//    }

    public Table getTable(Class<?> clazz) {
        String simpleClassName = simpleClassNames.get(clazz);
        if (simpleClassName == null) {
            simpleClassName = clazz.getSimpleName();
            simpleClassNames.put(clazz, simpleClassName);
        }
        return transaction.getTable(TABLE_PREFIX + simpleClassName);
    }

    /**
     * Realm static constructor for the default realm "default.realm"
     * @param context an Android context
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     * must be migrated
     * @throws RealmIOException Error when accessing underlying file
     * @throws RealmException Other errors
     */
    public static Realm getInstance(Context context) {
        return Realm.getInstance(context, DEFAULT_REALM_NAME, null);
    }

    /**
     * Realm static constructor
     * @param context an Android context
     * @param fileName the name of the file to save the Realm to
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     * must be migrated
     * @throws RealmIOException Error when accessing underlying file
     * @throws RealmException Other errors
     */
    public static Realm getInstance(Context context, String fileName) {
        return Realm.create(context.getFilesDir(), fileName, null);
    }

    /**
     * Realm static constructor
     * @param context an Android context
     * @param key a 32-byte encryption key
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     * must be migrated
     * @throws RealmIOException Error when accessing underlying file
     * @throws RealmException Other errors
     */
    public static Realm getInstance(Context context, byte[] key) {
        return Realm.getInstance(context, DEFAULT_REALM_NAME, key);
    }

    /**
     * Realm static constructor
     * @param context an Android context
     * @param fileName the name of the file to save the Realm to
     * @param key a 32-byte encryption key
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     * must be migrated
     * @throws RealmIOException Error when accessing underlying file
     * @throws RealmException Other errors
     */
    public static Realm getInstance(Context context, String fileName, byte[] key) {
        return Realm.create(context.getFilesDir(), fileName, key);
    }

    /**
     * Realm static constructor
     * @param writableFolder absolute path to a writable directory
     * @param key a 32-byte encryption key
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     * must be migrated
     * @throws RealmIOException Error when accessing underlying file
     * @throws RealmException Other errors
     */
    public static Realm getInstance(File writableFolder, byte[] key) {
        return Realm.create(writableFolder, DEFAULT_REALM_NAME, key);
    }

    /**
     * Realm static constructor
     * @param writableFolder absolute path to a writable directory
     * @param filename the name of the file to save the Realm to
     * @param key a 32-byte encryption key
     * @return an instance of the Realm class
     * @throws RealmMigrationNeededException The model classes have been changed and the Realm
     * must be migrated
     * @throws RealmIOException Error when accessing underlying file
     * @throws RealmException Other errors
     */
    public static Realm create(File writableFolder, String filename, byte[] key) {
        String absolutePath = new File(writableFolder, filename).getAbsolutePath();
        return createAndValidate(absolutePath, key, true);
    }

    private static Realm createAndValidate(String absolutePath, byte[] key, boolean validateSchema) {
        ThreadRealm threadRealm = realms.get(absolutePath);
        boolean needsValidation = (threadRealm == null);
        if (threadRealm == null) {
            threadRealm = new ThreadRealm(absolutePath, key);
        }
        SoftReference<Realm> realmSoftReference = threadRealm.get();
        Realm realm = realmSoftReference.get();
        if (realm == null) {
            // The garbage collector decided to get rid of the realm instance
            threadRealm = new ThreadRealm(absolutePath, key);
            realmSoftReference = threadRealm.get();
            realm = realmSoftReference.get();
        }
        if (validateSchema && needsValidation) {
            Class<?> validationClass;
            try {
                validationClass = Class.forName("io.realm.ValidationList");
            } catch (ClassNotFoundException e) {
                throw new RealmException("Could not find the generated ValidationList class");
            }
            Method getProxyClassesMethod;
            try {
                getProxyClassesMethod = validationClass.getMethod("getProxyClasses");
            } catch (NoSuchMethodException e) {
                throw new RealmException("Could not find the getProxyClasses method in the ValidationList class");
            }
            List<String> proxyClasses;
            try {
                proxyClasses = (List<String>) getProxyClassesMethod.invoke(null);
            } catch (IllegalAccessException e) {
                throw new RealmException("Could not execute the getProxyClasses method in the ValidationList class");
            } catch (InvocationTargetException e) {
                throw new RealmException("An exception was thrown in the getProxyClasses method in the ValidationList class");
            }

            long version = realm.getVersion();
            try {
                realm.beginTransaction();
                if (version == UNVERSIONED) {
                    realm.setVersion(0);
                }

                for (String className : proxyClasses) {
                    String[] splitted = className.split("\\.");
                    String modelClassName = splitted[splitted.length - 1];
                    String generatedClassName = getProxyClassName(modelClassName);
                    Class<?> generatedClass;
                    try {
                        generatedClass = Class.forName(generatedClassName);
                    } catch (ClassNotFoundException e) {
                        throw new RealmException("Could not find the generated " + generatedClassName + " class");
                    }

                    // if not versioned, create table
                    if (version == UNVERSIONED) {
                        Method initTableMethod;
                        try {
                            initTableMethod = generatedClass.getMethod("initTable", new Class[]{ImplicitTransaction.class});
                        } catch (NoSuchMethodException e) {
                            throw new RealmException("Could not find the initTable method in the generated " + generatedClassName + " class");
                        }
                        try {
                            initTableMethod.invoke(null, realm.transaction);
                        } catch (IllegalAccessException e) {
                            throw new RealmException("Could not execute the initTable method in the " + generatedClassName + " class");
                        } catch (InvocationTargetException e) {
                            throw new RealmException("An exception was thrown in the initTable method in the " + generatedClassName + " class");
                        }
                    }

                    // validate created table
                    Method validateMethod;
                    try {
                        validateMethod = generatedClass.getMethod("validateTable", new Class[]{ImplicitTransaction.class});
                    } catch (NoSuchMethodException e) {
                        throw new RealmException("Could not find the validateTable method in the generated " + generatedClassName + " class");
                    }
                    try {
                        validateMethod.invoke(null, realm.transaction);
                    } catch (IllegalAccessException e) {
                        throw new RealmException("Could not execute the validateTable method in the " + generatedClassName + " class");
                    } catch (InvocationTargetException e) {
                        throw new RealmMigrationNeededException(e.getMessage(), e);
                    }

                    // Populate the columnIndices table
                    Method fieldNamesMethod;
                    try {
                        fieldNamesMethod = generatedClass.getMethod("getFieldNames");
                    } catch (NoSuchMethodException e) {
                        throw new RealmException("Could not find the getFieldNames method in the generated " + generatedClassName + " class");
                    }
                    List<String> fieldNames;
                    try {
                        fieldNames = (List<String>)fieldNamesMethod.invoke(null);
                    } catch (IllegalAccessException e) {
                        throw new RealmException("Could not execute the getFieldNames method in the generated " + generatedClassName + " class");
                    } catch (InvocationTargetException e) {
                        throw new RealmException("An exception was thrown in the getFieldNames method in the generated " + generatedClassName + " class");
                    }
                    Table table = realm.transaction.getTable(TABLE_PREFIX + modelClassName);
                    for (String fieldName : fieldNames) {
                        long columnIndex = table.getColumnIndex(fieldName);
                        if (columnIndex == -1) {
                            throw new RealmMigrationNeededException("Column '" + fieldName + "' not found for type '" + modelClassName + "'");
                        }
                        Map<String, Long> innerMap = columnIndices.get(modelClassName);
                        if (innerMap == null) {
                            innerMap = new HashMap<String, Long>();
                        }
                        innerMap.put(fieldName, columnIndex);
                        columnIndices.put(modelClassName, innerMap);
                    }
                }

                // cache realm after validation
                realms.put(absolutePath, threadRealm);
            }
            finally {
                realm.commitTransaction();
            }
        }

        return realm;
    }

    /**
     * Add an array of of JsonObjects to the Realm as a new object. This must be done inside a transaction.
     *
     * @param clazz Class of object the json will map to. All Objects in the array must be of the same type.
     * @param json  Array of JsonObject's that can map to the chosen clazz. Properties not in the class are ignored.
     *
     * @throws RealmException if the mapping fail.
     */
    public <E extends RealmObject> void createAllFromJson(Class<E> clazz, JSONArray json) {
        if (json == null) return;
        for (int i = 0; i < json.length(); i++) {
            E obj = createObject(clazz);
            try {
                obj.populateUsingJsonObject(json.getJSONObject(i));
            } catch (Exception e) {
                throw new RealmException("Could not map Json", e);
            }
        }
    }

    /**
     * Add a Json InputStream to the Realm as new objects. This must be done inside a transaction.
     *
     * @param clazz         Class of object the json will map to. All Objects in the array must be of the same type.
     * @param inputStream   A JSON InputStream of objects of type clazz. All objects must be of the chosen clazz. Properties not in the class are ignored.
     *
     * @throws RealmException if the mapping fail.
     * @throws IOException if something is wrong with the input stream.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmObject> void createAllFromJson(Class<E> clazz, InputStream inputStream) throws IOException {
        if (inputStream != null && clazz != null) {
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            reader.beginArray();
            while (reader.hasNext()) {
                E obj = createObject(clazz);
                obj.populateUsingJsonStream(reader);
            }
            reader.endArray();
            reader.close();
        }
    }

    /**
     * Add a JsonObject to the Realm as a new object. This must be done inside a transaction.
     *
     * @param clazz Class of object the json will map to.
     * @param json  JsonObject that can map to the chosen clazz. Properties not in the class are ignored.
     * @return Object with data or null if no json data was provided.
     *
     * @throws RealmException if the mapping fail.
     */
    public <E extends RealmObject> E createFromJson(Class<E> clazz, JSONObject json) {
        if (json == null) return null;

        E obj = createObject(clazz);
        try {
            obj.populateUsingJsonObject(json);
        } catch (Exception e) {
            // TODO Remove object from realm
            throw new RealmException("Could not map Json", e);
        }

        return obj;
    }

    /**
     * Add a JsonObject from a InputStream to the Realm as a new object. This must be done inside a transaction.
     *
     * @param clazz         Class of object the json will map to.
     * @param inputStream   JSONObject as a input stream of the chosen clazz. Properties not in the class are ignored.
     * @return Object with data or null if no json data was provided.
     *
     * @throws RealmException if the mapping fail.
     * @throws IOException if something is wrong with the input stream.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmObject> E createFromJson(Class<E> clazz, InputStream inputStream) throws IOException {
        if (inputStream != null && clazz != null) {
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            E obj = createObject(clazz);
            obj.populateUsingJsonStream(reader);
            reader.close();
            return obj;
        }

        return null;
    }

    // This class stores soft-references to realm objects per thread per realm file
    private static class ThreadRealm extends ThreadLocal<SoftReference<Realm>> {
        private String absolutePath;
        private byte[] key;

        private ThreadRealm(String absolutePath, byte[] key) {
            this.absolutePath = absolutePath;
            this.key = key;
        }

        @Override
        protected SoftReference<Realm> initialValue() {
            Realm realm = new Realm(absolutePath, key);
            key = null;
            return new SoftReference<Realm>(realm);
        }
    }

    /**
     * Instantiates and adds a new object to the realm
     * @return The new object
     * @param clazz The Class of the object to create
     * @throws RealmException An object could not be created
     */
    public <E extends RealmObject> E createObject(Class<E> clazz) {
        Table table;
        table = tables.get(clazz);
        if (table == null) {
            String simpleClassName = simpleClassNames.get(clazz);
            if (simpleClassName == null) {
                simpleClassName = clazz.getSimpleName();
                simpleClassNames.put(clazz, simpleClassName);
            }
            String generatedClassName = getProxyClassName(simpleClassName);

            Class<?> generatedClass = generatedClasses.get(generatedClassName);
            if (generatedClass == null) {
                try {
                    generatedClass = Class.forName(generatedClassName);
                } catch (ClassNotFoundException e) {
                    throw new RealmException("Could not find the generated proxy class");
                }
                generatedClasses.put(generatedClassName, generatedClass);
            }

            Method method = initTableMethods.get(generatedClass);
            if (method == null) {
                try {
                    method = generatedClass.getMethod("initTable", new Class[]{ImplicitTransaction.class});
                } catch (NoSuchMethodException e) {
                    throw new RealmException("Could not find the initTable() method in generated proxy class");
                }
                initTableMethods.put(generatedClass, method);
            }

            try {
                table = (Table) method.invoke(null, transaction);
                tables.put(clazz, table);
            } catch (IllegalAccessException e) {
                throw new RealmException("Could not launch the initTable method");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new RealmException("An exception occurred while running the initTable method");
            }
        }

        long rowIndex = table.addEmptyRow();
        return get(clazz, rowIndex);
    }

    <E> void remove(Class<E> clazz, long objectIndex) {
        getTable(clazz).moveLastOver(objectIndex);
    }

    <E extends RealmObject> E get(Class<E> clazz, long rowIndex) {
        E result;

        Table table = tables.get(clazz);
        if (table == null) {
            String simpleClassName = simpleClassNames.get(clazz);
            if (simpleClassName == null) {
                simpleClassName = clazz.getSimpleName();
                simpleClassNames.put(clazz, simpleClassName);
            }

            table = transaction.getTable(TABLE_PREFIX + simpleClassName);
            tables.put(clazz, table);
        }

        Row row = table.getRow(rowIndex);

        Constructor constructor = generatedConstructors.get(clazz);
        if (constructor == null) {
            String simpleClassName = simpleClassNames.get(clazz);
            if (simpleClassName == null) {
                simpleClassName = clazz.getSimpleName();
                simpleClassNames.put(clazz, simpleClassName);
            }
            String generatedClassName = getProxyClassName(simpleClassName);


            Class<?> generatedClass = generatedClasses.get(generatedClassName);
            if (generatedClass == null) {
                try {
                    generatedClass = Class.forName(generatedClassName);
                } catch (ClassNotFoundException e) {
                    throw new RealmException("Could not find the generated proxy class");
                }
                generatedClasses.put(generatedClassName, generatedClass);
            }

            constructor = constructors.get(generatedClass);
            if (constructor == null) {
                try {
                    constructor = generatedClass.getConstructor();
                } catch (NoSuchMethodException e) {
                    throw new RealmException("Could not find the constructor in generated proxy class");
                }
                constructors.put(generatedClass, constructor);
                generatedConstructors.put(clazz, constructor);
            }
        }

        try {
            // We are know the casted type since we generated the class
            //noinspection unchecked
            result = (E) constructor.newInstance();
        } catch (InstantiationException e) {
            throw new RealmException("Could not instantiate the proxy class");
        } catch (IllegalAccessException e) {
            throw new RealmException("Could not run the constructor of the proxy class");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RealmException("An exception occurred while instantiating the proxy class");
        }
        result.realmSetRow(row);
        result.setRealm(this);
        return result;
    }

    private static String getProxyClassName(String simpleClassName) {
        return "io.realm." + simpleClassName + "RealmProxy";
    }

    boolean contains(Class<?> clazz) {
        String simpleClassName = simpleClassNames.get(clazz);
        if (simpleClassName == null) {
            simpleClassName = clazz.getSimpleName();
            simpleClassNames.put(clazz, simpleClassName);
        }
        return transaction.hasTable(TABLE_PREFIX+simpleClassName);
    }

    /**
     * Returns a typed RealmQuery, which can be used to query for specific objects of this type
     * @param clazz The class of the object which is to be queried for
     * @return A typed RealmQuery, which can be used to query for specific objects of this type
     * @see io.realm.RealmQuery
     * @throws java.lang.RuntimeException Any other error
     */
    public <E extends RealmObject> RealmQuery<E> where(Class<E> clazz) {
        return new RealmQuery<E>(this, clazz);
    }

    /**
     * Get all objects of a specific Class
     * @param clazz the Class to get objects of
     * @return A RealmResult list containing the objects
     * @see io.realm.RealmResults
     * @throws java.lang.RuntimeException Any other error
     */
    public <E extends RealmObject> RealmResults<E> allObjects(Class<E> clazz) {
        return where(clazz).findAll();
    }

    // Notifications

    /**
     * Add a change listener to the Realm
     * @param listener the change listener
     * @see io.realm.RealmChangeListener
     */
    public void addChangeListener(RealmChangeListener listener) {
        changeListeners.add(listener);
        LooperThread.handlers.put(handler, id);
    }

    /**
     * Remove the specified change listener
     * @param listener the change listener to be removed
     * @see io.realm.RealmChangeListener
     */
    public void removeChangeListener(RealmChangeListener listener) {
        changeListeners.remove(listener);
        if (changeListeners.isEmpty()) {
            LooperThread.handlers.remove(handler);
        }
    }

    /**
     * Remove all user-defined change listeners
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        changeListeners.clear();
        LooperThread.handlers.remove(handler);
    }

    void sendNotifications() {
        for(RealmChangeListener listener : changeListeners) {
            listener.onChange();
        }
    }

    boolean hasChanged() {
        return sharedGroup.hasChanged();
    }

    /**
     * Transactions
     */

//    public void refresh() {
//        transaction.advanceRead();
//    }

    /**
     * Starts a write transaction, this must be closed with commitTransaction().
     * Notice: it is not possible to nest write transactions. If you start a write
     * transaction within a write transaction an exception is thrown.
     *
     * @throws io.realm.exceptions.RealmException If already in a write transaction.
     * @throws java.lang.RuntimeException Any other error.
     */
    public void beginTransaction() {
        transaction.promoteToWrite();
    }

    /**
     * Commits a write transaction
     * @throws java.lang.RuntimeException Any other error
     */
    public void commitTransaction() {
        transaction.commitAndContinueAsRead();

        Message message = Message.obtain();
        message.arg1 = LooperThread.REALM_CHANGED;
        message.arg2 = id;
        if (looperThread.handler != null) {
            looperThread.handler.sendMessage(message);
        } else {
            Log.i(TAG, "The LooperThread is not up and running yet. Commit message not sent");
        }
    }

    /**
     * Remove all objects of the specified class
     * @param classSpec The class which objects should be removed
     * @throws java.lang.RuntimeException Any other error
     */
    public void clear(Class<?> classSpec) {
        getTable(classSpec).clear();
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

    static public void migrateRealmAtPath(String realmPath, RealmMigration migration) {
        migrateRealmAtPath(realmPath, null, migration);
    }

    static public void migrateRealmAtPath(String realmPath, byte [] key, RealmMigration migration) {
        Realm realm = Realm.createAndValidate(realmPath, key, false);
        realm.beginTransaction();
        realm.setVersion(migration.execute(realm, realm.getVersion()));
        realm.commitTransaction();
    }

    /**
     * Delete the Realm file from the filesystem for the default Realm (named "default.realm").
     * The realm must be unused and closed before calling this method.
     * WARNING: Your Realm must not be open (typically when your app launch).
     * @param context an Android context.
     * @return false if a file could not be deleted. The failing file will be logged.
     * @see io.realm.Realm#clear(Class)
     */
    public static boolean deleteRealmFile(Context context) {
        return deleteRealmFile(context, DEFAULT_REALM_NAME);
    }

    /**
     * Delete the Realm file from the filesystem for a custom named Realm.
     * The realm must be unused and closed before calling this method.
     * @param context an Android context.
     * @param fileName the name of the custom Realm (i.e. "myCustomRealm.realm").
     * @return false if a file could not be deleted. The failing file will be logged.
     */
    public static boolean deleteRealmFile(Context context, String fileName) {
        boolean result = true;
        File writableFolder = context.getFilesDir();
        List<File> filesToDelete = Arrays.asList(
                new File(writableFolder, fileName),
                new File(writableFolder, fileName + ".lock"));
        for (File fileToDelete : filesToDelete) {
            if (fileToDelete.exists()) {
                boolean deleteResult = fileToDelete.delete();
                if (!deleteResult) {
                    result = false;
                    Log.w(TAG, "Could not delete the file " + fileToDelete);
                }
            }
        }
        return result;
    }
}
