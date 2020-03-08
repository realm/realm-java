package io.realm.entities.embedded;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.RealmClass;

@RealmClass(embedded = true)
public class EmbeddedTreeNode extends RealmObject {
    public String id = UUID.randomUUID().toString();

    public EmbeddedTreeNode middleNodeæ;
    public EmbeddedTreeLeaf leafNode;

    public RealmList<EmbeddedTreeNode> middleNodeList;
    public RealmList<EmbeddedTreeLeaf> leafNodeList;

}
