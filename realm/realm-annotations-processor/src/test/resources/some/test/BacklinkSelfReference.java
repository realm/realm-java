package some.test;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class BacklinkSelfReference extends RealmObject {

    public String id;
    public BacklinkSelfReference self;

    @LinkingObjects("self")
    final RealmResults<BacklinkSelfReference> parents = null;
}
