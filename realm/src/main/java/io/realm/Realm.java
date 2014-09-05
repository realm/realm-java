package io.realm;

import com.google.dexmaker.stock.ProxyBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
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

    private List<RealmChangeListener> changeListeners;
    boolean runEventHandler = false;


    /**
     * Obtains an instance of the default Realm.
     *
     * @throws IOException
     */
    public Realm() throws IOException {
        // FIXME: find a way to deduce default path
        this("/", "default.realm");
    }

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

    public Table getTable(Class<?> classSpec) {
        return transaction.getTable(classSpec.getSimpleName());
    }


    private <E> void initTable(Class<E> classSpec) {

        // Check for table existence
        if(!transaction.hasTable(classSpec.getSimpleName())) {
            // Create the table
            Table table = transaction.getTable(classSpec.getSimpleName());

            System.out.println(classSpec.getSimpleName());

            Field[] fields = classSpec.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {

                Field f = fields[i];

                Class<?> fieldType = f.getType();

                System.out.println(f.getName() + " - " + fieldType.getName());


                if (fieldType.equals(String.class)) {
                    table.addColumn(ColumnType.STRING, f.getName().toLowerCase(Locale.getDefault()));
                } else if (fieldType.equals(int.class) || fieldType.equals(long.class) || fieldType.equals(Integer.class) || fieldType.equals(Long.class)) {
                    table.addColumn(ColumnType.INTEGER, f.getName().toLowerCase(Locale.getDefault()));
                } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                    table.addColumn(ColumnType.DOUBLE, f.getName().toLowerCase(Locale.getDefault()));
                } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                    table.addColumn(ColumnType.FLOAT, f.getName().toLowerCase(Locale.getDefault()));
                } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                    table.addColumn(ColumnType.BOOLEAN, f.getName().toLowerCase(Locale.getDefault()));
                } else if (fieldType.equals(Date.class)) {
                    table.addColumn(ColumnType.DATE, f.getName().toLowerCase(Locale.getDefault()));
                } else if (fieldType.equals(byte[].class)) {
                    table.addColumn(ColumnType.BINARY, f.getName().toLowerCase(Locale.getDefault()));
                } else if (RealmObject.class.equals(fieldType.getSuperclass())) {
                    // Link
                    initTable(fieldType);
                    table.addColumnLink(ColumnType.LINK, f.getName().toLowerCase(Locale.getDefault()), getTable(fieldType));
                } else if (RealmList.class.isAssignableFrom(fieldType)) {
                    // Link List
                    Type genericType = f.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pType = (ParameterizedType) genericType;
                        Class<?> actual = (Class<?>) pType.getActualTypeArguments()[0];
                        if(RealmObject.class.equals(actual.getSuperclass())) {
                            initTable(actual);
                            table.addColumnLink(ColumnType.LINK_LIST, f.getName().toLowerCase(Locale.getDefault()), getTable(actual));
                        }
                    }
                } else {
                    System.err.println("Type not supported: " + fieldType.getName());
                }


            }

        }

    }


    /**
     * Instantiates and adds a new object to the realm
     *
     * @return              The new object
     * @param <E>
     */
    public <E extends RealmObject> E create(Class<E> classSpec) {

        initTable(classSpec);

        Table table = getTable(classSpec);

        long rowIndex = table.addEmptyRow();

        return get(classSpec, rowIndex);
    }


    public <E> void remove(Class<E> clazz, long objectIndex) {
        getTable(clazz).moveLastOver(objectIndex);
    }

    private Map<String, List<Field>> cache = new HashMap<String, List<Field>>();

    /**
     * Adds an object to the realm, and returns a new instance which is backed by the Realm
     *
     * @param element           The element to add to this realm.
     * @param <E>
     * @return
     */
    public <E extends RealmObject> void add(E element) {

        System.out.println("Adding " + element.getClass().getName());

        initTable(element.getClass());

        String className = element.getClass().getSimpleName();

        if(!cache.containsKey(className)) {


            List<Field> fields = Arrays.asList(element.getClass().getDeclaredFields());
            List<Field> persistedFields = new ArrayList<Field>();
            for(Field f : fields) {
                if(f.getType().equals(String.class) ||
                        f.getType().equals(int.class) ||
                        f.getType().equals(Integer.class) ||
                        f.getType().equals(long.class) ||
                        f.getType().equals(Long.class) ||
                        f.getType().equals(float.class) ||
                        f.getType().equals(Float.class) ||
                        f.getType().equals(double.class) ||
                        f.getType().equals(Double.class) ||
                        f.getType().equals(boolean.class) ||
                        f.getType().equals(Boolean.class) ||
                        f.getType().equals(Date.class) ||
                        f.getType().equals(byte[].class) ||
                        RealmObject.class.equals(f.getType().getSuperclass())

                        ) {

                    f.setAccessible(true);
                    persistedFields.add(f);
                } else if (RealmList.class.isAssignableFrom(f.getType())) {
                    // Link List
                    Type genericType = f.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pType = (ParameterizedType) genericType;
                        Class<?> actual = (Class<?>) pType.getActualTypeArguments()[0];
                        if(RealmObject.class.equals(actual.getSuperclass())) {
                            f.setAccessible(true);
                            persistedFields.add(f);
                        }
                    }
                }
            }

            cache.put(className, persistedFields);

        }

        Table table = getTable(element.getClass());
        long rowIndex = table.addEmptyRow();
        long columnIndex = 0;

        element.realmAddedAtRowIndex = rowIndex;

        List<Field> fields = cache.get(className);

        // Inspect fields and add them
        for(Field f : fields) {

            try {
                Class<?> type = f.getType();

                if(type.equals(String.class)) {
                    table.setString(columnIndex, rowIndex, (String)f.get(element));
                } else if(type.equals(int.class) || type.equals(Integer.class)) {
                    table.setLong(columnIndex, rowIndex, f.getInt(element));
                } else if(type.equals(long.class) || type.equals(Long.class)) {
                    table.setLong(columnIndex, rowIndex, f.getLong(element));
                } else if(type.equals(double.class) || type.equals(Double.class)) {
                    table.setDouble(columnIndex, rowIndex, f.getDouble(element));
                } else if(type.equals(float.class) || type.equals(Float.class)) {
                    table.setFloat(columnIndex, rowIndex, f.getFloat(element));
                } else if(type.equals(boolean.class) || type.equals(Boolean.class)) {
                    table.setBoolean(columnIndex, rowIndex, f.getBoolean(element));
                } else if(type.equals(Date.class)) {
                    table.setDate(columnIndex, rowIndex, (Date)f.get(element));
                } else if(type.equals(byte[].class)) {
                    table.setBinaryByteArray(columnIndex, rowIndex, (byte[])f.get(element));
                } else if(RealmObject.class.equals(f.getType().getSuperclass())) {

                    RealmObject linkedObject = (RealmObject)f.get(element);
                    if(linkedObject != null) {
                        if(linkedObject.realmGetRow() == null) {
                            if(linkedObject.realmAddedAtRowIndex == -1) {
                                add(linkedObject);
                            }
                            table.setLink(columnIndex, rowIndex, linkedObject.realmAddedAtRowIndex);
                        } else {
                            table.setLink(columnIndex, rowIndex, linkedObject.realmGetRow().getIndex());
                        }
                    }

                } else if (RealmList.class.isAssignableFrom(f.getType())) {
                    // Link List
                    Type genericType = f.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pType = (ParameterizedType) genericType;
                        Class<?> actual = (Class<?>) pType.getActualTypeArguments()[0];
                        if(RealmObject.class.equals(actual.getSuperclass())) {

                            LinkView links = table.getRow(rowIndex).getLinkList(columnIndex);

                            // Loop through list and add them to the link list and possibly to the realm
                            for(RealmObject linkedObject : (List<RealmObject>)f.get(element)) {

                                if(linkedObject.realmGetRow() == null) {
                                    if(linkedObject.realmAddedAtRowIndex == -1) {
                                        add(linkedObject);
                                    }
                                    links.add(linkedObject.realmAddedAtRowIndex);
                                } else {
                                    links.add(linkedObject.realmGetRow().getIndex());
                                }
                            }
                        }
                    }
                }

            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }

            columnIndex++;
        }

    }

    <E extends RealmObject> E get(Class<E> clazz, long rowIndex) {

        E obj = null;

        try {
            Row row = transaction.getTable(clazz.getSimpleName()).getRow(rowIndex);
            obj = ProxyBuilder.forClass(clazz)
                    .parentClassLoader(clazz.getClassLoader())
                    .dexCache(getBytecodeCache())
                    .handler(new RealmProxy(this, row))
                    .build();
            obj.realmSetRow(row);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return obj;
    }

    public boolean contains(Class<?> clazz) {
        return transaction.hasTable(clazz.getSimpleName());
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
