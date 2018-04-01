package io.realm.entities.inheritence;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

// Base class which implements RealmModel
@RealmClass
public abstract class InterfaceBase implements RealmModel {
    @PrimaryKey
    public long id;
}
