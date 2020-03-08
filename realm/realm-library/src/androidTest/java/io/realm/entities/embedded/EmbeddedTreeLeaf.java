package io.realm.entities.embedded;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.RealmClass;

@RealmClass(embedded = true)
public class EmbeddedTreeLeaf extends RealmObject {
    public String id = UUID.randomUUID().toString();

    @LinkingObjects("leafNode")
    public final EmbeddedTreeNode parentRef = null;

    @LinkingObjects("leafNodeList")
    public final EmbeddedTreeNode parentListRef = null;

}
