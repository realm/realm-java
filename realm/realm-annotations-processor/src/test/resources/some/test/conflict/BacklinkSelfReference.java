package some.test.conflict;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.RealmClass;

// Test Backlink resolution when there is simple class name conflicts, but not internal name conflicts.
@RealmClass(name = "!BacklinkSelfReference")
public class BacklinkSelfReference extends RealmObject {
    public String name;
}
