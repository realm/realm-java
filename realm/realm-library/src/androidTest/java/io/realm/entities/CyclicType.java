package io.realm.entities;

import io.realm.RealmList;
import io.realm.RealmObject;

public class CyclicType extends RealmObject {

    private String name;
    private CyclicType object;
    private RealmList<CyclicType> objects;

    public CyclicType() {
    }

    public CyclicType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CyclicType getObject() {
        return object;
    }

    public void setObject(CyclicType object) {
        this.object = object;
    }

    public RealmList<CyclicType> getObjects() {
        return objects;
    }

    public void setObjects(RealmList<CyclicType> objects) {
        this.objects = objects;
    }
}
