package io.realm.typed;

import android.content.Context;

import com.google.dexmaker.stock.ProxyBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.ColumnType;
import io.realm.Group;
import io.realm.ReadTransaction;
import io.realm.SharedGroup;
import io.realm.Table;
import io.realm.WriteTransaction;


public class Realm {

    private Context context;
    private SharedGroup sg;
    private Group transaction;
    private String filePath;
    private int version;
    private File bytecodeCache;

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
        this.bytecodeCache = context.getDir("dx", Context.MODE_PRIVATE);
    }

    public Table getTable(Class<?> classSpec) {
        return transaction.getTable(classSpec.getSimpleName());
    }


    private <E> void initTable(Class<E> classSpec) {
        // Check for table existence
        if(!this.transaction.hasTable(classSpec.getSimpleName())) {
            // Create the table
            Table table = this.transaction.getTable(classSpec.getSimpleName());

            System.out.println(classSpec.getSimpleName());

            Field[] fields = classSpec.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {

                Field f = fields[i];

                Class<?> fieldType = f.getType();

                System.out.println(f.getName() + " - " + fieldType.getName());


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
                } else if (RealmObject.class.equals(fieldType.getSuperclass())) {
                    // Link
                    // Check if the table representing the object which is linked to exists
                    System.out.println("Creating linked objects table");
                    initTable(fieldType);
                } else {
                    System.err.println("Type not supported: " + fieldType.getName());
                }

            }

            System.out.println("Tables in realm: ");
            for(int i = 0; i < transaction.size(); i++) {
                System.out.println(transaction.getTableName(i));
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

        try {
            long index = table.addEmptyRow();
            E obj = ProxyBuilder.forClass(classSpec)
                    .dexCache(getBytecodeCache())
                    .handler(new RealmProxy(this, index))
                    .build();
            return obj;
        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
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
                        f.getType().equals(byte[].class)) {
                    f.setAccessible(true);
                    persistedFields.add(f);
                }
            }

            cache.put(className, persistedFields);

        }


        List<Field> fields = cache.get(className);

        List<Object> rows = new ArrayList<Object>(fields.size());

        // Inspect fields and add them
        for(Field f : fields) {

            try {



                rows.add(f.get(element));
/*
                } else if(RealmObject.class.equals(f.getType().getSuperclass())) {
                    // This is a link, should add in different table and update the link
                    System.out.println("Insert linked object in corresponding table");
                    RealmObject linkedObject = (RealmObject)f.get(element);
                    if(linkedObject != null) {
                        add(linkedObject);
                        System.out.println("Object added");
                    }

                    // Add link
                }
*/
            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        ((Table)getTable(element.getClass())).add(rows.toArray());

     //   E proxiedObject = null;
        /*
        try {
            proxiedObject = (E)ProxyBuilder.forClass((element).getClass())
                    .dexCache(this.context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(new RealmProxy(this, index))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

        //return proxiedObject;
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


    public void ensureRealmAtVersion(int version, RealmMigration migration) {
        migration.execute(this, version);
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public File getBytecodeCache() {
        return bytecodeCache;
    }

}
