/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm;


import java.util.AbstractList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.internal.ColumnType;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.TableView;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    public E min(String attrName) throws NotImplementedException {
        throw new NotImplementedException();
    }


    /**
     * Find the minimum value.
     *
     * @param attrName    The property to look for a minimum on. Only double is supported.
     * @return            The returned value is the minimum value.
     */
    public double minimumDouble(String attrName) {
        long columnIndex;
        TableOrView table;

        table = getTable();
        columnIndex = table.getColumnIndex(attrName);

        if (table.getColumnType(columnIndex) == ColumnType.DOUBLE) {
            return table.minimumDouble(columnIndex);
        }
        else {
            throw new RuntimeException("No such attribute");
        }
    }

    /**
     * Find the minimum value.
     *
     * @param attrName    The property to look for a minimum on. Only float is supported.
     * @return            The returned value is the minimum value.
     */
    public float minimumFloat(String attrName) {
        long columnIndex;
        TableOrView table;

        table = getTable();
        columnIndex = table.getColumnIndex(attrName);

        if (table.getColumnType(columnIndex) == ColumnType.FLOAT) {
            return table.minimumFloat(columnIndex);
        }
        else {
            throw new RuntimeException("No such attribute");
        }
    }

    /**
     * Find the minimum value.
     *
     * @param attrName    The property to look for a minimum on. Only int is supported.
     * @return            The returned value is the minimum value.
     */
    public long minimumLong(String attrName) {
        long columnIndex;
        TableOrView table;

        table = getTable();
        columnIndex = table.getColumnIndex(attrName);

        if (table.getColumnType(columnIndex) == ColumnType.INTEGER) {
            return table.minimumLong(columnIndex);
        }
        else {
            throw new RuntimeException("No such attribute");
        }
    }

    /**
     * Find the minimum value.
     *
     * @param attrName    The property to look for a minimum on. Only date is supported.
     * @return            The returned value is the minimum value.
     */
    public Date minimumDate(String attrName) {
        long columnIndex;
        TableOrView table;

        table = getTable();
        columnIndex = table.getColumnIndex(attrName);

        if (table.getColumnType(columnIndex) == ColumnType.DATE) {
            return table.minimumDate(columnIndex);
        }
        else {
            throw new RuntimeException("No such attribute");
        }
    }


    /**
     * Find an object with the maximum value.
     *
     * @param attrName   The property to look for a maximum on. Only properties of type int, float
     *                   and double are supported.
     * @return           The returned object is the first object in the RealmList which has the
     *                   maximum value.
     */
    public E max(String attrName) throws NotImplementedException {
        throw new NotImplementedException();
    }

    /**
     * Find the maximum value.
     *
     * @param attrName    The property to look for a maximum on. Only double is supported.
     * @return            The returned value is the maximum value.
     */
    public double maximumDouble(String attrName) {
        long columnIndex;
        TableOrView table;

        table = getTable();
        columnIndex = table.getColumnIndex(attrName);

        if (table.getColumnType(columnIndex) == ColumnType.DOUBLE) {
            return table.maximumDouble(columnIndex);
        }
        else {
            throw new RuntimeException("No such attribute");
        }
    }

    /**
     * Find the maximum value.
     *
     * @param attrName    The property to look for a maximum on. Only float is supported.
     * @return            The returned value is the maximum value.
     */
    public float maximumFloat(String attrName) {
        long columnIndex;
        TableOrView table;

        table = getTable();
        columnIndex = table.getColumnIndex(attrName);

        if (table.getColumnType(columnIndex) == ColumnType.FLOAT) {
            return table.maximumFloat(columnIndex);
        }
        else {
            throw new RuntimeException("No such attribute");
        }
    }

    /**
     * Find the maximum value.
     *
     * @param attrName    The property to look for a maximum on. Only int is supported.
     * @return            The returned value is the maximum value.
     */
    public long maximumLong(String attrName) {
        long columnIndex;
        TableOrView table;

        table = getTable();
        columnIndex = table.getColumnIndex(attrName);

        if (table.getColumnType(columnIndex) == ColumnType.INTEGER) {
            return table.maximumLong(columnIndex);
        }
        else {
            throw new RuntimeException("No such attribute");
        }
    }

    /**
     * Find the maximum value.
     *
     * @param attrName    The property to look for a minimum on. Only date is supported.
     * @return            The returned value is the maximum value.
     */
    public Date maximumDate(String attrName) {
        long columnIndex;
        TableOrView table;

        table = getTable();
        columnIndex = table.getColumnIndex(attrName);

        if (table.getColumnType(columnIndex) == ColumnType.DATE) {
            return table.maximumDate(columnIndex);
        }
        else {
            throw new RuntimeException("No such attribute");
        }
    }


    /**
     * Calculate the sum.
     *
     * @param attrName    The property to sum. Only double is supported.
     * @return            The returned value is the sum.
     */
    public double sumDouble(String attrName) {
        long columnIndex;
        TableOrView table;

        table = getTable();
        columnIndex = table.getColumnIndex(attrName);

        if (table.getColumnType(columnIndex) == ColumnType.DOUBLE) {
            return table.sumDouble(columnIndex);
        }
        else {
            throw new RuntimeException("No such attribute");
        }
    }

    /**
     * Calculate the sum.
     *
     * @param attrName    The property to sum. Only float is supported.
     * @return            The returned value is the sum.
     */
    public double sumFloat(String attrName) {
        long columnIndex;
        TableOrView table;

        table = getTable();
        columnIndex = table.getColumnIndex(attrName);

        if (table.getColumnType(columnIndex) == ColumnType.FLOAT) {
            return table.sumFloat(columnIndex);
        }
        else {
            throw new RuntimeException("No such attribute");
        }
    }

    /**
     * Calculate the sum.
     *
     * @param attrName    The property to sum. Only int is supported.
     * @return            The returned value is the sum.
     */
    public long sumLong(String attrName) {
        long columnIndex;
        TableOrView table;

        table = getTable();
        columnIndex = table.getColumnIndex(attrName);

        if (table.getColumnType(columnIndex) == ColumnType.INTEGER) {
            return table.sumLong(columnIndex);
        }
        else {
            throw new RuntimeException("No such attribute");
        }
    }


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
        long columnIndex;
        columnIndex = table.getColumnIndex(attrName);
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
    public RealmList<E> sort(String attrName) {
        return sort(attrName, Order.ascending);
    }

    /**
     * Get a sorted RealmList from an existing RealmList.
     *
     * @param attrName   The attribute name to sort by.
     * @param sortOrder  The direction to sort by.
     * @return           A sorted RealmList.
     */
    public RealmList<E> sort(String attrName, Order sortOrder) {
        long columnIndex;
        TableOrView table = getTable();
        columnIndex = table.getColumnIndex(attrName);
        TableView newView = table.where().findAll();
        if (sortOrder == Order.ascending) {
            newView.sort(columnIndex, TableView.Order.ascending);
        }
        else {
            newView.sort(columnIndex, TableView.Order.descending);
        }
        RealmTableOrViewList<E> newList;
        newList = new RealmTableOrViewList<E>(realm, newView, newView.getClass());
        return newList;
    }

    // Deleting
    /**
     * Removes an object at a given index.
     *
     * @param index      The array index identifying the object to be removed.
     */
    public void remove(int index) {
        TableOrView table = getTable();
        table.remove(index);
    }

    /**
     * Removes the last object in a RealmList.
     *
     */
    public void removeLast() {
        TableOrView table = getTable();
        table.removeLast();
    }

    /**
     * Removes all objects from an RealmList.
     *
     */
    public void clear() {
        TableOrView table = getTable();
        table.clear();
    }

    // Adding objects

    /**
     * Add an object.
     *
     * @param element    The object to add.
     * @return           true if object was added.
     */
    public boolean add(E element) {
        TableOrView table;

        table = getTable();
        if (table instanceof Table) {
            ((Table)table).add(element);
            return true;
        }
        else {
            throw new RuntimeException("Cannot add objects to a result set.");
        }
    }

    /**
     * Add an object
     *
     * @param index        The place to add the object at.
     * @param element      The object to add.
     */
    public void add(int index, E element) {
        TableOrView table;

        table = getTable();
        if (table instanceof Table) {
            if (index >= 0 && index < table.size()) {
                ((Table) table).addAt(index, element);
            }
            else {
                throw new RuntimeException("Out of range.");
            }
        }
        else {
            throw new RuntimeException("Cannot add objects to a result set.");
        }
    }

    /**
     * Replaces an object at the given index with a new object.
     *
     * @param index       The array index of the object to be replaced.
     * @param element     An object.
     */
    public void replace(int index, E element) {
        TableOrView table;

        table = getTable();
        if (index < 0 || index >= table.size()) {
            throw new RuntimeException("Out of range.");
        }

        if (table instanceof Table) {
            ((Table)table).set(index, element);
        }
        else {
            // FIXME: TableView hasn't a set method
            throw new NotImplementedException();
        }
    }
}
