package some.test;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

@RealmClass(embedded = true)
public class EmbeddedClassMultipleRequiredParents extends RealmObject {
    public String name;
    public int age;

    // If multiple @LinkingObjects are defined
    // the @Required annotation is not allowed.
    @Required
    @LinkingObjects("child6")
    public final EmbeddedClassParent parent1 = new EmbeddedClassParent();

    @Required
    @LinkingObjects("child7")
    public final EmbeddedClassParent parent2 = new EmbeddedClassParent();
}
