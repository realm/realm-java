package some.test;

import io.realm.RealmObject;
import io.realm.annotations.Backlink;

public class Backlinks extends RealmObject {

    private Backlinks child;

    // Backlinks are only allowed on RealmResults
    @Backlink
    private Backlinks singleParent;
}
