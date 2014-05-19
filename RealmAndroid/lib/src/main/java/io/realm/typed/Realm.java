package io.realm.typed;

import android.content.Context;

import com.google.dexmaker.stock.ProxyBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;

import io.realm.ColumnType;
import io.realm.Group;
import io.realm.ReadTransaction;
import io.realm.SharedGroup;
import io.realm.Table;
import io.realm.TableOrView;
import io.realm.WriteTransaction;


public class Realm {

    private Context context;
    private SharedGroup sg;
    private Group transaction;
    private String filePath;

    /**
     * Initializes a default realm
     *
     * @param context
     */
    public Realm(Context context) {
        this(context, context.getFilesDir()+"/default.realm");
    }

    /**
     * Initializes a realm backed by the file specified
     *
     * @param context
     * @param filePath      Path to the file backing this realm
     */
    public Realm(Context context, String filePath) {
        this.context = context;
        this.sg = new SharedGroup(filePath);
        this.transaction = sg.beginRead();
        this.filePath = filePath;
    }

    Context getContext() {
        return this.context;
    }

    TableOrView getTable(Class<?> classSpec) {
        return this.transaction.getTable(classSpec.getName());
    }


    private <E> void initTable(Class<E> classSpec) {
        // Check for table existence
        if(!this.transaction.hasTable(classSpec.getName())) {
            // Create the table
            Table table = this.transaction.getTable(classSpec.getName());

            for (Field f : classSpec.getDeclaredFields()) {

                Class<?> fieldType = f.getType();


                if (fieldType.equals(String.class)) {
                    table.addColumn(ColumnType.STRING, f.getName().toLowerCase());
                } else if (fieldType.equals(int.class) || fieldType.equals(long.class) || fieldType.equals(Integer.class) || fieldType.equals(Long.class)) {
                    table.addColumn(ColumnType.INTEGER, f.getName().toLowerCase());
                } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                    table.addColumn(ColumnType.DOUBLE, f.getName().toLowerCase());
                } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                    table.addColumn(ColumnType.FLOAT, f.getName().toLowerCase());
                } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                    table.addColumn(ColumnType.BOOLEAN, f.getName().toLowerCase());
                } else if (fieldType.equals(Date.class)) {
                    table.addColumn(ColumnType.DATE, f.getName().toLowerCase());
                } else if (fieldType.equals(byte[].class)) {
                    table.addColumn(ColumnType.BINARY, f.getName().toLowerCase());
                } else if (fieldType.equals(RealmObject.class.equals(fieldType.getSuperclass()))) {
                    // Link
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

        Table table = this.transaction.getTable(classSpec.getName());

        try {
            long index = table.addEmptyRow();
            E obj = ProxyBuilder.forClass(classSpec)
                    .dexCache(this.context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(new RealmProxy(this, index))
                    .build();
            return obj;
        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Adds an object to the realm, and returns a new instance which is backed by the Realm
     *
     * @param element           The element to add to this realm.
     * @param <E>
     * @return
     */
    public <E extends RealmObject> E add(E element) {

        initTable(element.getClass());

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

        long index = ((Table)getTable(element.getClass())).add(row);

        E proxiedObject = null;
        try {
            proxiedObject = (E)ProxyBuilder.forClass((element).getClass())
                    .dexCache(this.context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(new RealmProxy(this, index))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return proxiedObject;
    }

    /**
     * Returns a typed RealmQuery, which can be used to query for specific objects of this type
     *
     * @param classSpec         The class of the object which is to be queried for
     * @param <E>
     * @return
     */
    public <E extends RealmObject> RealmQuery<E> where(Class<E> classSpec) {
        return new RealmQuery<E>(this, classSpec);
    }


    private void beginRead() {
        this.transaction = this.sg.beginRead();
    }

    /**
     * Starts a write transaction, this must be closed with either commit() or rollback()
     */
    public void beginWrite() {
        ((ReadTransaction)this.transaction).endRead();
        this.transaction = this.sg.beginWrite();
    }

    /**
     * Commits a write transaction
     */
    public void commit() {
        ((WriteTransaction)this.transaction).commit();
        beginRead();
    }

    /**
     * Does a rollback on the active write transaction
     */
    public void rollback() {
        ((WriteTransaction)this.transaction).rollback();
        beginRead();
    }

    public void clear(Class<?> classSpec) {
        getTable(classSpec).clear();
    }

    public void clear() {
        new File(filePath).delete();
        new File(filePath+".lock").delete();
    }

}
