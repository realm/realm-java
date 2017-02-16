package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks_InvalidFieldType extends RealmObject {

    private Backlinks_InvalidFieldType child;

    // Backlinks are only allowed on RealmResults
    @LinkingObjects("child")
    private Backlinks_InvalidFieldType selectedFieldParents;
}
