package io.realm;

import java.io.File;
import java.io.IOException;
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

    private SharedGroup sg;
    private ImplicitTransaction transaction;
    private String filePath;
    private int version;
    private File bytecodeCache;
    private ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

    private Map<Class<?>, String> generatedClassNames = new HashMap<Class<?>, String>();
    private Map<Class<?>, String> simpleClassNames = new HashMap<Class<?>, String>();
    private Map<String, Class<?>> generatedClasses = new HashMap<String, Class<?>>();
    private Map<Class<?>, Method> initTableMethods = new HashMap<Class<?>, Method>();
    private Map<Class<?>, Constructor> constructors = new HashMap<Class<?>, Constructor>();
    private Map<Class<?>, Constructor> generatedConstructors = new HashMap<Class<?>, Constructor>();

    private List<RealmChangeListener> changeListeners;
    boolean runEventHandler = false;

    public Realm(File writeablePath) throws IOException {
        this(writeablePath, "default.realm");
    }

    public Realm(File writeablePath, String filePath) throws IOException {
        this.filePath = new File(writeablePath, filePath).getAbsolutePath();
        File bytecodeCache = new File(writeablePath, "dx");
        if (!bytecodeCache.exists()) {
            boolean success = bytecodeCache.mkdirs();
            if (!success) {
                throw new IOException("Could not create the bytecode cache folder");
            }
        }

        this.bytecodeCache = bytecodeCache;
        this.changeListeners = new ArrayList<RealmChangeListener>();
        init();
    }

    private void startEventHandler() {
        runEventHandler = true;
        RealmEventHandler realmEventHandler = new RealmEventHandler(this);
        ses.scheduleWithFixedDelay(realmEventHandler, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void finalize() throws Throwable {
        transaction.endRead();
        System.out.println("finalize");
        super.finalize();
    }

    private void init() {
        this.sg = new SharedGroup(filePath, defaultDurability);
        this.transaction = sg.beginImplicitTransaction();
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

    /**
     * Instantiates and adds a new object to the realm
     *
     * @return              The new object
     * @param <E>
     */
    public <E extends RealmObject> E create(Class<E> clazz) {
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
                method = generatedClass.getMethod("initTable", new Class[] {ImplicitTransaction.class});
                initTableMethods.put(generatedClass, method);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null; // TODO: throw RealmException
        }

        Table table;
        try {
            table = (Table)method.invoke(null, transaction);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null; // TODO: throw RealmException
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null; // TODO: throw RealmException
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
    <E extends RealmObject> E get(Class<E> clazz, long rowIndex) {
        String generatedClassName;
        if (generatedClassNames.containsKey(clazz)) {
            generatedClassName = generatedClassNames.get(clazz);
        } else {
            generatedClassName = clazz.getName() + "RealmProxy";
            generatedClassNames.put(clazz, generatedClassName);
        }
        return get(clazz, rowIndex, generatedClassName);
    }


    private <E extends RealmObject> E get(Class<E> clazz, long rowIndex, String generatedClassName) {
        E result;

        String simpleClassName;
        if (simpleClassNames.containsKey(clazz)) {
            simpleClassName = simpleClassNames.get(clazz);
        } else {
            simpleClassName = clazz.getSimpleName();
            simpleClassNames.put(clazz, simpleClassName);
        }

        Row row = transaction.getTable(simpleClassName).getRow(rowIndex);

        Constructor constructor;
        if (generatedConstructors.containsKey(clazz)) {
            constructor = generatedConstructors.get(clazz);
        } else {

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


    public <E extends RealmObject> RealmTableOrViewList<E> allObjects(Class<E> clazz) {
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
        return sg.hasChanged();
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
        if (sg.hasChanged()) {
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

    public void clear() {
        transaction.endRead();
        sg.close();
        new File(filePath).delete();
        new File(filePath+".lock").delete();
        init();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    private File getBytecodeCache() {
        return bytecodeCache;
    }

}
