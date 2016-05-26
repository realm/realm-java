package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Backlink;

public class Backlinks_InvalidParentName extends RealmObject {

    private Backlinks child;

    // Field name doesn't exist on parent object
    @Backlink("foo")
    private RealmResults<Backlinks> selectedFieldParents;
}
