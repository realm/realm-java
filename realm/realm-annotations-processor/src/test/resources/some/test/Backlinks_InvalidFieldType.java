package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks_InvalidFieldType extends RealmObject {
    private int id;

    // Backlinks must be RealmResults
    @LinkingObjects("child")
    private final BacklinkTarget parents = null;
}
