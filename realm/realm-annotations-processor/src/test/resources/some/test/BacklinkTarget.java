package some.test;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class BacklinkTarget extends RealmObject {
    private String id;
    private Backlinks child;
    private RealmList<Backlinks> children;
}
