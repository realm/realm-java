package io.realm.entities.embedded;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class EmbeddedTreeParent extends RealmObject {
    @PrimaryKey
    public String id;
    public EmbeddedTreeNode middleNode;
    public RealmList<EmbeddedTreeNode> middleNodeList;
}
