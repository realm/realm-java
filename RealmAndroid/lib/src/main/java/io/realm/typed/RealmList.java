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

public class RealmList<T> extends AbstractList<T> {

    private Class<T> type;

    private Context context;

    private SharedGroup sg;
    private Group transaction;

    private TableOrView dataStore = null;


    public RealmList(Class<T> type, Context context) {
        this(type, context, context.getFilesDir()+"/default.realm");
    }

    public RealmList(Class<T> type, Context context, String filePath) {
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

    RealmList(RealmList<T> realm, TableOrView dataStore) {
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

    public T create() {
        try {
            T obj = ProxyBuilder.forClass(this.type)
                    .dexCache(this.context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(new RealmProxy<T>(this, -1))
                    .build();
            return obj;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RealmQuery<T> where() {
        return new RealmQuery<T>(this);
    }

    @Override
    public void add(int rowIndex, T element) {

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

    @Override
    public T remove(int rowIndex) {
        this.dataStore.remove(rowIndex);
        return null;
    }

    @Override
    public T set(int rowIndex, T element) {

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



    @Override
    public T get(int rowIndex) {
        try {
            T obj = ProxyBuilder.forClass(this.type)
                    .dexCache(this.context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(new RealmProxy<T>(this, rowIndex))
                    .build();
            ((RealmProxy)ProxyBuilder.getInvocationHandler(obj)).realmSetRowIndex(rowIndex);
            return obj;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int size() {
        return ((Long)this.dataStore.size()).intValue();
    }




}
