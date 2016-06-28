package some.test;

import io.realm.RealmObject;
import io.realm.annotations.Backlink;

public class FieldRealmResults extends RealmObject {

    // RealmResults should only be allowed if combined with the @Backlink annotation
    private RealmResults<FieldRealmResults> results;
}
