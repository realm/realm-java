package com.realm.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

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

    public RealmQuery<T> equalTo(String columnName, String value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<T> equalTo(String columnName, long value) {
        int columnIndex = columns.get(columnName);
        this.query.equalTo(columnIndex, value);
        return this;
    }

    public RealmQuery<T> equalTo(String columnName, int value) {
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


    public Realm<T> find() {
        return new Realm<T>(realm, query.findAll());
    }

}
