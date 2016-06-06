package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks_MissingGeneric extends RealmObject {

    private Backlinks_MissingGeneric child;

    @LinkingObjects("child")
    private RealmResults selectedFieldParents; // Forgot to specify the generic param
}
