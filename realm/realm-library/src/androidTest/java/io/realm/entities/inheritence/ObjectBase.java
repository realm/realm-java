package io.realm.entities.inheritence;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

// Base class which extends RealmObject
public abstract class ObjectBase extends RealmObject {
    @PrimaryKey
    public long id;
}
