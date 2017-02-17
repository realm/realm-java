package some.test;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks extends RealmObject {
    private Backlinks child;
    private RealmList<Backlinks> listChildren;

    @LinkingObjects("child")
    private RealmResults<Backlinks> simpleParents;

    @LinkingObjects("listChildren")
    private RealmResults<Backlinks> listParents;
}
