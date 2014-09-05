package io.realm.typed;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.realm.ImplicitTransaction;
import io.realm.Row;
import io.realm.SharedGroup;
import io.realm.Table;


public class Realm {

    private static SharedGroup.Durability defaultDurability = SharedGroup.Durability.FULL;

    private SharedGroup sg;
    private ImplicitTransaction transaction;
    private String filePath;
    private int version;
    private ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

    public ImplicitTransaction getTransaction() {return transaction;}

    private List<RealmChangeListener> changeListeners;
    boolean runEventHandler = false;

    public Realm(File writeablePath) throws IOException {
        this(writeablePath, "default.realm");
    }

    public Realm(File writeablePath, String filePath) throws IOException {
        this.filePath = new File(writeablePath, filePath).getAbsolutePath();

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

    /**
     * Instantiates and adds a new object to the realm
     *
     * @return              The new object
     * @param <E>
     */
    public <E extends RealmObject> E create(Class<E> classSpec) {
        Table table = null;
        try {
            String className = classSpec.getName() + "RealmProxy";
            Class cl = Class.forName(className);
            Method method = cl.getMethod("initTable", new Class[] {io.realm.ImplicitTransaction.class});
            table = (Table)method.invoke(null, new Object[] {transaction});
            long rowIndex = table.addEmptyRow();
            E obj = get(classSpec, rowIndex);
            obj.realmAddedAtRowIndex = rowIndex;
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public <E> void remove(Class<E> clazz, long objectIndex) {
        getTable(clazz).moveLastOver(objectIndex);
    }

    private Map<String, List<Field>> cache = new HashMap<String, List<Field>>();


    <E extends RealmObject> E get(Class<E> clazz, long rowIndex) {

        E obj = null;

        try {
            Row row = transaction.getTable(clazz.getSimpleName()).getRow(rowIndex);

            String className = clazz.getName()+"RealmProxy";

            Class cl = Class.forName(className);
            Constructor con = cl.getConstructor();
            obj = (E)con.newInstance();
            obj.row = row;
        } catch (Exception e) {
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
}
