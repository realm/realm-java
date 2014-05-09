package io.realm.typed;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.TableOrView;
import io.realm.TableQuery;

/**
 *
 * @param <E> The type of objects to be queried
 */
public class RealmQuery<E> {

    private RealmList<E> realm;
    private TableQuery query;
    private Map<String, Integer> columns = new HashMap<String, Integer>();

    public RealmQuery(RealmList<E> realmList) {
        this.realm = realmList;

        TableOrView dataStore = realmList.getDataStore();
        this.query = dataStore.where();

        for(int i = 0; i < dataStore.getColumnCount(); i++) {
            this.columns.put(dataStore.getColumnName(i), i);
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


    // Aggregates

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

    // Execute

    public RealmList<E> findAll() {
        return new RealmList<E>(realm, query.findAll());
    }

}
