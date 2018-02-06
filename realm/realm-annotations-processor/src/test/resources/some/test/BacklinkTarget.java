package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class BacklinkTarget extends RealmObject {
    private int id;

    @LinkingObjects("child")
    private final RealmResults<BacklinkSource> simpleParents = null;

    @LinkingObjects("children")
    private final RealmResults<BacklinkSource> listParents = null;
}
