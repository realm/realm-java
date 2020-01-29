package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.RealmClass;

@RealmClass(embedded = true)
public class EmbeddedClass extends RealmObject {
    public String name;
    public int age;
}
