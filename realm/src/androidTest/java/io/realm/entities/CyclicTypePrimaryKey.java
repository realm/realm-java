package io.realm.entities;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CyclicTypePrimaryKey extends RealmObject {

    @PrimaryKey
    private long id;
    private String name;
    private CyclicTypePrimaryKey object;
    private RealmList<CyclicTypePrimaryKey> objects;

    public CyclicTypePrimaryKey() {
    }

    public CyclicTypePrimaryKey(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CyclicTypePrimaryKey getObject() {
        return object;
    }

    public void setObject(CyclicTypePrimaryKey object) {
        this.object = object;
    }

    public RealmList<CyclicTypePrimaryKey> getObjects() {
        return objects;
    }

    public void setObjects(RealmList<CyclicTypePrimaryKey> objects) {
        this.objects = objects;
    }
}
