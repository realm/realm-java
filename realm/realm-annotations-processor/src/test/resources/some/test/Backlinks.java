package some.test;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks extends RealmObject {
    private int id;

    @LinkingObjects("child")
    private RealmResults<BacklinkTarget> simpleParents;

    @LinkingObjects("children")
    private RealmResults<BacklinkTarget> listParents;
}
