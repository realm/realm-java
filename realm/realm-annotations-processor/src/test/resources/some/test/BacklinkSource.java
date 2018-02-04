package some.test;

import io.realm.RealmList;
import io.realm.RealmObject;

public class BacklinkSource extends RealmObject {
    private String id;
    private BacklinkTarget child;
    private RealmList<BacklinkTarget> children;
}
