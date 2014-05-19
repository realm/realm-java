package io.realm.typed;

import android.content.Context;

import com.google.dexmaker.stock.ProxyBuilder;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.AbstractList;
import java.util.Date;

import io.realm.Table;
import io.realm.TableOrView;

/**
 *
 * @param <E> The class of objects in this list
 */
public class RealmList<E> extends AbstractList<E> {

    private Class<E> classSpec;
    private Realm realm;
    private TableOrView table = null;

    RealmList(Realm realm, Class<E> classSpec) {
        this.realm = realm;
        this.classSpec = classSpec;
    }

    RealmList(Realm realm, TableOrView table, Class<E> classSpec) {
        this(realm, classSpec);
        this.table = table;
    }

    TableOrView getTable() {
        if(table == null) {
            return realm.getTable(classSpec);
        } else {
            return table;
        }
    }


    /**
     * Returns a RealmQuery, used to filter this RealmList
     *
     * @return          A RealmQuery to filter the list
     */
    public RealmQuery<E> where() {
        return new RealmQuery<E>(realm, classSpec);
    }

    /**
     *
     * @param rowIndex      The index position where the object should be inserted
     * @param element       The object to insert
     */
    @Override
    public void add(int rowIndex, E element) {

        Field[] fields = element.getClass().getDeclaredFields();

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

        ((Table)getTable()).addAt(rowIndex, row);

    }

    /**
     *
     * @param rowIndex      The index of the object to be removed
     * @return              Always returns null, as the object is no longer backed by Realm
     */
    @Override
    public E remove(int rowIndex) {
        getTable().remove(rowIndex);
        return null;
    }

    /**
     *
     * @param rowIndex      The index position where the object should be inserted
     * @param element       The object to insert
     * @return              The inserted object
     */
    @Override
    public E set(int rowIndex, E element) {

        Field[] fields = element.getClass().getDeclaredFields();

        TableOrView dataStore = getTable();

        // Inspect fields and add them
        for (int i = 0; i < fields.length; i++) {

            Field f = fields[i];

            Class<?> fieldType = f.getType();

            System.out.println(f.getName());
            long columnIndex = dataStore.getColumnIndex(f.getName());

            f.setAccessible(true);

            try {

                if (fieldType.equals(String.class)) {
                    dataStore.setString(columnIndex, rowIndex, (String) f.get(element));
                } else if (fieldType.equals(int.class) || fieldType.equals(long.class) || fieldType.equals(Integer.class) || fieldType.equals(Long.class)) {
                    dataStore.setLong(columnIndex, rowIndex, f.getLong(element));
                } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                    dataStore.setDouble(columnIndex, rowIndex, f.getDouble(element));
                } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                    dataStore.setFloat(columnIndex, rowIndex, f.getFloat(element));
                } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                    dataStore.setBoolean(columnIndex, rowIndex, f.getBoolean(element));
                } else if (fieldType.equals(Date.class)) {
                    dataStore.setDate(columnIndex, rowIndex, (Date) f.get(element));
                } else {
                    System.err.println("Type not supported: " + fieldType.getName());
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }


        return this.get(rowIndex);

    }

    /**
     *
     * @param rowIndex      The objects index in the list
     * @return              An object of type E, which is backed by Realm
     */
    @Override
    public E get(int rowIndex) {
        try {
            E obj = ProxyBuilder.forClass(classSpec)
                    .dexCache(realm.getContext().getDir("dx", Context.MODE_PRIVATE))
                    .handler(new RealmProxy(this, rowIndex))
                    .build();
            return obj;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @return              The number of elements in this RealmList
     */
    @Override
    public int size() {
        return ((Long)getTable().size()).intValue();
    }

}
