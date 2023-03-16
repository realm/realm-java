package io.realm.entities.migration;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

// Parent, now having an embedded object as a child, respecting table names
public class HandleBackLinksParent2 extends RealmObject {
    @PrimaryKey
    public long id;

    public HandleBackLinksChild2 child;

    public HandleBackLinksParent2() {}
}
