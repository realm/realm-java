package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks_LinkedFields extends RealmObject {
    private int id;

    // Defining a backlink more than one levels back is not supported.
    // It can be queried though: `equalTo("selectedFieldParents.selectedFieldParents")
    @LinkingObjects("child.id")
    private final RealmResults<BacklinkTarget> parents = null;
}
