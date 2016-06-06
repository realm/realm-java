package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class Backlinks_MissingField extends RealmObject {

    private Backlinks_MissingField child;

    @LinkingObjects // Forgot to specify the field
    private RealmResults<Backlinks_MissingField> selectedFieldParents;
}
