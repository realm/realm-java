package com.realm.typed;

import android.content.Context;

import com.google.dexmaker.stock.ProxyBuilder;
import com.tightdb.ColumnType;
import com.tightdb.ReadTransaction;
import com.tightdb.SharedGroup;
import com.tightdb.Table;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Realm<T> implements List<T> {

    private Class<T> type;

    private Context context;

    private SharedGroup sg;
    private ReadTransaction rt;

    private Table table;

    public Realm(Class<T> type, Context context) {
        this(type, context, context.getFilesDir()+"/default.realm");
    }

    public Realm(Class<T> type, Context context, String filePath) {
        this.context = context;
        this.sg = new SharedGroup(filePath);

        this.table = new Table();


        this.type = type;

        for(Field f : this.type.getDeclaredFields()) {

            Class<?> fieldType = f.getType();


            if(fieldType.equals(String.class)) {
                table.addColumn(ColumnType.STRING, f.getName());
            } else if(fieldType.equals(int.class) || fieldType.equals(long.class) || fieldType.equals(Integer.class) || fieldType.equals(Long.class)) {
                table.addColumn(ColumnType.INTEGER, f.getName());
            } else if(fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                table.addColumn(ColumnType.DOUBLE, f.getName());
            } else if(fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                table.addColumn(ColumnType.FLOAT, f.getName());
            } else if(fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                table.addColumn(ColumnType.BOOLEAN, f.getName());
            } else if(fieldType.equals(Date.class)) {
                table.addColumn(ColumnType.DATE, f.getName());
            } else {
                System.err.println("Type not supported: " + fieldType.getName());
            }

        }
    }

    public T create() {
        try {
            return ProxyBuilder.forClass(this.type)
                    .dexCache(this.context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(new RealmProxy<T>(table, -1))
                    .build();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void add(int i, T t) {

    }

    public boolean add(T obj) {
        long rowIndex = table.addEmptyRow();

        // Inspect fields and add them
        for(Field f : obj.getClass().getDeclaredFields()) {

            Class<?> fieldType = f.getType();

            long columnIndex = table.getColumnIndex(f.getName());

            f.setAccessible(true);

            try {
                if (fieldType.equals(String.class)) {
                    table.setString(columnIndex, rowIndex, (String) f.get(obj));
                } else if (fieldType.equals(int.class) || fieldType.equals(long.class) || fieldType.equals(Integer.class) || fieldType.equals(Long.class)) {
                    table.setLong(columnIndex, rowIndex, f.getLong(obj));
                } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                    table.setDouble(columnIndex, rowIndex, f.getDouble(obj));
                } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                    table.setFloat(columnIndex, rowIndex, f.getFloat(obj));
                } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                    table.setBoolean(columnIndex, rowIndex, f.getBoolean(obj));
                } else if (fieldType.equals(Date.class)) {
                    table.setDate(columnIndex, rowIndex, (Date) f.get(obj));
                } else {
                    System.err.println("Type not supported: " + fieldType.getName());
                }
            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        return true;

    }

    @Override
    public boolean addAll(int i, Collection<? extends T> ts) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> ts) {
        return false;
    }

    @Override
    public void clear() {
        this.table.clear();
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        return false;
    }


    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public T get(int rowIndex) {
        try {
            T obj = ProxyBuilder.forClass(this.type)
                    .dexCache(this.context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(new RealmProxy<T>(table, rowIndex))
                    .build();
            return obj;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int size() {
        return ((Long)this.table.size()).intValue();
    }

    @Override
    public List<T> subList(int i, int i2) {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        return null;
    }

    /*
    public <T> RealmQuery<T> query(Class<T> type) {
        return new RealmQuery<T>();
    }
*/

    @Override
    public Iterator<T> iterator() {
        return new RealmIterator<T>(this);
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        return null;
    }

    @Override
    public T remove(int i) {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        return false;
    }

    @Override
    public T set(int i, T t) {
        return null;
    }
}
