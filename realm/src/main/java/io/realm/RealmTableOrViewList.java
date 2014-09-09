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
     * Find the minimum value.
     *
     * @param attrName    The property to look for a minimum on. Only double is supported.
     * @return            The returned value is the minimum value.
     */
    public double minimumDouble(String attrName) {
        long columnIndex;

        columnIndex = this.table.getColumnIndex(attrName);
        return this.table.minimumDouble(columnIndex);
    }

    /**
     * Find the minimum value.
     *
     * @param attrName    The property to look for a minimum on. Only float is supported.
     * @return            The returned value is the minimum value.
     */
    public float minimumFloat(String attrName) {
        long columnIndex;

        columnIndex = this.table.getColumnIndex(attrName);
        return this.table.minimumFloat(columnIndex);
    }

    /**
     * Find the minimum value.
     *
     * @param attrName    The property to look for a minimum on. Only int is supported.
     * @return            The returned value is the minimum value.
     */
    public long minimumLong(String attrName) {
        long columnIndex;

        columnIndex = this.table.getColumnIndex(attrName);
        return this.table.minimumLong(columnIndex);
    }

    /**
     * Find the minimum value.
     *
     * @param attrName    The property to look for a minimum on. Only date is supported.
     * @return            The returned value is the minimum value.
     */
    public Date minimumDate(String attrName) {
        long columnIndex;

        columnIndex = this.table.getColumnIndex(attrName);
        return this.table.minimumDate(columnIndex);
    }

    /**
     * Find the maximum value.
     *
     * @param attrName    The property to look for a maximum on. Only double is supported.
     * @return            The returned value is the maximum value.
     */
    public double maximumDouble(String attrName) {
        long columnIndex;

        columnIndex = this.table.getColumnIndex(attrName);
        return this.table.maximumDouble(columnIndex);
    }

    /**
     * Find the maximum value.
     *
     * @param attrName    The property to look for a maximum on. Only float is supported.
     * @return            The returned value is the maximum value.
     */
    public float maximumFloat(String attrName) {
        long columnIndex;

        columnIndex = this.table.getColumnIndex(attrName);
        return this.table.maximumFloat(columnIndex);
    }

    /**
     * Find the maximum value.
     *
     * @param attrName    The property to look for a maximum on. Only int is supported.
     * @return            The returned value is the maximum value.
     */
    public long maximumLong(String attrName) {
        long columnIndex;

        columnIndex = this.table.getColumnIndex(attrName);
        return this.table.maximumLong(columnIndex);
    }

    /**
     * Find the maximum value.
     *
     * @param attrName    The property to look for a minimum on. Only date is supported.
     * @return            The returned value is the maximum value.
     */
    public Date maximumDate(String attrName) {
        long columnIndex;

        columnIndex = this.table.getColumnIndex(attrName);
        return this.table.maximumDate(columnIndex);
    }


    /**
     * Calculate the sum.
     *
     * @param attrName    The property to sum. Only double is supported.
     * @return            The returned value is the sum.
     */
    public double sumDouble(String attrName) {
        long columnIndex;

        columnIndex = this.table.getColumnIndex(attrName);
        return this.table.sumDouble(columnIndex);
    }

    /**
     * Calculate the sum.
     *
     * @param attrName    The property to sum. Only float is supported.
     * @return            The returned value is the sum.
     */
    public double sumFloat(String attrName) {
        long columnIndex;

        columnIndex = this.table.getColumnIndex(attrName);
        return this.table.sumFloat(columnIndex);
    }

    /**
     * Calculate the sum.
     *
     * @param attrName    The property to sum. Only int is supported.
     * @return            The returned value is the sum.
     */
    public long sumLong(String attrName) {
        long columnIndex;

        columnIndex = this.table.getColumnIndex(attrName);
        return this.table.sumLong(columnIndex);
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
        long columnIndex;
        columnIndex = this.table.getColumnIndex(attrName);
        switch (this.table.getColumnType(columnIndex)) {
            case INTEGER:
                return this.table.averageLong(columnIndex);
            case DOUBLE:
                return this.table.averageDouble(columnIndex);
            case FLOAT:
                return this.table.averageFloat(columnIndex);
            default:
                throw new RuntimeException("Wrong type");
        }
    }
}
