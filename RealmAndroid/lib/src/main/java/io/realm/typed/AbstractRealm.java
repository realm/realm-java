package io.realm.typed;

import android.content.Context;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.ColumnType;
import io.realm.Group;
import io.realm.ReadTransaction;
import io.realm.SharedGroup;
import io.realm.Table;
import io.realm.TableOrView;
import io.realm.WriteTransaction;

abstract class AbstractRealm<E> extends AbstractList<E> {

    protected Class<E> type;
    protected Context context;
    protected SharedGroup sg;
    protected Group transaction;
    private Map<String, Long> columnIndexes = new HashMap<String, Long>();
    private TableOrView dataStore = null;

    /**
     *
     * @param type
     * @param context
     */
    AbstractRealm(Class<E> type, Context context) {
        this(type, context, context.getFilesDir()+"/default.realm");
    }

    /**
     *
     * @param type
     * @param context
     * @param filePath
     */
    AbstractRealm(Class<E> type, Context context, String filePath) {
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
                    } else {
                        System.err.println("Type not supported: " + fieldType.getName());
                    }

                }

            }

            Table table = wt.getTable(type.getName());
            table.clear();

            for(long i = 0; i < table.getColumnCount(); i++) {
                this.columnIndexes.put(table.getColumnName(i), i);
            }

            wt.commit();



        } catch(Throwable t) {
            t.printStackTrace();
            wt.rollback();
        }

        beginRead();

    }

    AbstractRealm(RealmList<E> realm, TableOrView dataStore) {
        this.context = realm.context;
        this.type = realm.type;
        this.dataStore = dataStore;
        for(long i = 0; i < dataStore.getColumnCount(); i++) {
            this.columnIndexes.put(dataStore.getColumnName(i), i);
        }
    }

    Long getColumnIndex(String name) {
        return this.columnIndexes.get(name);
    }

    TableOrView getDataStore() {

        return this.dataStore;

    }

    private void beginRead() {
        this.transaction = this.sg.beginRead();
        this.dataStore = this.transaction.getTable(type.getName());
    }

    public void beginWrite() {
        ((ReadTransaction)this.transaction).endRead();
        this.transaction = this.sg.beginWrite();
        this.dataStore = this.transaction.getTable(type.getName());
    }

    public void commit() {
        ((WriteTransaction)this.transaction).commit();
        beginRead();
    }

    public void rollback() {
        ((WriteTransaction)this.transaction).rollback();
        beginRead();
    }

}
