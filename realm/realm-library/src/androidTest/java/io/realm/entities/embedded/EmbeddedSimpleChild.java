package io.realm.entities.embedded;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.RealmClass;

@RealmClass(embedded = true)
public class EmbeddedSimpleChild extends RealmObject {
    public String id = UUID.randomUUID().toString();
    @LinkingObjects("child")
    public final EmbeddedSimpleParent parent = null;

    public EmbeddedSimpleChild() {
    }

    public EmbeddedSimpleChild(String id) {
        super();
        this.id = id;
    }
}
