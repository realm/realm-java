package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.Required;

public class Backlinks_Required extends RealmObject {
    private int id;

    // A backlinked field may not be @Required
    @Required
    @LinkingObjects("child")
    private final RealmResults<BacklinkTarget> parents = null;
}
