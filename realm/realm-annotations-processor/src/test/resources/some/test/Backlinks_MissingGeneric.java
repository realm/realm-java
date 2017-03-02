package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks_MissingGeneric extends RealmObject {
    private int id;

    // Forgot to specify the backlink generic param
    @LinkingObjects("child")
    private final RealmResults parents = null;
}
