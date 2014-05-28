package io.realm.typed;


import com.google.dexmaker.stock.ProxyBuilder;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Table;
import io.realm.TableOrView;

/**
 *
 * @param <E> The class of objects in this list
 */
public class RealmList<E extends RealmObject> extends AbstractList<E> {

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

    Realm getRealm() {
        return realm;
    }

    TableOrView getTable() {

        if(table == null) {
            return realm.getTable(classSpec);
        } else {
            return table;
        }
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    Map<String, Class<?>> cache = new HashMap<String, Class<?>>();


    /**
     * Returns a RealmQuery, used to filter this RealmList
     *
     * @return          A RealmQuery to filter the list
     */
    public RealmQuery<E> where() {
        return new RealmQuery<E>(this, classSpec);
    }


    /**
     *
     * @param rowIndex      The objects index in the list
     * @return              An object of type E, which is backed by Realm
     */
    @Override
    public E get(int rowIndex) {

        E obj = null;

        try {
            obj = ProxyBuilder.forClass(classSpec)
                    .parentClassLoader(this.getClass().getClassLoader())
                    .dexCache(realm.getBytecodeCache())
                    .handler(new RealmProxy(this, rowIndex))
                    .build();

            obj.realmSetRowIndex(rowIndex);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public E first() {
        return get(0);
    }

    public E last() {
        return get(size()-1);
    }

    // Aggregates


    /**
     *
     * @return              The number of elements in this RealmList
     */
    @Override
    public int size() {
        return ((Long)getTable().size()).intValue();
    }

}
