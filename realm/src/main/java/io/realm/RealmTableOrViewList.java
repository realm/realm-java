package io.realm;


import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;

import io.realm.internal.TableOrView;
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


    @Override
    public RealmQuery<E> where() {
        return new RealmQuery<E>(this, classSpec);
    }

    @Override
    public void addObject(E element) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public void addObjectFromArray(Array array) throws NoSuchMethodException {
        throw new NoSuchMethodException();
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

    @Override
    public String JSONString() throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public void replaceObjectAtIndexWithObject(int index, E newElement) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public void removeLastObject() throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public void insertObjectAtIndex(E element, int index) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public int indexOfObjectWhere() throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public int indexOfObject(E element) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }


    @Override
    public double averageOfProperty(String propertyName) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public double sumOfProperty(String propertyName) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public int maxOfProperty(String propertyName) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public int minOfProperty(String propertyName) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public RealmList<E> arraySortForProperty(String propertyName) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

}
