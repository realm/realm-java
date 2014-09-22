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

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.realm.internal.ImplicitTransaction;
import io.realm.internal.Row;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;


public class Realm {

    private static SharedGroup.Durability defaultDurability = SharedGroup.Durability.FULL;
    public static final String DEFAULT_REALM_NAME = "default.realm";
    private static final Map<String, ThreadRealm> realms = new HashMap<String, ThreadRealm>();

    private SharedGroup sharedGroup;
    private ImplicitTransaction transaction;
    private String filePath;
    private int version;
    private ScheduledExecutorService scheduledExecutorService
            = Executors.newSingleThreadScheduledExecutor();

    private Map<Class<?>, String> generatedClassNames = new HashMap<Class<?>, String>();
    private Map<Class<?>, String> simpleClassNames = new HashMap<Class<?>, String>();
    private Map<String, Class<?>> generatedClasses = new HashMap<String, Class<?>>();
    private Map<Class<?>, Method> initTableMethods = new HashMap<Class<?>, Method>();
    private Map<Class<?>, Constructor> constructors = new HashMap<Class<?>, Constructor>();
    private Map<Class<?>, Constructor> generatedConstructors = new HashMap<Class<?>, Constructor>();
    private Map<Class<?>, Table> tables = new HashMap<Class<?>, Table>();

    private List<RealmChangeListener> changeListeners = new ArrayList<RealmChangeListener>();
    boolean runEventHandler = false;

    // The constructor in private to enforce the use of the static one
    private Realm(String absolutePath) {
        this.filePath = absolutePath;
        this.sharedGroup = new SharedGroup(filePath, defaultDurability);
        this.transaction = sharedGroup.beginImplicitTransaction();
    }

    private void startEventHandler() {
        runEventHandler = true;
        RealmEventHandler realmEventHandler = new RealmEventHandler(this);
        scheduledExecutorService.scheduleWithFixedDelay(realmEventHandler, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void finalize() throws Throwable {
        transaction.endRead();
        super.finalize();
    }

    public static void setDefaultDurability(SharedGroup.Durability durability) {
        defaultDurability = durability;
    }

    public Table getTable(Class<?> clazz) {
        String simpleClassName;
        if (simpleClassNames.containsKey(clazz)) {
            simpleClassName = simpleClassNames.get(clazz);
        } else {
            simpleClassName = clazz.getSimpleName();
            simpleClassNames.put(clazz, simpleClassName);
        }
        return transaction.getTable(simpleClassName);
    }

    public static Realm create(Context context) {
        return Realm.create(context, DEFAULT_REALM_NAME);
    }

    public static Realm create(Context context, String fileName) {
        return Realm.create(context.getFilesDir(), fileName);
    }

    public static Realm create(File writeableFolder) {
        return Realm.create(writeableFolder, DEFAULT_REALM_NAME);
    }

    public static Realm create(File writableFolder, String filename) {
        String absolutePath = new File(writableFolder, filename).getAbsolutePath();
        return create(absolutePath);
    }

    private static Realm create(String absolutePath) {
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
     *
     * @return              The new object
     * @param <E>
     */
    public <E extends RealmObject> E createObject(Class<E> clazz) {
        Table table;
        table = tables.get(clazz);
        if (table == null) {
            String generatedClassName;
            if (generatedClassNames.containsKey(clazz)) {
                generatedClassName = generatedClassNames.get(clazz);
            } else {
                generatedClassName = clazz.getName() + "RealmProxy";
                generatedClassNames.put(clazz, generatedClassName);
            }

            Class<?> generatedClass;
            try {
                if (generatedClasses.containsKey(generatedClassName)) {
                    generatedClass = generatedClasses.get(generatedClassName);
                } else {
                    generatedClass = Class.forName(generatedClassName);
                    generatedClasses.put(generatedClassName, generatedClass);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null; // TODO: throw RealmException
            }

            Method method;
            try {
                if (initTableMethods.containsKey(generatedClass)) {
                    method = initTableMethods.get(generatedClass);
                } else {
                    method = generatedClass.getMethod("initTable", new Class[]{ImplicitTransaction.class});
                    initTableMethods.put(generatedClass, method);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null; // TODO: throw RealmException
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


    public <E> void remove(Class<E> clazz, long objectIndex) {
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

    public <E extends RealmObject> E get(Class<E> clazz, long rowIndex) {
        E result;

        String generatedClassName = null;
        Table table = tables.get(clazz);
        if (table == null) {
            if (generatedClassNames.containsKey(clazz)) {
                generatedClassName = generatedClassNames.get(clazz);
            } else {
                generatedClassName = clazz.getName() + "RealmProxy";
                generatedClassNames.put(clazz, generatedClassName);
            }

            String simpleClassName;
            if (simpleClassNames.containsKey(clazz)) {
                simpleClassName = simpleClassNames.get(clazz);
            } else {
                simpleClassName = clazz.getSimpleName();
                simpleClassNames.put(clazz, simpleClassName);
            }
            table = transaction.getTable(simpleClassName);
            tables.put(clazz, table);
        }

        Row row = table.getRow(rowIndex);

        Constructor constructor;
        constructor = generatedConstructors.get(clazz);
        if (constructor == null) {
            if (generatedClassName == null) {
                generatedClassName = generatedClassNames.get(clazz);
                if (generatedClassName == null) {
                    generatedClassName = clazz.getName() + "RealmProxy";
                    generatedClassNames.put(clazz, generatedClassName);
                }
            }

            Class<?> generatedClass;
            try {
                if (generatedClasses.containsKey(generatedClassName)) {
                    generatedClass = generatedClasses.get(generatedClassName);
                } else {
                    generatedClass = Class.forName(generatedClassName);
                    generatedClasses.put(generatedClassName, generatedClass);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null; // TODO: throw RealmException
            }

            try {
                if (constructors.containsKey(generatedClass)) {
                    constructor = constructors.get(generatedClass);
                } else {
                    constructor = generatedClass.getConstructor();
                    constructors.put(generatedClass, constructor);
                    generatedConstructors.put(clazz, constructor);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null; // TODO: throw RealmException
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

    public boolean contains(Class<?> clazz) {
        String simpleClassName;
        if (simpleClassNames.containsKey(clazz)) {
            simpleClassName = simpleClassNames.get(clazz);
            simpleClassNames.put(clazz, simpleClassName);
        } else {
            simpleClassName = clazz.getSimpleName();
        }

        return transaction.hasTable(simpleClassName);
    }

    /**
     * Returns a typed RealmQuery, which can be used to query for specific objects of this type
     *
     * @param clazz         The class of the object which is to be queried for
     * @param <E extends RealmObject>
     * @return
     */
    public <E extends RealmObject> RealmQuery<E> where(Class<E> clazz) {
        return new RealmQuery<E>(this, clazz);
    }


    public <E extends RealmObject> RealmResults<E> allObjects(Class<E> clazz) {
        return where(clazz).findAll();
    }


    // Migration

    public void ensureRealmAtVersion(int version, RealmMigration migration) {
        migration.execute(this, version);
    }

    // Notifications

    public void addChangeListener(RealmChangeListener listener) {
        changeListeners.add(listener);
        if(!runEventHandler) {
            startEventHandler();
        }
    }

    public void removeChangeListener(RealmChangeListener listener) {
        changeListeners.remove(listener);
        if(runEventHandler && changeListeners.isEmpty()) {
            runEventHandler = false;
        }
    }

    public void removeAllChangeListeners() {
        changeListeners.clear();
    }

    void sendNotifications() {
        for(RealmChangeListener listener : changeListeners) {
            listener.onChange();
        }
        if(runEventHandler && changeListeners.isEmpty()) {
            runEventHandler = false;
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
     * Starts a write transaction, this must be closed with either commit() or rollback()
     */
    public void beginWrite() {

        // If we are moving the transaction forward, send local notifications
        if (sharedGroup.hasChanged()) {
            sendNotifications();
        }

        transaction.promoteToWrite();
    }

    /**
     * Commits a write transaction
     */
    public void commit() {
        transaction.commitAndContinueAsRead();

        // Send notifications because we did a local change
        sendNotifications();

    }

    public void clear(Class<?> classSpec) {
        getTable(classSpec).clear();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getFilePath() {
        return filePath;
    }
}
