package some.test;

import io.realm.RealmObject;
import io.realm.annotations.LinkingObjects;

public class Backlinks_InvalidFieldType extends RealmObject {

    private Backlinks_InvalidFieldType child;

    // Backlinks are only allowed on RealmResults
    @LinkingObjects
    private Backlinks_InvalidFieldType singleParent;
}
