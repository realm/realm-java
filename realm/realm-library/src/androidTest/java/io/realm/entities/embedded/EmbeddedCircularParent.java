package io.realm.entities.embedded;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class EmbeddedCircularParent extends RealmObject {
    @PrimaryKey
    public String id;
    public EmbeddedSimpleChild singleChild;


}
