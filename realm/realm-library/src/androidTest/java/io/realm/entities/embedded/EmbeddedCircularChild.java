package io.realm.entities.embedded;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;


@RealmClass(embedded = true)
public class EmbeddedCircularChild extends RealmObject {
    public String id = UUID.randomUUID().toString();

    public EmbeddedCircularChild singleChild;
}
