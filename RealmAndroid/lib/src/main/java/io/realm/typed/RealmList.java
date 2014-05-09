package io.realm.typed;

import android.content.Context;

import com.google.dexmaker.stock.ProxyBuilder;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.AbstractList;
import java.util.Date;

import io.realm.ColumnType;
import io.realm.Group;
import io.realm.ReadTransaction;
import io.realm.SharedGroup;
import io.realm.Table;
import io.realm.TableOrView;
import io.realm.WriteTransaction;

/**
 *
 * @param <E> The type of objects to be persisted in this list
 */
public class RealmList<E> extends AbstractList<E> {

    private Class<E> type;

    private Context context;

    private SharedGroup sg;
    private Group transaction;

    private TableOrView dataStore = null;

    /**
     *
     * @param type
     * @param context
     */
    RealmList(Class<E> type, Context context) {
        this(type, context, context.getFilesDir()+"/default.realm");
    }

    /**
     *
     * @param type
     * @param context
     * @param filePath
     */
    RealmList(Class<E> type, Context context, String filePath) {
        this.context = context;
        this.sg = new SharedGroup(filePath);

        this.type = type;


        WriteTransaction wt = sg.beginWrite();
        try {

            if (!wt.hasTable(type.getName())) {

                Table table = wt.getTable(type.getName());

                for (Field f : this.type.getDeclaredFields()) {

                    Class<?> fieldType = f.getType();


                    if (fieldType.equals(String.class)) {
                        table.addColumn(ColumnType.STRING, f.getName());
                    } else if (fieldType.equals(int.class) || fieldType.equals(long.class) || fieldType.equals(Integer.class) || fieldType.equals(Long.class)) {
                        table.addColumn(ColumnType.INTEGER, f.getName());
                    } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                        table.addColumn(ColumnType.DOUBLE, f.getName());
                    } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                        table.addColumn(ColumnType.FLOAT, f.getName());
                    } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                        table.addColumn(ColumnType.BOOLEAN, f.getName());
                    } else if (fieldType.equals(Date.class)) {
                        table.addColumn(ColumnType.DATE, f.getName());
                    } else {
                        System.err.println("Type not supported: " + fieldType.getName());
                    }

                }

            }

            wt.commit();

        } catch(Throwable t) {
            t.printStackTrace();
            wt.rollback();
        }

        this.transaction = sg.beginRead();
        this.dataStore = this.transaction.getTable(this.type.getName());

    }

    RealmList(RealmList<E> realm, TableOrView dataStore) {
        this.context = realm.context;
        this.type = realm.type;
        this.dataStore = dataStore;
    }

    TableOrView getDataStore() {
        return this.dataStore;
    }

    void refreshReadTransaction() {
        ((ReadTransaction)this.transaction).endRead();
        this.transaction = this.sg.beginRead();
        this.dataStore = this.transaction.getTable(this.type.getName());
    }

    void beginWriteTransaction() {
        ((ReadTransaction)this.transaction).endRead();
        this.transaction = this.sg.beginWrite();
        this.dataStore = this.transaction.getTable(this.type.getName());
    }

    void commitWriteTransaction() {
        ((WriteTransaction)this.transaction).commit();
        this.transaction = this.sg.beginRead();
        this.dataStore = this.transaction.getTable(this.type.getName());
    }

    void rollbackWriteTransaction() {
        ((WriteTransaction)this.transaction).rollback();
        this.transaction = this.sg.beginRead();
        this.dataStore = this.transaction.getTable(this.type.getName());
    }

    /**
     * Instantiates and adds a new object to the list
     *
     * @return          The new object
     */
    public E create() {
        try {
            E obj = ProxyBuilder.forClass(this.type)
                    .dexCache(this.context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(new RealmProxy(this, -1))
                    .build();
            return obj;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a RealmQuery, used to filter this collection
     *
     * @return          A RealmQuery to filter the list
     */
    public RealmQuery<E> where() {
        return new RealmQuery<E>(this);
    }

    /**
     *
     * @param rowIndex  The index position where the object should be inserted
     * @param element   The object to insert
     */
    @Override
    public void add(int rowIndex, E element) {

        Field[] fields = element.getClass().getSuperclass().getDeclaredFields();

        Object[] row = new Object[fields.length];

        // Inspect fields and add them
        for(int i = 0; i < fields.length; i++) {

            Field f = fields[i];
            f.setAccessible(true);


            try {

                row[i] = f.get(element);

            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        beginWriteTransaction();
        try {

            ((Table)this.dataStore).addAt(rowIndex, row);

            ((RealmProxy)ProxyBuilder.getInvocationHandler(element)).realmSetRowIndex(rowIndex);

            commitWriteTransaction();

        } catch(Throwable t) {
            t.printStackTrace();
            rollbackWriteTransaction();
        }

    }

    /**
     *
     * @param rowIndex  The index of the object to be removed
     * @return          Always returns null, as the object is no longer backed by Realm
     */
    @Override
    public E remove(int rowIndex) {
        this.dataStore.remove(rowIndex);
        return null;
    }

    /**
     *
     * @param rowIndex  The index position where the object should be inserted
     * @param element   The object to insert
     * @return          The inserted object
     */
    @Override
    public E set(int rowIndex, E element) {

        Field[] fields = element.getClass().getDeclaredFields();

        beginWriteTransaction();
        try {

            // Inspect fields and add them
            for (int i = 0; i < fields.length; i++) {

                Field f = fields[i];

                Class<?> fieldType = f.getType();

                System.out.println(f.getName());
                long columnIndex = this.dataStore.getColumnIndex(f.getName());

                f.setAccessible(true);

                try {

                    if (fieldType.equals(String.class)) {
                        this.dataStore.setString(columnIndex, rowIndex, (String) f.get(element));
                    } else if (fieldType.equals(int.class) || fieldType.equals(long.class) || fieldType.equals(Integer.class) || fieldType.equals(Long.class)) {
                        this.dataStore.setLong(columnIndex, rowIndex, f.getLong(element));
                    } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                        this.dataStore.setDouble(columnIndex, rowIndex, f.getDouble(element));
                    } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                        this.dataStore.setFloat(columnIndex, rowIndex, f.getFloat(element));
                    } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                        this.dataStore.setBoolean(columnIndex, rowIndex, f.getBoolean(element));
                    } else if (fieldType.equals(Date.class)) {
                        this.dataStore.setDate(columnIndex, rowIndex, (Date) f.get(element));
                    } else {
                        System.err.println("Type not supported: " + fieldType.getName());
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

            commitWriteTransaction();

        } catch(Throwable t) {
            t.printStackTrace();
            rollbackWriteTransaction();
        }

        return this.get(rowIndex);

    }

    /**
     *
     * @param rowIndex  The objects index in the list
     * @return          An object of type T, which is backed by Realm
     */
    @Override
    public E get(int rowIndex) {
        try {
            E obj = ProxyBuilder.forClass(this.type)
                    .dexCache(this.context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(new RealmProxy(this, rowIndex))
                    .build();
            ((RealmProxy)ProxyBuilder.getInvocationHandler(obj)).realmSetRowIndex(rowIndex);
            return obj;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @return          The number of elements in this RealmList
     */
    @Override
    public int size() {
        return ((Long)this.dataStore.size()).intValue();
    }




}
