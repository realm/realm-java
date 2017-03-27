package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks_MissingParameter extends RealmObject {
    private int id;

    // Forgot to specify the backlinked field
    @LinkingObjects
    private final RealmResults<BacklinkTarget> parents = null;
}
