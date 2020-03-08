package io.realm.entities.embedded;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class EmbeddedSimpleListParent extends RealmObject {
    @PrimaryKey
    public String id;
    public RealmList<EmbeddedSimpleChild> children;
}
