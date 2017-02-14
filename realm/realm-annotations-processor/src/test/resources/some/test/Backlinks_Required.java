package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Backlinks_Required extends RealmObject {
    private Backlinks_Required child;

    // Backlinks are not allowed on a Required Field
    @Required
    @LinkingObjects("child")
    private RealmResults<Backlinks_Required> fail;
}
