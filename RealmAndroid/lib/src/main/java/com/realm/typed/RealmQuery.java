package com.realm.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RealmQuery<T> {

    private Realm<T> realm;
    private TableQuery query;
    private Map<String, Integer> columns = new HashMap<String, Integer>();

    public RealmQuery(Realm<T> realm) {
        this.realm = realm;

        TableOrView dataStore = realm.getDataStore();
        this.query = dataStore.where();

        for(int i = 0; i < dataStore.getColumnCount(); i++) {
            this.columns.put(dataStore.getColumnName(i), i);
        }
    }

    // Equal

    public RealmQuery<T> equalTo(String columnName, String value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<T> equalTo(String columnName, int value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<T> equalTo(String columnName, long value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<T> equalTo(String columnName, double value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<T> equalTo(String columnName, float value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<T> equalTo(String columnName, boolean value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<T> equalTo(String columnName, Date value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    // Between

    public RealmQuery<T> between(String columnName, int from, int to) {
        int columnIndex = columns.get(columnName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<T> between(String columnName, long from, long to) {
        int columnIndex = columns.get(columnName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<T> between(String columnName, double from, double to) {
        int columnIndex = columns.get(columnName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<T> between(String columnName, float from, float to) {
        int columnIndex = columns.get(columnName);
        this.query.between(columnIndex, from, to);
        return this;
    }

    public RealmQuery<T> between(String columnName, Date from, Date to) {
        int columnIndex = columns.get(columnName);
        this.query.between(columnIndex, from, to);
        return this;
    }


    // Contains

    public RealmQuery<T> contains(String columnName, String value) {
        int columnIndex = columns.get(columnName);
        this.query.contains(columnIndex, value);
        return this;
    }

    public RealmQuery<T> contains(String columnName, String value, boolean caseSensitive) {
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

    public Realm<T> find() {
        return new Realm<T>(realm, query.findAll());
    }

}
