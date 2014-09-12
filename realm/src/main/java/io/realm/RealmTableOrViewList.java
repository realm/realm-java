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
import java.util.HashMap;
import java.util.Map;

import io.realm.internal.Table;
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

    Map<String, Class<?>> cache = new HashMap<String, Class<?>>();


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

    // Sorting

    public static enum Order {
        ASCENDING, DESCENDING
    }

    /**
     * Get a sorted (ASCENDING) RealmList from an existing RealmList.
     *
     * @param fieldName  The field name to sort by.
     * @return           A sorted RealmList
     */
    public RealmList<E> sort(String fieldName) {
        return sort(fieldName, Order.ASCENDING);
    }

    /**
     * Get a sorted RealmList from an existing RealmList.
     *
     * @param fieldName  The field name to sort by.
     * @param sortOrder  The direction to sort by.
     * @return           A sorted RealmList.
     */
    public RealmList<E> sort(String fieldName, Order sortOrder) {
        TableOrView table = getTable();
        long columnIndex = table.getColumnIndex(fieldName);
        TableView sorted;

        if (table instanceof TableView) {
            TableView v = (TableView)table;
            sorted = v.where().findAll();
            if (sortOrder == Order.ASCENDING) {
                sorted.sort(columnIndex, TableView.Order.ascending);
            }
            else {
                sorted.sort(columnIndex, TableView.Order.descending);
            }
        }
        else {
            Table t = (Table)table;
            if (sortOrder == Order.ASCENDING) {
                sorted = t.getSortedView(columnIndex, TableView.Order.ascending);
            }
            else {
                sorted = t.getSortedView(columnIndex, TableView.Order.descending);
            }
        }
        return new RealmTableOrViewList<E>(realm, sorted, classSpec);
    }


    // Aggregates

    @Override
    public int size() {
        return ((Long)getTable().size()).intValue();
    }

    /**
     * Find the minimum value of a field.
     *
     * @param fieldName   The field to look for a minimum on. Only int, float, and double
     *                    are supported.
     * @return
     */
    public Number min(String fieldName) {
        // TODO: Date
        long columnIndex = table.getColumnIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return table.minimumLong(columnIndex);
            case FLOAT:
                return table.minimumFloat(columnIndex);
            case DOUBLE:
                return table.minimumDouble(columnIndex);
            default:
                throw new RuntimeException("Wrong type");
        }
    }


    /**
     * Find the maximum value of a field.
     *
     * @param fieldName   The field to look for a maximum on. Only int, float, and double
     *                    are supported.
     * @return            The maximum value.
     */
    public Number max(String fieldName) {
        // TODO: Date
        long columnIndex = table.getColumnIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return table.maximumLong(columnIndex);
            case FLOAT:
                return table.maximumFloat(columnIndex);
            case DOUBLE:
                return table.maximumDouble(columnIndex);
            default:
                throw new RuntimeException("Wrong type");
        }
    }


    /**
     * Calculate the sum of a field.
     *
     * @param fieldName   The field to sum. Only int, float, and double are supported.
     * @return            The sum.
     */

    public Number sum(String fieldName) {
        long columnIndex = table.getColumnIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return table.sumLong(columnIndex);
            case FLOAT:
                return table.sumFloat(columnIndex);
            case DOUBLE:
                return table.sumDouble(columnIndex);
            default:
                throw new RuntimeException("Wrong type");
        }
    }


    /**
     * Returns the average of a given field for objects in a RealmList.
     *
     * @param fieldName  The field to calculate average on. Only properties of type int,
     *                   float and double are supported.
     * @return           The average for the given field amongst objects in an RealmList. This
     *                   will be of type double for both float and double field.
     */
    public double average(String fieldName) {
        long columnIndex = table.getColumnIndex(fieldName);
        switch (table.getColumnType(columnIndex)) {
            case INTEGER:
                return table.averageLong(columnIndex);
            case DOUBLE:
                return table.averageDouble(columnIndex);
            case FLOAT:
                return table.averageFloat(columnIndex);
            default:
                throw new RuntimeException("Wrong type");
        }
    }


    // Deleting

    /**
     * Removes an object at a given index.
     *
     * @param index      The array index identifying the object to be removed.
     * @return           Always return null.
     */
    @Override
    public E remove(int index) {
        TableOrView table = getTable();
        table.remove(index);
        return null;
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
     * Removes all objects from a RealmList.
     *
     */
    public void clear() {
        TableOrView table = getTable();
        table.clear();
    }

    // Adding objects

//    /**
//     * Add an object.
//     *
//     * @param element    The object to add.
//     * @return           true if object was added.
//     */
//    @Override
//    public boolean add(E element) {
//        throw new NoSuchMethodError();
//    }
//
//    /**
//     * Add an object
//     *
//     * @param index        The array index to add the object at.
//     * @param element      The object to add.
//     */
//    public void add(int index, E element) {
//        throw new NoSuchMethodError();
//    }
//
//    /**
//     * Replaces an object at the given index with a new object.
//     *
//     * @param index       The array index of the object to be replaced.
//     * @param element     An object.
//     */
//    public void replace(int index, E element) {
//        throw new NoSuchMethodError();
//    }
}
