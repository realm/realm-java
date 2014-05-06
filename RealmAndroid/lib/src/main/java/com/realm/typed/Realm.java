package com.realm.typed;

import android.content.Context;

import com.google.dexmaker.stock.ProxyBuilder;
import com.tightdb.ColumnType;
import com.tightdb.ReadTransaction;
import com.tightdb.SharedGroup;
import com.tightdb.Table;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.AbstractList;
import java.util.Date;

public class Realm<T> extends AbstractList<T> {

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

        this.type = type;

        this.table = new Table();



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

    @Override
    public void add(int rowIndex, T element) {

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

        table.addAt(rowIndex, row);

    }

    @Override
    public T remove(int rowIndex) {
        this.table.remove(rowIndex);
        return null;
    }

    @Override
    public T set(int rowIndex, T element) {

        Field[] fields = element.getClass().getDeclaredFields();

        // Inspect fields and add them
        for(int i = 0; i < fields.length; i++) {

            Field f = fields[i];

            Class<?> fieldType = f.getType();

            System.out.println(f.getName());
            long columnIndex = table.getColumnIndex(f.getName());

            f.setAccessible(true);

            try {

                if (fieldType.equals(String.class)) {
                    table.setString(columnIndex, rowIndex, (String) f.get(element));
                } else if (fieldType.equals(int.class) || fieldType.equals(long.class) || fieldType.equals(Integer.class) || fieldType.equals(Long.class)) {
                    table.setLong(columnIndex, rowIndex, f.getLong(element));
                } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                    table.setDouble(columnIndex, rowIndex, f.getDouble(element));
                } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                    table.setFloat(columnIndex, rowIndex, f.getFloat(element));
                } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                    table.setBoolean(columnIndex, rowIndex, f.getBoolean(element));
                } else if (fieldType.equals(Date.class)) {
                    table.setDate(columnIndex, rowIndex, (Date) f.get(element));
                } else {
                    System.err.println("Type not supported: " + fieldType.getName());
                }

            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        return this.get(rowIndex);

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


}
