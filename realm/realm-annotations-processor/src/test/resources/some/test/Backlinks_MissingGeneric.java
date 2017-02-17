package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks_MissingGeneric extends RealmObject {
    private Backlinks_MissingGeneric child;

    // Forgot to specify the backlink generic param
    @LinkingObjects("child")
    private RealmResults parents;
}
