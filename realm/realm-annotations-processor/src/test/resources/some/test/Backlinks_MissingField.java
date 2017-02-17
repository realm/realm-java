package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks_MissingField extends RealmObject {
    private Backlinks_MissingField child;

    // Forgot to specify the backlinked field
    @LinkingObjects
    private RealmResults<Backlinks_MissingField> parents;
}
