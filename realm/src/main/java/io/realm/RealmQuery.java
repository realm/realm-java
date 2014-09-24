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


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.internal.TableOrView;
import io.realm.internal.TableQuery;

/**
 *
 * @param <E> The class of objects to be queried
 */
public class RealmQuery<E extends RealmObject> {

    private RealmResults realmList;
    private Realm realm;
    private TableQuery query;
    private Map<String, Integer> columns = new HashMap<String, Integer>();
    private Class<E> clazz;

    public RealmQuery(Realm realm, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;

        TableOrView dataStore = getTable();
        this.query = dataStore.where();

        for(int i = 0; i < dataStore.getColumnCount(); i++) {
            this.columns.put(dataStore.getColumnName(i), i);
        }
    }

    public RealmQuery(RealmResults realmList, Class<E> clazz) {
        this.realmList = realmList;

        this.realm = realmList.getRealm();
        this.clazz = clazz;

        TableOrView dataStore = getTable();
        this.query = dataStore.where();

        for(int i = 0; i < dataStore.getColumnCount(); i++) {
            this.columns.put(dataStore.getColumnName(i), i);
        }
    }

    TableOrView getTable() {
        if(realmList != null) {
            return realmList.getTable();
        } else {
            return realm.getTable(clazz);
        }
    }

    // Equal

    public RealmQuery<E> equalTo(String fieldName, String value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String fieldName, boolean value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String fieldName, Date value) {
        
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    // Not Equal

    public RealmQuery<E> notEqualTo(String fieldName, String value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String fieldName, boolean value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, !value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String fieldName, Date value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    // Greater Than

    public RealmQuery<E> greaterThan(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThan(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThan(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThan(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThan(String fieldName, Date value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, Date value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    // Less Than

    public RealmQuery<E> lessThan(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThan(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThan(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThan(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThan(String fieldName, Date value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThanOrEqualTo(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThanOrEqualTo(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThanOrEqualTo(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThanOrEqualTo(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThanOrEqualTo(String fieldName, Date value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    // Between

    public RealmQuery<E> between(String fieldName, int from, int to) {
        int columnIndex = columns.get(fieldName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<E> between(String fieldName, long from, long to) {
        int columnIndex = columns.get(fieldName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<E> between(String fieldName, double from, double to) {
        int columnIndex = columns.get(fieldName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<E> between(String fieldName, float from, float to) {
        int columnIndex = columns.get(fieldName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<E> between(String fieldName, Date from, Date to) {
        int columnIndex = columns.get(fieldName);
        this.query.between(columnIndex, from, to);
        return this;
    }


    // Contains

    public RealmQuery<E> contains(String fieldName, String value) {
        int columnIndex = columns.get(fieldName);
        this.query.contains(columnIndex, value);
        return this;
    }

    public RealmQuery<E> contains(String fieldName, String value, boolean caseSensitive) {
        int columnIndex = columns.get(fieldName);
        this.query.contains(columnIndex, value, caseSensitive);
        return this;
    }

    public RealmQuery<E> beginsWith(String fieldName, String value) {
        int columnIndex = columns.get(fieldName);
        this.query.beginsWith(columnIndex, value);
        return this;
    }

    public RealmQuery<E> beginsWith(String fieldName, String value, boolean caseSensitive) {
        int columnIndex = columns.get(fieldName);
        this.query.beginsWith(columnIndex, value, caseSensitive);
        return this;
    }

    public RealmQuery<E> endsWith(String fieldName, String value) {
        int columnIndex = columns.get(fieldName);
        this.query.endsWith(columnIndex, value);
        return this;
    }

    public RealmQuery<E> endsWith(String fieldName, String value, boolean caseSensitive) {
        int columnIndex = columns.get(fieldName);
        this.query.endsWith(columnIndex, value, caseSensitive);
        return this;
    }

    // Grouping

    public RealmQuery<E> beginGroup() {
        this.query.group();
        return this;
    }

    public RealmQuery<E> endGroup() {
        this.query.endGroup();
        return this;
    }

    public RealmQuery<E> or() {
        this.query.or();
        return this;
    }


    // Aggregates

    // Sum

    public long sumInt(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.sumInt(columnIndex);
    }

    public double sumDouble(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.sumDouble(columnIndex);
    }

    public double sumFloat(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.sumFloat(columnIndex);
    }

    // Average

    public double averageInt(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.averageInt(columnIndex);
    }

    public double averageDouble(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.averageDouble(columnIndex);
    }

    public double averageFloat(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.averageFloat(columnIndex);
    }

    // Min

    public long minimumInt(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.minimumInt(columnIndex);
    }

    public double minimuDouble(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.minimumDouble(columnIndex);
    }

    public float minimuFloat(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.minimumFloat(columnIndex);
    }

    public Date minimumDate(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.minimumDate(columnIndex);
    }

    // Max

    public long maximumInt(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.maximumInt(columnIndex);
    }

    public double maximuDouble(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.maximumDouble(columnIndex);
    }

    public float maximuFloat(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.maximumFloat(columnIndex);
    }

    public Date maximumDate(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.maximumDate(columnIndex);
    }

    // Execute

    public RealmResults<E> findAll() {
        return new RealmResults<E>(realm, query.findAll(), clazz);
    }

    public E findFirst() {
        RealmResults<E> result = findAll();
        if(result.size() > 0) {
            return findAll().get(0);
        } else {
            return null;
        }
    }

}
