package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Backlink;

public class Backlinks extends RealmObject {

    private Backlinks child;

    // Backlink to all parents
    @Backlink
    private RealmResults<Backlinks> allParents;

    // Backlinks from only the child field
    @Backlink("child")
    private RealmResults<Backlinks> selectedFieldParents;
}
