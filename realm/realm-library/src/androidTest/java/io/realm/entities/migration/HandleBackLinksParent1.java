package io.realm.entities.migration;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

// Original parent with a regular object as a child
public class HandleBackLinksParent1 extends RealmObject {
    @PrimaryKey
    public long id;

    public HandleBackLinksChild1 child;

    public HandleBackLinksParent1() {}
}
