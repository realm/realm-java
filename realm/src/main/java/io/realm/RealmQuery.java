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

    private RealmTableOrViewList realmList;
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

    public RealmQuery(RealmTableOrViewList realmList, Class<E> clazz) {
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

    public RealmQuery<E> equalTo(String columnName, String value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String columnName, int value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String columnName, long value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String columnName, double value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String columnName, float value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String columnName, boolean value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> equalTo(String columnName, Date value) {
        
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    // Not Equal

    public RealmQuery<E> notEqualTo(String columnName, String value) {
        int columnIndex = columns.get(columnName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String columnName, int value) {
        int columnIndex = columns.get(columnName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String columnName, long value) {
        int columnIndex = columns.get(columnName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String columnName, double value) {
        int columnIndex = columns.get(columnName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String columnName, float value) {
        int columnIndex = columns.get(columnName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String columnName, boolean value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, !value);
        return this;
    }

    public RealmQuery<E> notEqualTo(String columnName, Date value) {
        int columnIndex = columns.get(columnName);
        this.query.notEqualTo(columnIndex, value);
        return this;
    }

    // Greater Than

    public RealmQuery<E> greaterThan(String columnName, int value) {
        int columnIndex = columns.get(columnName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThan(String columnName, long value) {
        int columnIndex = columns.get(columnName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThan(String columnName, double value) {
        int columnIndex = columns.get(columnName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThan(String columnName, float value) {
        int columnIndex = columns.get(columnName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThan(String columnName, Date value) {
        int columnIndex = columns.get(columnName);
        this.query.greaterThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThanOrEqualTo(String columnName, int value) {
        int columnIndex = columns.get(columnName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThanOrEqualTo(String columnName, long value) {
        int columnIndex = columns.get(columnName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThanOrEqualTo(String columnName, double value) {
        int columnIndex = columns.get(columnName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThanOrEqualTo(String columnName, float value) {
        int columnIndex = columns.get(columnName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> greaterThanOrEqualTo(String columnName, Date value) {
        int columnIndex = columns.get(columnName);
        this.query.greaterThanOrEqual(columnIndex, value);
        return this;
    }

    // Less Than

    public RealmQuery<E> lessThan(String columnName, int value) {
        int columnIndex = columns.get(columnName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThan(String columnName, long value) {
        int columnIndex = columns.get(columnName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThan(String columnName, double value) {
        int columnIndex = columns.get(columnName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThan(String columnName, float value) {
        int columnIndex = columns.get(columnName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThan(String columnName, Date value) {
        int columnIndex = columns.get(columnName);
        this.query.lessThan(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThanOrEqualTo(String columnName, int value) {
        int columnIndex = columns.get(columnName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThanOrEqualTo(String columnName, long value) {
        int columnIndex = columns.get(columnName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThanOrEqualTo(String columnName, double value) {
        int columnIndex = columns.get(columnName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThanOrEqualTo(String columnName, float value) {
        int columnIndex = columns.get(columnName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    public RealmQuery<E> lessThanOrEqualTo(String columnName, Date value) {
        int columnIndex = columns.get(columnName);
        this.query.lessThanOrEqual(columnIndex, value);
        return this;
    }

    // Between

    public RealmQuery<E> between(String columnName, int from, int to) {
        int columnIndex = columns.get(columnName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<E> between(String columnName, long from, long to) {
        int columnIndex = columns.get(columnName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<E> between(String columnName, double from, double to) {
        int columnIndex = columns.get(columnName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<E> between(String columnName, float from, float to) {
        int columnIndex = columns.get(columnName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<E> between(String columnName, Date from, Date to) {
        int columnIndex = columns.get(columnName);
        this.query.between(columnIndex, from, to);
        return this;
    }


    // Contains

    public RealmQuery<E> contains(String columnName, String value) {
        int columnIndex = columns.get(columnName);
        this.query.contains(columnIndex, value);
        return this;
    }

    public RealmQuery<E> contains(String columnName, String value, boolean caseSensitive) {
        int columnIndex = columns.get(columnName);
        this.query.contains(columnIndex, value, caseSensitive);
        return this;
    }

    public RealmQuery<E> beginsWith(String columnName, String value) {
        int columnIndex = columns.get(columnName);
        this.query.beginsWith(columnIndex, value);
        return this;
    }

    public RealmQuery<E> beginsWith(String columnName, String value, boolean caseSensitive) {
        int columnIndex = columns.get(columnName);
        this.query.beginsWith(columnIndex, value, caseSensitive);
        return this;
    }

    public RealmQuery<E> endsWith(String columnName, String value) {
        int columnIndex = columns.get(columnName);
        this.query.endsWith(columnIndex, value);
        return this;
    }

    public RealmQuery<E> endsWith(String columnName, String value, boolean caseSensitive) {
        int columnIndex = columns.get(columnName);
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

    public long sumInt(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.sumInt(columnIndex);
    }

    public double sumDouble(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.sumDouble(columnIndex);
    }

    public double sumFloat(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.sumFloat(columnIndex);
    }

    // Average

    public double averageInt(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.averageInt(columnIndex);
    }

    public double averageDouble(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.averageDouble(columnIndex);
    }

    public double averageFloat(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.averageFloat(columnIndex);
    }

    // Min

    public long minimumInt(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.minimumInt(columnIndex);
    }

    public double minimuDouble(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.minimumDouble(columnIndex);
    }

    public float minimuFloat(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.minimumFloat(columnIndex);
    }

    public Date minimumDate(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.minimumDate(columnIndex);
    }

    // Max

    public long maximumInt(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.maximumInt(columnIndex);
    }

    public double maximuDouble(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.maximumDouble(columnIndex);
    }

    public float maximuFloat(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.maximumFloat(columnIndex);
    }

    public Date maximumDate(String columnName) {
        int columnIndex = columns.get(columnName);
        return this.query.maximumDate(columnIndex);
    }

    // Execute

    public RealmTableOrViewList<E> findAll() {
        return new RealmTableOrViewList<E>(realm, query.findAll(), clazz);
    }

    public E findFirst() {
        RealmList<E> result = findAll();
        if(result.size() > 0) {
            return findAll().get(0);
        } else {
            return null;
        }
    }

}
