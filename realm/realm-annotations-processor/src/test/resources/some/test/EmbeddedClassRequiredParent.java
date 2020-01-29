package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

@RealmClass(embedded = true)
public class EmbeddedClassRequiredParent extends RealmObject {
    public String name;
    public int age;

    @Required // Optional, is implied if only a single @LinkingObjects parent is defined
    @LinkingObjects("child2")
    public final EmbeddedClassParent parent = new EmbeddedClassParent(); // Field must be final, because parent cannot change once set
}
