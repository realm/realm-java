package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass(embedded = true)
public class EmbeddedClassPrimaryKey extends RealmObject {
    @PrimaryKey // This is not allowed in embedded classes
    public String name;
    public int age;
}
