package io.realm.entities.migration;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

// Child, now as an embedded object, respecting table names
@RealmClass(embedded = true)
public class HandleBackLinksChild2 extends RealmObject {

    public String name;

    public HandleBackLinksChild2() {}

}
