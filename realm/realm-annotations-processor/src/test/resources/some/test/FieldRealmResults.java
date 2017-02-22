package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;

public class FieldRealmResults extends RealmObject {

    // RealmResults should only be allowed if combined with a @LinkingObjects annotation
    private RealmResults<FieldRealmResults> results;
}
