package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks extends RealmObject {

    private Backlinks child;

    @LinkingObjects("child")
    private RealmResults<Backlinks> selectedFieldParents;
}
