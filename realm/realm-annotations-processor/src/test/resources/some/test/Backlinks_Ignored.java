package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.Ignore;

public class Backlinks_Ignored extends RealmObject {
    private int id;

    // An  @Ignored, backlinked field is completely ignored
    @Ignore
    @LinkingObjects("foo")
    private int parents = 0;
}
