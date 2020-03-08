package io.realm.entities.embedded;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class EmbeddedSimpleParent extends RealmObject {
    @PrimaryKey
    public String id;
    public EmbeddedSimpleChild child;
}
