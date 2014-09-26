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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.internal.ImplicitTransaction;
import io.realm.internal.Row;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;
import io.realm.internal.android.LooperThread;


public class Realm {
    public static final String DEFAULT_REALM_NAME = "default.realm";
    private static final Map<String, ThreadRealm> realms = new HashMap<String, ThreadRealm>();
    private static final String TAG = "REALM";
    private static final String TABLE_PREFIX = "class_";
    private static boolean validated = false;

    private static SharedGroup.Durability defaultDurability = SharedGroup.Durability.FULL;

    private final int id;
    private final LooperThread looperThread = LooperThread.getInstance();
    private final SharedGroup sharedGroup;
    private final ImplicitTransaction transaction;
    private final Map<Class<?>, String> simpleClassNames = new HashMap<Class<?>, String>();
    private final Map<String, Class<?>> generatedClasses = new HashMap<String, Class<?>>();
    private final Map<Class<?>, Method> initTableMethods = new HashMap<Class<?>, Method>();
    private final Map<Class<?>, Constructor> constructors = new HashMap<Class<?>, Constructor>();
    private final Map<Class<?>, Constructor> generatedConstructors = new HashMap<Class<?>, Constructor>();
    private final Map<Class<?>, Table> tables = new HashMap<Class<?>, Table>();
    private final List<RealmChangeListener> changeListeners = new ArrayList<RealmChangeListener>();

    private int version;
    private Handler handler;
    private static boolean autoRefresh = true;

    // The constructor in private to enforce the use of the static one
    private Realm(String absolutePath) {
        this.sharedGroup = new SharedGroup(absolutePath, true);
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

    Table getTable(Class<?> clazz) {
        String simpleClassName = simpleClassNames.get(clazz);
        if (simpleClassName == null) {
            simpleClassName = clazz.getSimpleName();
            simpleClassNames.put(clazz, simpleClassName);
        }
        return transaction.getTable(TABLE_PREFIX + simpleClassName);
    }

    /**
     * Realm static constructor
     * @param context an Android context
     * @return an instance of the Realm class
     */
    public static Realm getInstance(Context context) {
        return Realm.getInstance(context, DEFAULT_REALM_NAME);
    }

    /**
     * Realm static constructor
     * @param context an Android context
     * @param fileName the name of the file to save the Realm to
     * @return an instance of the Realm class
     */
    public static Realm getInstance(Context context, String fileName) {
        return Realm.create(context.getFilesDir(), fileName);
    }

    /**
     * Realm static constructor
     * @param writableFolder absolute path to a writable directory
     * @return an instance of the Realm class
     */
    public static Realm getInstance(File writableFolder) {
        return Realm.create(writableFolder, DEFAULT_REALM_NAME);
    }

    /**
     * Realm static constructor
     * @param writableFolder absolute path to a writable directory
     * @param filename the name of the file to save the Realm to
     * @return an instance of the Realm class
     */
    public static Realm create(File writableFolder, String filename) {
        String absolutePath = new File(writableFolder, filename).getAbsolutePath();
        return createAndValidate(absolutePath, true);
    }

    private static Realm createAndValidate(String absolutePath, boolean validateSchema) {
        ThreadRealm threadRealm = realms.get(absolutePath);
        if (threadRealm == null) {
            threadRealm = new ThreadRealm(absolutePath);
            realms.put(absolutePath, threadRealm);
        }
        SoftReference<Realm> realmSoftReference = threadRealm.get();
        Realm realm = realmSoftReference.get();
        if (realm == null) {
            // The garbage collector decided to get rid of the realm instance
            threadRealm = new ThreadRealm(absolutePath);
            realms.put(absolutePath, threadRealm);
            realmSoftReference = threadRealm.get();
            realm = realmSoftReference.get();
        }
        if (validateSchema) {
            // FIXME - thread safety
            if (!validated) {
                try {
                    Class<?> validationClass = Class.forName("io.realm.ValidationList");
                    Method getProxyClassesMethod = validationClass.getMethod("getProxyClasses");
                    List<String> proxyClasses = (List<String>)getProxyClassesMethod.invoke(null);
                    for (String className : proxyClasses) {
                        Class<?> modelClass = Class.forName(className);
                        Method validateMethod = modelClass.getMethod("validateTable");
                        validateMethod.invoke(null, realm.transaction);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null; // TODO: throw RealmException
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    return null; // TODO: throw RealmException
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return null; // TODO: throw RealmException
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    return null; // TODO: throw RealmException
                }
                validated = true;
            }
        }
        return realm;
    }

    // This class stores soft-references to realm objects per thread per realm file
    private static class ThreadRealm extends ThreadLocal<SoftReference<Realm>> {
        private String absolutePath;

        private ThreadRealm(String absolutePath) {
            this.absolutePath = absolutePath;
        }

        @Override
        protected SoftReference<Realm> initialValue() {
            Realm realm = new Realm(absolutePath);
            return new SoftReference<Realm>(realm);
        }
    }

    /**
     * Instantiates and adds a new object to the realm
     * @return The new object
     * @param clazz The Class of the object to create
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
            String generatedClassName = "io.realm." + simpleClassName + "RealmProxy";

            Class<?> generatedClass = generatedClasses.get(generatedClassName);
            if (generatedClass == null) {
                try {
                    generatedClass = Class.forName(generatedClassName);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null; // TODO: throw RealmException
                }
                generatedClasses.put(generatedClassName, generatedClass);
            }

            Method method = initTableMethods.get(generatedClass);
            if (method == null) {
                try {
                    method = generatedClass.getMethod("initTable", new Class[]{ImplicitTransaction.class});
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    return null; // TODO: throw RealmException
                }
                initTableMethods.put(generatedClass, method);
            }

            try {
                table = (Table) method.invoke(null, transaction);
                tables.put(clazz, table);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null; // TODO: throw RealmException
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return null; // TODO: throw RealmException
            }
        }

        long rowIndex = table.addEmptyRow();
        return get(clazz, rowIndex);
    }


    <E> void remove(Class<E> clazz, long objectIndex) {
        getTable(clazz).moveLastOver(objectIndex);
    }

//    private Map<String, List<Field>> cache = new HashMap<String, List<Field>>();

//    /**
//     * Adds an object to the realm, and returns a new instance which is backed by the Realm
//     *
//     * @param element           The element to add to this realm.
//     * @param <E>
//     * @return
//     */
//    public <E extends RealmObject> void add(E element) {
//
//        System.out.println("Adding " + element.getClass().getName());
//
//        initTable(element.getClass());
//
//        String className = element.getClass().getSimpleName();
//
//        if(!cache.containsKey(className)) {
//
//
//            List<Field> fields = Arrays.asList(element.getClass().getDeclaredFields());
//            List<Field> persistedFields = new ArrayList<Field>();
//            for(Field f : fields) {
//                if(f.getType().equals(String.class) ||
//                        f.getType().equals(int.class) ||
//                        f.getType().equals(Integer.class) ||
//                        f.getType().equals(long.class) ||
//                        f.getType().equals(Long.class) ||
//                        f.getType().equals(float.class) ||
//                        f.getType().equals(Float.class) ||
//                        f.getType().equals(double.class) ||
//                        f.getType().equals(Double.class) ||
//                        f.getType().equals(boolean.class) ||
//                        f.getType().equals(Boolean.class) ||
//                        f.getType().equals(Date.class) ||
//                        f.getType().equals(byte[].class) ||
//                        RealmObject.class.equals(f.getType().getSuperclass())
//
//                        ) {
//
//                    f.setAccessible(true);
//                    persistedFields.add(f);
//                } else if (RealmList.class.isAssignableFrom(f.getType())) {
//                    // Link List
//                    Type genericType = f.getGenericType();
//                    if (genericType instanceof ParameterizedType) {
//                        ParameterizedType pType = (ParameterizedType) genericType;
//                        Class<?> actual = (Class<?>) pType.getActualTypeArguments()[0];
//                        if(RealmObject.class.equals(actual.getSuperclass())) {
//                            f.setAccessible(true);
//                            persistedFields.add(f);
//                        }
//                    }
//                }
//            }
//
//            cache.put(className, persistedFields);
//
//        }
//
//        Table table = getTable(element.getClass());
//        long rowIndex = table.addEmptyRow();
//        long columnIndex = 0;
//
//        element.realmAddedAtRowIndex = rowIndex;
//
//        List<Field> fields = cache.get(className);
//
//        // Inspect fields and add them
//        for(Field f : fields) {
//
//            try {
//                Class<?> type = f.getType();
//
//                if(type.equals(String.class)) {
//                    table.setString(columnIndex, rowIndex, (String)f.get(element));
//                } else if(type.equals(int.class) || type.equals(Integer.class)) {
//                    table.setLong(columnIndex, rowIndex, f.getInt(element));
//                } else if(type.equals(long.class) || type.equals(Long.class)) {
//                    table.setLong(columnIndex, rowIndex, f.getLong(element));
//                } else if(type.equals(double.class) || type.equals(Double.class)) {
//                    table.setDouble(columnIndex, rowIndex, f.getDouble(element));
//                } else if(type.equals(float.class) || type.equals(Float.class)) {
//                    table.setFloat(columnIndex, rowIndex, f.getFloat(element));
//                } else if(type.equals(boolean.class) || type.equals(Boolean.class)) {
//                    table.setBoolean(columnIndex, rowIndex, f.getBoolean(element));
//                } else if(type.equals(Date.class)) {
//                    table.setDate(columnIndex, rowIndex, (Date)f.get(element));
//                } else if(type.equals(byte[].class)) {
//                    table.setBinaryByteArray(columnIndex, rowIndex, (byte[])f.get(element));
//                } else if(RealmObject.class.equals(f.getType().getSuperclass())) {
//
//                    RealmObject linkedObject = (RealmObject)f.get(element);
//                    if(linkedObject != null) {
//                        if(linkedObject.realmGetRow() == null) {
//                            if(linkedObject.realmAddedAtRowIndex == -1) {
//                                add(linkedObject);
//                            }
//                            table.setLink(columnIndex, rowIndex, linkedObject.realmAddedAtRowIndex);
//                        } else {
//                            table.setLink(columnIndex, rowIndex, linkedObject.realmGetRow().getIndex());
//                        }
//                    }
//
//                } else if (RealmList.class.isAssignableFrom(f.getType())) {
//                    // Link List
//                    Type genericType = f.getGenericType();
//                    if (genericType instanceof ParameterizedType) {
//                        ParameterizedType pType = (ParameterizedType) genericType;
//                        Class<?> actual = (Class<?>) pType.getActualTypeArguments()[0];
//                        if(RealmObject.class.equals(actual.getSuperclass())) {
//
//                            LinkView links = table.getRow(rowIndex).getLinkList(columnIndex);
//
//                            // Loop through list and add them to the link list and possibly to the realm
//                            for(RealmObject linkedObject : (List<RealmObject>)f.get(element)) {
//
//                                if(linkedObject.realmGetRow() == null) {
//                                    if(linkedObject.realmAddedAtRowIndex == -1) {
//                                        add(linkedObject);
//                                    }
//                                    links.add(linkedObject.realmAddedAtRowIndex);
//                                } else {
//                                    links.add(linkedObject.realmGetRow().getIndex());
//                                }
//                            }
//                        }
//                    }
//                }
//
//            } catch(IllegalAccessException e) {
//                e.printStackTrace();
//            }
//
//            columnIndex++;
//        }
//
//    }

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
            String generatedClassName = "io.realm." + simpleClassName + "RealmProxy";


            Class<?> generatedClass = generatedClasses.get(generatedClassName);
            if (generatedClass == null) {
                try {
                    generatedClass = Class.forName(generatedClassName);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null; // TODO: throw RealmException
                }
                generatedClasses.put(generatedClassName, generatedClass);
            }

            constructor = constructors.get(generatedClass);
            if (constructor == null) {
                try {
                    constructor = generatedClass.getConstructor();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    return null; // TODO: throw RealmException
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
            e.printStackTrace();
            return null; // TODO: throw RealmException
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null; // TODO: throw RealmException
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null; // TODO: throw RealmException
        }
        result.realmSetRow(row);
        result.setRealm(this);
        return result;
    }

    boolean contains(Class<?> clazz) {
        String simpleClassName = simpleClassNames.get(clazz);
        if (simpleClassName == null) {
            simpleClassName = clazz.getSimpleName();
            simpleClassNames.put(clazz, simpleClassName);
        }
        return transaction.hasTable(simpleClassName);
    }

    /**
     * Returns a typed RealmQuery, which can be used to query for specific objects of this type
     * @param clazz The class of the object which is to be queried for
     * @return A typed RealmQuery, which can be used to query for specific objects of this type
     */
    public <E extends RealmObject> RealmQuery<E> where(Class<E> clazz) {
        return new RealmQuery<E>(this, clazz);
    }

    /**
     * Get all objects of a specific Class
     * @param clazz the Class to get objects of
     * @return A RealmResult list containing the objects
     */
    public <E extends RealmObject> RealmResults<E> allObjects(Class<E> clazz) {
        return where(clazz).findAll();
    }

    // Notifications
    public void addChangeListener(RealmChangeListener listener) {
        changeListeners.add(listener);
        LooperThread.handlers.put(handler, id);
    }

    public void removeChangeListener(RealmChangeListener listener) {
        changeListeners.remove(listener);
        if (changeListeners.isEmpty()) {
            LooperThread.handlers.remove(handler);
        }
    }

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

    // Transactions

    public void refresh() {
        transaction.advanceRead();
    }

    /**
     * Starts a write transaction, this must be closed with commitTransaction()
     */
    public void beginTransaction() {
        transaction.promoteToWrite();
    }

    /**
     * Commits a write transaction
     */
    public void commitTransaction() {
        transaction.commitAndContinueAsRead();

        Message message = Message.obtain();
        message.arg1 = LooperThread.REALM_CHANGED;
        message.arg2 = id;
        looperThread.handler.sendMessage(message);
    }

    public void clear(Class<?> classSpec) {
        getTable(classSpec).clear();
    }

    private int getVersion() {
        return version;
    }

    private void setVersion(int version) {
        this.version = version;
    }

    /**
     * Delete the Realm file from the filesystem for the default Realm (named "default.realm").
     * The realm must be unused and closed before calling this method.
     * @param context an Android context.
     * @return false if a file could not be deleted. The failing file will be logged.
     */
    public void migrateRealmAtPath(String realmPath, RealmMigration migration) {
        migration.execute(this, version);
    }

    /**
     * Delete the Realm file from the filesystem for the default Realm (named "default.realm").
     * The realm must be unused and closed before calling this method.
     * @param context an Android context.
     * @return false if a file could not be deleted. The failing file will be logged.
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
