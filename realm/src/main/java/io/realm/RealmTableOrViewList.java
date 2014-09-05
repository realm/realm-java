package io.realm;


import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;

import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.TableQuery;
import io.realm.internal.TableView;

/**
 *
 * @param <E> The class of objects in this list
 */
public class RealmTableOrViewList<E extends RealmObject> extends AbstractList<E> implements RealmList<E> {

    private Class<E> classSpec;
    private Realm realm;
    private TableOrView table = null;

    RealmTableOrViewList(Realm realm, Class<E> classSpec) {
        this.realm = realm;
        this.classSpec = classSpec;
    }

    RealmTableOrViewList(Realm realm, TableOrView table, Class<E> classSpec) {
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
    public void move(int oldPos, int newPos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    Map<String, Class<?>> cache = new HashMap<String, Class<?>>();

    /**
     * Returns a RealmQuery, used to filter this list
     *
     * @return              A RealmQuery to filter the list
     */
    @Override
    public RealmQuery<E> where() {
        return new RealmQuery<E>(this, classSpec);
    }


    @Override
    public E get(int rowIndex) {

        E obj;

        TableOrView table = getTable();
        if(table instanceof TableView) {
            obj = realm.get(classSpec, ((TableView)table).getSourceRowIndex(rowIndex));
        } else {
            obj = realm.get(classSpec, rowIndex);
        }

        return obj;
    }

    @Override
    public E first() {
        return get(0);
    }

    @Override
    public E last() {
        return get(size()-1);
    }

    // Aggregates


    @Override
    public int size() {
        return ((Long)getTable().size()).intValue();
    }

    /**
     * Find an object with the minimum value.
     *
     * @param attrName   The property to look for a minimum on. Only properties of type int, float
     *                   and double are supported.
     * @return           The returned object is the first object in the RealmList which has the
     *                   minimum value.
     */
    public E min(String attrName);

    /**
     * Find an object with the maximum value.
     *
     * @param attrName   The property to look for a maximum on. Only properties of type int, float
     *                   and double are supported.
     * @return           The returned object is the first object in the RealmList which has the
     *                   maximum value.
     */
    public E max(String attrName);

    /**
     * Returns the average of a given property for objects in a RealmList.
     *
     * @param attrName   The property to calculate average on. Only properties of type int,
     *                   float and double are supported.
     * @return           The average for the given property amongst objects in an RealmList. This
     *                   will be of type double for both float and double properties.
     */
    public double average(String attrName) {
        TableOrView table = getTable();
        int columnIndex = table.getColumnIndex(attrName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return table.averageLong(columnIndex);
            case DOUBLE:
                return table.averageDouble(columnIndex);
            case FLOAT:
                return table.averageDouble(columnIndex);
            default:
                throw new RuntimeException("Wrong type");
        }
    }

    // TODO: sum

    // Sorting
    public static enum Order {
        ascending, descending;
    }

    /**
     * Get a sorted (ascending) RealmList from an existing RealmList.
     *
     * @param attrName   The attribute name to sort by.
     * @return           A sorted RealmList
     */
    RealmList<E> sort(String attrName) {
        return sort(attrName, Order.ascending);
    }

    /**
     * Get a sorted RealmList from an existing RealmList.
     *
     * @param attrName   The attribute name to sort by.
     * @param sortOrder  The direction to sort by.
     * @return           A sorted RealmList.
     */
    RealmList<E> sort(String attrName, Order sortOrder) {
        TableOrView table = getTable();
        int columnIndex = table.getColumnIndex(attrName);
        TableView newView = table.where().findAll();
        if (sortOrder == Order.ascending) {
            newView.sort(columnIndex, TableView.Order.ascending);
        }
        else {
            newView.sort(columnIndex, TableView.Order.descending);
        }
        RealmTableOrViewList<E> newList;
        newList = new RealmTableOrViewList<E>(realm, table, E);
        return newList;
    }

    // Deleting
    /**
     * Removes an object at a given index.
     *
     * @param index      The array index identifying the object to be removed.
     */
    void remove(int index) {
        TableOrView table = getTable();
        table.remove(index);
    }

    /**
     * Removes the last object in a RealmList.
     *
     */
    void removeLast() {
        TableOrView table = getTable();
        table.removeLast();
    }

    /**
     * Removes all objects from an RealmList.
     *
     */
    void clear() {
        TableOrView table = getTable();
        table.clear();
    }


}
