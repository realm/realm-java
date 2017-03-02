package some.test;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks extends RealmObject {
    private int id;

    @LinkingObjects("child")
    private final RealmResults<BacklinkTarget> simpleParents = null;

    @LinkingObjects("children")
    private final RealmResults<BacklinkTarget> listParents = null;
}
