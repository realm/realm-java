package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

@RealmClass(embedded = true)
public class EmbeddedClassOptionalParents extends RealmObject {
    public String name;
    public int age;

    // If multiple @LinkingObjects are defined
    // They are not treated as @Required.
    // This mostly impact Kotlin model classes
    @LinkingObjects("child3")
    public final EmbeddedClassParent parent1 = new EmbeddedClassParent(); // Field must be final, because parent cannot change once set

    @LinkingObjects("child4")
    public final EmbeddedClassParent parent2 = new EmbeddedClassParent(); // Field must be final, because parent cannot change once set
}
