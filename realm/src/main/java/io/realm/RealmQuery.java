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

    /**
     * Creating a RealmQuery instance
     * @param realm  The realm to query within
     * @param clazz  The class to query
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery(Realm realm, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;

        TableOrView dataStore = getTable();
        this.query = dataStore.where();

        for(int i = 0; i < dataStore.getColumnCount(); i++) {
            this.columns.put(dataStore.getColumnName(i), i);
        }
    }

    /**
     * Create a RealmQuery instance from a RealmResults
     * @param realmList   The RealmResults to query
     * @param clazz       The class to query
     * @throws java.lang.RuntimeException Any other error
     */
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

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> equalTo(String fieldName, String value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> equalTo(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> equalTo(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> equalTo(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> equalTo(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> equalTo(String fieldName, boolean value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    /**
     * Equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> equalTo(String fieldName, Date value) {
        
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    // Not Equal

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> notEqualTo(String fieldName, String value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> notEqualTo(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> notEqualTo(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> notEqualTo(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> notEqualTo(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> notEqualTo(String fieldName, boolean value) {
        int columnIndex = columns.get(fieldName);
        this.query.equalTo(columnIndex, !value);
        return this;
    }

    /**
     * Not-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> notEqualTo(String fieldName, Date value) {
        int columnIndex = columns.get(fieldName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    // Greater Than

    /**
     * Greater-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> greaterThan(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    /**
     * Greater-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> greaterThan(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    /**
     * Greater-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> greaterThan(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    /**
     * Greater-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> greaterThan(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    /**
     * Greater-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> greaterThan(String fieldName, Date value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    /**
     * Greater-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    /**
     * Greater-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    /**
     * Greater-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    /**
     * Greater-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    /**
     * Greater-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> greaterThanOrEqualTo(String fieldName, Date value) {
        int columnIndex = columns.get(fieldName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    // Less Than

    /**
     * Less-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> lessThan(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    /**
     * Less-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> lessThan(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    /**
     * Less-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> lessThan(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    /**
     * Less-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> lessThan(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    /**
     * Less-than comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> lessThan(String fieldName, Date value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    /**
     * Less-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, int value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    /**
     * Less-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, long value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    /**
     * Less-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, double value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    /**
     * Less-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, float value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    /**
     * Less-than-or-equal-to comparison
     * @param fieldName  The field to compare
     * @param value      The value to compare with
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> lessThanOrEqualTo(String fieldName, Date value) {
        int columnIndex = columns.get(fieldName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    // Between

    /**
     * Between condition
     * @param fieldName  The field to compare
     * @param from       Lowest value (inclusive)
     * @param to         Highest value (inclusive)
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> between(String fieldName, int from, int to) {
        int columnIndex = columns.get(fieldName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    /**
     * Between condition
     * @param fieldName  The field to compare
     * @param from       Lowest value (inclusive)
     * @param to         Highest value (inclusive)
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> between(String fieldName, long from, long to) {
        int columnIndex = columns.get(fieldName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    /**
     * Between condition
     * @param fieldName  The field to compare
     * @param from       Lowest value (inclusive)
     * @param to         Highest value (inclusive)
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> between(String fieldName, double from, double to) {
        int columnIndex = columns.get(fieldName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    /**
     * Between condition
     * @param fieldName  The field to compare
     * @param from       Lowest value (inclusive)
     * @param to         Highest value (inclusive)
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> between(String fieldName, float from, float to) {
        int columnIndex = columns.get(fieldName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    /**
     * Between condition
     * @param fieldName  The field to compare
     * @param from       Lowest value (inclusive)
     * @param to         Highest value (inclusive)
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> between(String fieldName, Date from, Date to) {
        int columnIndex = columns.get(fieldName);
        this.query.between(columnIndex, from, to);
        return this;
    }


    // Contains

    /**
     * Condition that value of field contains the specified substring
     * @param fieldName  The field to compare
     * @param value      The substring
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> contains(String fieldName, String value) {
        int columnIndex = columns.get(fieldName);
        this.query.contains(columnIndex, value);
        return this;
    }

    /**
     * Condition that value of field contains the specified substring
     * @param fieldName  The field to compare
     * @param value      The substring
     * @param caseSensitive if true, substring matching is case sensitive
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> contains(String fieldName, String value, boolean caseSensitive) {
        int columnIndex = columns.get(fieldName);
        this.query.contains(columnIndex, value, caseSensitive);
        return this;
    }

    /**
     * Condition that the value of field begins with the specified string
     * @param fieldName The field to compare
     * @param value     The string
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> beginsWith(String fieldName, String value) {
        int columnIndex = columns.get(fieldName);
        this.query.beginsWith(columnIndex, value);
        return this;
    }

    /**
     * Condition that the value of field begins with the specified substring
     * @param fieldName The field to compare
     * @param value     The substring
     * @param caseSensitive if true, substring matching is case sensitive
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> beginsWith(String fieldName, String value, boolean caseSensitive) {
        int columnIndex = columns.get(fieldName);
        this.query.beginsWith(columnIndex, value, caseSensitive);
        return this;
    }

    /**
     * Condition that the value of field ends with the specified string
     * @param fieldName The field to compare
     * @param value     The string
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> endsWith(String fieldName, String value) {
        int columnIndex = columns.get(fieldName);
        this.query.endsWith(columnIndex, value);
        return this;
    }

    /**
     * Condition that the value of field ends with the specified substring
     * @param fieldName The field to compare
     * @param value     The substring
     * @param caseSensitive if true, substring matching is case sensitive
     * @return The query object
     * @throws java.lang.IllegalArgumentException One or more arguments do not match class or
     * field type
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> endsWith(String fieldName, String value, boolean caseSensitive) {
        int columnIndex = columns.get(fieldName);
        this.query.endsWith(columnIndex, value, caseSensitive);
        return this;
    }

    // Grouping

    /**
     * Begin grouping of conditions ("left parenthesis")
     * @return The query object
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> beginGroup() {
        this.query.group();
        return this;
    }

    /**
     * End grouping of conditions ("right parenthesis")
     * @return The query object
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> endGroup() {
        this.query.endGroup();
        return this;
    }

    /**
     * Logical-or two conditions
     * @return The query object
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery<E> or() {
        this.query.or();
        return this;
    }


    // Aggregates

    // Sum

    /**
     * Calculate the sum of a field
     * @param fieldName The field name
     * @return The sum
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public long sumInt(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.sumInt(columnIndex);
    }

    /**
     * Calculate the sum of a field
     * @param fieldName The field name
     * @return The sum
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double sumDouble(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.sumDouble(columnIndex);
    }

    /**
     * Calculate the sum of a field
     * @param fieldName The field name
     * @return The sum
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double sumFloat(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.sumFloat(columnIndex);
    }

    // Average

    /**
     * Calculate the average of a field
     * @param fieldName The field name
     * @return The average
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double averageInt(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.averageInt(columnIndex);
    }

    /**
     * Calculate the average of a field
     * @param fieldName The field name
     * @return The average
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double averageDouble(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.averageDouble(columnIndex);
    }

    /**
     * Calculate the average of a field
     * @param fieldName The field name
     * @return The average
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double averageFloat(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.averageFloat(columnIndex);
    }

    // Min

    /**
     * Find the minimum value of a field
     * @param fieldName  The field name
     * @return The minimum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public long minimumInt(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.minimumInt(columnIndex);
    }

    /**
     * Find the minimum value of a field
     * @param fieldName  The field name
     * @return The minimum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double minimumDouble(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.minimumDouble(columnIndex);
    }

    /**
     * Find the minimum value of a field
     * @param fieldName  The field name
     * @return The minimum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public float minimumFloat(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.minimumFloat(columnIndex);
    }

    /**
     * Find the minimum value of a field
     * @param fieldName  The field name
     * @return The minimum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public Date minimumDate(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.minimumDate(columnIndex);
    }

    // Max

    /**
     * Find the maximum value of a field
     * @param fieldName  The field name
     * @return The maximum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public long maximumInt(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.maximumInt(columnIndex);
    }

    /**
     * Find the maximum value of a field
     * @param fieldName  The field name
     * @return The maximum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public double maximuDouble(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.maximumDouble(columnIndex);
    }

    /**
     * Find the maximum value of a field
     * @param fieldName  The field name
     * @return The maximum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public float maximuFloat(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.maximumFloat(columnIndex);
    }

    /**
     * Find the maximum value of a field
     * @param fieldName  The field name
     * @return The maximum value
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public Date maximumDate(String fieldName) {
        int columnIndex = columns.get(fieldName);
        return this.query.maximumDate(columnIndex);
    }

    /**
     * Count the number of objects that fulfill the query conditions.
     *
     * @return The number of matching objects.
     * @throws java.lang.UnsupportedOperationException The query is not valid ("syntax error")
     */
    public long count() {
        return this.query.count();
    }

    // Execute

    /**
     * Find all objects that fulfill the query conditions.
     * @return A list of objects
     * @see io.realm.RealmResults
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmResults<E> findAll() {
        return new RealmResults<E>(realm, query.findAll(), clazz);
    }

    /**
     * Find the first object that fulfills the query conditions.
     * @return The object found or null if no object matches the query conditions.
     * @see io.realm.RealmObject
     * @throws java.lang.RuntimeException Any other error.
     */
    public E findFirst() {
        long rowIndex = this.query.find();
        if (rowIndex >= 0) {
            return realm.get(clazz, rowIndex);
        } else {
            return null;
        }
    }
}
